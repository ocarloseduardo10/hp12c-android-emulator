package com.hp12c.android.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
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
 * Estende [Activity] do framework (não AppCompatActivity) para evitar
 * dependências de androidx.fragment, androidx.lifecycle e similares.
 *
 * - Mantém tela ligada (FLAG_KEEP_SCREEN_ON)
 * - Estado do motor financeiro vive no singleton [CalculatorEngine]
 * - UI é notificada via callback simples [CalcStateListener] (sem LiveData)
 *
 * Tratamento de erros defensivo: se algo falhar, mostra mensagem visível
 * em vez de crashar.
 */
class MainActivity : Activity(), CalcStateListener, QuitListener {

    private lateinit var lcdDisplay: LcdDisplayView
    private var engine: CalculatorEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate begin")
        try {
            // Manter tela ligada durante uso da calculadora.
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Modo imersivo via flags legadas (compatível com qualquer versão Android).
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )

            Log.d(TAG, "setContentView")
            setContentView(R.layout.activity_main)

            lcdDisplay = findViewById(R.id.lcd_display)
            Log.d(TAG, "lcd_display encontrado: $lcdDisplay")

            // Instanciar engine singleton
            Log.d(TAG, "criando CalculatorEngine")
            engine = CalculatorEngine.getInstance(application)
            engine?.addStateListener(this)
            engine?.quitListener = this
            Log.d(TAG, "Engine criado")

            wireAllKeys()
            Log.d(TAG, "onCreate ok")
        } catch (e: Throwable) {
            Log.e(TAG, "onCreate falhou", e)
            showErrorAndFinish("onCreate: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        engine?.removeStateListener(this)
    }

    /** Recebe snapshot atualizado da engine e atualiza o display. */
    override fun onStateChanged(state: CalcUiState) {
        try {
            lcdDisplay.setState(
                display = state.display,
                activeIndicators = state.indicators,
                summary = state.financeSummary
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Erro ao atualizar display", e)
        }
    }

    override fun onQuit() {
        // Usuário pressionou ON+ON — apenas finish a Activity.
        finish()
    }

    /** Mostra erro na tela em vez de crashar silenciosamente. */
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
        } catch (_: Throwable) {
            finish()
        }
    }

    /** Liga cada CalculatorKeyView do layout à sua Key e ao callback de pressão. */
    private fun wireAllKeys() {
        val eng = engine ?: return
        val onPress: (Key) -> Unit = { k ->
            Log.d(TAG, "Tecla pressionada: $k (code=${k.code})")
            eng.sendKey(k)
        }
        val haptic = eng.hapticEnabled

        var wired = 0
        walkKeyViews(findViewById<View>(android.R.id.content)) { keyView ->
            val k = keyView.key ?: run {
                Log.w(TAG, "CalculatorKeyView sem key_code! id=${keyView.id}")
                return@walkKeyViews
            }
            keyView.onPress = onPress
            keyView.hapticEnabled = haptic
            wired++
        }
        Log.d(TAG, "wireAllKeys: $wired teclas conectadas")
    }

    /** Recursivamente visita cada CalculatorKeyView abaixo de [root]. */
    private fun walkKeyViews(root: View, block: (CalculatorKeyView) -> Unit) {
        if (root is CalculatorKeyView) {
            block(root)
            return
        }
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) {
                walkKeyViews(root.getChildAt(i), block)
            }
        }
    }

    companion object {
        private const val TAG = "HP12C/MainActivity"
    }
}
