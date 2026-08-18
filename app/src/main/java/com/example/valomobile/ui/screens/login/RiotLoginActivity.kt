package com.example.valomobile.ui.screens.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import okhttp3.*
import java.io.IOException

class RiotLoginActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var isHandled = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private var authTriggeredWithCookies = false

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFF0F1923.toInt())
        }

        // Top Toolbar
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (56 * resources.displayMetrics.density).toInt()
            )
            setBackgroundColor(0xFF0F1923.toInt())
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (12 * resources.displayMetrics.density).toInt(), 0,
                (16 * resources.displayMetrics.density).toInt(), 0
            )
        }

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener { finish() }
        }

        val titleText = TextView(this).apply {
            text = "Official Riot Games Login"
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding((12 * resources.displayMetrics.density).toInt(), 0, 0, 0)
        }

        topBar.addView(backButton)
        topBar.addView(titleText)

        // Web Container
        val webContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        progressBar = ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                javaScriptCanOpenWindowsAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onAuthUrl(url: String?) {
                    if (!url.isNullOrBlank()) {
                        runOnUiThread {
                            checkAndHandleUrl(url)
                        }
                    }
                }
            }, "AndroidAuthBridge")

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    val currentUrl = view?.url ?: ""
                    checkAndHandleUrl(currentUrl)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: ""
                    Log.d("RiotLogin", "shouldOverrideUrlLoading: $url")
                    return checkAndHandleUrl(url)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("RiotLogin", "onPageStarted: $url, view.url: ${view?.url}")
                    checkAndHandleUrl(url)
                    checkAndHandleUrl(view?.url)
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    Log.d("RiotLogin", "doUpdateVisitedHistory: $url, view.url: ${view?.url}")
                    checkAndHandleUrl(url)
                    checkAndHandleUrl(view?.url)
                }

                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    val reqUrl = request?.url?.toString() ?: ""
                    Log.d("RiotLogin", "onReceivedHttpError: $reqUrl, view.url: ${view?.url}")
                    checkAndHandleUrl(reqUrl)
                    checkAndHandleUrl(view?.url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = android.view.View.GONE
                    Log.d("RiotLogin", "onPageFinished: $url, view.url: ${view?.url}")

                    checkAndHandleUrl(url)
                    checkAndHandleUrl(view?.url)

                    // Inject JavaScript watcher
                    val watcherJs = """
                        (function() {
                            function checkUrl() {
                                var href = window.location.href;
                                if (href && (href.indexOf('access_token=') !== -1 || href.indexOf('playvalorant.com/opt_in') !== -1)) {
                                    if (window.AndroidAuthBridge) {
                                        window.AndroidAuthBridge.onAuthUrl(href);
                                    }
                                }
                            }
                            checkUrl();
                            window.addEventListener('hashchange', checkUrl);
                            window.addEventListener('popstate', checkUrl);
                            setInterval(checkUrl, 50);
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(watcherJs, null)
                }
            }

            loadUrl(RIOT_AUTH_URL)
        }

        webContainer.addView(webView)
        webContainer.addView(progressBar)

        // Bottom Manual Confirmation Bar
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFF16222F.toInt())
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (10 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt()
            )
        }

        val confirmButton = Button(this).apply {
            text = "OPEN STORE"
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFFFF4655.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                val currentUrl = webView.url ?: ""
                if (checkAndHandleUrl(currentUrl)) return@setOnClickListener

                webView.evaluateJavascript("window.location.href") { jsUrl ->
                    val cleanJsUrl = jsUrl?.trim('"', '\'') ?: ""
                    if (checkAndHandleUrl(cleanJsUrl)) return@evaluateJavascript

                    // Check cookies & try background session exchange
                    fetchTokensWithCookies { success ->
                        if (!success) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@RiotLoginActivity,
                                    "Please enter your username and password above and tap the login arrow (->).",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        bottomBar.addView(confirmButton)

        rootLayout.addView(topBar)
        rootLayout.addView(webContainer)
        rootLayout.addView(bottomBar)

        setContentView(rootLayout)

        startUrlPolling()
    }

    private fun startUrlPolling() {
        pollingRunnable = object : Runnable {
            override fun run() {
                if (isHandled) return
                val currentUrl = webView.url ?: ""
                if (checkAndHandleUrl(currentUrl)) return

                webView.evaluateJavascript("window.location.href") { jsUrl ->
                    val cleanJsUrl = jsUrl?.trim('"', '\'') ?: ""
                    if (checkAndHandleUrl(cleanJsUrl)) return@evaluateJavascript

                    // If cookies are already set after user logged in
                    val cookies = CookieManager.getInstance().getCookie("https://auth.riotgames.com") ?: ""
                    if (!authTriggeredWithCookies && (cookies.contains("ssid=") || cookies.contains("sub="))) {
                        authTriggeredWithCookies = true
                        fetchTokensWithCookies { /* handled */ }
                    }
                }

                mainHandler.postDelayed(this, 100)
            }
        }
        mainHandler.postDelayed(pollingRunnable!!, 100)
    }

    private fun fetchTokensWithCookies(onComplete: (Boolean) -> Unit) {
        val cookies = CookieManager.getInstance().getCookie("https://auth.riotgames.com") ?: ""
        if (cookies.isBlank()) {
            onComplete(false)
            return
        }

        val client = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()

        val request = Request.Builder()
            .url(RIOT_AUTH_URL)
            .header("Cookie", cookies)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val location = response.header("Location") ?: ""
                if (location.isNotBlank() && checkAndHandleUrl(location)) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnable?.let { mainHandler.removeCallbacks(it) }
    }

    private fun checkAndHandleUrl(url: String?): Boolean {
        if (isHandled || url.isNullOrBlank()) return false

        val tokenRegex = Regex("access_token=([A-Za-z0-9-_=.]+)")
        val match = tokenRegex.find(url) ?: return false
        val accessToken = match.groupValues[1]

        val idTokenRegex = Regex("id_token=([A-Za-z0-9-_=.]+)")
        val idToken = idTokenRegex.find(url)?.groupValues?.get(1) ?: accessToken

        if (accessToken.isNotBlank()) {
            isHandled = true
            pollingRunnable?.let { mainHandler.removeCallbacks(it) }
            try {
                webView.stopLoading()
            } catch (e: Exception) {
                // ignore
            }
            val resultIntent = Intent().apply {
                putExtra(EXTRA_ACCESS_TOKEN, accessToken)
                putExtra(EXTRA_ID_TOKEN, idToken)
            }
            runOnUiThread {
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            return true
        }
        return false
    }

    companion object {
        const val EXTRA_ACCESS_TOKEN = "extra_access_token"
        const val EXTRA_ID_TOKEN = "extra_id_token"

        private const val RIOT_AUTH_URL =
            "https://auth.riotgames.com/authorize?client_id=play-valorant-web-prod&response_type=token%20id_token&redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&scope=account%20openid&nonce=1"
    }
}
