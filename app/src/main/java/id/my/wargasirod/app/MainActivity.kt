package id.my.wargasirod.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import id.my.wargasirod.app.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // WebView URLs for each menu
    companion object {
        const val BASE_URL       = "https://wargasirod.my.id"
        const val URL_HOME       = "https://wargasirod.my.id"
        const val URL_BAYAR      = "https://wargasirod.my.id/pembayaran"
        const val URL_TAGIHAN    = "https://wargasirod.my.id/tagihan"
        const val URL_RIWAYAT    = "https://wargasirod.my.id/riwayat"
        const val URL_PROFIL     = "https://wargasirod.my.id/profil"
    }

    // File chooser support
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uri = data?.data ?: cameraImageUri
                filePathCallback?.onReceiveValue(uri?.let { arrayOf(it) } ?: arrayOf())
            } else {
                filePathCallback?.onReceiveValue(arrayOf())
            }
            filePathCallback = null
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupBottomNavigation()
        setupSwipeRefresh()

        // Load homepage on start
        loadUrl(URL_HOME)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                allowFileAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "WargaSirodApp/1.0 ${userAgentString}"
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutOffline.visibility = View.GONE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    injectMobileCSS()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        binding.progressBar.visibility = View.GONE
                        if (!isNetworkAvailable()) {
                            showOfflineScreen()
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    return when {
                        url.startsWith("tel:") -> {
                            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                            true
                        }
                        url.startsWith("mailto:") -> {
                            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                            true
                        }
                        url.startsWith("whatsapp:") || url.contains("wa.me") -> {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            true
                        }
                        url.startsWith(BASE_URL) -> false
                        else -> {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            true
                        }
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    this@MainActivity.filePathCallback?.onReceiveValue(arrayOf())
                    this@MainActivity.filePathCallback = filePathCallback

                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val photoFile = createImageFile()
                    cameraImageUri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.fileprovider",
                        photoFile
                    )
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

                    val chooserIntent = Intent.createChooser(
                        Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" },
                        "Pilih Gambar"
                    )
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePicture))
                    fileChooserLauncher.launch(chooserIntent)
                    return true
                }
            }

            // Enable cookies
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(this@apply, true)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadUrl(URL_HOME)
                    binding.tvPageTitle.text = "Beranda"
                    true
                }
                R.id.nav_bayar -> {
                    loadUrl(URL_BAYAR)
                    binding.tvPageTitle.text = "Pembayaran"
                    true
                }
                R.id.nav_tagihan -> {
                    loadUrl(URL_TAGIHAN)
                    binding.tvPageTitle.text = "Tagihan"
                    true
                }
                R.id.nav_riwayat -> {
                    loadUrl(URL_RIWAYAT)
                    binding.tvPageTitle.text = "Riwayat"
                    true
                }
                R.id.nav_profil -> {
                    loadUrl(URL_PROFIL)
                    binding.tvPageTitle.text = "Profil"
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.primary, R.color.primary_dark)
            setOnRefreshListener {
                if (isNetworkAvailable()) {
                    binding.webView.reload()
                } else {
                    isRefreshing = false
                    showOfflineScreen()
                }
            }
        }

        binding.btnRetry.setOnClickListener {
            if (isNetworkAvailable()) {
                binding.layoutOffline.visibility = View.GONE
                binding.webView.reload()
            } else {
                Toast.makeText(this, "Masih tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUrl(url: String) {
        if (isNetworkAvailable()) {
            binding.layoutOffline.visibility = View.GONE
            binding.webView.loadUrl(url)
        } else {
            showOfflineScreen()
        }
    }

    /**
     * Inject CSS to improve mobile responsiveness of the website
     */
    private fun injectMobileCSS() {
        val css = """
            (function() {
                var style = document.createElement('style');
                style.innerHTML = `
                    * { -webkit-tap-highlight-color: rgba(0,0,0,0.1) !important; }
                    body { padding-bottom: 70px !important; }
                    img { max-width: 100% !important; height: auto !important; }
                    table { max-width: 100% !important; overflow-x: auto !important; display: block !important; }
                    input, select, textarea { font-size: 16px !important; }
                    .navbar, nav.navbar { position: sticky !important; top: 0 !important; z-index: 999 !important; }
                `;
                document.head.appendChild(style);
                
                // Hide desktop-only navigation if bottom nav available
                var viewport = document.querySelector('meta[name="viewport"]');
                if (!viewport) {
                    var meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                    document.head.appendChild(meta);
                } else {
                    viewport.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                }
            })();
        """.trimIndent()
        binding.webView.evaluateJavascript(css, null)
    }

    private fun showOfflineScreen() {
        binding.layoutOffline.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
        CookieManager.getInstance().flush()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
