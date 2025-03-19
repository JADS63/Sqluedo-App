package com.example.sqluedo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sqluedo.ViewModel.EnqueteListViewModel
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.model.Stub
import com.example.sqluedo.data.repository.EnqueteRepository
import com.example.sqluedo.data.service.createCodeFirstService
import com.example.sqluedo.ui.connexion.ConnectionScreen
import com.example.sqluedo.ui.connexion.InscriptionScreen
import com.example.sqluedo.ui.home.EnqueteScreen
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
object EnqueteDetail

@Composable
fun SQLuedoNavigation() {
    val navController = rememberNavController()

    // Commenté temporairement car l'API n'est pas fonctionnelle
    /*
    val apiService = createCodeFirstService()
    val repository = EnqueteRepository(apiService)

    val enqueteListViewModel: EnqueteListViewModel = viewModel(
        factory = EnqueteListViewModel.Factory(repository)
    )

    val enquetes by enqueteListViewModel.enquetes.collectAsState()
    val isLoading by enqueteListViewModel.isLoading.collectAsState()
    val error by enqueteListViewModel.error.collectAsState()

    val fallbackEnquetes = Stub.enquetes
    val displayedEnquetes = if (enquetes.isEmpty() && !isLoading) fallbackEnquetes else enquetes
    */

    // Utiliser directement les données du Stub en attendant que l'API soit fonctionnelle
    val displayedEnquetes = Stub.enquetes

    val statistiques = Stub.statistiques
    val utilisateur = Stub.utilisateur1

    var selectedEnquete by remember { mutableStateOf<Enquete?>(null) }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                enquetes = displayedEnquetes,
                goConnexion = { navController.navigate(Connexion) },
                goEnquete = { enquete ->
                    selectedEnquete = enquete
                    navController.navigate(EnqueteDetail)
                }
                // Commenté temporairement car isLoading et error ne sont pas disponibles
                /*
                isLoading = isLoading,
                error = error
                */
            )
        }

        composable<Connexion> {
            ConnectionScreen(
                goHome = { navController.navigate(Home) },
                goInscription = { navController.navigate(Inscription) }
            )
        }

        composable<Inscription> {
            InscriptionScreen(
                goConnection = { navController.navigate(Connexion) }
            )
        }

        composable<Informations> {
            InformationsScreen(
                user = utilisateur,
                stat = statistiques,
                goHome = { navController.navigate(Home) }
            )
        }

        composable<EnqueteDetail> {
            val enquete = selectedEnquete ?: displayedEnquetes.firstOrNull() ?: displayedEnquetes.first()

            EnqueteScreen(
                goHome = { navController.navigate(Home) },
                goJeu = { navController.navigate(Jeu) },
                goConnection = { navController.navigate(Connexion) },
                enquete = enquete
            )
        }

        composable<Jeu> {
            val enquete = selectedEnquete ?: displayedEnquetes.firstOrNull() ?: displayedEnquetes.first()

            JeuScreen(
                goHome = { navController.navigate(Home) },
                goResultat = { navController.navigate(Resultat) },
                enquete = enquete
            )
        }

        composable<Resultat> {
            val enquete = selectedEnquete ?: displayedEnquetes.firstOrNull() ?: displayedEnquetes.first()

            ResultatScreen(
                enquete = enquete,
                goHome = { navController.navigate(Home) }
            )
        }
    }
}