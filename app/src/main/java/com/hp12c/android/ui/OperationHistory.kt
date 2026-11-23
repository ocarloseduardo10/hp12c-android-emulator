package com.hp12c.android.ui

import net.sf.finanx.fx12c.calc.Key

/**
 * Item do histórico de operações.
 * Registra cada tecla pressionada com o display antes e depois.
 */
data class HistoryEntry(
    val key: Key,
    val keyName: String,
    val displayBefore: String,
    val displayAfter: String,
    val timestamp: Long
)

/**
 * Mantém o histórico das últimas N operações da calculadora.
 * Suporte para "undo" visual e auditoria de cálculos.
 */
class OperationHistory(private val maxSize: Int = 100) {
    private val entries = mutableListOf<HistoryEntry>()

    /** Adiciona uma operação ao histórico. */
    fun add(entry: HistoryEntry) {
        entries.add(entry)
        if (entries.size > maxSize) {
            entries.removeAt(0)
        }
    }

    /** Retorna todas as entradas do histórico (mais recente primeiro). */
    fun getAll(): List<HistoryEntry> = entries.reversed()

    /** Retorna as últimas N entradas. */
    fun getLast(n: Int): List<HistoryEntry> =
        if (entries.isEmpty()) emptyList()
        else entries.takeLast(n).reversed()

    /** Limpa todo o histórico. */
    fun clear() = entries.clear()

    /** Número de entradas no histórico. */
    fun size(): Int = entries.size

    /** Formata o histórico como texto para exibição. */
    fun toText(): String {
        val sb = StringBuilder()
        for (entry in getAll()) {
            sb.append("[${entry.keyName}]")
            if (entry.displayBefore != entry.displayAfter) {
                sb.append("  ${entry.displayBefore} → ${entry.displayAfter}")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
