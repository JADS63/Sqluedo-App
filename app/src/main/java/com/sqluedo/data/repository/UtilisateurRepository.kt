package com.sqluedo.data.repository

import com.sqluedo.data.model.Utilisateur
import com.sqluedo.data.service.CodeFirstService
import com.sqluedo.data.service.createLoginRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject

class UtilisateurRepository(private val service: CodeFirstService) {
    private var authToken: String? = null
    private var currentUsername: String? = null
    private var currentUser: Utilisateur? = null
    /**
     * Définit le nom d'utilisateur actuel
     *
     * @param username Le nom d'utilisateur à définir
     */
    fun setCurrentUsername(username: String) {
        currentUsername = username
    }

    /**
     * Récupère le nom d'utilisateur actuel
     *
     * @return Le nom d'utilisateur actuel ou null
     */
    fun getCurrentUsername(): String? = currentUsername

    /**
     * Définit l'utilisateur actuel
     *
     * @param user L'utilisateur à définir
     */
    fun setCurrentUser(user: Utilisateur) {
        currentUser = user
        currentUsername = user.nomUtilisateur
    }

    /**
     * Récupère l'utilisateur actuel
     *
     * @return L'utilisateur actuel ou null
     */
    fun getCurrentUser(): Utilisateur? = currentUser

    /**
     * Authentifie auprès de l'API pour obtenir un token
     * Utilise des identifiants d'API fixes (ceux fournis)
     *
     * @return true si l'authentification a réussi, false sinon
     */

    /**
     * Récupère le nom d'utilisateur de l'utilisateur connecté
     * Si aucun utilisateur n'est connecté, retourne un nom par défaut
     *
     * @return Le nom d'utilisateur ou un nom par défaut
     */
    fun getActiveUsername(): String {
        return currentUsername ?: currentUser?.nomUtilisateur ?: "admin_prof"
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté
     *
     * @return true si un utilisateur est connecté, false sinon
     */
    fun isUserLoggedIn(): Boolean {
        return currentUsername != null || currentUser != null
    }
    suspend fun authenticateApi(): Boolean {
        return try {
            println("Tentative d'authentification API avec admin@sqluedo.com")
            // Utiliser les identifiants d'API fixes (ceux qui fonctionnent)
            val requestBody = createLoginRequestBody("admin@sqluedo.com", "Admin123!")
            val response = service.login(requestBody)
            val jsonResponse = JSONObject(response.string())
            val token = jsonResponse.getString("token")
            authToken = "Bearer $token"
            println("Authentification API réussie, token obtenu")
            true
        } catch (e: Exception) {
            println("Erreur lors de l'authentification API: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Authentifie un utilisateur avec son nom d'utilisateur et mot de passe
     *
     * @param username Le nom d'utilisateur
     * @param password Le mot de passe
     * @return true si l'authentification a réussi, false sinon
     */
    suspend fun authenticateUser(username: String, password: String): Boolean {
        // Vérifier d'abord que nous avons un token
        if (authToken == null) {
            println("Pas de token, tentative d'authentification API")
            val apiAuthenticated = authenticateApi()
            if (!apiAuthenticated) {
                println("Échec de l'authentification API")
                return false
            }
        }

        return try {
            println("Vérification de l'existence de l'utilisateur $username")
            // Utilisez ce token pour vérifier l'existence de l'utilisateur
            val userExists = service.checkUserExists(username, authToken!!)
            if (!userExists) {
                println("Utilisateur non trouvé: $username")
                return false
            }

            println("Récupération des informations de l'utilisateur $username")
            // Récupérez l'utilisateur pour vérifier son mot de passe
            val user = service.getUserByName(username, authToken!!)

            // Vérifiez que le mot de passe correspond
            if (user.mdp == password) {
                println("Authentification réussie pour $username")
                return true
            } else {
                println("Mot de passe incorrect pour $username")
                return false
            }
        } catch (e: Exception) {
            println("Erreur lors de l'authentification utilisateur: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Vérifie si un utilisateur existe.
     *
     * @param username Le nom d'utilisateur à vérifier
     * @return true si l'utilisateur existe, false sinon
     */
    suspend fun checkUserExists(username: String): Boolean {
        if (authToken == null) {
            println("Pas de token, tentative d'authentification API")
            val success = authenticateApi()
            if (!success) {
                println("Échec de l'authentification API")
                return false
            }
        }

        return try {
            println("Vérification de l'existence de l'utilisateur $username avec le token")
            val result = service.checkUserExists(username, authToken!!)
            println("Résultat de la vérification pour $username: $result")
            result
        } catch (e: Exception) {
            println("Erreur lors de la vérification de l'existence de l'utilisateur: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Inscrit un nouvel utilisateur
     *
     * @param username Le nom d'utilisateur à inscrire
     * @param password Le mot de passe de l'utilisateur
     * @return true si l'inscription a réussi, false sinon
     */
    suspend fun registerUser(username: String, password: String): Boolean {
        if (authToken == null) {
            println("Pas de token pour l'inscription, tentative d'authentification API")
            val success = authenticateApi()
            if (!success) {
                println("Échec de l'authentification API pour l'inscription")
                return false
            }
        }

        return try {
            println("Appel de l'API d'inscription pour $username avec le token: ${authToken?.take(20)}...")
            // Appel à l'API d'inscription
            val response = service.registerUser(username, password, authToken!!)
            println("Réponse de l'API d'inscription: $response")

            // Vérifie si la réponse contient le nom d'utilisateur
            val success = response.nomUtilisateur == username
            println("Inscription réussie pour $username: $success")
            success
        } catch (e: Exception) {
            println("Erreur lors de l'inscription de l'utilisateur $username: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Récupère un utilisateur par son nom.
     *
     * @param username Le nom d'utilisateur à récupérer
     * @return Flow émettant l'utilisateur s'il existe, null sinon
     */
    fun getUserByName(username: String): Flow<Utilisateur?> = flow {
        if (authToken == null) {
            val success = authenticateApi()
            if (!success) {
                emit(null)
                return@flow
            }
        }

        try {
            val userExists = checkUserExists(username)
            if (userExists) {
                val user = service.getUserByName(username, authToken!!)
                emit(user)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
            println("Erreur lors de la récupération de l'utilisateur: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Récupère directement un utilisateur par son nom (version suspend).
     *
     * @param username Le nom d'utilisateur à récupérer
     * @return L'utilisateur s'il existe, null sinon
     */
    suspend fun getUserByNameSuspend(username: String): Utilisateur? {
        if (authToken == null) {
            val success = authenticateApi()
            if (!success) {
                return null
            }
        }

        return try {
            val userExists = checkUserExists(username)
            if (userExists) {
                service.getUserByName(username, authToken!!)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Erreur lors de la récupération de l'utilisateur: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtient le token d'authentification actuel.
     *
     * @return Le token d'authentification ou null s'il n'y en a pas
     */
    fun getAuthToken(): String? {
        return authToken
    }

    /**
     * Vérifie si l'utilisateur est authentifié.
     *
     * @return true si l'utilisateur est authentifié, false sinon
     */
    fun isAuthenticated(): Boolean {
        return authToken != null
    }

    /**
     * Déconnecte l'utilisateur en supprimant le token.
     */
    fun logout() {
        authToken = null
    }
}