package com.example.sqluedo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sqluedo.data.Enquete
import com.example.sqluedo.data.Stub
import com.example.sqluedo.ui.connexion.ConnectionScreen
import com.example.sqluedo.ui.connexion.InscriptionScreen
import com.example.sqluedo.ui.home.EnqueteScreen
import com.example.sqluedo.ui.home.HomeScreen
import com.example.sqluedo.ui.jeu.JeuScreen

object Home;
object Connexion;
object Inscription;
object Informations;
object Jeu;
@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()
    val enquetes = Stub.enquetes


    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "home"
    ) {
        composable(route = "home") {
            HomeScreen(
                enquetes = enquetes,
                navController = navController
            )
        }
        composable(route = "connexion") {
            ConnectionScreen(
                navController = navController

            )
        }
        composable(route = "inscription") {
            InscriptionScreen(
                navController = navController

            )
        }
        composable(
            route = "enquete/{enqueteId}",
            arguments = listOf(navArgument("enqueteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val enqueteId = backStackEntry.arguments?.getString("enqueteId")
            val enquete = enquetes.find { it.id == enqueteId }
            if (enquete != null) {
                EnqueteScreen(
                    enquete = enquete,
                    navController = navController
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable(
            route = "jeu/{enqueteId}",
            arguments = listOf(navArgument("enqueteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val enqueteId = backStackEntry.arguments?.getString("enqueteId")
            val enquete = enquetes.find { it.id == enqueteId }
            if (enquete != null) {
                JeuScreen(
                    enquete = enquete,
                    navController = navController
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

    }
}
