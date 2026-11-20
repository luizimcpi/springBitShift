# Bitshift API — booleanos compactados com bitshift

API REST em **Java 21**, **Spring Boot 4** e **H2** que demonstra como armazenar
**2 valores booleanos em um único campo inteiro do banco** usando operadores de bits
(`<<`, `|`, `&`, `~`).

> Exemplo didático: `ativo` + `verificado` → 1 coluna `flags` (INT).

---

## 1. Como funciona o bitshift aqui?

Cada booleano ocupa **1 bit** de um `int`:

| `verificado` (bit 1) | `ativo` (bit 0) | `flags` (decimal) | `flags` (binário) |
|---|---|---|---|
| 0 | 0 | 0 | `00000000` |
| 0 | 1 | 1 | `00000001` |
| 1 | 0 | 2 | `00000010` |
| 1 | 1 | 3 | `00000011` |

Definições no código (`flags/UserFlags.java`):

```java
public static final int ATIVO      = 1 << 0; // 1  (0b01)
public static final int VERIFICADO = 1 << 1; // 2  (0b10)
```

Operações:

| Operação | Código | Exemplo |
|---|---|---|
| **Ligar** um bit | `flags \| MASCARA` | `0 \| 1 = 1` (ativa) |
| **Desligar** um bit | `flags & ~MASCARA` | `3 & ~1 = 2` (desativa, mantém verificado) |
| **Testar** um bit | `(flags & MASCARA) != 0` | `(2 & 1) != 0 → false` |
| **Definir** por booleano | `ligado ? ligar : desligar` | `definir(0, ATIVO, true) = 1` |

A entidade `domain/Usuario.java` persiste **só o `int flags`**:

```java
@Column(nullable = false)
private int flags = 0;

public boolean isAtivo()          { return UserFlags.isLigado(flags, UserFlags.ATIVO); }
public void setAtivo(boolean v)   { flags = UserFlags.definir(flags, UserFlags.ATIVO, v); }
public boolean isVerificado()     { return UserFlags.isLigado(flags, UserFlags.VERIFICADO); }
public void setVerificado(boolean v) { flags = UserFlags.definir(flags, UserFlags.VERIFICADO, v); }
```

O banco vê `usuarios(id, nome, flags)` — **3 colunas em vez de 4**.
A API continua amigável: recebe/devolve `ativo`/`verificado` separados e
expõe `flags` + `flagsBinario` para inspeção.

### Por que fazer isso?

- Menos colunas / migrações para flags simples.
- Leitura e escrita atômicas de várias flags em 1 UPDATE.
- Filtro no banco com 1 operação bitwise (`BITAND`), sem JOINs.
- Didático para entender bitmask (feature flags, permissões Unix, etc.).

### Quando NÃO fazer?

- Se precisar indexar/filtrar cada booleano isoladamente com alta performance.
- Se o time não está confortável com bitwise (legibilidade).
- Se as flags têm metadados próprios (data, motivo) — aí modele como entidade.

---

## 2. Stack e estrutura

- Java 21 (`java.version=21`), Spring Boot 4.0.1, Spring Data JPA, Validation, H2
- Build: Maven Wrapper (`./mvnw` / `mvnw.cmd`) — não precisa instalar Maven

```
src/main/java/com/example/bitshift/
├── flags/UserFlags.java            # constantes 1<<0, 1<<1 + ligar/desligar/testar
├── domain/Usuario.java             # @Entity com 1 coluna flags + getters booleanos
├── repository/UsuarioRepository.java # JpaRepository + findByFlagsMask (BITAND)
├── dto/UsuarioRequest.java         # {nome, ativo, verificado}
├── dto/UsuarioResponse.java        # {id, nome, ativo, verificado, flags, flagsBinario}
├── service/UsuarioService.java     # conversão + máscara de filtro
├── controller/UsuarioController.java # CRUD /api/usuarios
└── exception/                      # 404 + 400 padronizados
src/test/java/.../
├── flags/UserFlagsTest.java        # 7 testes da matemática bitwise
├── domain/UsuarioTest.java         # 6 testes da entidade
├── service/UsuarioServiceTest.java # 7 testes com Mockito
└── controller/UsuarioControllerTest.java # 3 testes de integração (MockMvc + H2)
```

---

## 3. Como rodar

Pré-requisito: JDK 21+ (`java -version`).

