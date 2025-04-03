package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Group
import com.sqluedo.data.model.Utilisateur
import com.sqluedo.data.repository.GroupeRepository
import com.sqluedo.data.repository.UtilisateurRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les détails du groupe.
 */
class GroupeDetailViewModel(
    private val groupeRepository: GroupeRepository,
    private val utilisateurRepository: UtilisateurRepository
) : ViewModel() {

    private val _groupe = MutableStateFlow<Group?>(null)
    val groupe: StateFlow<Group?> = _groupe

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _membershipState = MutableStateFlow<MembershipState>(MembershipState.Initial)
    val membershipState: StateFlow<MembershipState> = _membershipState

    /**
     * Vérifie si un utilisateur est membre du groupe
     */
    fun checkMembership(utilisateur: Utilisateur, nomGroupe: String) {
        _membershipState.value = if (utilisateur.nomGroupe?.nom == nomGroupe) {
            MembershipState.Member
        } else {
            MembershipState.NotMember
        }
    }

    /**
     * Permet à un utilisateur de rejoindre un groupe
     */
    fun joinGroupe(nomGroupe: String, nomUtilisateur: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = groupeRepository.joinGroupe(nomGroupe, nomUtilisateur)
                result.fold(
                    onSuccess = {
                        _membershipState.value = MembershipState.Member
                        // Mettre à jour l'utilisateur localement
                        updateUserGroup(nomUtilisateur)
                    },
                    onFailure = {
                        _error.value = it.message ?: "Erreur lors de la tentative de rejoindre le groupe"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la tentative de rejoindre le groupe"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Permet à un utilisateur de quitter un groupe
     */
    fun leaveGroupe(nomGroupe: String, nomUtilisateur: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = groupeRepository.leaveGroupe(nomGroupe, nomUtilisateur)
                result.fold(
                    onSuccess = {
                        _membershipState.value = MembershipState.NotMember
                        // Mettre à jour l'utilisateur localement
                        updateUserGroup(nomUtilisateur)
                    },
                    onFailure = {
                        _error.value = it.message ?: "Erreur lors de la tentative de quitter le groupe"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la tentative de quitter le groupe"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Met à jour les informations de groupe pour l'utilisateur
     */
    private suspend fun updateUserGroup(nomUtilisateur: String) {
        utilisateurRepository.getUserByName(nomUtilisateur).collect { user ->
            if (user != null) {
                // L'utilisateur a été mis à jour avec le nouveau groupe
            }
        }
    }

    /**
     * Factory pour la création du ViewModel avec dépendances
     */
    class Factory(
        private val groupeRepository: GroupeRepository,
        private val utilisateurRepository: UtilisateurRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupeDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GroupeDetailViewModel(groupeRepository, utilisateurRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * État d'appartenance à un groupe
 */
sealed class MembershipState {
    object Initial : MembershipState()
    object Member : MembershipState()
    object NotMember : MembershipState()
}