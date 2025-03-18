package com.lideatech.CerciOnline

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.lideatech.CerciOnline.ui.theme.CerciOnlineTheme

class MainActivity : ComponentActivity() {
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge to edge uyumlu hale getirme
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CerciOnlineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewContent(modifier = Modifier.padding(innerPadding)) { webViewInstance ->
                        // WebView'ı burada alıyoruz
                        webView = webViewInstance
                    }
                }
            }
        }
    }

    // Geri tuşu ile WebView içinde geri gitmeyi kontrol et
    override fun onBackPressed() {
        if (webView != null && webView!!.canGoBack()) {
            webView!!.goBack()  // WebView içinde geri git
        } else {
            super.onBackPressed()  // WebView'de geri gitmeye yer yoksa, uygulamadan çık
        }
    }
}

@Composable
fun WebViewContent(modifier: Modifier = Modifier, onWebViewCreated: (WebView) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }

    // WebView'i AndroidView composable'ı ile ekliyoruz
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // WebView referansını alıyoruz
                onWebViewCreated(this)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        isLoading = true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading = false

                        // WebView üzerinde JavaScript çalıştırma: sınıfı kaldırma
                        view?.evaluateJavascript(
                            """
                                (function() {
                            let elements = document.querySelectorAll('.homeFooter');
                            elements.forEach(el => el.style.display ='none'); 
                            
                         
                            })
                            ();
                            """.trimIndent()
                        ) {}
                    }


                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        if (request?.url?.host == "www.cercionline.com") {
                            return false
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, request?.url)
                            context.startActivity(intent)
                            return true
                        }
                    }
                }
                loadUrl("https://www.cercionline.com/") // Başlangıç URL'si
            }
        },
        modifier = modifier.fillMaxSize()
    )

    // Yükleme göstergesi
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()  // Yükleme göstergesi
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CerciOnlineTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            WebViewContent(modifier = Modifier.padding(innerPadding)) { _ -> }
        }
    }
}
