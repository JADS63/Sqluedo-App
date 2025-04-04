package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Utilisateur
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la connexion des utilisateurs.
 */
class UserConnexionViewModel : ViewModel() {
    private val _connexionState = MutableStateFlow<ConnexionState>(ConnexionState.Initial)
    val connexionState: StateFlow<ConnexionState> = _connexionState

    private val _currentUser = MutableStateFlow<Utilisateur?>(null)
    val currentUser: StateFlow<Utilisateur?> = _currentUser

    private val apiService = createCodeFirstService()
    private val utilisateurRepository = UtilisateurRepository(apiService)

    /**
     * Tente de connecter un utilisateur avec ses identifiants.
     *
     * @param username Le nom d'utilisateur
     * @param password Le mot de passe
     */
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _connexionState.value = ConnexionState.Error("Veuillez remplir tous les champs")
            return
        }

        _connexionState.value = ConnexionState.Loading

        viewModelScope.launch {
            try {
                val userAuthenticated = utilisateurRepository.authenticateUser(username, password)

                if (userAuthenticated) {
                    utilisateurRepository.getUserByName(username).collect { user ->
                        if (user != null) {
                            // Définir l'utilisateur dans le repository et dans le ViewModel
                            utilisateurRepository.setCurrentUser(user)
                            _currentUser.value = user
                            _connexionState.value = ConnexionState.Success(user)
                        } else {
                            _connexionState.value = ConnexionState.Error("Utilisateur authentifié mais impossible de récupérer ses informations")
                        }
                    }
                } else {
                    _connexionState.value = ConnexionState.Error("Identifiants incorrects")
                }
            } catch (e: Exception) {
                _connexionState.value = ConnexionState.Error("Erreur de connexion: ${e.message}")
            }
        }
    }

    /**
     * Vérifie si un utilisateur existe
     *
     * @param username Le nom d'utilisateur à vérifier
     */
    fun checkUserExists(username: String) {
        if (username.isBlank()) {
            _connexionState.value = ConnexionState.Error("Veuillez entrer un nom d'utilisateur")
            return
        }

        viewModelScope.launch {
            try {
                val exists = utilisateurRepository.checkUserExists(username)
                if (exists) {
                    _connexionState.value = ConnexionState.UserExists(username)
                } else {
                    _connexionState.value = ConnexionState.UserNotFound(username)
                }
            } catch (e: Exception) {
                _connexionState.value = ConnexionState.Error("Erreur lors de la vérification: ${e.message}")
            }
        }
    }

    /**
     * Réinitialise l'état de connexion
     */
    fun resetState() {
        _connexionState.value = ConnexionState.Initial
    }

    /**
     * Déconnecte l'utilisateur
     */
    fun logout() {
        _currentUser.value = null
        utilisateurRepository.logout()
        _connexionState.value = ConnexionState.Initial
    }

    /**
     * Obtient le nom d'utilisateur actuel
     *
     * @return Le nom d'utilisateur ou null
     */
    fun getCurrentUsername(): String? {
        return utilisateurRepository.getCurrentUsername()
    }

    /**
     * Obtient l'utilisateur actuel
     *
     * @return L'utilisateur actuel ou null
     */
    fun getCurrentUser(): Utilisateur? {
        return utilisateurRepository.getCurrentUser()
    }

    /**
     * Vérifie si un utilisateur est connecté
     *
     * @return true si un utilisateur est connecté, false sinon
     */
    fun isUserLoggedIn(): Boolean {
        return utilisateurRepository.isUserLoggedIn()
    }
}

/**
 * État de la connexion utilisateur
 */
sealed class ConnexionState {
    object Initial : ConnexionState()
    object Loading : ConnexionState()
    data class Success(val user: Utilisateur) : ConnexionState()
    data class Error(val message: String) : ConnexionState()
    data class UserExists(val username: String) : ConnexionState()
    data class UserNotFound(val username: String) : ConnexionState()
}