```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Ou com Maven instalado:

```bash
mvn test        # roda os 24 testes
mvn spring-boot:run
```

- API: http://localhost:8080/api/usuarios
- Console H2: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bitshiftdb` · User: `sa` · Password: *(vazio)*
  - Verifique: `SELECT id, nome, flags FROM usuarios;`

---

## 4. Como utilizar (endpoints)

### Criar — `POST /api/usuarios` → 201

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana","ativo":true,"verificado":false}'
```

Resposta:

```json
{
  "id": 1,
  "nome": "Ana",
  "ativo": true,
  "verificado": false,
  "flags": 1,
  "flagsBinario": "00000001"
}
```

Mais exemplos de `flags`:

```bash
{"nome":"Beto","ativo":false,"verificado":true}   # → flags 2 (00000010)
{"nome":"Cris","ativo":true,"verificado":true}    # → flags 3 (00000011)
{"nome":"Duda","ativo":false,"verificado":false}  # → flags 0 (00000000)
```

### Listar — `GET /api/usuarios` → 200

```bash
curl http://localhost:8080/api/usuarios
```

Filtro bitwise (opcional, combinado com AND):

```bash
curl "http://localhost:8080/api/usuarios?ativo=true"
curl "http://localhost:8080/api/usuarios?verificado=false"
curl "http://localhost:8080/api/usuarios?ativo=true&verificado=true"
```

Como o filtro é montado (`UsuarioService.listar`):

```java
int mask = 0, expected = 0;
if (ativo.isPresent())      { mask |= ATIVO;      if (ativo.get())      expected |= ATIVO; }
if (verificado.isPresent()) { mask |= VERIFICADO; if (verificado.get()) expected |= VERIFICADO; }
// mask=3, expected=2  →  "verificado=true e ativo=false"
// SQL: WHERE BITAND(flags, CAST(:mask AS INT)) = CAST(:expected AS INT)
```

### Buscar — `GET /api/usuarios/{id}` → 200 | 404

```bash
curl http://localhost:8080/api/usuarios/1
```

404:

```json
{"timestamp":"...","status":404,"erro":"Usuário 99 não encontrado"}
```

### Atualizar — `PUT /api/usuarios/{id}` → 200

```bash
curl -X PUT http://localhost:8080/api/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","ativo":true,"verificado":true}'
# → flags vira 3
```

### Remover — `DELETE /api/usuarios/{id}` → 204

```bash
curl -X DELETE http://localhost:8080/api/usuarios/1 -i
```

### Validação — 400

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"","ativo":false,"verificado":false}'
# → {"status":400,"erro":"nome: nome é obrigatório"}
```

---

## 5. Como estender para 3, 4, ... 32 flags

1. Adicione a constante em `UserFlags`:

```java
public static final int ADMIN = 1 << 2;      // 4  (0b100)
public static final int BLOQUEADO = 1 << 3;  // 8  (0b1000)
```

2. Adicione getter/setter na entidade:

```java
public boolean isAdmin()        { return UserFlags.isLigado(flags, UserFlags.ADMIN); }
public void setAdmin(boolean v) { flags = UserFlags.definir(flags, UserFlags.ADMIN, v); }
```

3. Inclua no Request/Response e no filtro do service. **Nenhuma migração de
coluna nova** — o `int` comporta até 32 flags (`long` → 64).

---

## 6. Testes unitários

```bash
./mvnw test
# Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
```

| Classe | Tipo | O que cobre |
|---|---|---|
| `UserFlagsTest` (7) | unitário puro | constantes, `\|`, `& ~`, tabela-verdade 00/01/10/11 |
| `UsuarioTest` (6) | unitário puro | setters não interferem entre si, `setFlags` bruto |
| `UsuarioServiceTest` (7) | Mockito | conversão, máscara `findByFlagsMask(3,2)`, 404 |
| `UsuarioControllerTest` (3) | integração MockMvc+H2 | CRUD ponta a ponta, 400, 404 |
| `BitshiftApiApplicationTests` (1) | smoke | contexto Spring sobe |

---

## 7. Notas de implementação

- `application.properties` usa H2 em memória com `ddl-auto=update` e console habilitado.
- `GlobalExceptionHandler` padroniza 404 (`ResourceNotFoundException`) e 400 (Bean Validation).
- `UsuarioRequest` usa `Boolean` (nullable) com `ativoOuFalse()` para tratar `null` como `false`.
- O `CAST(:mask AS INT)` na query nativa é exigência do H2 (ele não infere o tipo do `?` dentro de `BITAND`).
