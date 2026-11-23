package com.hp12c.android.ui

/**
 * Snapshot imutável do estado da calculadora para a UI desenhar.
 */
data class CalcUiState(
    val display: String = "0.00",
    val indicators: Set<String> = emptySet(),
    val financeSummary: String = "",
    val pressedKey: net.sf.finanx.fx12c.calc.Key? = null
)

/**
 * Callback para notificar a UI de mudanças de estado.
 */
interface CalcStateListener {
    fun onStateChanged(state: CalcUiState)
}

/**
 * Listener estendido — também notifica mudanças no estado "running"
 * (cálculos longos como IRR, AMORT, NPV).
 */
interface CalcStateListenerEx : CalcStateListener {
    fun onRunningStateChanged(running: Boolean)
}

/**
 * Listener simples para "quit" (quando o usuário pressiona ON+ON).
 */
interface QuitListener {
    fun onQuit()
}
