package com.hp12c.android.ui

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import net.sf.finanx.fx12c.calc.Calculator
import net.sf.finanx.fx12c.calc.CalculatorException
import net.sf.finanx.fx12c.calc.CalculatorListener
import net.sf.finanx.fx12c.calc.Key
import net.sf.finanx.fx12c.math.Number
import java.math.RoundingMode

/**
 * Hospeda o motor financeiro HP 12C (Java puro, MIT) e notifica a UI via
 * callback simples [CalcStateListener].
 *
 * IMPORTANTE: nesta versão, sendKey() executa na MESMA thread do chamador
 * (geralmente a main thread do Android). O motor finanx-12c é síncrono e
 * rápido para a maioria das operações, então isto é seguro. Para operações
 * potencialmente lentas (IRR, AMORT com muitos períodos), o motor emite
 * resultado via listener onDisplayUpdate() que é processado na thread atual.
 */
class CalculatorEngine(app: Application) {

    private val app: Application = app
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Motor financeiro. Inicializado uma vez. */
    private val calculator: Calculator = Calculator().apply {
        setListener(object : CalculatorListener {
            override fun onDisplayUpdate() {
                Log.d(TAG, "onDisplayUpdate chamado pelo motor")
                pushSnapshot()
            }
            override fun onQuit() {
                Log.d(TAG, "onQuit chamado pelo motor")
                quitListener?.onQuit()
            }
        })
        // HP 12C real usa 2 casas decimais por padrão (modo FIX 2).
        try {
            display.setPrecision(2)
            Log.d(TAG, "Precisão setada para 2")
        } catch (e: Throwable) {
            Log.e(TAG, "Erro ao setar precisão", e)
        }
    }

    /** Lista de listeners de UI a serem notificados. */
    private val stateListeners = mutableListOf<CalcStateListener>()
    @Volatile var quitListener: QuitListener? = null

    /** Haptic / som configuráveis. */
    @Volatile var hapticEnabled: Boolean = true
        private set
    @Volatile var soundEnabled: Boolean = false
        private set

    /** Histórico de operações (últimas 100 teclas). */
    private val history = OperationHistory(maxSize = 100)

    init {
        Log.d(TAG, "CalculatorEngine init begin")
        loadPersistedState()
        pushSnapshot()
        Log.d(TAG, "CalculatorEngine init end")
    }

    /** Registra listener para receber updates de estado. */
    fun addStateListener(l: CalcStateListener) {
        stateListeners.add(l)
        Log.d(TAG, "Listener adicionado: $l (total: ${stateListeners.size})")
    }

    fun removeStateListener(l: CalcStateListener) {
        stateListeners.remove(l)
    }

    /**
     * Envia uma tecla ao motor. Thread-safe via synchronized.
     * Executa na thread do chamador para simplificar — o motor é síncrono.
     */
    @Synchronized
    fun sendKey(key: Key) {
        Log.d(TAG, "sendKey: $key (code=${key.code})")
        val displayBefore = try { calculator.display.string ?: "0" } catch (_: Throwable) { "0" }
        // Indicar "running" para operações potencialmente longas (IRR, AMORT, NPV, YTM)
        val isLongOp = key == Key.KEY_15 || key == Key.KEY_13 || key == Key.KEY_14 || key == Key.KEY_11
        if (isLongOp) {
            running = true
            notifyRunning(true)
        }
        try {
            calculator.keyPressed(key)
            calculator.keyReleased(key)
            Log.d(TAG, "sendKey: processado, chamando pushSnapshot")
        } catch (e: CalculatorException) {
            Log.e(TAG, "CalculatorException em sendKey($key)", e)
        } catch (e: Throwable) {
            Log.e(TAG, "Throwable em sendKey($key)", e)
            e.printStackTrace()
        }
        running = false
        notifyRunning(false)
        // Registrar no histórico
        val displayAfter = try { calculator.display.string ?: "0" } catch (_: Throwable) { "0" }
        history.add(HistoryEntry(
            key = key,
            keyName = key.name,
            displayBefore = displayBefore,
            displayAfter = displayAfter,
            timestamp = System.currentTimeMillis()
        ))
        pushSnapshot(key)
        saveState()
    }

    @Volatile
    var running: Boolean = false
        private set

    private fun notifyRunning(running: Boolean) {
        mainHandler.post {
            for (l in stateListeners.toList()) {
                try {
                    if (l is CalcStateListenerEx) l.onRunningStateChanged(running)
                } catch (_: Throwable) {}
            }
        }
    }

