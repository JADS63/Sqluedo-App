package com.example.sqluedo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Enquete(
    val id: String,
    val nom: String,
    val description: String,
    val difficulteIntermediare: Int,
    val difficulteDificile: Int,
//    val mld: Image,
    val solution:String,
    val indice:String,
    val nomDatabase:String,
)
