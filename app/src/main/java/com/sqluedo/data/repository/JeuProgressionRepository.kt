package com.sqluedo.data.repository

import com.sqluedo.data.local.dao.JeuProgressionDao
import com.sqluedo.data.local.entity.JeuProgression
import kotlinx.coroutines.flow.Flow
import java.util.Date

class JeuProgressionRepository(private val jeuProgressionDao: JeuProgressionDao) {

    /**
     * Récupère la progression pour un utilisateur et une enquête donnés
     */
    suspend fun getProgressionForUserAndEnquete(nomUtilisateur: String, enqueteId: Int): JeuProgression? {
        return jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId)
    }

    /**
     * Enregistre ou met à jour la progression sans marquer comme réussie
     */
    suspend fun saveProgression(
        nomUtilisateur: String,
        enqueteId: Int,
        nbTentatives: Int,
        tempsPasse: Long,
    ) {
        val existingProgression = jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId)

        if (existingProgression != null) {
            val updatedProgression = existingProgression.copy(
                nbTentatives = nbTentatives,
                tempsPasse = tempsPasse,
                dateDerniereModification = Date()
            )
            jeuProgressionDao.update(updatedProgression)
        } else {
            // Création d'une nouvelle progression
            val newProgression = JeuProgression(
                nomUtilisateur = nomUtilisateur,
                enqueteId = enqueteId,
                nbTentatives = nbTentatives,
                tempsPasse = tempsPasse
            )
            jeuProgressionDao.insert(newProgression)
        }
    }

    /**
     * Enregistre une tentative réussie et met à jour les statistiques de l'utilisateur
     */
    suspend fun saveSuccessfulAttempt(
        nomUtilisateur: String,
        enqueteId: Int,
        nbTentatives: Int,
        tempsPasse: Long
    ) {
        val existingProgression = jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId)

        if (existingProgression != null) {

            if (existingProgression.reussi) {
                val bestTime = existingProgression.bestTempsPasse ?: existingProgression.tempsPasse
                val minAttempts = existingProgression.minTentatives ?: existingProgression.nbTentatives

                val newBestTime = if (tempsPasse < bestTime) tempsPasse else bestTime
                val newMinAttempts = if (nbTentatives < minAttempts) nbTentatives else minAttempts

                val updatedProgression = existingProgression.copy(
                    nbTentatives = existingProgression.nbTentatives + nbTentatives,
                    tempsPasse = existingProgression.tempsPasse + tempsPasse,
                    bestTempsPasse = newBestTime,
                    minTentatives = newMinAttempts,
                    dateDerniereModification = Date()
                )
                jeuProgressionDao.update(updatedProgression)
            } else {
                // Première réussite
                val updatedProgression = existingProgression.copy(
                    nbTentatives = existingProgression.nbTentatives + nbTentatives,
                    tempsPasse = existingProgression.tempsPasse + tempsPasse,
                    reussi = true,
                    bestTempsPasse = tempsPasse,
                    minTentatives = nbTentatives,
                    dateDerniereModification = Date()
                )
                jeuProgressionDao.update(updatedProgression)
            }
        } else {
            // Nouvelle progression avec réussite
            val newProgression = JeuProgression(
                nomUtilisateur = nomUtilisateur,
                enqueteId = enqueteId,
                nbTentatives = nbTentatives,
                tempsPasse = tempsPasse,
                reussi = true,
                bestTempsPasse = tempsPasse,
                minTentatives = nbTentatives
            )
            jeuProgressionDao.insert(newProgression)
        }
    }

    /**
     * Récupère toutes les progressions pour un utilisateur
     */
    fun getAllProgressionsForUser(nomUtilisateur: String): Flow<List<JeuProgression>> {
        return jeuProgressionDao.getAllProgressionsForUser(nomUtilisateur)
    }

    /**
     * Récupère les enquêtes réussies par l'utilisateur
     */
    fun getReussiesProgressionsForUser(nomUtilisateur: String): Flow<List<JeuProgression>> {
        return jeuProgressionDao.getReussiesProgressionsForUser(nomUtilisateur)
    }

    /**
     * Compte le nombre d'enquêtes réussies par l'utilisateur
     */
    suspend fun getCountEnquetesReussiesForUser(nomUtilisateur: String): Int {
        return jeuProgressionDao.getCountEnquetesReussiesForUser(nomUtilisateur)
    }

    /**
     * Calcule le temps total passé par l'utilisateur sur toutes les enquêtes
     */
    suspend fun getTotalTempsPasseForUser(nomUtilisateur: String): Long {
        return jeuProgressionDao.getTotalTempsPasseForUser(nomUtilisateur) ?: 0L
    }

    /**
     * Récupère les enquêtes résolues les plus rapidement
     */
    fun getFastestCompletions(limit: Int = 5): Flow<List<JeuProgression>> {
        return jeuProgressionDao.getTopFastestCompletions(limit)
    }

    /**
     * Récupère les enquêtes résolues avec le moins de tentatives
     */
    fun getLeastAttemptsCompletions(limit: Int = 5): Flow<List<JeuProgression>> {
        return jeuProgressionDao.getTopLeastAttemptsCompletions(limit)
    }

    /**
     * Supprime toutes les données de progression pour un utilisateur
     */
    suspend fun resetUserProgress(nomUtilisateur: String) {
        jeuProgressionDao.deleteAllProgressionForUser(nomUtilisateur)
    }
}