    /** Atualiza o snapshot e notifica todos os listeners (na main thread). */
    private fun pushSnapshot(pressedKey: Key? = null) {
        val dsp = calculator.display
        val fin = calculator.financeMemory
        val flags = calculator.flags

        val displayStr: String = try {
            dsp.string ?: "0"
        } catch (e: Throwable) {
            Log.e(TAG, "Erro ao ler display.string", e)
            "0"
        }
        Log.d(TAG, "pushSnapshot: display='$displayStr', pressedKey=$pressedKey")

        val finSummary = buildString {
            append("n=").append(formatNum(fin.n, 0)).append("  ")
            append("i=").append(formatNum(fin.i, 4)).append("  ")
            append("PV=").append(formatNum(fin.pv, 2)).append("  ")
            append("PMT=").append(formatNum(fin.pmt, 2)).append("  ")
            append("FV=").append(formatNum(fin.fv, 2))
        }

        val activeIndicators = mutableSetOf<String>()
        if (flags.begin == 1) activeIndicators.add("BEGIN")
        if (flags.dmy == 1) activeIndicators.add("D.MY")
        if (flags.c == 1) activeIndicators.add("C")
        if (flags.prgm == 1) activeIndicators.add("PRGM")
        if (flags.f == 1) activeIndicators.add("f")
        if (flags.g == 1) activeIndicators.add("g")

        val state = CalcUiState(
            display = displayStr,
            indicators = activeIndicators,
            financeSummary = finSummary,
            pressedKey = pressedKey
        )
        // Notificar na main thread
        mainHandler.post {
            // Copiar a lista para evitar ConcurrentModificationException
            val copy = stateListeners.toList()
            Log.d(TAG, "Notificando ${copy.size} listeners")
            for (l in copy) {
                try {
                    l.onStateChanged(state)
                } catch (e: Throwable) {
                    Log.e(TAG, "Erro ao notificar listener", e)
                }
            }
        }
    }

    /** Formata um Number com `decimals` casas para exibição no painel. */
    private fun formatNum(n: Number?, decimals: Int): String {
        if (n == null) return "0"
        return try {
            n.copyValue().setScale(decimals, RoundingMode.HALF_UP).toPlainString()
        } catch (e: Throwable) { "0" }
    }

    // --- Persistência ---
    private fun loadPersistedState() {
        try {
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true)
            soundEnabled = prefs.getBoolean(KEY_SOUND, false)
            StateRepository.restore(calculator, prefs.getString(KEY_STATE, null))
        } catch (e: Throwable) {
            Log.e(TAG, "Erro em loadPersistedState", e)
        }
    }

    private fun saveState() {
        try {
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = StateRepository.serialize(calculator)
            prefs.edit()
                .putString(KEY_STATE, json)
                .putBoolean(KEY_HAPTIC, hapticEnabled)
                .putBoolean(KEY_SOUND, soundEnabled)
                .apply()
        } catch (e: Throwable) {
            Log.e(TAG, "Erro em saveState", e)
        }
    }

    fun updateHapticEnabled(v: Boolean) {
        hapticEnabled = v
        saveState()
    }

    fun updateSoundEnabled(v: Boolean) {
        soundEnabled = v
        saveState()
    }

    /** Limpa todo o estado (memória financeira, geral, pilha). */
    fun clearAll() {
        try {
            calculator.keyPressed(Key.KEY_42); calculator.keyReleased(Key.KEY_42)
            calculator.keyPressed(Key.KEY_35); calculator.keyReleased(Key.KEY_35)
        } catch (_: Throwable) {}
        pushSnapshot()
        saveState()
    }

    /** Acesso ao motor (para testes). */
    fun getEngine(): Calculator = calculator

    /** Retorna o texto formatado do histórico de operações. */
    fun getHistoryText(): String = history.toText()

    /** Limpa o histórico de operações. */
    fun clearHistory() = history.clear()

    companion object {
        private const val TAG = "HP12C/Engine"
        private const val PREFS_NAME = "hp12c_state"
        private const val KEY_STATE = "engine_state_json"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SOUND = "sound_enabled"

        @Volatile
        private var instance: CalculatorEngine? = null

        /** Singleton — inicializado uma vez por processo. */
        fun getInstance(app: Application): CalculatorEngine {
            return instance ?: synchronized(this) {
                instance ?: CalculatorEngine(app).also { instance = it }
            }
        }
    }
}
