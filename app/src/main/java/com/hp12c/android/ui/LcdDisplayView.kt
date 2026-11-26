package com.hp12c.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * Display LCD da HP 12C com dígitos de 7 segmentos reais.
 *
 * Reproduz fielmente o display da HP 12C física:
 *  - 10 posições de 7 segmentos
 *  - Sinal de menos à esquerda
 *  - Pontos e vírgulas decimais entre os dígitos
 *  - Indicadores: low_battery (*), f, g, BEGIN, D.MY, C, PRGM
 *  - Segmentos "ghost" (desligados mas visíveis) — como no LCD real
 *
 * Cores extraídas do skin.json do projeto hp12c-emulator-rust (MIT).
 */
class LcdDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var displayText: String = "0.00"
    private var indicators: Set<String> = emptySet()
    private var financeSummary: String = ""
    private var running: Boolean = false

    // Paints
    private val lcdBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HpColors.lcd()
    }
    private val lcdShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HpColors.lcdShadow()
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val segmentOnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HpColors.segment()
        style = Paint.Style.FILL
    }
    private val segmentOffPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HpColors.segmentGhost()
        style = Paint.Style.FILL
        alpha = 70  // ghost segments muito sutis
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
    private val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HpColors.bezel()
        style = Paint.Style.FILL
    }
    private val cornerRect = RectF()

    /** Atualiza o conteúdo do display. */
    fun setState(display: String, activeIndicators: Set<String>, summary: String) {
        this.displayText = display.ifBlank { "0.00" }
        this.indicators = activeIndicators
        this.financeSummary = summary
        invalidate()
    }

    /** Marca o display como "running" (cálculo em andamento, pisca). */
    fun setRunning(running: Boolean) {
        this.running = running
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val density = resources.displayMetrics.density
        val desiredHeight = (110f * density).toInt()  // um pouco maior para o display de 7 seg

        val finalWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> widthSize
        }
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

        // 1) Bezel (moldura escura ao redor do LCD)
        val bezelPad = 4f * density
        cornerRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(cornerRect, 6f * density, 6f * density, bezelPaint)

        // 2) Fundo do LCD (verde-oliva)
        val lcdRect = RectF(
            bezelPad, bezelPad,
            w - bezelPad, h - bezelPad
        )
        canvas.drawRoundRect(lcdRect, 4f * density, 4f * density, lcdBgPaint)

        // 3) Faixa de indicadores (linha do topo)
        val indicatorSize = 9f * density
        indicatorPaint.textSize = indicatorSize
        var x = bezelPad + 6f * density
        val yInd = bezelPad + 6f * density + indicatorSize

        // Cores para indicadores
        val inactiveColor = HpColors.segmentGhost()
        val activeDarkColor = HpColors.segment()
        val activeOrangeColor = HpColors.fLabel()
        val activeBlueColor = HpColors.gLabel()

        // Indicadores na esquerda: BEGIN, D.MY, C, PRGM
        for (label in listOf("BEGIN", "D.MY", "C", "PRGM")) {
            val active = label in indicators
            indicatorPaint.color = if (active) activeDarkColor else inactiveColor
            indicatorPaint.alpha = if (active) 255 else 90
            canvas.drawText(label, x, yInd, indicatorPaint)
            x += indicatorPaint.measureText(label) + 10f * density
        }
        // Indicadores na direita: f, g
        indicatorPaint.textAlign = Paint.Align.RIGHT
        var xR = w - bezelPad - 6f * density
        for (label in listOf("g", "f")) {
            val active = label in indicators
            indicatorPaint.color = when {
                !active -> inactiveColor
                label == "f" -> activeOrangeColor
                else -> activeBlueColor
            }
            indicatorPaint.alpha = if (active) 255 else 90
            canvas.drawText(label, xR, yInd, indicatorPaint)
            xR -= indicatorPaint.measureText(label) + 10f * density
        }
        indicatorPaint.textAlign = Paint.Align.LEFT

        // 4) Dígitos de 7 segmentos (10 posições)
        // O display da HP 12C tem 10 posições, alinhadas à direita
        val digitAreaTop = yInd + 4f * density
        val summaryHeight = if (financeSummary.isNotEmpty()) 14f * density else 0f
        val digitAreaBottom = h - bezelPad - summaryHeight - 4f * density
        val digitAreaHeight = digitAreaBottom - digitAreaTop
        val digitAreaWidth = w - 2 * (bezelPad + 6f * density)

        // Preparar texto para o display (10 caracteres, alinhado à direita)
        // Em notação científica, mantissa ocupa posições 0-6, sinal na 7, expoente nas 8-9
        val paddedDisplay = displayText.padStart(10, ' ').take(10)

        // Dimensões de cada dígito
        val digitPitch = digitAreaWidth / 10f
        val digitWidth = digitPitch * 0.72f
        val digitHeight = digitAreaHeight * 0.85f
        val digitThickness = digitWidth * 0.16f
        val slant = 0.09f  // leve inclinação à direita, como no LCD real

        // Desenhar cada caractere
        for (i in 0 until 10) {
            val ch = paddedDisplay[i]
            val dx = bezelPad + 6f * density + (i + 0.14f) * digitPitch
            val dy = digitAreaTop + (digitAreaHeight - digitHeight) / 2f

            // Tratar ponto e vírgula como separadores decimais
            if (ch == '.') {
                // Ponto decimal: pequeno círculo na base do dígito anterior
                val dotX = dx - digitPitch * 0.15f
                val dotY = dy + digitHeight * 0.88f
                val dotRadius = digitThickness * 0.55f
                canvas.drawCircle(dotX, dotY, dotRadius, segmentOnPaint)
                continue
            }
            if (ch == ',') {
                // Vírgula decimal: arco curvo elegante (estilo LCD de 7 segmentos)
                // Desenha um pequeno segmento diagonal curvo na base
                val dotX = dx - digitPitch * 0.15f
                val dotY = dy + digitHeight * 0.82f
                val r = digitThickness * 0.5f
                // Desenhar um pequeno ponto
                canvas.drawCircle(dotX, dotY, r * 0.8f, segmentOnPaint)
                // Desenhar a cauda como um pequeno retângulo diagonal (segmento do LCD)
                val tailWidth = r * 0.7f
                val tailHeight = r * 2.2f
                canvas.save()
                canvas.rotate(30f, dotX, dotY + r * 0.3f) // inclinação de 30 graus
                val tailRect = RectF(
                    dotX - tailWidth / 2f,
                    dotY + r * 0.3f,
                    dotX + tailWidth / 2f,
                    dotY + r * 0.3f + tailHeight
                )
                canvas.drawRoundRect(tailRect, tailWidth / 2f, tailWidth / 2f, segmentOnPaint)
                canvas.restore()
                continue
            }
            if (ch == ' ') continue

            // Desenhar o dígito de 7 segmentos
            drawDigit(canvas, ch, dx, dy, digitWidth, digitHeight, digitThickness, slant)

            // Se próximo caractere é ponto/vírgula, já tratado acima
        }

        // Sinal de menos à esquerda (se o número for negativo)
        if (displayText.startsWith("-")) {
            val minusX = bezelPad + 4f * density
            val minusY = digitAreaTop + (digitAreaHeight - digitHeight) / 2f
            val minusW = digitPitch * 0.5f
            val minusH = digitHeight
            val minusThickness = digitThickness
            val minusPaths = SevenSegment.segmentPaths(minusW, minusH, minusThickness, slant)
            minusPaths[6]?.let { canvas.drawPath(it, segmentOnPaint) }  // segmento g (meio)
        }

        // 5) Indicador "running" piscante (cálculo em andamento)
        if (running) {
            val runPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = HpColors.segment()
                textSize = 8f * density
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("running", bezelPad + 6f * density, h - bezelPad - 4f * density, runPaint)
        }

        // 6) Resumo financeiro (linha de baixo)
        if (financeSummary.isNotEmpty()) {
            val summarySize = 8f * density
            summaryPaint.textSize = summarySize
            val summaryBaseline = h - bezelPad - 4f * density
            val maxSummaryWidth = w - 2 * (bezelPad + 6f * density)
            var summary = financeSummary
            while (summaryPaint.measureText(summary) > maxSummaryWidth && summary.length > 4) {
                summary = summary.dropLast(2)
            }
            canvas.drawText(summary, bezelPad + 6f * density, summaryBaseline, summaryPaint)
        }
    }

    /** Desenha um único caractere como dígito de 7 segmentos. */
    private fun drawDigit(
        canvas: Canvas,
        ch: Char,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        thickness: Float,
        slant: Float
    ) {
        val paths = SevenSegment.segmentPaths(width, height, thickness, slant)
        val mask = SevenSegment.segments(ch).toInt() and 0xFF
        for (i in 0 until 7) {
            val path = paths[i] ?: continue
            canvas.save()
            canvas.translate(x, y)
            val bit = 1 shl i
            val isOn = (mask and bit) != 0
            val paint = if (isOn) segmentOnPaint else segmentOffPaint
            canvas.drawPath(path, paint)
            canvas.restore()
        }
    }
}
