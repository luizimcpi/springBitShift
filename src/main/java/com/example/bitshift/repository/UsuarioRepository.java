package com.example.bitshift.repository;

import com.example.bitshift.domain.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório JPA. O método {@code findByFlagsMask} demonstra filtro
 * <b>no banco</b> usando operação bitwise (BITAND do H2), sem precisar
 * de colunas booleanas separadas.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Filtra pela máscara de bits: retorna linhas onde
     * {@code BITAND(flags, :mask) = :expected}.
     *
     * <p>Ex.: máscara=1 (ATIVO), esperado=1 → só ativos.
     * máscara=3, esperado=3 → ativos E verificados.</p>
     *
     * <p>O {@code CAST(... AS INT)} é necessário porque o H2 não infere o
     * tipo dos bind parameters dentro de BITAND.</p>
     */
    @Query(value = "SELECT * FROM usuarios WHERE BITAND(flags, CAST(:mask AS INT)) = CAST(:expected AS INT)", nativeQuery = true)
    List<Usuario> findByFlagsMask(@Param("mask") int mask, @Param("expected") int expected);
}
