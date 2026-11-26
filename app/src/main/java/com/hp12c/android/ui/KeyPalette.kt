package com.hp12c.android.ui

import android.graphics.Color

/**
 * Cores de fundo das teclas, com fidelidade à HP 12C física.
 * Atualizado com valores extraídos do skin.json do projeto hp12c-emulator-rust (MIT).
 */
enum class KeyColor {
    BEIGE,    // teclas numéricas e financeiras (bege/cream)
    ORANGE,   // tecla ENTER e f (dourado)
    BLUE,     // tecla g (azul)
    DARK      // tecla ON (grafite)
}

/**
 * Paleta de cores da HP 12C — valores exatos do skin.json.
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
        /** Paleta padrão com cores exatas da HP 12C física. */
        val Default = KeyPalette(
            beigeBg = Color.parseColor("#D6C8A9"),  // bege cream (mantém)
            beigeFg = Color.parseColor("#101010"),
            // Laranja/dourado da tecla f e ENTER
            orangeBg = HpColors.fKeyTop(),         // #e3a53a
            orangeFg = Color.parseColor(HpColors.PREFIX_KEY_TEXT),  // #101010
            // Azul da tecla g
            blueBg = HpColors.gKeyTop(),            // #4f9fdc
            blueFg = Color.parseColor("#FFFFFF"),
            darkBg = Color.parseColor("#3A3A3A"),  // ON (mantém)
            darkFg = Color.parseColor("#FFFFFF"),
            // Labels das funções
            labelOrange = HpColors.fLabel(),        // #e2a93b (dourado)
            labelBlue = HpColors.gLabel(),           // #6bb6ea (azul claro)
            labelGray = Color.parseColor("#6B6B6B")
        )
    }
}

fun KeyColor.background(p: KeyPalette): Int = when (this) {
    KeyColor.BEIGE -> p.beigeBg
    KeyColor.ORANGE -> p.orangeBg
    KeyColor.BLUE -> p.blueBg
    KeyColor.DARK -> p.darkBg
}

fun KeyColor.foreground(p: KeyPalette): Int = when (this) {
    KeyColor.BEIGE -> p.beigeFg
    KeyColor.ORANGE -> p.orangeFg
    KeyColor.BLUE -> p.blueFg
    KeyColor.DARK -> p.darkFg
}
