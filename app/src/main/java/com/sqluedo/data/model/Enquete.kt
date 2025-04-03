package com.sqluedo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Enquete(
    val id: Int,
    val nomCreateur: String,
    @SerialName("titre")
    val nom: String,
    val description: String,
    @SerialName("difficulteIntermediaire")
    val difficulteIntermediare: Int,
    @SerialName("difficulteDifficile")
    val difficulteDificile: Int,
    val mld: String,
    val solution: String,
    val indice: String,
    val nomDatabase: String
)