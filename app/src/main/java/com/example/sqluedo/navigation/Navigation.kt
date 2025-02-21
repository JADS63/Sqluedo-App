package com.example.sqluedo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sqluedo.data.Stub
import com.example.sqluedo.ui.connexion.ConnectionScreen
import com.example.sqluedo.ui.connexion.InscriptionScreen
import com.example.sqluedo.ui.home.EnqueteScreen
import com.example.sqluedo.ui.home.HomeScreen
import com.example.sqluedo.ui.jeu.JeuScreen
import kotlinx.serialization.Serializable

interface NavigationDestination {
    // La route est automatiquement déduite du nom de l'objet en minuscule
    val route: String
        get() = this::class.simpleName?.lowercase() ?: ""
}

@Serializable
object Home : NavigationDestination

@Serializable
object Connexion : NavigationDestination

@Serializable
object Inscription : NavigationDestination

@Serializable
object Informations : NavigationDestination

@Serializable
object Jeu : NavigationDestination

@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()
    val enquetes = Stub.enquetes

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Home.route
    ) {
        composable(route = Home.route) {
            HomeScreen(enquetes = enquetes, navController = navController)
        }
        composable(route = Connexion.route) {
            ConnectionScreen(navController = navController)
        }
        composable(route = Inscription.route) {
            InscriptionScreen(navController = navController)
        }
        composable(
            route = "enquete/{enqueteId}",
            arguments = listOf(navArgument("enqueteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val enqueteId = backStackEntry.arguments?.getString("enqueteId")
            val enquete = enquetes.find { it.id == enqueteId }
            if (enquete != null) {
                EnqueteScreen(enquete = enquete, navController = navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable(
            route = "${Jeu.route}/{enqueteId}",
            arguments = listOf(navArgument("enqueteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val enqueteId = backStackEntry.arguments?.getString("enqueteId")
            val enquete = enquetes.find { it.id == enqueteId }
            if (enquete != null) {
                JeuScreen(enquete = enquete, navController = navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

    }
}
