package com.example.bitshift.domain;

import com.example.bitshift.flags.UserFlags;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidade {@code Usuario} — apenas <b>UM</b> campo inteiro ({@code flags})
 * é persistido para guardar DOIS booleanos (ativo e verificado).
 *
 * <p>Mapeamento:</p>
 * <ul>
 *   <li>bit 0 ({@code 1 << 0} = 1) → ativo</li>
 *   <li>bit 1 ({@code 1 << 1} = 2) → verificado</li>
 * </ul>
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    /**
     * Coluna única que armazena os dois booleanos via bitmask.
     * Ex.: 0 = nenhum, 1 = ativo, 2 = verificado, 3 = ambos.
     */
    @Column(nullable = false)
    private int flags = 0;

    protected Usuario() {
        // JPA
    }

    public Usuario(String nome, boolean ativo, boolean verificado) {
        this.nome = nome;
        setAtivo(ativo);
        setVerificado(verificado);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /** Valor bruto persistido no banco (didático: exponha na API para inspeção). */
    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    // ---- Acesso booleano via bitshift (a API pública da entidade) ----

    public boolean isAtivo() {
        return UserFlags.isLigado(flags, UserFlags.ATIVO);
    }

    public void setAtivo(boolean ativo) {
        this.flags = UserFlags.definir(this.flags, UserFlags.ATIVO, ativo);
    }

    public boolean isVerificado() {
        return UserFlags.isLigado(flags, UserFlags.VERIFICADO);
    }

    public void setVerificado(boolean verificado) {
        this.flags = UserFlags.definir(this.flags, UserFlags.VERIFICADO, verificado);
    }
}
