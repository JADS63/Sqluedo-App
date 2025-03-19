package com.example.sqluedo.data.repository
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.service.CodeFirstService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EnqueteRepository(private val service: CodeFirstService) {

    fun getEnquetes(index: Int = 0, count: Int = 10): Flow<List<Enquete>> = flow {
        try {
            val enquetes = service.listEnquetesSuspend(index, count)
            emit(enquetes)
        } catch (e: Exception) {
            emit(emptyList())
            println("Erreur lors de la récupération des enquêtes: ${e.message}")
        }
    }

    // Récupère une enquête par son ID
//    suspend fun getEnqueteById(id: String): Enquete? {
//        return try {
//            service.enqueteParIdSuspend(id)
//        } catch (e: Exception) {
//            println("Erreur lors de la récupération de l'enquête par ID: ${e.message}")
//            null
//        }
//    }
//
//    // Récupère une enquête par son nom
//    suspend fun getEnqueteByName(name: String): Enquete? {
//        return try {
//            service.enqueteParNomSuspend(name)
//        } catch (e: Exception) {
//            println("Erreur lors de la récupération de l'enquête par nom: ${e.message}")
//            null
//        }
//    }
}

//// Fonction d'extension pour convertir à l'ancien format si nécessaire (pour compatibilité)
//suspend fun loadEnquetesProgress(
//    repository: EnqueteRepository,
//    updateProgress: suspend (List<Enquete>) -> Unit
//): List<Enquete> {
//    var enquetes = emptyList<Enquete>()
//    repository.getEnquetes().collect {
//        enquetes = it
//        updateProgress(it)
//    }
//    return enquetes
//}
//
//suspend fun loadEnqueteByName(
//    repository: EnqueteRepository,
//    enqueteName: String
//): Enquete? {
//    return repository.getEnqueteByName(enqueteName)
//}