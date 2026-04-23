package com.erivaldogelson.remedios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.erivaldogelson.remedios.core.appContainer
import com.erivaldogelson.remedios.ui.navigation.RemediosApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemediosApp(container = applicationContext.appContainer)
        }
    }
}

