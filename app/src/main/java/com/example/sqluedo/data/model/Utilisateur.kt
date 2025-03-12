package com.example.sqluedo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Utilisateur(
    val nomUtilisateur: String,
    var nomGroupe: Group?,
    val mdp: String,
    val role: String,
)
