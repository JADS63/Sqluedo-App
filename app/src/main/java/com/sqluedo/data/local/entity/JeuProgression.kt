package com.sqluedo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "jeu_progression",
    indices = [
        Index(value = ["nomUtilisateur", "enqueteId"], unique = true)
    ]
)
data class JeuProgression(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nomUtilisateur: String,
    val enqueteId: Int,
    val nbTentatives: Int = 0,
    val tempsPasse: Long = 0,
    val reussi: Boolean = false,
    val bestTempsPasse: Long? = null,
    val minTentatives: Int? = null,
    val dateDerniereModification: Date = Date()
)