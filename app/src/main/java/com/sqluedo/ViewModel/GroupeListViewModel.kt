package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Group
import com.sqluedo.data.repository.GroupeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la liste des groupes.
 */
class GroupeListViewModel(private val repository: GroupeRepository) : ViewModel() {

    private val _groupes = MutableStateFlow<List<Group>>(emptyList())
    val groupes: StateFlow<List<Group>> = _groupes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _operationMessage = MutableStateFlow<OperationMessage?>(null)
    val operationMessage: StateFlow<OperationMessage?> = _operationMessage

    init {
        loadGroupes()
    }

    /**
     * Charge la liste des groupes
     */
    fun loadGroupes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.getGroupes().collect { groupesList ->
                    _groupes.value = groupesList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement des groupes"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Permet à un utilisateur de rejoindre un groupe
     */
    fun joinGroupe(nomGroupe: String, nomUtilisateur: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _operationMessage.value = null

            try {
                val result = repository.joinGroupe(nomGroupe, nomUtilisateur)
                result.fold(
                    onSuccess = { message ->
                        _operationMessage.value = OperationMessage(message, OperationType.JOIN, true)
                        loadGroupes()
                    },
                    onFailure = {
                        _error.value = it.message ?: "Erreur lors de la tentative de rejoindre le groupe"
                        _operationMessage.value = OperationMessage(
                            "Impossible de rejoindre le groupe",
                            OperationType.JOIN,
                            false
                        )
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la tentative de rejoindre le groupe"
                _operationMessage.value = OperationMessage(
                    "Erreur: ${e.message}",
                    OperationType.JOIN,
                    false
                )
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
            _operationMessage.value = null

            try {
                val result = repository.leaveGroupe(nomGroupe, nomUtilisateur)
                result.fold(
                    onSuccess = { message ->
                        _operationMessage.value = OperationMessage(message, OperationType.LEAVE, true)
                        loadGroupes()
                    },
                    onFailure = {
                        _error.value = it.message ?: "Erreur lors de la tentative de quitter le groupe"
                        _operationMessage.value = OperationMessage(
                            "Impossible de quitter le groupe",
                            OperationType.LEAVE,
                            false
                        )
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la tentative de quitter le groupe"
                _operationMessage.value = OperationMessage(
                    "Erreur: ${e.message}",
                    OperationType.LEAVE,
                    false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Efface le message d'opération
     */
    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    /**
     * Factory pour la création du ViewModel avec dépendances
     */
    class Factory(private val repository: GroupeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupeListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GroupeListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

enum class OperationType {
    JOIN, LEAVE, CREATE
}

data class OperationMessage(
    val message: String,
    val type: OperationType,
    val isSuccess: Boolean
)