package com.hp12c.android.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import net.sf.finanx.fx12c.calc.Calculator
import net.sf.finanx.fx12c.memory.GeneralMemory
import java.math.RoundingMode

/**
 * Janela de registradores ao vivo (estilo F2 do projeto Rust).
 *
 * Mostra em tempo real:
 *  - Pilha RPN: X, Y, Z, T, LastX
 *  - Registradores financeiros: n, i, PV, PMT, FV
 *  - Registradores de memória: R0-R9, R.0-R.9 (20 registradores)
 *  - Indicadores: BEGIN/END, D.MY/M.DY, C, PRGM, f, g
 *
 * Atualiza a cada 500ms enquanto aberta, lendo do motor via [Calculator].
 */
class RegistersDialog(
    context: Context,
    private val calculator: Calculator
) : Dialog(context) {

    private var refreshRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private lateinit var stackView: TextView
    private lateinit var finView: TextView
    private lateinit var memView: TextView
    private lateinit var indView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Registradores")
        buildView()
        scheduleRefresh()
    }

    private fun buildView() {
        val density = context.resources.displayMetrics.density
        val padding = (12f * density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        // Indicadores
        indView = TextView(context).apply {
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF000000.toInt())
        }
        container.addView(indView)

        // Pilha
        val stackTitle = TextView(context).apply {
            text = "PILHA RPN"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF666666.toInt())
            setPadding(0, padding, 0, padding / 2)
        }
        container.addView(stackTitle)
        stackView = TextView(context).apply {
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF1A1A1A.toInt())
        }
        container.addView(stackView)

        // Registradores financeiros
        val finTitle = TextView(context).apply {
            text = "REGISTRADORES FINANCEIROS"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF666666.toInt())
            setPadding(0, padding, 0, padding / 2)
        }
        container.addView(finTitle)
        finView = TextView(context).apply {
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF1A1A1A.toInt())
        }
        container.addView(finView)

        // Registradores de memória
        val memTitle = TextView(context).apply {
            text = "MEMÓRIA (R0-R.9)"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF666666.toInt())
            setPadding(0, padding, 0, padding / 2)
        }
        container.addView(memTitle)
        memView = TextView(context).apply {
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF1A1A1A.toInt())
        }
        container.addView(memView)

        val scroll = ScrollView(context).apply {
            addView(container)
        }
        setContentView(scroll)
    }

    private fun scheduleRefresh() {
        refreshRunnable = object : Runnable {
            override fun run() {
                refresh()
                handler.postDelayed(this, 500)
            }
        }
        refreshRunnable?.run()
    }

    private fun refresh() {
        try {
            val stk = calculator.stack
            val fin = calculator.financeMemory
            val mem = calculator.generalMemory
            val flags = calculator.flags

            // Indicadores
            val sb = StringBuilder()
            if (flags.begin == 1) sb.append("[BEGIN] ")
            if (flags.dmy == 1) sb.append("[D.MY] ") else sb.append("[M.DY] ")
            if (flags.c == 1) sb.append("[C] ")
            if (flags.prgm == 1) sb.append("[PRGM] ")
            if (flags.f == 1) sb.append("[f] ")
            if (flags.g == 1) sb.append("[g] ")
            indView.text = if (sb.isEmpty()) "(modo RUN)" else sb.toString()

            // Pilha
            stackView.text = buildString {
                append("X  = ").append(fmt(stk.get(0))).append("\n")
                append("Y  = ").append(fmt(stk.get(1))).append("\n")
                append("Z  = ").append(fmt(stk.get(2))).append("\n")
                append("T  = ").append(fmt(stk.get(3))).append("\n")
                append("Lx = ").append(fmt(stk.lastX))
            }

            // Financeiros
            finView.text = buildString {
                append("n   = ").append(fmt(fin.n, 0)).append("\n")
                append("i   = ").append(fmt(fin.i, 4)).append("\n")
                append("PV  = ").append(fmt(fin.pv)).append("\n")
                append("PMT = ").append(fmt(fin.pmt)).append("\n")
                append("FV  = ").append(fmt(fin.fv))
            }

            // Memória (R0-R9 e R.0-R.9)
            val memSb = StringBuilder()
            for (i in 0 until 10) {
                memSb.append("R").append(i).append(" = ").append(fmt(mem.get(i))).append("    ")
                memSb.append("R.").append(i).append(" = ").append(fmt(mem.get(i + 10))).append("\n")
            }
            memView.text = memSb.toString()
        } catch (e: Exception) {
            // Ignora erro de leitura
        }
    }

    private fun fmt(n: net.sf.finanx.fx12c.math.Number?, decimals: Int = 4): String {
        if (n == null) return "0"
        return try {
            n.copyValue().setScale(decimals, RoundingMode.HALF_UP).toPlainString()
        } catch (e: Exception) { "0" }
    }

    override fun dismiss() {
        refreshRunnable?.let { handler.removeCallbacks(it) }
        super.dismiss()
    }
}
