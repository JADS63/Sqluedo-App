package com.example.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.repository.EnqueteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la liste des enquêtes.
 */
class EnqueteListViewModel(private val repository: EnqueteRepository) : ViewModel() {

    private val _enquetes = MutableStateFlow<List<Enquete>>(emptyList())
    val enquetes: StateFlow<List<Enquete>> = _enquetes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadEnquetes(index: Int = 0, count: Int = 10) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.getEnquetes(index, count).collect { result ->
                    _enquetes.value = result
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des enquêtes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Fonction pour rafraîchir les données
    fun refreshData() {
        loadEnquetes()
    }

    // Initialisation
    init {
        loadEnquetes()
    }

    // Factory pour créer le ViewModel avec les dépendances
    class Factory(private val repository: EnqueteRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EnqueteListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EnqueteListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}