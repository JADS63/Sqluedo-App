package com.example.sqluedo.data

import android.os.Parcelable.Creator

data class Groupe(
    val nom: String,
    val code: String,
    val nbUtilisateur: Int,
    val nomCreator: Utilisateur,
)
