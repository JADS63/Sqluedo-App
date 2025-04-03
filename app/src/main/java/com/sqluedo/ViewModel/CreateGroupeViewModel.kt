package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.repository.GroupeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la création des groupes.
 */
class CreateGroupeViewModel(private val groupeRepository: GroupeRepository) : ViewModel() {

    private val _creationState = MutableStateFlow<GroupeCreationState>(GroupeCreationState.Initial)
    val creationState: StateFlow<GroupeCreationState> = _creationState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Crée un nouveau groupe
     *
     * @param nomGroupe Nom du groupe à créer
     * @param code Code d'accès au groupe
     * @param nomCreateur Nom de l'utilisateur créateur
     */
    fun createGroupe(nomGroupe: String, code: String, nomCreateur: String) {
        if (nomGroupe.isBlank() || code.isBlank() || nomCreateur.isBlank()) {
            _creationState.value = GroupeCreationState.Error("Veuillez remplir tous les champs")
            return
        }

        _isLoading.value = true
        _creationState.value = GroupeCreationState.Loading

        viewModelScope.launch {
            try {
                // Créer le groupe
                val result = groupeRepository.createGroupe(nomGroupe, code, nomCreateur)
                result.fold(
                    onSuccess = { message ->
                        // Si le groupe est créé avec succès, faire rejoindre l'utilisateur
                        try {
                            val joinResult = groupeRepository.joinGroupe(nomGroupe, nomCreateur)
                            joinResult.fold(
                                onSuccess = { joinMessage ->
                                    _creationState.value = GroupeCreationState.Success(
                                        "Groupe créé avec succès et vous avez rejoint le groupe.",
                                        nomGroupe
                                    )
                                },
                                onFailure = { joinError ->
                                    // Le groupe a été créé mais l'utilisateur n'a pas rejoint
                                    _creationState.value = GroupeCreationState.Success(
                                        "Groupe créé avec succès mais erreur lors de la tentative de rejoindre le groupe: ${joinError.message}",
                                        nomGroupe
                                    )
                                }
                            )
                        } catch (e: Exception) {
                            // Le groupe a été créé mais il y a eu une erreur pour rejoindre
                            _creationState.value = GroupeCreationState.Success(
                                "Groupe créé, mais erreur lors de la tentative de rejoindre: ${e.message}",
                                nomGroupe
                            )
                        }
                    },
                    onFailure = { error ->
                        _creationState.value = GroupeCreationState.Error(
                            error.message ?: "Erreur lors de la création du groupe"
                        )
                    }
                )
            } catch (e: Exception) {
                _creationState.value = GroupeCreationState.Error(
                    "Erreur de création: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Réinitialise l'état de création
     */
    fun resetState() {
        _creationState.value = GroupeCreationState.Initial
    }

    /**
     * Factory pour la création du ViewModel avec dépendances
     */
    class Factory(private val repository: GroupeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateGroupeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CreateGroupeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * État de la création de groupe
 */
sealed class GroupeCreationState {
    object Initial : GroupeCreationState()
    object Loading : GroupeCreationState()
    data class Success(val message: String, val nomGroupe: String) : GroupeCreationState()
    data class Error(val message: String) : GroupeCreationState()
}