package com.sqluedo.data.repository

import com.sqluedo.data.service.CodeFirstService
import kotlinx.serialization.Serializable
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType

class StatistiquesRepository(
    private val service: CodeFirstService,
    private val utilisateurRepository: UtilisateurRepository
) {
    @Serializable
    data class StatResponse(
        val message: String? = null,
        val success: Boolean = false
    )

    suspend fun updateBestStats(
        nomUtilisateur: String,
        idEnquete: Int,
        nbTentatives: Int,
        tempsPasse: Int,
        reussi: Boolean
    ): StatResponse {
        // Vérifier la validité des paramètres
        println("Paramètres de mise à jour des statistiques:")
        println("Nom utilisateur: $nomUtilisateur")
        println("ID Enquête: $idEnquete")
        println("Nombre de tentatives: $nbTentatives")
        println("Temps passé: $tempsPasse")
        println("Réussi: $reussi")

        // Récupérer le token d'authentification
        val token = utilisateurRepository.getAuthToken()
            ?: throw IllegalStateException("Aucun token d'authentification")

        // Effectuer la requête à l'API
        return try {
            val response = service.updateBestStats(
                nomUtilisateur = nomUtilisateur,
                idEnquete = idEnquete,
                nbTentatives = nbTentatives,
                tempsPasse = tempsPasse,
                reussi = reussi,
                token = token
            )
            val responseString = response.string()
            println("Réponse du serveur: $responseString")

            val responseJson = JSONObject(responseString)

            StatResponse(
                message = responseJson.optString("message"),
                success = responseJson.optBoolean("success", false)
            )
        } catch (e: Exception) {
            println("Erreur détaillée lors de la mise à jour des statistiques: ${e.message}")
            e.printStackTrace()
            StatResponse(
                message = "Erreur lors de la mise à jour des statistiques : ${e.message}",
                success = false
            )
        }
    }
}