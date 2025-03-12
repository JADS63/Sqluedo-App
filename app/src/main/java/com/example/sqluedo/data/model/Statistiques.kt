package com.example.sqluedo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Statistiques(
    val idStatistique: String,
    val nomUtilisateur: Utilisateur,
    val idEnquete: Enquete,
    val nbTentatives: Int,
    val reussi: Boolean,
    val tempsPasse: Int,
)
