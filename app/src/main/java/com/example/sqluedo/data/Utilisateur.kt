package com.example.sqluedo.data

data class Utilisateur(
    val nomUtilisateur: String,
    var nomGroupe: Groupe?,
    val mdp: String,
    val role: String,
)
