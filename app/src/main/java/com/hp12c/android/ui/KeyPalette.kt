package com.hp12c.android.ui

import android.graphics.Color

/**
 * Cores de fundo das teclas, com fidelidade à HP 12C física.
 */
enum class KeyColor {
    BEIGE,    // teclas numéricas e financeiras (bege/cream)
    ORANGE,   // tecla ENTER (laranja vibrante)
    BLUE,     // não usado em teclas inteiras, apenas em labels
    DARK      // tecla ON (grafite)
}

/**
 * Paleta de cores da HP 12C (cores ARGB como Int).
 * Inspirada na HP 12C Platinum física:
 *  - Corpo grafite escuro
 *  - LCD verde-azulado
 *  - Teclas laranja (f) e azul (g)
 */
data class KeyPalette(
    val beigeBg: Int,
    val beigeFg: Int,
    val orangeBg: Int,
    val orangeFg: Int,
    val blueBg: Int,
    val blueFg: Int,
    val darkBg: Int,
    val darkFg: Int,
    val labelOrange: Int,
    val labelBlue: Int,
    val labelGray: Int,
) {
    companion object {
        val Default = KeyPalette(
            beigeBg = Color.parseColor("#D6C8A9"),
            beigeFg = Color.parseColor("#1A1A1A"),
            orangeBg = Color.parseColor("#E87722"),
            orangeFg = Color.parseColor("#FFFFFF"),
            blueBg = Color.parseColor("#2D6CC9"),
            blueFg = Color.parseColor("#FFFFFF"),
            darkBg = Color.parseColor("#3A3A3A"),
            darkFg = Color.parseColor("#FFFFFF"),
            labelOrange = Color.parseColor("#E87722"),
            labelBlue = Color.parseColor("#2D6CC9"),
            labelGray = Color.parseColor("#6B6B6B"),
        )
    }
}

/** Resolvedor de cor de fundo de uma tecla. */
fun KeyColor.background(p: KeyPalette): Int = when (this) {
    KeyColor.BEIGE -> p.beigeBg
    KeyColor.ORANGE -> p.orangeBg
    KeyColor.BLUE -> p.blueBg
    KeyColor.DARK -> p.darkBg
}

/** Resolvedor de cor de texto de uma tecla. */
fun KeyColor.foreground(p: KeyPalette): Int = when (this) {
    KeyColor.BEIGE -> p.beigeFg
    KeyColor.ORANGE -> p.orangeFg
    KeyColor.BLUE -> p.blueFg
    KeyColor.DARK -> p.darkFg
}
