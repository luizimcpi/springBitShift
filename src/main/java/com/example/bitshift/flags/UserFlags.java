package com.example.bitshift.flags;

/**
 * Utilitário central que demonstra o uso de <b>bitshift</b> para compactar
 * vários valores booleanos em um único {@code int} persistido no banco.
 *
 * <p>Cada flag ocupa 1 bit da variável {@code flags}:</p>
 * <pre>
 * bit 1 | bit 0
 * ------+------
 *   0   |   0    = 0 (nada ligado)
 *   0   |   1    = 1 (ATIVO)
 *   1   |   0    = 2 (VERIFICADO)
 *   1   |   1    = 3 (ATIVO + VERIFICADO)
 * </pre>
 *
 * <p>Para adicionar uma terceira flag basta criar {@code 1 << 2} (= 4),
 * quarta flag {@code 1 << 3} (= 8), e assim por diante — até 32 flags em um int.</p>
 */
public final class UserFlags {

    /** Bit 0 (valor 1): usuário ativo. {@code 1 << 0} */
    public static final int ATIVO = 1 << 0; // 0b01 = 1

    /** Bit 1 (valor 2): e-mail/documento verificado. {@code 1 << 1} */
    public static final int VERIFICADO = 1 << 1; // 0b10 = 2

    // Exemplo de extensão futura (basta descomentar):
    // public static final int ADMIN = 1 << 2; // 0b100 = 4
    // public static final int BLOQUEADO = 1 << 3; // 0b1000 = 8

    /** Máscara com todos os bits conhecidos ligados (1 | 2 = 3). */
    public static final int TODAS = ATIVO | VERIFICADO;

    private UserFlags() {
    }

    /** Liga o(s) bit(s) da máscara: {@code flags | mascara}. */
    public static int ligar(int flags, int mascara) {
        return flags | mascara;
    }

    /** Desliga o(s) bit(s) da máscara: {@code flags & ~mascara}. */
    public static int desligar(int flags, int mascara) {
        return flags & ~mascara;
    }

    /** Define o bit conforme o booleano (atalho para ligar/desligar). */
    public static int definir(int flags, int mascara, boolean ligado) {
        return ligado ? ligar(flags, mascara) : desligar(flags, mascara);
    }

    /** Testa se o(s) bit(s) da máscara estão ligados: {@code (flags & mascara) != 0}. */
    public static boolean isLigado(int flags, int mascara) {
        return (flags & mascara) != 0;
    }

    /** Representação binária de 8 bits para fins didáticos/logs (ex.: "00000011"). */
    public static String paraBinario(int flags) {
        String bin = Integer.toBinaryString(flags & 0xFF);
        return "0".repeat(Math.max(0, 8 - bin.length())) + bin;
    }
}
