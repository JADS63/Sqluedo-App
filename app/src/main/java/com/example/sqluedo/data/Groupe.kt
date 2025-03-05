package com.example.sqluedo.data

import kotlinx.serialization.Serializable

@Serializable
data class Groupe(
    val nom: String,
    val code: String,
    val nbUtilisateur: Int,
    val nomCreator: Utilisateur,
)
