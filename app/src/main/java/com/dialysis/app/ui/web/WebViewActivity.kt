package com.dialysis.app.ui.web

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dialysis.app.R
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.ui.theme.AppTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image

class WebViewActivity : BaseActivity() {
    companion object {
        const val EXTRA_URL = "extra_web_url"
        const val EXTRA_TITLE = "extra_web_title"
    }

    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ContentView() {
        val context = LocalContext.current
        val url = (context as Activity).intent.getStringExtra(EXTRA_URL) ?: ""
        val title = (context as Activity).intent.getStringExtra(EXTRA_TITLE) ?: ""

        AppTheme {
            Scaffold(topBar = {
                TopAppBar(
                    title = { Text(text = if (title.isNotBlank()) title else stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Image(painter = painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.common_back))
                        }
                    }
                )
            }) { innerPadding ->
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}
