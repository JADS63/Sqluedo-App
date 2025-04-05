package com.sqluedo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sqluedo.data.local.entity.JeuProgression
import kotlinx.coroutines.flow.Flow

@Dao
interface JeuProgressionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progression: JeuProgression): Long

    @Update
    suspend fun update(progression: JeuProgression)

    @Query("SELECT * FROM jeu_progression WHERE nomUtilisateur = :nomUtilisateur AND enqueteId = :enqueteId")
    suspend fun getProgressionByUserAndEnquete(nomUtilisateur: String, enqueteId: Int): JeuProgression?

    @Query("SELECT * FROM jeu_progression WHERE nomUtilisateur = :nomUtilisateur")
    fun getAllProgressionsForUser(nomUtilisateur: String): Flow<List<JeuProgression>>

    @Query("SELECT * FROM jeu_progression WHERE nomUtilisateur = :nomUtilisateur AND reussi = 1")
    fun getReussiesProgressionsForUser(nomUtilisateur: String): Flow<List<JeuProgression>>

    @Query("SELECT COUNT(*) FROM jeu_progression WHERE nomUtilisateur = :nomUtilisateur AND reussi = 1")
    suspend fun getCountEnquetesReussiesForUser(nomUtilisateur: String): Int

    @Query("SELECT SUM(tempsPasse) FROM jeu_progression WHERE nomUtilisateur = :nomUtilisateur")
    suspend fun getTotalTempsPasseForUser(nomUtilisateur: String): Long?

    @Query("SELECT * FROM jeu_progression WHERE reussi = 1 ORDER BY tempsPasse ASC LIMIT :limit")
    fun getTopFastestCompletions(limit: Int): Flow<List<JeuProgression>>

    @Query("SELECT * FROM jeu_progression WHERE reussi = 1 ORDER BY nbTentatives ASC LIMIT :limit")
    fun getTopLeastAttemptsCompletions(limit: Int): Flow<List<JeuProgression>>

    @Query("DELETE FROM jeu_progression WHERE nomUtilisateur = :nomUtilisateur")
    suspend fun deleteAllProgressionForUser(nomUtilisateur: String)
}