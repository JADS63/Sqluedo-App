package com.example.sqluedo.data.repository
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.service.CodeFirstService
import com.example.sqluedo.data.service.createLoginRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject

class EnqueteRepository(private val service: CodeFirstService) {
    private var authToken: String? = null

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val requestBody = createLoginRequestBody(email, password)
            val response = service.login(requestBody)
            val jsonResponse = JSONObject(response.string())
            val token = jsonResponse.getString("token")
            authToken = "Bearer $token"
            true
        } catch (e: Exception) {
            println("Erreur lors de l'authentification: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getEnquetes(index: Int = 0, count: Int = 3): Flow<List<Enquete>> = flow {
        if (authToken == null) {
            val success = login("admin@sqluedo.com", "Admin123!")
            if (!success) {
                emit(emptyList())
                return@flow
            }
        }

        try {
            val response = service.listEnquetesSuspend(authToken!!, index, count)
            emit(response.items)
        } catch (e: Exception) {
            emit(emptyList())
            println("Erreur lors de la récupération des enquêtes: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun getTotalEnquetesCount(): Int {
        if (authToken == null) {
            login("admin@sqluedo.com", "Admin123!")
        }

        return try {
            val response = service.listEnquetesSuspend(authToken!!, 0, 1)
            response.totalCount
        } catch (e: Exception) {
            println("Erreur lors de la récupération du nombre total d'enquêtes: ${e.message}")
            20
        }
    }
}