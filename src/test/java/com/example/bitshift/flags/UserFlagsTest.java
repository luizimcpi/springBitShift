package com.example.bitshift.flags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Testes unitários puros da matemática de bitshift (sem Spring). */
class UserFlagsTest {

    @Test
    void constantesDevemSerPotenciasDeDois() {
        assertEquals(1, UserFlags.ATIVO); // 1 << 0
        assertEquals(2, UserFlags.VERIFICADO); // 1 << 1
        assertEquals(3, UserFlags.TODAS); // 1 | 2
    }

    @Test
    void ligarDeveUsarOperadorOr() {
        assertEquals(1, UserFlags.ligar(0, UserFlags.ATIVO));
        assertEquals(3, UserFlags.ligar(1, UserFlags.VERIFICADO));
        // ligar algo já ligado não muda nada (idempotente)
        assertEquals(3, UserFlags.ligar(3, UserFlags.ATIVO));
    }

    @Test
    void desligarDeveUsarAndComNot() {
        assertEquals(2, UserFlags.desligar(3, UserFlags.ATIVO));
        assertEquals(0, UserFlags.desligar(1, UserFlags.ATIVO));
        // desligar algo já desligado não muda nada
        assertEquals(0, UserFlags.desligar(0, UserFlags.VERIFICADO));
    }

    @Test
    void definirDeveRespeitarBooleano() {
        assertEquals(1, UserFlags.definir(0, UserFlags.ATIVO, true));
        assertEquals(0, UserFlags.definir(1, UserFlags.ATIVO, false));
        assertEquals(2, UserFlags.definir(0, UserFlags.VERIFICADO, true));
    }

    @Test
    void isLigadoDeveUsarAnd() {
        assertTrue(UserFlags.isLigado(1, UserFlags.ATIVO));
        assertFalse(UserFlags.isLigado(2, UserFlags.ATIVO));
        assertTrue(UserFlags.isLigado(3, UserFlags.ATIVO));
        assertTrue(UserFlags.isLigado(3, UserFlags.VERIFICADO));
        assertFalse(UserFlags.isLigado(0, UserFlags.VERIFICADO));
    }

    @Test
    void tabelaVerdadeCompletaDosDoisBits() {
        // 00
        assertFalse(UserFlags.isLigado(0, UserFlags.ATIVO));
        assertFalse(UserFlags.isLigado(0, UserFlags.VERIFICADO));
        // 01
        assertTrue(UserFlags.isLigado(1, UserFlags.ATIVO));
        assertFalse(UserFlags.isLigado(1, UserFlags.VERIFICADO));
        // 10
        assertFalse(UserFlags.isLigado(2, UserFlags.ATIVO));
        assertTrue(UserFlags.isLigado(2, UserFlags.VERIFICADO));
        // 11
        assertTrue(UserFlags.isLigado(3, UserFlags.ATIVO));
        assertTrue(UserFlags.isLigado(3, UserFlags.VERIFICADO));
    }

    @Test
    void paraBinarioDeveTer8Digitos() {
        assertEquals("00000000", UserFlags.paraBinario(0));
        assertEquals("00000001", UserFlags.paraBinario(1));
        assertEquals("00000010", UserFlags.paraBinario(2));
        assertEquals("00000011", UserFlags.paraBinario(3));
    }
}
