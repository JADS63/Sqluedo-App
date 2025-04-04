package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.repository.StatistiquesRepository
import com.sqluedo.data.repository.UtilisateurRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class StatistiquesFinDePartieViewModel(
    private val enquete: Enquete,
    private val utilisateurRepository: UtilisateurRepository,
    private val statistiquesRepository: StatistiquesRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun envoyerStatistiquesFinPartie(
        nomUtilisateur: String,
        nbTentatives: Int,
        tempsPasse: Int
    ): Flow<StatistiquesRepository.StatResponse> = flow {
        _isLoading.value = true
        _error.value = null

        try {
            // Assurez-vous d'être authentifié
            if (utilisateurRepository.getAuthToken() == null) {
                utilisateurRepository.authenticateApi()
            }

            // Envoyer les statistiques
            val reponse = statistiquesRepository.updateBestStats(
                nomUtilisateur = nomUtilisateur,
                idEnquete = enquete.id,
                nbTentatives = nbTentatives,
                tempsPasse = tempsPasse,
                reussi = true
            )

            // Vérifier la réponse
            if (reponse.success) {
                // Statistiques mises à jour avec succès
                _error.value = null
                emit(reponse)
            } else {
                // Erreur lors de la mise à jour
                val errorMessage = reponse.message ?: "Erreur lors de la mise à jour des statistiques"
                _error.value = errorMessage
                emit(reponse)
            }
        } catch (e: Exception) {
            // Gérer les erreurs de réseau ou autres
            val errorMessage = "Erreur : ${e.message}"
            _error.value = errorMessage
            emit(StatistiquesRepository.StatResponse(
                message = errorMessage,
                success = false
            ))
        } finally {
            _isLoading.value = false
        }
    }
}