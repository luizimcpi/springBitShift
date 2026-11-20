package com.example.bitshift.dto;

import com.example.bitshift.domain.Usuario;
import com.example.bitshift.flags.UserFlags;

/**
 * Payload de saída. Expõe os booleanos decodificados <b>e</b> o valor bruto
 * {@code flags} (+ binário) para fins didáticos e de depuração.
 */
public record UsuarioResponse(
        Long id,
        String nome,
        boolean ativo,
        boolean verificado,
        int flags,
        String flagsBinario) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.isAtivo(),
                u.isVerificado(),
                u.getFlags(),
                UserFlags.paraBinario(u.getFlags()));
    }
}
