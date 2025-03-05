package com.example.sqluedo.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sqluedo.data.Enquete
import com.example.sqluedo.data.Stub
import com.example.sqluedo.ui.connexion.ConnectionScreen
import com.example.sqluedo.ui.connexion.InscriptionScreen
import com.example.sqluedo.ui.home.HomeScreen
import com.example.sqluedo.ui.informations.InformationsScreen
import com.example.sqluedo.ui.jeu.JeuScreen
import com.example.sqluedo.ui.jeu.ResultatScreen
import kotlinx.serialization.Serializable


@Serializable
object Home

@Serializable
object Connexion

@Serializable
object Inscription

@Serializable
object Informations

@Serializable
object Jeu

@Serializable
object Resultat

@Serializable
object Enquete

@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()
    val enquetes = Stub.enquetes

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Home
    ) {
        composable<Home>{
            HomeScreen(enquetes = enquetes, goConnexion = {navController.navigate(Connexion)}, goEnquete = {navController.navigate(Enquete)})
        }
        composable<Connexion>{
            ConnectionScreen(goHome ={navController.navigate(Home)},goInscription={navController.navigate(Inscription)})
        }
        composable<Inscription> {
            InscriptionScreen(goConnection ={navController.navigate(Connexion)})
        }
        composable<Informations> {
            InformationsScreen(user = Null, stat = Null, goHome = {navController.navigate(Home)})
        }
        composable<Jeu> {
            JeuScreen(goHome = {navController.navigate(Home)}, goResultat = {navController.navigate(Resultat)}, enquete = Null)
        }
        composable<Resultat> {
            ResultatScreen(enquete = Null, goHome = {navController.navigate((Home))})
        }
        composable<Enquete> {
            ResultatScreen(enquete = Null, goHome = {navController.navigate((Home))})
        }

//        composable(
//            route = "enquete/{enqueteId}",
//            arguments = listOf(navArgument("enqueteId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val enqueteId = backStackEntry.arguments?.getString("enqueteId")
//            val enquete = enquetes.find { it.id == enqueteId }
//            if (enquete != null) {
//                EnqueteScreen(enquete = enquete, navController = navController)
//            } else {
//                LaunchedEffect(Unit) {
//                    navController.popBackStack()
//                }
//            }
//        }
//        composable(
//            route = "${Jeu.route}/{enqueteId}",
//            arguments = listOf(navArgument("enqueteId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val enqueteId = backStackEntry.arguments?.getString("enqueteId")
//            val enquete = enquetes.find { it.id == enqueteId }
//            if (enquete != null) {
//                JeuScreen(enquete = enquete, navController = navController)
//            } else {
//                LaunchedEffect(Unit) {
//                    navController.popBackStack()
//                }
//            }
//        }

    }
}
