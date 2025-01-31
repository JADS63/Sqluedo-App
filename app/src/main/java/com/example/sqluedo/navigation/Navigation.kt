package com.example.sqluedo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sqluedo.data.Stub
import com.example.sqluedo.ui.connexion.ConnectionScreen
import com.example.sqluedo.ui.home.HomeScreen

@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()
    val enquetes = Stub.enquetes


    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "connexion"
    ) {
        composable(route = "home") {
            HomeScreen(
                enquetes = enquetes,

            )
        }
        composable(route = "connexion") {
            ConnectionScreen()
        }
    }
}
