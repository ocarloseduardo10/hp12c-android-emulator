package com.hp12c.android.ui

import net.sf.finanx.fx12c.calc.Key

/**
 * Ajuda contextual — mostra o que cada tecla faz quando f ou g está ativo.
 *
 * Quando o usuário pressiona f ou g, a próxima tecla terá uma função
 * alternativa. Este objeto fornece dicas rápidas para exibir na UI.
 */
object ContextualHelp {

    /** Retorna uma dica rápida para a tecla, considerando o estado de f/g. */
    fun getHint(key: Key, fActive: Boolean, gActive: Boolean): String? {
        if (fActive) return getFunctionHint(key, isF = true)
        if (gActive) return getFunctionHint(key, isF = false)
        return null
    }

    private fun getFunctionHint(key: Key, isF: Boolean): String? {
        val map = if (isF) fHints else gHints
        return map[key]
    }

    /** Dicas para funções f (laranja). */
    private val fHints: Map<Key, String> = mapOf(
        Key.KEY_11 to "AMORT: amortização de n períodos",
        Key.KEY_12 to "INT: juros simples do período",
        Key.KEY_13 to "NPV: valor presente líquido",
        Key.KEY_14 to "RND: arredondar conforme precisão",
        Key.KEY_15 to "IRR: taxa interna de retorno",
        Key.KEY_21 to "PRICE: preço de título (Y=venc, X=liquid, i=rend, PMT=cupom)",
        Key.KEY_22 to "YTM: rendimento até vencimento (Y=venc, X=liquid, PV=preço, PMT=cupom)",
        Key.KEY_23 to "SL: depreciação linear",
        Key.KEY_24 to "SOYD: depreciação soma dos dígitos",
        Key.KEY_25 to "DB: depreciação declínio balanceado",
        Key.KEY_31 to "P/R: alternar modo programa/execução",
        Key.KEY_32 to "Σ: limpar registradores estatísticos",
        Key.KEY_33 to "PRGM: limpar memória de programa",
        Key.KEY_34 to "FIN: limpar registradores financeiros",
        Key.KEY_35 to "REG: limpar todos os registradores",
        Key.KEY_36 to "PREFIX: mostrar mantissa (manter tecla)",
        Key.KEY_48 to "s: desvio padrão amostral",
        Key.KEY_49 to "Σ-: remover último ponto estatístico"
    )

    /** Dicas para funções g (azul). */
    private val gHints: Map<Key, String> = mapOf(
        Key.KEY_00 to "x̄: média aritmética de X",
        Key.KEY_01 to "x̄,r: estimativa X via regressão",
        Key.KEY_02 to "ȳ,r: estimativa Y via regressão",
        Key.KEY_03 to "n!: fatorial de X",
        Key.KEY_04 to "D.MY: formato dia.mês.ano",
        Key.KEY_05 to "M.DY: formato mês.dia.ano",
        Key.KEY_06 to "x̄w: média ponderada de X",
        Key.KEY_07 to "BEG: pagamentos no início (annuity due)",
        Key.KEY_08 to "END: pagamentos no fim (ordinary annuity)",
        Key.KEY_09 to "MEM: mostrar memória disponível",
        Key.KEY_11 to "12×: multiplicar n por 12",
        Key.KEY_12 to "12÷: dividir i por 12",
        Key.KEY_13 to "CFo: fluxo de caixa inicial",
        Key.KEY_14 to "CFj: fluxo de caixa periódico j",
        Key.KEY_15 to "Nj: número de repetições do CFj",
        Key.KEY_16 to "DATE: data futura + dia da semana",
        Key.KEY_21 to "√x: raiz quadrada de X",
        Key.KEY_22 to "eˣ: exponencial de X",
        Key.KEY_23 to "LN: logaritmo natural de X",
        Key.KEY_24 to "FRAC: parte fracionária de X",
        Key.KEY_25 to "INTG: parte inteira de X",
        Key.KEY_26 to "ΔDYS: dias entre datas (Y, X)",
        Key.KEY_30 to "←: apagar último dígito (backspace)",
        Key.KEY_31 to "PSE: pausar execução de programa",
        Key.KEY_32 to "BST: voltar uma linha de programa",
        Key.KEY_33 to "GTO: ir para linha de programa",
        Key.KEY_34 to "x≤y: teste se X ≤ Y",
        Key.KEY_35 to "x=0: teste se X = 0",
        Key.KEY_36 to "LSTx: recuperar último X"
    )
}
