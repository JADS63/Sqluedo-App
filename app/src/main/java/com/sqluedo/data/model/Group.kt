package com.sqluedo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val nom: String,
    val code: String,
    val nbUtilisateur: Int,
    val nomCreator: Utilisateur?,
)
