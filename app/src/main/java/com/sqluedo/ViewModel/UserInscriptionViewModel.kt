package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer l'inscription des utilisateurs.
 */
class UserInscriptionViewModel : ViewModel() {
    private val _inscriptionState = MutableStateFlow<InscriptionState>(InscriptionState.Initial)
    val inscriptionState: StateFlow<InscriptionState> = _inscriptionState

    private val apiService = createCodeFirstService()
    private val utilisateurRepository = UtilisateurRepository(apiService)

    /**
     * Vérifie si un utilisateur existe déjà
     *
     * @param username Nom d'utilisateur à vérifier
     */
    fun checkUserExists(username: String) {
        if (username.isBlank()) {
            _inscriptionState.value = InscriptionState.Error("Veuillez entrer un nom d'utilisateur")
            return
        }

        println("Vérification de l'existence de l'utilisateur: $username")
        _inscriptionState.value = InscriptionState.Loading

        viewModelScope.launch {
            try {
                val exists = utilisateurRepository.checkUserExists(username)
                println("Résultat de la vérification pour $username: existe = $exists")

                if (exists) {
                    _inscriptionState.value = InscriptionState.UserExists(username)
                } else {
                    _inscriptionState.value = InscriptionState.UserAvailable(username)
                }
            } catch (e: Exception) {
                println("Erreur lors de la vérification de l'utilisateur: ${e.message}")
                e.printStackTrace()
                _inscriptionState.value = InscriptionState.Error("Erreur lors de la vérification: ${e.message}")
            }
        }
    }

    /**
     * Inscrit un nouvel utilisateur
     *
     * @param username Nom d'utilisateur
     * @param password Mot de passe
     */
    fun registerUser(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _inscriptionState.value = InscriptionState.Error("Veuillez remplir tous les champs")
            return
        }

        println("Tentative d'inscription de l'utilisateur: $username")
        _inscriptionState.value = InscriptionState.Loading

        viewModelScope.launch {
            try {
                val exists = utilisateurRepository.checkUserExists(username)
                println("Vérification avant inscription: l'utilisateur $username existe? $exists")

                if (exists) {
                    println("Inscription impossible: l'utilisateur $username existe déjà")
                    _inscriptionState.value = InscriptionState.Error("Ce nom d'utilisateur est déjà pris")
                    return@launch
                }

                println("Appel de la méthode d'inscription pour $username...")
                val success = utilisateurRepository.registerUser(username, password)
                println("Résultat de l'inscription pour $username: $success")

                if (success) {
                    _inscriptionState.value = InscriptionState.Success(username)
                } else {
                    _inscriptionState.value = InscriptionState.Error("Échec de l'inscription")
                }
            } catch (e: Exception) {
                println("Erreur lors de l'inscription: ${e.message}")
                e.printStackTrace()
                _inscriptionState.value = InscriptionState.Error("Erreur lors de l'inscription: ${e.message}")
            }
        }
    }

    /**
     * Réinitialise l'état d'inscription
     */
    fun resetState() {
        _inscriptionState.value = InscriptionState.Initial
    }
}

/**
 * État de l'inscription utilisateur
 */
sealed class InscriptionState {
    object Initial : InscriptionState()
    object Loading : InscriptionState()
    data class Success(val username: String) : InscriptionState()
    data class Error(val message: String) : InscriptionState()
    data class UserExists(val username: String) : InscriptionState()
    data class UserAvailable(val username: String) : InscriptionState()

    override fun toString(): String {
        return when (this) {
            is Initial -> "Initial"
            is Loading -> "Loading"
            is Success -> "Success(username=$username)"
            is Error -> "Error(message=$message)"
            is UserExists -> "UserExists(username=$username)"
            is UserAvailable -> "UserAvailable(username=$username)"
        }
    }
}