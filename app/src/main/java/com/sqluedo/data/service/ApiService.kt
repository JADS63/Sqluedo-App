package com.sqluedo.data.service

import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Group
import com.sqluedo.data.model.Utilisateur
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val CODEFIRST_API_BASE = "https://codefirst.iut.uca.fr/containers/sqluedo-webservices/api/v1/"

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val index: Int,
    val count: Int
)

@Serializable
data class RegisterResponse(
    val nomUtilisateur: String
)

interface CodeFirstService {
    // Endpoint de connexion
    @POST("Auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body requestBody: RequestBody): ResponseBody

    // Vérifier si un utilisateur existe
    @GET("Utilisateur/exist/{name}")
    suspend fun checkUserExists(
        @Path("name") name: String,
        @Header("Authorization") token: String
    ): Boolean

    // Récupérer un utilisateur par son nom
    @GET("Utilisateur/{nom}")
    suspend fun getUserByName(
        @Path("nom") nom: String,
        @Header("Authorization") token: String
    ): Utilisateur

    // Inscrire un nouvel utilisateur
    @POST("Utilisateur/insert")
    suspend fun registerUser(
        @Query("name") name: String,
        @Query("mdp") mdp: String,
        @Header("Authorization") token: String
    ): RegisterResponse

    // Récupérer la liste des enquêtes
    @GET("Enquete")
    suspend fun listEnquetesSuspend(
        @Header("Authorization") token: String,
        @Query("index") index: Int = 0,
        @Query("count") count: Int = 10
    ): PaginatedResponse<Enquete>

    // Récupérer une enquête par son nom
    @GET("Enquete/{enquete}")
    suspend fun enqueteParNomSuspend(
        @Path("enquete") enqueteName: String,
        @Header("Authorization") token: String
    ): Enquete

    // Récupérer une solution par l'id de l'enquête
    @GET("Enquete/{id}/solution")
    suspend fun enqueteSolutionParIdSuspend(
        @Path("id") enqueteId: Int,
        @Header("Authorization") token: String
    ): String

    // Récupérer une database par l'id de l'enquête
    @GET("Enquete/{id}/database")
    suspend fun enqueteDatabaseParIdSuspend(
        @Path("id") enqueteId: Int,
        @Header("Authorization") token: String
    ): String

    // Récupérer la liste des groupes
    @GET("Groupe")
    suspend fun listGroupesSuspend(
        @Header("Authorization") token: String
    ): List<Group>

    @POST("Enquete/query")
    @Headers("Content-Type: application/json")
    suspend fun executeQuery(
        @Body requestBody: RequestBody,
        @Header("Authorization") token: String
    ): ResponseBody

    // Créer un nouveau groupe
    @POST("Groupe/creer")
    @Headers("Content-Type: application/json")
    suspend fun createGroupe(
        @Body requestBody: RequestBody,
        @Header("Authorization") token: String
    ): String

    // Rejoindre un groupe
    @POST("Groupe/rejoindre")
    suspend fun joinGroupe(
        @Query("nomGroupe") nomGroupe: String,
        @Query("nomUtilisateur") nomUtilisateur: String,
        @Header("Authorization") token: String
    ): ResponseBody

    // Quitter un groupe
    @POST("Groupe/quitter")
    suspend fun leaveGroupe(
        @Query("nomGroupe") nomGroupe: String,
        @Query("nomUtilisateur") nomUtilisateur: String,
        @Header("Authorization") token: String
    ): ResponseBody
}

fun createLoginRequestBody(email: String, password: String): RequestBody {
    val jsonObject = JSONObject()
    jsonObject.put("email", email)
    jsonObject.put("password", password)
    val jsonString = jsonObject.toString()
    return jsonString.toRequestBody("application/json".toMediaType())
}

fun createCodeFirstRetrofit(): Retrofit {
    val contentType = "application/json".toMediaType()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    return Retrofit.Builder()
        .baseUrl(CODEFIRST_API_BASE)
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}

fun createCodeFirstService(): CodeFirstService {
    val retrofit = createCodeFirstRetrofit()
    return retrofit.create(CodeFirstService::class.java)
}