package com.hp12c.android.ui

import net.sf.finanx.fx12c.calc.Calculator
import net.sf.finanx.fx12c.math.Number
import net.sf.finanx.fx12c.memory.FinanceMemory
import net.sf.finanx.fx12c.memory.GeneralMemory
import net.sf.finanx.fx12c.memory.Stack
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializa/deserializa o estado completo do motor HP 12C em JSON.
 *
 * Estado persistido:
 *  - Stack: X/Y/Z/T + LastX (BigDecimal serializado como string para preservar precisão)
 *  - FinanceMemory: n, i, PV, PMT, FV, Begin
 *  - GeneralMemory: 20 registradores R0..R9, .0...9
 *  - Display flags: precisão, separador
 *
 * Observação: usamos apenas a API pública do motor finanx-12c (FinanceMemory
 * não expõe setters para cash flows — esses são manipulados pela Calculadora
 * via keyPressed, então não persistimos estado de cash flows separadamente).
 *
 * Formato JSON compacto, robusto a mudanças de versão (campos opcionais).
 */
object StateRepository {

    /** Serializa o motor para uma string JSON. */
    fun serialize(calc: Calculator): String {
        val json = JSONObject()
        try {
            json.put("version", 1)
            json.put("stack", serializeStack(calc.stack))
            json.put("finance", serializeFinance(calc.financeMemory))
            json.put("general", serializeGeneral(calc.generalMemory))
            json.put("config", serializeConfig(calc))
        } catch (e: Exception) {
            return JSONObject().put("version", 1).toString()
        }
        return json.toString()
    }

    /** Restaura o estado a partir de uma string JSON (ou null se inválida). */
    fun restore(calc: Calculator, json: String?) {
        if (json.isNullOrBlank()) return
        try {
            val obj = JSONObject(json)
            val version = obj.optInt("version", 0)
            if (version < 1) return

            obj.optJSONObject("stack")?.let { restoreStack(calc.stack, it) }
            obj.optJSONObject("finance")?.let { restoreFinance(calc.financeMemory, it) }
            obj.optJSONObject("general")?.let { restoreGeneral(calc.generalMemory, it) }
            obj.optJSONObject("config")?.let { restoreConfig(calc, it) }
            calc.updateDisplay()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Stack ---
    private fun serializeStack(stk: Stack): JSONObject {
        val obj = JSONObject()
        try {
            val arr = JSONArray()
            for (i in 0 until 4) {
                arr.put(stk.get(i).toString())
            }
            obj.put("regs", arr)
            obj.put("lastX", stk.lastX.toString())
        } catch (e: Exception) { /* skip */ }
        return obj
    }

    private fun restoreStack(stk: Stack, obj: JSONObject) {
        try {
            val arr = obj.optJSONArray("regs")
            if (arr != null && arr.length() == 4) {
                for (i in 0 until 4) {
                    stk.set(i, Number.getInstance(arr.getString(i)))
                }
            }
            obj.optString("lastX").let { s ->
                if (s.isNotEmpty()) stk.setLastX(Number.getInstance(s))
            }
        } catch (e: Exception) { /* skip */ }
    }

    // --- FinanceMemory (apenas o que é exposto publicamente) ---
    private fun serializeFinance(fin: FinanceMemory): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("n", fin.n.toString())
            obj.put("i", fin.i.toString())
            obj.put("pv", fin.pv.toString())
            obj.put("pmt", fin.pmt.toString())
            obj.put("fv", fin.fv.toString())
            obj.put("begin", fin.isBegin)
            obj.put("c", fin.isC)
        } catch (e: Exception) { /* skip */ }
        return obj
    }

    private fun restoreFinance(fin: FinanceMemory, obj: JSONObject) {
        try {
            obj.optString("n").let { if (it.isNotEmpty()) fin.setN(Number.getInstance(it)) }
            obj.optString("i").let { if (it.isNotEmpty()) fin.setI(Number.getInstance(it)) }
            obj.optString("pv").let { if (it.isNotEmpty()) fin.setPv(Number.getInstance(it)) }
            obj.optString("pmt").let { if (it.isNotEmpty()) fin.setPmt(Number.getInstance(it)) }
            obj.optString("fv").let { if (it.isNotEmpty()) fin.setFv(Number.getInstance(it)) }
            fin.setBegin(obj.optBoolean("begin", false))
            fin.setC(obj.optBoolean("c", false))
        } catch (e: Exception) { /* skip */ }
    }

    // --- GeneralMemory ---
    private fun serializeGeneral(mem: GeneralMemory): JSONObject {
        val obj = JSONObject()
        try {
            val arr = JSONArray()
            for (i in 0 until GeneralMemory.SIZE) {
                arr.put(mem.get(i).toString())
            }
            obj.put("regs", arr)
        } catch (e: Exception) { /* skip */ }
        return obj
    }

    private fun restoreGeneral(mem: GeneralMemory, obj: JSONObject) {
        try {
            val arr = obj.optJSONArray("regs")
            if (arr != null) {
                for (i in 0 until minOf(arr.length(), GeneralMemory.SIZE)) {
                    mem.set(i, Number.getInstance(arr.getString(i)))
                }
            }
        } catch (e: Exception) { /* skip */ }
    }

    // --- Configuration (apenas o que é exposto publicamente) ---
    private fun serializeConfig(calc: Calculator): JSONObject {
        val obj = JSONObject()
        try {
            val dsp = calc.display
            val flg = calc.flags
            obj.put("comma", dsp.comma)
            obj.put("precision", dsp.precision)
            obj.put("begin", flg.begin)
            obj.put("dmy", flg.dmy)
            // C flag persistido via FinanceMemory.isC; não há setter público
            // direto em Flags para o flag "c", então ignoramos aqui.
        } catch (e: Exception) { /* skip */ }
        return obj
    }

    private fun restoreConfig(calc: Calculator, obj: JSONObject) {
        try {
            val dsp = calc.display
            val flg = calc.flags
            dsp.setComma(obj.optBoolean("comma", false))
            dsp.setPrecision(obj.optInt("precision", 2))
            flg.setBegin(obj.optInt("begin", 0))
            flg.setDmy(obj.optInt("dmy", 0))
        } catch (e: Exception) { /* skip */ }
    }
}
