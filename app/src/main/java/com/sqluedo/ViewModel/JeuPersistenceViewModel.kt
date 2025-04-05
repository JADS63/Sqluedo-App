package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.JeuProgressionRepository
import com.sqluedo.data.repository.UtilisateurRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel gérant la persistance des données de jeu
 */
class JeuPersistenceViewModel(
    private val jeuProgressionRepository: JeuProgressionRepository,
    private val utilisateurRepository: UtilisateurRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _enqueteActuelle = MutableStateFlow<Enquete?>(null)
    val enqueteActuelle: StateFlow<Enquete?> = _enqueteActuelle

    private val _nbTentatives = MutableStateFlow(0)
    val nbTentatives: StateFlow<Int> = _nbTentatives

    private val _tempsPasse = MutableStateFlow(0L)
    val tempsPasse: StateFlow<Long> = _tempsPasse

    private val _tentativesLocales = MutableStateFlow(0)
    val tentativesLocales: StateFlow<Int> = _tentativesLocales

    private val _tempsLocal = MutableStateFlow(0L)
    val tempsLocal: StateFlow<Long> = _tempsLocal

    private val _enquetesReussies = MutableStateFlow(0)
    val enquetesReussies: StateFlow<Int> = _enquetesReussies

    private val _tempsTotal = MutableStateFlow(0L)
    val tempsTotal: StateFlow<Long> = _tempsTotal

    /**
     * Initialise le ViewModel avec une enquête
     */
    fun initEnquete(enquete: Enquete) {
        _enqueteActuelle.value = enquete
        loadProgressionData()
    }

    /**
     * Charge les données de progression depuis la base de données locale
     */
    private fun loadProgressionData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val enquete = _enqueteActuelle.value
                val nomUtilisateur = utilisateurRepository.getCurrentUsername() ?: "anonymous"

                if (enquete != null) {
                    val progression = jeuProgressionRepository.getProgressionForUserAndEnquete(
                        nomUtilisateur,
                        enquete.id
                    )

                    if (progression != null) {
                        _nbTentatives.value = progression.nbTentatives
                        _tempsPasse.value = progression.tempsPasse
                    } else {
                        _nbTentatives.value = 0
                        _tempsPasse.value = 0L
                    }

                    _tentativesLocales.value = 0
                    _tempsLocal.value = 0L
                }

                val countReussies = jeuProgressionRepository.getCountEnquetesReussiesForUser(nomUtilisateur)
                val tempsTotal = jeuProgressionRepository.getTotalTempsPasseForUser(nomUtilisateur)

                _enquetesReussies.value = countReussies
                _tempsTotal.value = tempsTotal

            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des données: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Enregistre une tentative (exécution d'une requête SQL)
     */
    fun recordAttempt() {
        _tentativesLocales.value = _tentativesLocales.value + 1
        saveCurrentProgress()
    }

    /**
     * Met à jour le temps passé
     */
    fun updateTime(seconds: Long) {
        _tempsLocal.value = seconds
        saveCurrentProgress()
    }

    /**
     * Sauvegarde la progression actuelle
     */
    private fun saveCurrentProgress() {
        viewModelScope.launch {
            try {
                val enquete = _enqueteActuelle.value
                val nomUtilisateur = utilisateurRepository.getCurrentUsername() ?: "anonymous"

                if (enquete != null) {
                    val totalTentatives = _nbTentatives.value + _tentativesLocales.value
                    val totalTemps = _tempsPasse.value + _tempsLocal.value

                    jeuProgressionRepository.saveProgression(
                        nomUtilisateur = nomUtilisateur,
                        enqueteId = enquete.id,
                        nbTentatives = totalTentatives,
                        tempsPasse = totalTemps
                    )
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la sauvegarde de la progression: ${e.message}"
            }
        }
    }

    /**
     * Enregistre une résolution réussie de l'enquête
     */
    fun recordSuccess() {
        viewModelScope.launch {
            try {
                val enquete = _enqueteActuelle.value
                val nomUtilisateur = utilisateurRepository.getCurrentUsername() ?: "anonymous"

                if (enquete != null) {
                    jeuProgressionRepository.saveSuccessfulAttempt(
                        nomUtilisateur = nomUtilisateur,
                        enqueteId = enquete.id,
                        nbTentatives = _tentativesLocales.value,
                        tempsPasse = _tempsLocal.value
                    )
                }

                // Recharger les statistiques
                loadProgressionData()
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'enregistrement du succès: ${e.message}"
            }
        }
    }

    /**
     * Factory pour créer le ViewModel avec les dépendances
     */
    class Factory(
        private val jeuProgressionRepository: JeuProgressionRepository,
        private val utilisateurRepository: UtilisateurRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JeuPersistenceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return JeuPersistenceViewModel(jeuProgressionRepository, utilisateurRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}