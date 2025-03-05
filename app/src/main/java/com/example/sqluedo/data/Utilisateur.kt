package com.example.sqluedo.data

import kotlinx.serialization.Serializable

@Serializable
data class Utilisateur(
    val nomUtilisateur: String,
    var nomGroupe: Groupe?,
    val mdp: String,
    val role: String,
)
