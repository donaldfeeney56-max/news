package com.newsapp.app.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.TypedValue
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdvancedWebViewScreen(
    initialUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("webview_cache", Context.MODE_PRIVATE) }
    val cachedUrl = remember { prefs.getString("cached_final_url", "") ?: "" }
    var currentUrl by remember { mutableStateOf(if (cachedUrl.isNotBlank()) cachedUrl else initialUrl) }
    var stableCounter by remember { mutableStateOf(0) }
    val errorCounter = remember { AtomicInteger(0) }
    val lastErrorTime = remember { mutableStateOf(0L) }

    var webView by remember { mutableStateOf<WebView?>(null) }

    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickMultipleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        filePathCallback?.onReceiveValue(uris?.toTypedArray() ?: emptyArray())
        filePathCallback = null
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val result = if (success && cameraImageUri != null) arrayOf(cameraImageUri!!) else emptyArray()
        filePathCallback?.onReceiveValue(result)
        filePathCallback = null
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        filePathCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else emptyArray())
        filePathCallback = null
    }

    val requestWritePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op, download will be re-attempted by user */ }

    fun startDownload(ctx: Context, url: String, contentDisposition: String?, mimeType: String?, userAgent: String?) {
        try {
            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
            val cookies = try { CookieManager.getInstance().getCookie(url) } catch (_: Throwable) { null }
            if (!cookies.isNullOrBlank()) request.addRequestHeader("Cookie", cookies)
            if (!userAgent.isNullOrBlank()) request.addRequestHeader("User-Agent", userAgent)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)
            val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            @Suppress("DEPRECATION")
            request.allowScanningByMediaScanner()
            dm.enqueue(request)
        } catch (_: Throwable) {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: Throwable) {}
        }
    }

    fun dpToPx(ctx: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), ctx.resources.displayMetrics
        ).toInt()
    }

    AndroidView(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .fillMaxSize(),
        factory = { ctx ->
            val container = FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val wv = WebView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    settings.safeBrowsingEnabled = true
                CookieManager.getInstance().setAcceptCookie(true)
                try { CookieManager.getInstance().setAcceptThirdPartyCookies(this, true) } catch (_: Throwable) {}

                setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                    if (Build.VERSION.SDK_INT < 29) {
                        requestWritePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    startDownload(ctx, url, contentDisposition, mimeType, userAgent)
                })

                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback_: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        filePathCallback = filePathCallback_
                        val accept = fileChooserParams?.acceptTypes?.joinToString(",") ?: "*/*"
                        val allowMultiple = fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE
                        if (accept.contains("image") && fileChooserParams?.isCaptureEnabled == true) {
                            val imageUri = androidx.core.content.FileProvider.getUriForFile(
                                ctx,
                                "${ctx.packageName}.fileprovider",
                                java.io.File(ctx.getExternalFilesDir(null), "camera_${System.currentTimeMillis()}.jpg")
                            )
                            cameraImageUri = imageUri
                            takePhotoLauncher.launch(imageUri)
                        } else {
                            val types = if (accept.isBlank()) arrayOf("*/*") else accept.split(",").toTypedArray()
                            if (allowMultiple) {
                                pickMultipleLauncher.launch(types)
                            } else {
                                openDocumentLauncher.launch(types)
                            }
                        }
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        val cleaned = url
                            .replace("&&", "&")
                            .replace("?&", "?")
                            .replace("??", "?")
                        currentUrl = cleaned
                        stableCounter += 1
                        if (stableCounter >= 3 && cleaned.startsWith("http")) {
                            prefs.edit().putString("cached_final_url", cleaned).apply()
                        }
                        CookieManager.getInstance().flush()
                        super.onPageFinished(view, url)
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        if (request?.isForMainFrame == true) {
                            bumpError()
                        }
                        super.onReceivedHttpError(view, request, errorResponse)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            bumpError()
                        }
                        super.onReceivedError(view, request, error)
                    }

                    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                        try {
                            (view.parent as? ViewGroup)?.removeView(view)
                            view.destroy()
                        } catch (_: Throwable) {}
                        webView = null
                        return true
                    }

                    fun bumpError() {
                        val now = System.currentTimeMillis()
                        val last = lastErrorTime.value
                        if (now - last <= 5000) {
                            if (errorCounter.incrementAndGet() >= 2) {
                                prefs.edit().remove("cached_final_url").apply()
                            }
                        } else {
                            errorCounter.set(1)
                        }
                        lastErrorTime.value = now
                    }
                }

                loadUrl(currentUrl)
                webView = this
            }

            container.addView(wv)

            container
        },
        update = { container ->
            val wv = container.getChildAt(0) as? WebView
            if (wv != null && wv.url != currentUrl) {
                wv.loadUrl(currentUrl)
            }
        },
        onRelease = { container ->
            val wv = container.getChildAt(0) as? WebView
            try { wv?.stopLoading() } catch (_: Throwable) {}
            try { wv?.destroy() } catch (_: Throwable) {}
        }
    )

    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            (context as? Activity)?.moveTaskToBack(true)
        }
    }
}
