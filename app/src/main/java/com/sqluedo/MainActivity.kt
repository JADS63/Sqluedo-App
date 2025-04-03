package com.sqluedo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sqluedo.navigation.SQLuedoNavigation
import com.sqluedo.ui.theme.SQLuedoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SQLuedoTheme {
                SQLuedoNavigation()
            }
        }
    }
}

