package com.sqluedo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sqluedo.ViewModel.UserConnexionViewModel
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Stub
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.service.createCodeFirstService
import com.sqluedo.ui.connexion.ConnectionScreen
import com.sqluedo.ui.connexion.InscriptionScreen
import com.sqluedo.ui.home.EnqueteScreen
import com.sqluedo.ui.home.HomeScreenWithViewModel
import com.sqluedo.ui.informations.InformationsScreen
import com.sqluedo.ui.jeu.JeuScreen
import com.sqluedo.ui.jeu.ResultatScreen

// Définition des routes de base sous forme de chaînes
private const val HOME_ROUTE = "Home"
private const val CONNEXION_ROUTE = "Connexion"
private const val INSCRIPTION_ROUTE = "Inscription"
private const val INFORMATIONS_ROUTE = "Informations"
private const val ENQUETE_DETAIL_ROUTE = "EnqueteDetail"
private const val JEU_ROUTE = "Jeu"
// La route de résultat contient deux paramètres
private const val RESULTAT_ROUTE = "Resultat/{attempts}/{timeTaken}"

@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()

    val statistiques = Stub.statistiques
    val utilisateur = Stub.utilisateur1

    // ViewModel de connexion partagé
    val connexionViewModel = remember { UserConnexionViewModel() }

    // États de connexion et de l'enquête sélectionnée
    val isLoggedIn = remember { mutableStateOf(false) }
    val selectedEnquete = remember { mutableStateOf<Enquete?>(null) }

    // Créer le repository une seule fois
    val repository = remember { EnqueteRepository(createCodeFirstService()) }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        composable(HOME_ROUTE) {
            HomeScreenWithViewModel(
                goConnexion = {
                    if (isLoggedIn.value) {
                        navController.navigate(INFORMATIONS_ROUTE)
                    } else {
                        navController.navigate(CONNEXION_ROUTE)
                    }
                },
                goEnquete = { enquete ->
                    selectedEnquete.value = enquete
                    navController.navigate(ENQUETE_DETAIL_ROUTE)
                }
            )
        }
        composable(CONNEXION_ROUTE) {
            ConnectionScreen(
                goHome = { navController.navigate(HOME_ROUTE) },
                goInscription = { navController.navigate(INSCRIPTION_ROUTE) },
                goInformations = {
                    isLoggedIn.value = true
                    navController.navigate(HOME_ROUTE)
                },
                viewModel = connexionViewModel
            )
        }
        composable(INSCRIPTION_ROUTE) {
            val inscriptionViewModel = remember { com.sqluedo.ViewModel.UserInscriptionViewModel() }
            InscriptionScreen(
                goConnection = { navController.navigate(CONNEXION_ROUTE) },
                viewModel = inscriptionViewModel
            )
        }
        composable(INFORMATIONS_ROUTE) {
            val connectedUser = connexionViewModel.currentUser.value ?: utilisateur
            InformationsScreen(
                user = connectedUser,
                stat = statistiques,
                goHome = { navController.navigate(HOME_ROUTE) },
                goLogout = {
                    connexionViewModel.logout()
                    isLoggedIn.value = false
                    navController.navigate(CONNEXION_ROUTE)
                }
            )
        }
        composable(ENQUETE_DETAIL_ROUTE) {
            val enquete = selectedEnquete.value ?: Stub.enquetes.first()
            EnqueteScreen(
                goHome = { navController.navigate(HOME_ROUTE) },
                goJeu = { navController.navigate(JEU_ROUTE) },
                goConnection = {
                    if (isLoggedIn.value) {
                        navController.navigate(INFORMATIONS_ROUTE)
                    } else {
                        navController.navigate(CONNEXION_ROUTE)
                    }
                },
                enquete = enquete
            )
        }
        composable(JEU_ROUTE) {
            val enquete = selectedEnquete.value ?: Stub.enquetes.first()
            JeuScreen(
                goHome = { navController.navigate(HOME_ROUTE) },
                goResultat = { a, t ->
                    // Transmettez les statistiques en naviguant vers la route avec paramètres
                    navController.navigate("Resultat/${a}/${t}")
                },
                enquete = enquete
            )
        }
        // Définition de la route pour le résultat avec arguments
        composable(
            route = RESULTAT_ROUTE,
            arguments = listOf(
                navArgument("attempts") { type = NavType.IntType },
                navArgument("timeTaken") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val attempts = backStackEntry.arguments?.getInt("attempts") ?: 1
            val timeTaken = backStackEntry.arguments?.getLong("timeTaken") ?: 0L
            ResultatScreen(
                enquete = selectedEnquete.value ?: Stub.enquetes.first(),
                goHome = { navController.navigate(HOME_ROUTE) },
                attempts = attempts,
                timeTaken = timeTaken
            )
        }
    }
}
