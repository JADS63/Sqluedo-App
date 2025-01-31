package com.example.sqluedo.data

data class Statistiques(
    val idStatistique: String,
    val nomUtilisateur: Utilisateur,
    val idEnquete: Enquete,
    val nbTentatives: Int,
    val reussi: Boolean,
    val tempsPasse: Int,
)
