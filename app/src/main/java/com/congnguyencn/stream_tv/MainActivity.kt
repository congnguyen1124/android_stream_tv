package com.congnguyencn.stream_tv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.congnguyencn.stream_tv.app.StreamTvApp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            StreamTvTheme {
                StreamTvSurface {
                    StreamTvApp()
                }
            }
        }
    }
}
