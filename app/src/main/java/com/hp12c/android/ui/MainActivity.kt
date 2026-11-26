package com.hp12c.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.hp12c.android.R
import net.sf.finanx.fx12c.calc.Key

/**
 * Activity principal do HP 12C Mobile.
 *
 * Implementa CalcStateListenerEx para receber updates de estado.
 * Suporta teclado físico (Bluetooth/USB) via onKeyDown.
 * Inclui histórico de operações e ajuda contextual.
 */
class MainActivity : Activity(), CalcStateListenerEx, QuitListener {

    private lateinit var lcdDisplay: LcdDisplayView
    private var engine: CalculatorEngine? = null
    private var registersDialog: RegistersDialog? = null
    private var historyDialog: android.app.Dialog? = null
    private var helpToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate begin")
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )

            setContentView(R.layout.activity_main)
            lcdDisplay = findViewById(R.id.lcd_display)

            engine = CalculatorEngine.getInstance(application)
            engine?.addStateListener(this)
            engine?.quitListener = this

            wireAllKeys()
            Log.d(TAG, "onCreate ok")
        } catch (e: Throwable) {
            Log.e(TAG, "onCreate falhou", e)
            showErrorAndFinish("onCreate: ${e.message}")
        }
    }

    // === Suporte a teclado físico (Bluetooth/USB) ===
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val key = KeyboardMapper.mapKey(keyCode, event)
        if (key != null) {
            engine?.sendKey(key)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // === Menu ===
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_registers -> { showRegisters(); true }
            R.id.menu_manual -> { startActivity(Intent(this, ManualActivity::class.java)); true }
            R.id.menu_clear_all -> { engine?.clearAll(); true }
            R.id.menu_history -> { showHistory(); true }
            R.id.menu_theme -> {
                ThemeManager.toggle()
                lcdDisplay.invalidate()
                Toast.makeText(this,
                    if (ThemeManager.isDarkTheme) "Modo escuro" else "Modo claro",
                    Toast.LENGTH_SHORT).show()
                recreate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // === Callbacks da engine ===
    override fun onStateChanged(state: CalcUiState) {
        try {
            lcdDisplay.setState(
                display = state.display,
                activeIndicators = state.indicators,
                summary = state.financeSummary
            )
            // Ajuda contextual: se f ou g está ativo, mostrar dica
            if ("f" in state.indicators || "g" in state.indicators) {
                // Dica genérica — o usuário verá ao pressionar a próxima tecla
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Erro ao atualizar display", e)
        }
    }

    override fun onRunningStateChanged(running: Boolean) {
        lcdDisplay.setRunning(running)
    }

    override fun onQuit() { finish() }

    // === Janelas auxiliares ===
    fun showRegisters() {
        try {
            val eng = engine ?: return
            val calc = eng.getEngine()
            registersDialog?.dismiss()
            registersDialog = RegistersDialog(this, calc).also { it.show() }
        } catch (e: Throwable) {
            Log.e(TAG, "Erro ao abrir registradores", e)
        }
    }

    fun showHistory() {
        try {
            val eng = engine ?: return
            historyDialog?.dismiss()
            val dialog = android.app.Dialog(this)
            dialog.setTitle("Histórico de Operações")
            val scroll = android.widget.ScrollView(this)
            val tv = TextView(this).apply {
                textSize = 13f
                typeface = android.graphics.Typeface.MONOSPACE
                setPadding(24, 24, 24, 24)
                text = eng.getHistoryText().ifEmpty { "(nenhuma operação ainda)" }
            }
            scroll.addView(tv)
            dialog.setContentView(scroll)
            dialog.show()
            historyDialog = dialog
        } catch (e: Throwable) {
            Log.e(TAG, "Erro ao abrir histórico", e)
        }
    }

    private fun showErrorAndFinish(msg: String) {
        try {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }
            container.addView(TextView(this).apply {
                text = "HP 12C Mobile — Erro\n\n$msg\n\nPor favor reinstale o app."
                textSize = 16f
            })
            setContentView(container)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        } catch (_: Throwable) { finish() }
    }

    private fun wireAllKeys() {
        val eng = engine ?: return
        val onPress: (Key) -> Unit = { k ->
            Log.d(TAG, "Tecla pressionada: $k (code=${k.code})")
            eng.sendKey(k)
        }
        val haptic = eng.hapticEnabled
        var wired = 0
        walkKeyViews(findViewById<View>(android.R.id.content)) { keyView ->
            val k = keyView.key ?: return@walkKeyViews
            keyView.onPress = onPress
            keyView.hapticEnabled = haptic
            wired++
        }
        Log.d(TAG, "wireAllKeys: $wired teclas conectadas")
    }

    private fun walkKeyViews(root: View, block: (CalculatorKeyView) -> Unit) {
        if (root is CalculatorKeyView) { block(root); return }
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) {
                walkKeyViews(root.getChildAt(i), block)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        engine?.removeStateListener(this)
        registersDialog?.dismiss()
        historyDialog?.dismiss()
    }

    companion object {
        private const val TAG = "HP12C/MainActivity"
    }
}
