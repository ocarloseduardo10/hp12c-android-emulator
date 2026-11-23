package com.hp12c.android.ui

import android.graphics.Color

/**
 * Paletas de tema para modo escuro (padrão HP 12C) e modo claro.
 *
 * Alternável pelo usuário via menu → "Modo claro/escuro".
 */
object ThemeManager {

    @Volatile
    var isDarkTheme: Boolean = true
        private set

    /** Cores do tema ativo. */
    object Active {
        fun bodyBg(): Int = if (isDarkTheme) HpColors.body() else Color.parseColor("#FAFAFA")
        fun panelBg(): Int = if (isDarkTheme) HpColors.panel() else Color.parseColor("#F0F0F0")
        fun lcdBg(): Int = if (isDarkTheme) HpColors.lcd() else Color.parseColor("#D4DCC4")
        fun segment(): Int = if (isDarkTheme) HpColors.segment() else Color.parseColor("#1A1A1A")
        fun segmentGhost(): Int = if (isDarkTheme) HpColors.segmentGhost() else Color.parseColor("#B8C0A8")
        fun keyBg(): Int = if (isDarkTheme) HpColors.keyTop() else Color.parseColor("#E0E0E0")
        fun keyText(): Int = if (isDarkTheme) HpColors.keyText() else Color.parseColor("#1A1A1A")
        fun fLabel(): Int = if (isDarkTheme) HpColors.fLabel() else Color.parseColor("#B45309")
        fun gLabel(): Int = if (isDarkTheme) HpColors.gLabel() else Color.parseColor("#1F5BB8")
    }

    /** Alterna entre tema escuro e claro. */
    fun toggle() {
        isDarkTheme = !isDarkTheme
    }

    /** Define o tema explicitamente. */
    fun setDark(dark: Boolean) {
        isDarkTheme = dark
    }
}
