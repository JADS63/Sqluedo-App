package com.example.sqluedo.data.service

import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.model.Group
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val CODEFIRST_API_BASE = "https://codefirst.iut.uca.fr/containers/sqluedo-webservices/api/v1/"

interface CodeFirstService {

    @GET("Enquete")
    suspend fun listEnquetesSuspend(
        @Query("index") index: Int = 0,
        @Query("count") count: Int = 10
    ): List<Enquete>

//    @GET("Enquete/{enquete}")
//    suspend fun enqueteParNomSuspend(@Path("enquete") enqueteName: String): Enquete
//
//    @GET("Enquete/id/{id}")
//    suspend fun enqueteParIdSuspend(@Path("id") enqueteId: String): Enquete
//
//    @GET("Groupe")
//    suspend fun listGroupesSuspend(): List<Group>
}

fun createCodeFirstRetrofit(): Retrofit {
    val contentType = "application/json".toMediaType()

    val json = Json { ignoreUnknownKeys = true }

    return Retrofit.Builder()
        .baseUrl(CODEFIRST_API_BASE)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}

fun createCodeFirstService(): CodeFirstService {
    val retrofit = createCodeFirstRetrofit()
    return retrofit.create(CodeFirstService::class.java)
}