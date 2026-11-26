package com.hp12c.android.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hp12c.android.R

/**
 * Activity que abre o manual HTML interativo embutido em assets/manual/.
 *
 * O manual é do projeto hp12c-emulator-rust (MIT) e inclui:
 *  - Mapa interativo do teclado
 *  - Pilha RPN explicada passo a passo
 *  - Exemplos financeiros com resultado no display
 *  - Mensagens de erro
 */
class ManualActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_manual)
        val webView = findViewById<WebView>(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/manual/manual-usuario.html")
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webview)
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
