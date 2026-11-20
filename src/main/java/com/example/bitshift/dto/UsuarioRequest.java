package com.example.bitshift.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada. Os booleanos são recebidos separados (UX amigável)
 * e convertidos para bitmask dentro do service/entidade.
 */
public record UsuarioRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        Boolean ativo,
        Boolean verificado) {

    public boolean ativoOuFalse() {
        return Boolean.TRUE.equals(ativo);
    }

    public boolean verificadoOuFalse() {
        return Boolean.TRUE.equals(verificado);
    }
}
