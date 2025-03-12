package com.example.sqluedo.data.repository
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.service.CodeFirstService

suspend fun loadEnquetesProgress(
    service: CodeFirstService,
    updateProgress:suspend (List<Enquete>) -> Unit
): List<Enquete> {
    val request = service.listEnquetesSuspend()
    updateProgress(request)
    return request
}

suspend fun loadEnqueteByName(
    service: CodeFirstService,
    enqueueName: String
): Enquete? {
    return try {
        service.enqueteParNomSuspend(enqueueName)
    } catch (e: Exception) {
        println("Erreur lors de la récupération de l'enquête par nom: ${e.message}")
        null
    }
}