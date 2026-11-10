package com.alain.epure.epub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.EpureTheme

// Jalon 0: an empty black screen. The reader and the home screen come later.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpureTheme {
                Box(
                    modifier = Modifier.fillMaxSize().background(Black)
                )
            }
        }
    }
}
