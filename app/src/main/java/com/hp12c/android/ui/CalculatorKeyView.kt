package com.hp12c.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.hp12c.android.R
import net.sf.finanx.fx12c.calc.Key

/**
 * Tecla individual da HP 12C, implementada como View customizada.
 *
 * Renderiza:
 *  - Fundo colorido (bege/laranja/azul/escuro conforme [keyColor])
 *  - Label principal no centro
 *  - Label laranja (função f) acima da tecla
 *  - Label azul (função g) abaixo da tecla
 *  - Animação de "press" via state-pressed
 *
 * A tecla é desenhada via Canvas — mais leve que múltiplos TextViews aninhados
 * e suficiente para o look fiel da HP 12C.
 *
 * Atributos XML suportados (ver res/values/attrs.xml):
 *  - key_code   : código da tecla (mapeia para Key enum do motor)
 *  - key_main    : texto principal
 *  - key_top     : texto laranja (função f) acima
 *  - key_bottom  : texto azul (função g) abaixo
 *  - key_color   : BEIGE | ORANGE | BLUE | DARK
 */
class CalculatorKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Código da tecla (Key enum do motor finanx-12c). */
    var key: Key? = null
        private set

    /** Texto principal da tecla (ex.: "ENTER", "1", "n", "+"). */
    var mainText: String = ""
        private set

    /** Texto laranja acima (função f). Ex.: "AMORT", "BOND". */
    var topText: String = ""
        private set

    /** Texto azul abaixo (função g). Ex.: "BEG", "END", "√". */
    var bottomText: String = ""
        private set

    /** Cor de fundo da tecla. */
    var keyColor: KeyColor = KeyColor.BEIGE
        private set

    /** Haptic feedback habilitado? */
    var hapticEnabled: Boolean = true

    /** Callback ao pressionar a tecla. */
    var onPress: ((Key) -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.argb(60, 0, 0, 0)
    }
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val topPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bottomPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val cornerRect = RectF()
    private var pressed = false

    init {
        isClickable = true
        isFocusable = false
        // Lê atributos custom do XML
        context.obtainStyledAttributes(attrs, R.styleable.CalculatorKeyView, defStyleAttr, 0).use { ta ->
            val code = ta.getInt(R.styleable.CalculatorKeyView_key_code, -1)
            if (code >= 0) {
                key = Key.valueOf(code)
            }
            mainText = ta.getString(R.styleable.CalculatorKeyView_key_main) ?: ""
            topText = ta.getString(R.styleable.CalculatorKeyView_key_top) ?: ""
            bottomText = ta.getString(R.styleable.CalculatorKeyView_key_bottom) ?: ""
            keyColor = when (ta.getInt(R.styleable.CalculatorKeyView_key_color, 0)) {
                1 -> KeyColor.ORANGE
                2 -> KeyColor.BLUE
                3 -> KeyColor.DARK
                else -> KeyColor.BEIGE
            }
        }
    }

    /** Configura a tecla em uma única chamada (conveniente para uso em código). */
    fun bind(
        key: Key,
        main: String,
        top: String = "",
        bottom: String = "",
        color: KeyColor = KeyColor.BEIGE,
        onPress: ((Key) -> Unit)? = null
    ) {
        this.key = key
        this.mainText = main
        this.topText = top
        this.bottomText = bottom
        this.keyColor = color
        this.onPress = onPress
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("HP12C/KeyView", "onTouchEvent: action=${event.action}, key=$key, main='$mainText'")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressed = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (pressed) {
                    pressed = false
                    invalidate()
                    if (hapticEnabled) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                    Log.d("HP12C/KeyView", "Disparando onPress para key=$key")
                    key?.let { onPress?.invoke(it) }
                    // Também chamar performClick para acessibilidade
                    performClick()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressed = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /**
     * CRÍTICO: sobrescreve onMeasure para que a View respeite o tamanho dado
     * pelo pai (LinearLayout com weight). Sem isto, a View usa o tamanho
     * interno do texto (que é grande) e empurra as outras views para fora
     * da tela.
     *
     * Quando o pai passa MeasureSpec.EXACTLY (que é o caso de 0dp + weight),
     * simplesmente usamos esse tamanho. Quando é AT_MOST, limitamos.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // Se o pai deu EXACTLY, usamos o tamanho do pai.
        // Se AT_MOST, limitamos ao tamanho dado.
        // Se UNSPECIFIED, default mínimo (40dp x 40dp para ser tocável).
        val density = resources.displayMetrics.density
        val minSize = (40f * density).toInt()

        val finalWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> minSize
        }
        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> heightSize
            else -> minSize
        }

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        val radius = 8f * density

        // Background
        val palette = KeyPalette.Default
        var bg = keyColor.background(palette)
        if (pressed) {
            bg = darken(bg, 0.85f)
        }
        bgPaint.color = bg
        cornerRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(cornerRect, radius, radius, bgPaint)
        canvas.drawRoundRect(cornerRect, radius, radius, borderPaint)

        // Tamanhos relativos de fonte
        val refSize = minOf(w, h)
        val mainSize = if (mainText.length > 4) refSize * 0.18f else refSize * 0.28f
        val labelSize = refSize * 0.13f

        // Texto principal (centro)
        mainPaint.textSize = mainSize
        mainPaint.textAlign = Paint.Align.CENTER
        mainPaint.color = keyColor.foreground(palette)
        mainPaint.isFakeBoldText = true
        val cx = w / 2f
        val cy = h / 2f
        val fm = mainPaint.fontMetrics
        val baseline = cy - (fm.ascent + fm.descent) / 2f
        canvas.drawText(mainText, cx, baseline, mainPaint)

        // Label laranja (função f) — acima
        if (topText.isNotEmpty()) {
            topPaint.textSize = labelSize
            topPaint.textAlign = Paint.Align.CENTER
            topPaint.color = palette.labelOrange
            topPaint.isFakeBoldText = true
            canvas.drawText(topText, cx, labelSize + 4f * density, topPaint)
        }

        // Label azul (função g) — abaixo
        if (bottomText.isNotEmpty()) {
            bottomPaint.textSize = labelSize
            bottomPaint.textAlign = Paint.Align.CENTER
            bottomPaint.color = palette.labelBlue
            bottomPaint.isFakeBoldText = true
            val bottomBaseline = h - 4f * density
            canvas.drawText(bottomText, cx, bottomBaseline, bottomPaint)
        }
    }

    /** Escurece uma cor ARGB por um fator [factor] (0..1). */
    private fun darken(color: Int, factor: Float): Int {
        val r = ((color shr 16) and 0xFF) * factor
        val g = ((color shr 8) and 0xFF) * factor
        val b = (color and 0xFF) * factor
        return (color and 0xFF000000.toInt()) or
            (r.toInt() shl 16) or
            (g.toInt() shl 8) or
            b.toInt()
    }
}
