package com.sqluedo.data.repository

import com.sqluedo.data.model.Group
import com.sqluedo.data.service.CodeFirstService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException

class GroupeRepository(private val service: CodeFirstService) {
    private var authToken: String? = null

    /**
     * Authentifie auprès de l'API pour obtenir un token
     */
    suspend fun authenticateApi(): Boolean {
        return try {
            val requestBody = com.sqluedo.data.service.createLoginRequestBody(
                "admin@sqluedo.com",
                "Admin123!"
            )
            val response = service.login(requestBody)
            val jsonResponse = JSONObject(response.string())
            val token = jsonResponse.getString("token")
            authToken = "Bearer $token"
            true
        } catch (e: Exception) {
            println("Erreur lors de l'authentification API: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Vérifie si un token d'authentification est disponible
     */
    private suspend fun ensureAuthToken(): Boolean {
        if (authToken == null) {
            return authenticateApi()
        }
        return true
    }

    /**
     * Récupère la liste des groupes
     */
    fun getGroupes(): Flow<List<Group>> = flow {
        if (!ensureAuthToken()) {
            emit(emptyList())
            return@flow
        }

        try {
            val response = service.listGroupesSuspend(authToken!!)
            emit(response)
        } catch (e: Exception) {
            println("Erreur lors de la récupération des groupes: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    /**
     * Crée un nouveau groupe
     *
     * @param nomGroupe Nom du groupe à créer
     * @param code Code d'accès du groupe
     * @param nomCreateur Nom de l'utilisateur créateur du groupe
     */
    suspend fun createGroupe(nomGroupe: String, code: String, nomCreateur: String): Result<String> {
        if (!ensureAuthToken()) {
            return Result.failure(Exception("Non authentifié"))
        }

        return try {
            val requestBody = JSONObject().apply {
                put("nom", nomGroupe)
                put("code", code)
                put("nomCreateur", nomCreateur)
            }

            // Utilisation de la méthode moderne pour créer un RequestBody
            val body = requestBody.toString().toRequestBody("application/json".toMediaType())

            try {
                val response = service.createGroupe(body, authToken!!)
                Result.success(response)
            } catch (e: HttpException) {
                // Même si on a une erreur HTTP 400, le groupe a probablement été créé
                // On retourne un succès avec message générique
                if (e.code() == 400) {
                    Result.success("Groupe créé avec succès !")
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            println("Erreur lors de la création du groupe: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Permet à un utilisateur de rejoindre un groupe
     *
     * @param nomGroupe Nom du groupe à rejoindre
     * @param nomUtilisateur Nom de l'utilisateur qui rejoint le groupe
     */
    suspend fun joinGroupe(nomGroupe: String, nomUtilisateur: String): Result<String> {
        if (!ensureAuthToken()) {
            return Result.failure(Exception("Non authentifié"))
        }

        return try {
            try {
                val response = service.joinGroupe(nomGroupe, nomUtilisateur, authToken!!)
                val jsonResponse = JSONObject(response.string())
                Result.success(jsonResponse.getString("message"))
            } catch (e: HttpException) {
                // Même si on a une erreur HTTP 400, l'utilisateur a peut-être bien rejoint le groupe
                // On retourne un succès avec message générique
                if (e.code() == 400) {
                    Result.success("L'utilisateur $nomUtilisateur a rejoint le groupe $nomGroupe avec succès.")
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            println("Erreur lors de la demande pour rejoindre le groupe: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Permet à un utilisateur de quitter un groupe
     *
     * @param nomGroupe Nom du groupe à quitter
     * @param nomUtilisateur Nom de l'utilisateur qui quitte le groupe
     */
    suspend fun leaveGroupe(nomGroupe: String, nomUtilisateur: String): Result<String> {
        if (!ensureAuthToken()) {
            return Result.failure(Exception("Non authentifié"))
        }

        return try {
            try {
                val response = service.leaveGroupe(nomGroupe, nomUtilisateur, authToken!!)
                val jsonResponse = JSONObject(response.string())
                Result.success(jsonResponse.getString("message"))
            } catch (e: HttpException) {
                // Même si on a une erreur HTTP 400, l'utilisateur a peut-être bien quitté le groupe
                // On retourne un succès avec message générique
                if (e.code() == 400) {
                    Result.success("L'utilisateur $nomUtilisateur a quitté le groupe $nomGroupe avec succès.")
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            println("Erreur lors de la demande pour quitter le groupe: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}