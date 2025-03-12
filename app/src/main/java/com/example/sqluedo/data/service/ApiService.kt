package com.example.sqluedo.data.service
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.model.Group
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

private const val CODEFIRST_API_BASE = "LienVersLApi"

interface CodeFirstService {

    //Récupérer toutes les enquêtes
    @GET("enquetes")
    suspend fun listEnquetesSuspend(): List<Enquete>

    //Récupérer une enquête à partir de son nom
    @GET("enquetes/{enquete}")
    suspend fun enqueteParNomSuspend(@Path("enquete") enqueteName: String): Enquete

    @GET("groupes")
    suspend fun listGroupesSuspend():List<Group>
}


// Builder qui créera l'instance de Retrofit pour accéder au service
fun createCodeFirstRetrofit(): Retrofit {
    // Définition du type MIME pour JSON
    val contentType = "application/json".toMediaType()

    // Configuration de kotlinx.serialization pour ignorer les clés inconnues
    val json = Json { ignoreUnknownKeys = true }

    return Retrofit.Builder()
        .baseUrl(CODEFIRST_API_BASE) // La base URL doit se terminer par '/'
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}