package com.hp12c.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * Display LCD da HP 12C: fundo verde-azulado, dígitos pretos em fonte
 * monoespaçada, com faixa de indicadores (BEGIN/D.MY/C/PRGM/f/g) no topo.
 *
 * Reproduz fielmente o estilo da calculadora física: 10 caracteres visíveis
 * no display, com grupos separados por vírgulas e casas decimais configuráveis.
 *
 * Características:
 *  - Indicadores INATIVOS aparecem como cinza bem claro (quase invisível),
 *    iguais aos segmentos desligados do LCD físico.
 *  - Indicadores ATIVOS aparecem em preto forte (BEGIN/D.MY/C/PRGM) ou em
 *    laranja/azul (f/g), como na HP 12C real.
 *  - Tamanho do texto principal é AUTO-AJUSTADO para caber na largura sem
 *    transbordar — importante porque precisão configurável pode gerar
 *    strings longas (ex.: "1,234,567.890123456").
 */
class LcdDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var displayText: String = "0.00"
    private var indicators: Set<String> = emptySet()
    private var financeSummary: String = ""

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B8C5C0")
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#4A4A4A")
    }
    private val digitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B1410")
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
        textAlign = Paint.Align.RIGHT
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }
    private val summaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A4A45")
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.LEFT
    }
    private val cornerRect = RectF()

    /** Atualiza o conteúdo do display. */
    fun setState(display: String, activeIndicators: Set<String>, summary: String) {
        this.displayText = display.ifBlank { "0.00" }
        this.indicators = activeIndicators
        this.financeSummary = summary
        invalidate()
    }

    /**
     * Altura fixa de 100dp para evitar que o display cresça e empurre o
     * teclado para fora da tela. Largura é match_parent.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val density = resources.displayMetrics.density
        val desiredHeight = (100f * density).toInt()

        val finalWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> widthSize
        }

        // Para altura: se o pai deu EXACTLY, respeitar; senão usar 100dp.
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        val radius = 6f * density

        // Fundo LCD
        cornerRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(cornerRect, radius, radius, bgPaint)
        canvas.drawRoundRect(cornerRect, radius, radius, borderPaint)

        val padX = 14f * density
        val padY = 10f * density

        // === Faixa de indicadores (linha do topo) ===
        val indicatorSize = 10f * density
        indicatorPaint.textSize = indicatorSize
        var x = padX
        val yInd = padY + indicatorSize

        // INATIVO: cor de fundo do LCD (B8C5C0 clareado) — quase invisível,
        // exatamente como segmentos desligados do LCD físico.
        // ATIVO: preto forte ou cor específica (laranja/azul para f/g).
        val inactiveColor = Color.parseColor("#A0A8A4")  // cinza bem claro, quase fundo
        val activeDarkColor = Color.parseColor("#0B1410")  // preto LCD
        val activeOrangeColor = Color.parseColor("#D85A14")  // laranja vibrante
        val activeBlueColor = Color.parseColor("#1F5BB8")  // azul vibrante

        // Renderiza BEGIN, D.MY, C, PRGM na esquerda
        val leftIndicators = listOf("BEGIN", "D.MY", "C", "PRGM")
        for (label in leftIndicators) {
            val active = label in indicators
            indicatorPaint.color = if (active) activeDarkColor else inactiveColor
            canvas.drawText(label, x, yInd, indicatorPaint)
            x += indicatorPaint.measureText(label) + 12f * density
        }

        // f e g na direita (cores especiais quando ativos)
        indicatorPaint.textAlign = Paint.Align.RIGHT
        var xR = w - padX
        for (label in listOf("g", "f")) {
            val active = label in indicators
            indicatorPaint.color = when {
                !active -> inactiveColor
                label == "f" -> activeOrangeColor
                else -> activeBlueColor
            }
            canvas.drawText(label, xR, yInd, indicatorPaint)
            xR -= indicatorPaint.measureText(label) + 12f * density
        }
        indicatorPaint.textAlign = Paint.Align.LEFT

        // === Display principal (texto grande, alinhado à direita, auto-fit) ===
        // Auto-ajustar tamanho do texto para caber em (w - 2*padX) de largura.
        // Limitar entre 14sp (mínimo legível) e 56dp (máximo estético).
        val maxTextWidth = w - 2 * padX
        val minSizeSp = 14f
        val maxSizeSp = 56f
        var textSizeSp = maxSizeSp
        digitPaint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, textSizeSp, resources.displayMetrics
        )
        // Medir largura do texto atual; se exceder, reduzir até caber
        var textWidth = digitPaint.measureText(displayText)
        while (textWidth > maxTextWidth && textSizeSp > minSizeSp) {
            textSizeSp -= 2f
            digitPaint.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, textSizeSp, resources.displayMetrics
            )
            textWidth = digitPaint.measureText(displayText)
        }

        // Calcular baseline para centralizar verticalmente abaixo dos indicadores
        val displayAreaTop = padY + indicatorSize + 8f * density
        val displayAreaBottom = h - padY - (if (financeSummary.isNotEmpty()) 16f * density else 0f)
        val displayAreaHeight = displayAreaBottom - displayAreaTop
        val fm = digitPaint.fontMetrics
        val baseline = displayAreaTop + (displayAreaHeight - (fm.descent - fm.ascent)) / 2f - fm.ascent
        canvas.drawText(displayText, w - padX, baseline, digitPaint)

        // === Resumo financeiro discreto (linha de baixo) ===
        if (financeSummary.isNotEmpty()) {
            val summarySize = 9f * density
            summaryPaint.textSize = summarySize
            val summaryBaseline = h - padY * 0.5f
            // Trunca se necessário
            val maxSummaryWidth = w - 2 * padX
            var summary = financeSummary
            while (summaryPaint.measureText(summary) > maxSummaryWidth && summary.length > 4) {
                summary = summary.dropLast(2)
            }
            canvas.drawText(summary, padX, summaryBaseline, summaryPaint)
        }
    }
}
