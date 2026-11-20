package com.example.bitshift.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Testes unitários da entidade: setters booleanos devem refletir no int. */
class UsuarioTest {

    @Test
    void novoUsuarioDeveCombinarBits() {
        Usuario u = new Usuario("Ana", true, false);
        assertEquals(1, u.getFlags());
        assertTrue(u.isAtivo());
        assertFalse(u.isVerificado());
    }

    @Test
    void ambosTrueDeveResultarEm3() {
        Usuario u = new Usuario("Beto", true, true);
        assertEquals(3, u.getFlags());
    }

    @Test
    void ambosFalseDeveResultarEm0() {
        Usuario u = new Usuario("Cris", false, false);
        assertEquals(0, u.getFlags());
    }

    @Test
    void setAtivoNaoDeveAfetarVerificado() {
        Usuario u = new Usuario("Duda", false, true); // flags = 2
        u.setAtivo(true); // flags deve ir para 3, verificado continua true
        assertEquals(3, u.getFlags());
        assertTrue(u.isVerificado());

        u.setAtivo(false); // volta para 2
        assertEquals(2, u.getFlags());
        assertFalse(u.isAtivo());
        assertTrue(u.isVerificado());
    }

    @Test
    void setVerificadoNaoDeveAfetarAtivo() {
        Usuario u = new Usuario("Edo", true, false); // flags = 1
        u.setVerificado(true); // 3
        assertEquals(3, u.getFlags());
        assertTrue(u.isAtivo());

        u.setVerificado(false); // 1
        assertEquals(1, u.getFlags());
        assertTrue(u.isAtivo());
    }

    @Test
    void setFlagsBrutoDeveSerDecodificado() {
        Usuario u = new Usuario("Fabi", false, false);
        u.setFlags(2);
        assertFalse(u.isAtivo());
        assertTrue(u.isVerificado());
    }
}
