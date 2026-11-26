package com.hp12c.android.ui

import net.sf.finanx.fx12c.calc.Key

/**
 * Modelo de cada tecla física da HP 12C.
 *
 * A HP 12C real tem 4 linhas x 10 colunas = 40 teclas.
 * Cada tecla tem:
 *  - [code]      : código interno (Key enum do motor finanx-12c)
 *  - [main]       : label principal (ex.: "ENTER", "1", "n", "+")
 *  - [top]        : label acima (função laranja f) — ex.: "BOND", "AMORT"
 *  - [bottom]     : label abaixo (função azul g) — ex.: "BEG", "END"
 *  - [color]      : cor de fundo da tecla (beige / orange / blue / dark)
 *
 * As cores seguem a HP 12C Platinum: teclas financeiras (n, i, PV, PMT, FV)
 * são beige; tecla ENTER é laranja; teclas aritméticas (+, -, x, ÷) são
 * beige; tecla ON é escura; teclas de dígito são beige.
 */
data class HpKey(
    val code: Key,
    val main: String,
    val top: String = "",
    val bottom: String = "",
    val color: KeyColor = KeyColor.BEIGE
)

/**
 * Layout completo do teclado HP 12C, 4 linhas x 10 colunas.
 *
 * Layout fiel à HP 12C Platinum física. Mapeamento de códigos Finanx 12c:
 *
 *   Col: 1   2   3   4   5   6   7   8   9   10
 *   L1:  n   i   PV  PMT FV  ÷   R↓  xy  CLx ENTER
 *   L2:  y^x 1/x %T  Δ%  %   EEX R/S SST g    f
 *   L3:  STO RCL 7   8   9   -   4   5   6    ×
 *   L4:  1   2   3   4   5   6   +   ON  .    Σ+
 *
 * Observação: na HP 12C física a linha 2 tem R/S, SST, GTO, x≤y. As
 * teclas f e g estão no canto inferior direito da linha 2.
 */
object HpKeyLayout {

    /** Layout de 4 linhas x 10 colunas, fiel à HP 12C física. */
    val grid: List<List<HpKey>> = listOf(
        // Linha 1: n, i, PV, PMT, FV, ÷, R↓, xy, CLx, ENTER
        listOf(
            HpKey(Key.KEY_11, "n",      top = "AMORT"),
            HpKey(Key.KEY_12, "i",      top = "INT"),
            HpKey(Key.KEY_13, "PV",     top = "NPV"),
            HpKey(Key.KEY_14, "PMT",    top = "RND"),
            HpKey(Key.KEY_15, "FV",     top = "IRR"),
            HpKey(Key.KEY_10, "÷",      top = "PRICE", bottom = "√"),
            HpKey(Key.KEY_33, "R↓",     top = "PRGM",  bottom = "GTO"),
            HpKey(Key.KEY_34, "x↔y",    top = "FIN",   bottom = "x≤y"),
            HpKey(Key.KEY_35, "CLx",    top = "REG",   bottom = "x=0"),
            HpKey(Key.KEY_36, "ENTER",  top = "PREFIX", color = KeyColor.ORANGE),
        ),
        // Linha 2: y^x, 1/x, %T, Δ%, %, EEX, R/S, SST, g, f
        listOf(
            HpKey(Key.KEY_21, "yˣ",     top = "BOND"),
            HpKey(Key.KEY_22, "1/x",    top = "YTM",  bottom = "eˣ"),
            HpKey(Key.KEY_23, "%T",     top = "SL",   bottom = "ln"),
            HpKey(Key.KEY_24, "Δ%",     top = "SOYD", bottom = "FRAC"),
            HpKey(Key.KEY_25, "%",      top = "DB",   bottom = "INTG"),
            HpKey(Key.KEY_26, "EEX",    bottom = "ΔDYS"),
            HpKey(Key.KEY_31, "R/S",    top = "PR",   bottom = "PSE"),
            HpKey(Key.KEY_32, "SST",    top = "Σ",    bottom = "BST"),
            HpKey(Key.KEY_43, "g",      color = KeyColor.BLUE),
            HpKey(Key.KEY_42, "f",      color = KeyColor.ORANGE),
        ),
        // Linha 3: STO, RCL, 7, 8, 9, -, 4, 5, 6, ×
        listOf(
            HpKey(Key.KEY_44, "STO",    top = "CLEAR"),
            HpKey(Key.KEY_45, "RCL",    top = "PREFIX"),
            HpKey(Key.KEY_07, "7",      top = "BEG"),
            HpKey(Key.KEY_08, "8",      top = "END"),
            HpKey(Key.KEY_09, "9",      top = "MEM"),
            HpKey(Key.KEY_30, "-",      top = "12÷",  bottom = "12x"),
            HpKey(Key.KEY_04, "4",      top = "x̄w"),
            HpKey(Key.KEY_05, "5",      top = "x̄r"),
            HpKey(Key.KEY_06, "6",      top = "x̄"),
            HpKey(Key.KEY_20, "×",      top = "Δ%",   bottom = "FRAC"),
        ),
        // Linha 4: 1, 2, 3, 4, 5, 6, +, ON, ., Σ+
        listOf(
            HpKey(Key.KEY_01, "1",      top = "x̄r"),
            HpKey(Key.KEY_02, "2",      top = "s"),
            HpKey(Key.KEY_03, "3",      top = "x̄"),
            HpKey(Key.KEY_04, "4"),
            HpKey(Key.KEY_05, "5"),
            HpKey(Key.KEY_06, "6"),
            HpKey(Key.KEY_40, "+",      top = "STO+"),
            HpKey(Key.KEY_41, "ON",     color = KeyColor.DARK),
            HpKey(Key.KEY_48, "."),
            HpKey(Key.KEY_49, "Σ+",     top = "Σ-",  bottom = "s"),
        )
    )

