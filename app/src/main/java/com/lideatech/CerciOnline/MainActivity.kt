package com.lideatech.CerciOnline

import android.content.Intent // Intent importu
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background // background fonksiyonunun import edilmesi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
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
                    // WebViewContent composable'ına webView callback fonksiyonu ile geçiyoruz
                    WebViewContent(modifier = Modifier.padding(innerPadding)) { webViewInstance ->
                        // webView instance'ını alıyoruz
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
    var isLoading by remember { mutableStateOf(true) } // Yükleme durumunu tutan değişken

    // WebView'i AndroidView composable'ı ile ekliyoruz
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // WebView'i bir referansa kaydediyoruz
                onWebViewCreated(this)

                settings.javaScriptEnabled = true  // JavaScript desteğini etkinleştiriyoruz
                settings.domStorageEnabled = true // DOM storage desteğini etkinleştiriyoruz
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        isLoading = true  // Sayfa yüklenmeye başlarsa, loading göstergesini aktif et
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading = false  // Sayfa yüklendikten sonra loading göstergesini kapat
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        // WebView içinde açılacak linki kontrol et
                        if (request?.url?.host == "www.cercionline.com") {
                            return false // WebView içinde açmaya devam et
                        } else {
                            // Diğer tüm bağlantıları dış tarayıcıda açmak için Intent kullan
                            val intent = Intent(Intent.ACTION_VIEW, request?.url)
                            context.startActivity(intent) // Dış tarayıcıda aç
                            return true // WebView içinde açma
                        }
                    }
                }
                loadUrl("https://www.cercionline.com/")  // Başlangıç URL'si
            }
        },
        modifier = modifier.fillMaxSize()
    )

    // Yükleme göstergesi
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()  // Ekranı tamamen kaplar
                .background(MaterialTheme.colorScheme.background),  // Arka plan rengini ayarlıyoruz
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
