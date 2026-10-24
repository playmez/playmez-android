// Playmez game for Android (Kotlin).
//
//   val game = SpinlineGameView(context)
//   game.onEvent = { e -> Log.d("spinline", "${e.type} ${e.game} ${e.net}") }
//   game.onClose = { finish() }
//   setContentView(game)
//   game.open(launchUrl)   // from YOUR backend (POST /partner/launch); never ship the API key
//
// The library manifest declares android.permission.INTERNET for you.
//
// Voice at shared tables: the view grants microphone capture to the game origin and nothing else;
// declare RECORD_AUDIO in your manifest and ask the user for it. Only needed when Playmez has
// switched voice on for you; set allowMicrophone = false to refuse it.
package app.spinline.embed

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject

/** One message from the game: `ready`, `result`, `close`, `error` or `resize`. */
class SpinlineEvent(val type: String, val json: JSONObject) {
    val game: String? get() = if (json.has("game")) json.optString("game") else null
    val won: Boolean get() = json.optBoolean("won")
    val net: Int get() = json.optInt("net")
    val code: String? get() = if (json.has("code")) json.optString("code") else null
    val message: String? get() = if (json.has("message")) json.optString("message") else null
    val height: Int get() = json.optInt("height")

    companion object {
        /** Parses one bridge message; null for anything that is not a Playmez event. */
        @JvmStatic
        fun parse(raw: String): SpinlineEvent? {
            val obj = try {
                JSONObject(raw)
            } catch (_: Exception) {
                return null
            }
            if (obj.optString("source") != "spinline") return null
            val type = obj.optString("type")
            if (type.isEmpty()) return null
            return SpinlineEvent(type, obj)
        }
    }
}

class SpinlineGameView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    var onEvent: ((SpinlineEvent) -> Unit)? = null
    /** The player tapped Leave or Close: finish the activity or hide the view. */
    var onClose: (() -> Unit)? = null
    /** Let the game use the microphone for voice at shared tables. */
    var allowMicrophone: Boolean = true

    private var allowedHost: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUp() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        setBackgroundColor(Color.parseColor("#0B0F1A"))
        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                val wantsMic = request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                val fromGame = request.origin?.host == allowedHost
                if (wantsMic && fromGame && allowMicrophone) request.grant(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) else request.deny()
            }
        }
        // Keep the view on Playmez: the bridge below is reachable by any page this view loads.
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                request.url.host != allowedHost
        }
        addJavascriptInterface(Bridge(), "SpinlineBridge")
    }

    init {
        setUp()
    }

    /** Loads a launch URL. Fetch a fresh one every time: it works once, within five minutes. */
    fun open(launchUrl: String) {
        allowedHost = Uri.parse(launchUrl).host
        loadUrl(launchUrl)
    }

    private inner class Bridge {
        @JavascriptInterface
        fun postMessage(json: String) {
            val event = SpinlineEvent.parse(json) ?: return
            post {
                onEvent?.invoke(event)
                if (event.type == "close") onClose?.invoke()
            }
        }
    }

    companion object {
        /** The library version, for your logs. */
        const val VERSION: String = BuildConfig.SDK_VERSION
    }
}
