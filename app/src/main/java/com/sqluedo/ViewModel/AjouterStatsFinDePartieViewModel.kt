package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.StatistiquesRepository
import com.sqluedo.data.repository.UtilisateurRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

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
            if (utilisateurRepository.getAuthToken() == null) {
                utilisateurRepository.authenticateApi()
            }

            val reponse = statistiquesRepository.updateBestStats(
                nomUtilisateur = nomUtilisateur,
                idEnquete = enquete.id,
                nbTentatives = nbTentatives,
                tempsPasse = tempsPasse,
                reussi = true
            )

            if (reponse.success) {
                _error.value = null
                emit(reponse)
            } else {
                val errorMessage = reponse.message ?: "Erreur lors de la mise à jour des statistiques"
                _error.value = errorMessage
                emit(reponse)
            }
        } catch (e: Exception) {
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