    /**
     * Quinta linha (bottom row) — contém apenas o "0" e o "CHS".
     *
     * Esta linha é SEPARADA do grid principal de 4×10 porque na HP 12C real
     * ela tem menos teclas e cada uma ocupa largura variável (o "0" ocupa
     * aproximadamente metade da largura da calculadora).
     *
     * Formatamos como uma lista de pares (key, weight) onde weight é a
     * proporção da largura que a tecla deve ocupar (1 = 1 coluna normal,
     * 2 = 2 colunas, etc.).
     */
    val bottomRow: List<Pair<HpKey, Int>> = listOf(
        HpKey(Key.KEY_00, "0", top = "") to 5,        // "0" ocupa 5 colunas (~metade da largura)
        HpKey(Key.KEY_16, "CHS", top = "DATE") to 5   // "CHS" ocupa as outras 5 colunas
    )

    /** Versão alternativa compacta para modo portrait (5 colunas x 8 linhas). */
    val gridPortrait: List<List<HpKey>> = listOf(
        listOf(grid[0][0], grid[0][1], grid[0][2], grid[0][3], grid[0][4]),
        listOf(grid[0][5], grid[0][6], grid[0][7], grid[0][8], grid[0][9]),
        listOf(grid[1][0], grid[1][1], grid[1][2], grid[1][3], grid[1][4]),
        listOf(grid[1][5], grid[1][6], grid[1][7], grid[1][8], grid[1][9]),
        listOf(grid[2][0], grid[2][1], grid[2][2], grid[2][3], grid[2][4]),
        listOf(grid[2][5], grid[2][6], grid[2][7], grid[2][8], grid[2][9]),
        listOf(grid[3][0], grid[3][1], grid[3][2], grid[3][3], grid[3][4]),
        listOf(grid[3][5], grid[3][6], grid[3][7], grid[3][8], grid[3][9]),
    )

    /** Lista linear de todas as teclas (para uso em adapters). */
    val all: List<HpKey> = grid.flatten()
}
