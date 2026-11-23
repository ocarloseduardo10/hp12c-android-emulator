package com.hp12c.android.ui

import android.graphics.Color

/**
 * Cores exatas da HP 12C física, extraídas do arquivo `skin.json`
 * do projeto hp12c-emulator-rust (MIT, Raul Kiaer).
 *
 * Estes são os valores RGB precisos que reproduzem o visual da calculadora real.
 */
object HpColors {
    // Corpo e painel
    const val BODY = "#0e0d0c"          // grafite profundo (corpo principal)
    const val PANEL = "#1f1b17"          // marrom escuro (painel interno)
    const val PANEL_EDGE = "#3a3229"     // borda do painel

    // Faixa metálica dourada (gradiente)
    const val METAL_TOP = "#d8c89f"
    const val METAL_BOTTOM = "#a99566"
    const val METAL_LINE = "#c3b083"

    // Bezel e bordas
    const val BEZEL = "#262422"
    const val BEZEL_EDGE = "#4a4640"

    // Display LCD
    const val LCD = "#b9c0a6"            // verde-oliva (fundo do LCD)
    const val LCD_SHADOW = "#8f9681"     // sombra ao redor do LCD
    const val SEGMENT = "#1a1e16"        // segmentos acesos (preto esverdeado)
    const val SEGMENT_GHOST = "#b0b79d"  // segmentos desligados (visíveis como ghosts)

    // Teclas
    const val KEY_TOP = "#2c2c2c"        // topo da tecla (cinza escuro)
    const val KEY_TOP_LIGHT = "#3a3a3a"   // topo da tecla (variante clara)
    const val KEY_FACE = "#121212"        // face da tecla (preto profundo)
    const val KEY_EDGE = "#000000"
    const val KEY_PRESSED = "#1c1c1c"
    const val KEY_TEXT = "#f4f4f4"        // texto das teclas (branco suave)

    // Labels das funções f (laranja/dourado) e g (azul)
    const val F_LABEL = "#e2a93b"        // dourado (labels f acima das teclas)
    const val G_LABEL = "#6bb6ea"        // azul claro (labels g abaixo das teclas)

    // Cores das teclas f e g (botões físicos)
    const val F_KEY_TOP = "#e3a53a"       // topo da tecla f (laranja dourado)
    const val F_KEY_FACE = "#a8741f"     // face da tecla f
    const val G_KEY_TOP = "#4f9fdc"       // topo da tecla g (azul)
    const val G_KEY_FACE = "#2d6ea5"     // face da tecla g
    const val PREFIX_KEY_TEXT = "#101010" // texto das teclas f/g

    // Emblema "12C FINANCIAL CALCULATOR"
    const val BADGE_TEXT = "#3b3226"
    const val BADGE_SUB = "#5a4d39"

    // Conversores para Color Int (Android)
    fun body(): Int = Color.parseColor(BODY)
    fun panel(): Int = Color.parseColor(PANEL)
    fun bezel(): Int = Color.parseColor(BEZEL)
    fun lcd(): Int = Color.parseColor(LCD)
    fun lcdShadow(): Int = Color.parseColor(LCD_SHADOW)
    fun segment(): Int = Color.parseColor(SEGMENT)
    fun segmentGhost(): Int = Color.parseColor(SEGMENT_GHOST)
    fun keyTop(): Int = Color.parseColor(KEY_TOP)
    fun keyFace(): Int = Color.parseColor(KEY_FACE)
    fun keyText(): Int = Color.parseColor(KEY_TEXT)
    fun fLabel(): Int = Color.parseColor(F_LABEL)
    fun gLabel(): Int = Color.parseColor(G_LABEL)
    fun fKeyTop(): Int = Color.parseColor(F_KEY_TOP)
    fun fKeyFace(): Int = Color.parseColor(F_KEY_FACE)
    fun gKeyTop(): Int = Color.parseColor(G_KEY_TOP)
    fun gKeyFace(): Int = Color.parseColor(G_KEY_FACE)
}
