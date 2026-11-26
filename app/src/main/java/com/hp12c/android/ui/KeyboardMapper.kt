package com.hp12c.android.ui

import android.view.KeyEvent
import net.sf.finanx.fx12c.calc.Key

/**
 * Mapeia teclas do teclado físico (Bluetooth/USB) para Key do motor HP 12C.
 *
 * Permite usar um teclado Bluetooth conectado ao celular para operar a calculadora,
 * exatamente como no projeto Rust (teclado do PC).
 *
 * Mapeamento baseado na tabela do README do hp12c-emulator-rust:
 *
 * | PC           | HP-12C |
 * |--------------|--------|
 * | 0-9, ., ,   | dígitos, ponto |
 * | Enter        | ENTER  |
 * | + - * /      | + − × ÷ |
 * | Backspace    | CLx    |
 * | F / G        | f / g  |
 * | S / R        | STO / RCL |
 * | D            | R↓     |
 * | Tab          | x↔y    |
 * | N, I, P, M, V | n, i, PV, PMT, FV |
 * | E / C        | EEX / CHS |
 */
object KeyboardMapper {

    /** Converte um Android KeyEvent para Key do motor HP 12C, ou null se não mapeado. */
    fun mapKey(keyCode: Int, event: KeyEvent): Key? {
        // Números 0-9
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            return Key.valueOf(keyCode - KeyEvent.KEYCODE_0)
        }
        // Numpad 0-9
        if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            return Key.valueOf(keyCode - KeyEvent.KEYCODE_NUMPAD_0)
        }

        return when (keyCode) {
            // Ponto decimal
            KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_NUMPAD_DOT -> Key.KEY_48
            // Vírgula = ponto decimal (algumas localizações)
            KeyEvent.KEYCODE_COMMA -> Key.KEY_48

            // Enter = ENTER
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> Key.KEY_36

            // Operadores
            KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_NUMPAD_ADD -> Key.KEY_40
            KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> Key.KEY_30
            KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> Key.KEY_20
            KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_NUMPAD_DIVIDE -> Key.KEY_10

            // Backspace/Delete = CLx
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> Key.KEY_35

            // F / G
            KeyEvent.KEYCODE_F -> Key.KEY_42
            KeyEvent.KEYCODE_G -> Key.KEY_43

            // S = STO, R = RCL
            KeyEvent.KEYCODE_S -> Key.KEY_44
            KeyEvent.KEYCODE_R -> Key.KEY_45

            // D = R↓
            KeyEvent.KEYCODE_D -> Key.KEY_33

            // Tab = x↔y
            KeyEvent.KEYCODE_TAB -> Key.KEY_34

            // TVM keys
            KeyEvent.KEYCODE_N -> Key.KEY_11
            KeyEvent.KEYCODE_I -> Key.KEY_12
            KeyEvent.KEYCODE_P -> Key.KEY_13  // PV
            KeyEvent.KEYCODE_M -> Key.KEY_14  // PMT
            KeyEvent.KEYCODE_V -> Key.KEY_15  // FV

            // E = EEX, C = CHS
            KeyEvent.KEYCODE_E -> Key.KEY_26
            KeyEvent.KEYCODE_C -> Key.KEY_16

            // Y = yˣ, X = 1/x
            KeyEvent.KEYCODE_Y -> Key.KEY_21
            KeyEvent.KEYCODE_X -> Key.KEY_22

            // Percent
            KeyEvent.KEYCODE_NUMPAD_EQUALS -> Key.KEY_25  // % (approximação)

            // ESC = cancela f/g (como ON)
            KeyEvent.KEYCODE_ESCAPE -> Key.KEY_41

            else -> null
        }
    }
}
