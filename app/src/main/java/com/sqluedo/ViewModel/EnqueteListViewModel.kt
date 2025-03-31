package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EnqueteListViewModel(private val repository: EnqueteRepository) : ViewModel() {

    private val _enquetes = MutableStateFlow<List<Enquete>>(emptyList())
    val enquetes: StateFlow<List<Enquete>> = _enquetes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _hasNextPage = MutableStateFlow(true)
    val hasNextPage: StateFlow<Boolean> = _hasNextPage

    private val itemsPerPage = 3
    private var totalItems = 0

    init {
        loadEnquetes()
    }

    fun loadEnquetes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (totalItems == 0) {
                    totalItems = repository.getTotalEnquetesCount()
                }

                val startIndex = _currentPage.value * itemsPerPage

                if (startIndex >= totalItems && totalItems > 0) {
                    _currentPage.value = (totalItems - 1) / itemsPerPage
                    return@launch loadEnquetes()
                }

                repository.getEnquetes(startIndex, itemsPerPage).collect { enquetesList ->
                    _enquetes.value = enquetesList

                    _hasNextPage.value = (startIndex + enquetesList.size) < totalItems && enquetesList.isNotEmpty()

                    if (enquetesList.isEmpty() && _currentPage.value > 0) {
                        _currentPage.value--
                        loadEnquetes()
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inconnue est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun nextPage() {
        val startIndex = (_currentPage.value + 1) * itemsPerPage
        if (_hasNextPage.value && startIndex < totalItems) {
            _currentPage.value++
            loadEnquetes()
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            loadEnquetes()
        }
    }

    fun refreshData() {
        _currentPage.value = 0
        totalItems = 0
        loadEnquetes()
    }

    fun authenticate() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val success = repository.login("admin@sqluedo.com", "Admin123!")
                if (success) {
                    loadEnquetes()
                } else {
                    _error.value = "Échec de l'authentification"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur d'authentification"
            } finally {
                _isLoading.value = false
            }
        }
    }

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