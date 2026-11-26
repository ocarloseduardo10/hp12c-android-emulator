package com.hp12c.android.ui

import android.graphics.Path
import android.graphics.RectF

/**
 * Display de 7 segmentos fiel à HP 12C física.
 *
 * Portado de `crates/hp12c-skin/src/seg7.rs` do projeto hp12c-emulator-rust (MIT).
 *
 * Segmentos:
 * ```
 *  aaa
 * f   b
 *  ggg
 * e   c
 *  ddd
 * ```
 *
 * Cada caractere é mapeado para uma máscara de bits indicando quais segmentos
 * estão acesos. Os polígonos dos segmentos são gerados em coordenadas locais
 * (origem no canto superior esquerdo do dígito, y cresce para baixo).
 */
object SevenSegment {

    // Bits dos segmentos
    const val A: Byte = 1
    const val B: Byte = 2
    const val C: Byte = 4
    const val D: Byte = 8
    const val E: Byte = 16
    const val F: Byte = 32
    const val G: Byte = 64

    /**
     * Retorna a máscara de segmentos acesos para um caractere.
     *
     * Suporta dígitos 0-9, sinal de menos, e letras usadas no display da HP 12C
     * (A, b, C, c, d, E, F, H, i, L, n, o, P, r, t, U, u, y, g, S, O).
     */
    fun segments(ch: Char): Byte {
        return when (ch) {
            '0', 'O'           -> (A.toInt() or B.toInt() or C.toInt() or D.toInt() or E.toInt() or F.toInt()).toByte()
            '1'                 -> (B.toInt() or C.toInt()).toByte()
            '2'                 -> (A.toInt() or B.toInt() or G.toInt() or E.toInt() or D.toInt()).toByte()
            '3'                 -> (A.toInt() or B.toInt() or G.toInt() or C.toInt() or D.toInt()).toByte()
            '4'                 -> (F.toInt() or G.toInt() or B.toInt() or C.toInt()).toByte()
            '5', 'S'           -> (A.toInt() or F.toInt() or G.toInt() or C.toInt() or D.toInt()).toByte()
            '6'                 -> (A.toInt() or F.toInt() or G.toInt() or E.toInt() or D.toInt() or C.toInt()).toByte()
            '7'                 -> (A.toInt() or B.toInt() or C.toInt()).toByte()
            '8'                 -> (A.toInt() or B.toInt() or C.toInt() or D.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            '9', 'g'           -> (A.toInt() or B.toInt() or C.toInt() or D.toInt() or F.toInt() or G.toInt()).toByte()
            '-'                 -> G
            'A'                 -> (A.toInt() or B.toInt() or C.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'b'                 -> (C.toInt() or D.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'C'                 -> (A.toInt() or D.toInt() or E.toInt() or F.toInt()).toByte()
            'c'                 -> (D.toInt() or E.toInt() or G.toInt()).toByte()
            'd'                 -> (B.toInt() or C.toInt() or D.toInt() or E.toInt() or G.toInt()).toByte()
            'E'                 -> (A.toInt() or D.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'F'                 -> (A.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'H'                 -> (B.toInt() or C.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'i'                 -> E
            'L'                 -> (D.toInt() or E.toInt() or F.toInt()).toByte()
            'n'                 -> (C.toInt() or E.toInt() or G.toInt()).toByte()
            'o'                 -> (C.toInt() or D.toInt() or E.toInt() or G.toInt()).toByte()
            'P'                 -> (A.toInt() or B.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'r'                 -> (E.toInt() or G.toInt()).toByte()
            't'                 -> (D.toInt() or E.toInt() or F.toInt() or G.toInt()).toByte()
            'U'                 -> (B.toInt() or C.toInt() or D.toInt() or E.toInt() or F.toInt()).toByte()
            'u'                 -> (C.toInt() or D.toInt() or E.toInt()).toByte()
            'y'                 -> (B.toInt() or C.toInt() or D.toInt() or F.toInt() or G.toInt()).toByte()
            else                -> 0
        }
    }

    /**
     * Gera os 7 polígonos (Paths) dos segmentos de um dígito, na ordem a..g.
     *
     * @param width largura do dígito em pixels
     * @param height altura do dígito em pixels
     * @param thickness espessura dos segmentos
     * @param slant inclinação para a direita (deslocamento em x por unidade de altura)
     * @return array de 7 Paths (a, b, c, d, e, f, g); null para segmentos que não devem ser desenhados
     */
    fun segmentPaths(
        width: Float,
        height: Float,
        thickness: Float,
        slant: Float
    ): Array<Path?> {
        val half = thickness / 2f
        val gap = thickness * 0.14f
        val left = half
        val right = width - half
        val top = half
        val mid = height / 2f
        val bottom = height - half

        fun horizontalPath(y: Float): Path {
            val (x0, x1) = left + gap to right - gap
            // Aplicar inclinação (slant) baseada na altura y
            val sx0 = x0 + slant * y
            val sx1 = x1 + slant * y
            return Path().apply {
                moveTo(sx0, y)
                lineTo(sx0 + half, y - half)
                lineTo(sx1 - half, y - half)
                lineTo(sx1, y)
                lineTo(sx1 - half, y + half)
                lineTo(sx0 + half, y + half)
                close()
            }
        }

        fun verticalPath(x: Float, y0: Float, y1: Float): Path {
            val (yy0, yy1) = y0 + gap to y1 - gap
            // Slant na posição x (depende da altura média)
            val sx = x + slant * ((y0 + y1) / 2f)
            return Path().apply {
                moveTo(sx, yy0)
                lineTo(sx + half, yy0 + half)
                lineTo(sx + half, yy1 - half)
                lineTo(sx, yy1)
                lineTo(sx - half, yy1 - half)
                lineTo(sx - half, yy0 + half)
                close()
            }
        }

        return arrayOf(
            horizontalPath(top),                              // a (topo)
            verticalPath(right, top, mid),                    // b (direita superior)
            verticalPath(right, mid, bottom),                 // c (direita inferior)
            horizontalPath(bottom),                           // d (base)
            verticalPath(left, mid, bottom),                  // e (esquerda inferior)
            verticalPath(left, top, mid),                     // f (esquerda superior)
            horizontalPath(mid)                                // g (meio)
        )
    }

    /** Indica se um segmento (0..6 = a..g) está aceso para o caractere dado. */
    fun isSegmentOn(ch: Char, segmentIndex: Int): Boolean {
        val mask = segments(ch).toInt() and (0xFF)
        val bit = 1 shl segmentIndex
        return (mask and bit) != 0
    }
}
