package com.example.lensclickapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lensclickapp.ui.LensClickApp
import com.example.lensclickapp.ui.theme.LensClickAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LensClickAppTheme { LensClickApp() }
        }
    }
}
