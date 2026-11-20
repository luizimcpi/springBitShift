package com.example.bitshift.exception;

/** Lançada quando o recurso não existe → mapeada para HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
