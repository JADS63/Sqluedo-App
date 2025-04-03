package com.sqluedo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sqluedo.ViewModel.ConnexionState
import com.sqluedo.ViewModel.EnqueteResultViewModel
import com.sqluedo.ViewModel.UserConnexionViewModel
import com.sqluedo.ViewModel.UserInscriptionViewModel
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
object EnqueteDetail

private fun getRoute(destination: Any): String {
    return destination::class.java.simpleName
}

// Extensions pour simplifier l'utilisation de la navigation
fun NavGraphBuilder.composable(
    route: Any,
    content: @Composable () -> Unit
) {
    composable(getRoute(route)) { content() }
}

fun NavHostController.navigateTo(destination: Any) {
    val route = getRoute(destination)
    this.navigate(route) {
        popUpTo(this@navigateTo.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()

    val statistiques = Stub.statistiques
    val utilisateur = Stub.utilisateur1

    // Création du ViewModel partagé avec état de connexion
    val connexionViewModel = remember { UserConnexionViewModel() }

    // État de connexion pour suivre si l'utilisateur est connecté
    val isLoggedIn = remember { mutableStateOf(false) }

    // État pour stocker l'enquête sélectionnée
    val selectedEnquete = remember { mutableStateOf<Enquete?>(null) }

    val attempts = remember { mutableStateOf(0) }
    val elapsedTime = remember { mutableStateOf(0L) }

    // Créer le repository une seule fois
    val repository = remember { EnqueteRepository(createCodeFirstService()) }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = getRoute(Home)
    ) {
        composable(Home) {
            HomeScreenWithViewModel(
                goConnexion = {
                    if (isLoggedIn.value) {
                        navController.navigateTo(Informations)
                    } else {
                        navController.navigateTo(Connexion)
                    }
                },
                goEnquete = { enquete ->
                    selectedEnquete.value = enquete
                    navController.navigateTo(EnqueteDetail)
                }
            )
        }

        composable(Connexion) {
            ConnectionScreen(
                goHome = { navController.navigateTo(Home) },
                goInscription = { navController.navigateTo(Inscription) },
                goInformations = {
                    isLoggedIn.value = true
                    navController.navigateTo(Home)
                },
                viewModel = connexionViewModel
            )
        }

        composable(Inscription) {
            val inscriptionViewModel = remember { UserInscriptionViewModel() }
            InscriptionScreen(
                goConnection = { navController.navigateTo(Connexion) },
                viewModel = inscriptionViewModel
            )
        }

        composable(Informations) {
            val connectedUser = connexionViewModel.currentUser.value ?: utilisateur
            InformationsScreen(
                user = connectedUser,
                stat = statistiques,
                goHome = { navController.navigateTo(Home) },
                goLogout = {
                    // Déconnecter l'utilisateur
                    connexionViewModel.logout()
                    isLoggedIn.value = false
                    // Rediriger vers la page de connexion
                    navController.navigateTo(Connexion)
                }
            )
        }

        composable(EnqueteDetail) {
            val enquete = selectedEnquete.value ?: Stub.enquetes.first()

            EnqueteScreen(
                goHome = { navController.navigateTo(Home) },
                goJeu = { navController.navigateTo(Jeu) },
                goConnection = {
                    if (isLoggedIn.value) {
                        navController.navigateTo(Informations)
                    } else {
                        navController.navigateTo(Connexion)
                    }
                },
                enquete = enquete
            )
        }

        composable(Jeu) {
            val enquete = selectedEnquete.value ?: Stub.enquetes.first()

            // Passer le repository au ViewModel
            val resultViewModel = remember {
                EnqueteResultViewModel(
                    enquete,
                    repository
                )
            }

            JeuScreen(
                goHome = { navController.navigateTo(Home) },
                goResultat = {
                    // Stocker les statistiques dans des variables partagées
                    attempts.value = resultViewModel.getAttemptCount()
                    elapsedTime.value = resultViewModel.getElapsedTime()

                    // Naviguer vers l'écran de résultat
                    navController.navigateTo(Resultat)
                },
                enquete = enquete
            )
        }

        composable(Resultat) {
            val enquete = selectedEnquete.value ?: Stub.enquetes.first()

            ResultatScreen(
                enquete = enquete,
                goHome = { navController.navigateTo(Home) },
                attempts = attempts.value,
                timeTaken = elapsedTime.value
            )
        }
    }
}