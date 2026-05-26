package com.soundboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.soundboard.app.ui.screens.MainScreen
import com.soundboard.app.ui.theme.SoundboardTheme
import com.soundboard.app.ui.theme.SurfaceDark
import com.soundboard.app.viewmodel.SoundboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SoundboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceDark
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
