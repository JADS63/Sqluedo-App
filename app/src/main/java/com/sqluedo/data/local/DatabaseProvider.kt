package com.sqluedo.data.local

import android.content.Context
import com.sqluedo.data.repository.JeuProgressionRepository
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.createCodeFirstService

/**
 * Classe utilitaire pour fournir les instances de la base de données et des repositories
 * associés dans toute l'application.
 */
object DatabaseProvider {

    /**
     * Fournit une instance du repository de progression de jeu
     */
    fun provideJeuProgressionRepository(context: Context): JeuProgressionRepository {
        val database = SQLuedoDatabase.getDatabase(context)
        return JeuProgressionRepository(database.jeuProgressionDao())
    }

    /**
     * Fournit tous les repositories nécessaires pour la persistance des données de jeu
     */
    fun provideRepositories(context: Context): Repositories {
        val database = SQLuedoDatabase.getDatabase(context)
        val codeFirstService = createCodeFirstService()

        val jeuProgressionRepository = JeuProgressionRepository(database.jeuProgressionDao())
        val utilisateurRepository = UtilisateurRepository(codeFirstService)

        return Repositories(
            jeuProgressionRepository = jeuProgressionRepository,
            utilisateurRepository = utilisateurRepository
        )
    }

    /**
     * Classe conteneur pour les repositories
     */
    data class Repositories(
        val jeuProgressionRepository: JeuProgressionRepository,
        val utilisateurRepository: UtilisateurRepository
    )
}