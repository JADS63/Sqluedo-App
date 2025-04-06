package com.sqluedo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sqluedo.data.local.SQLuedoDatabase
import com.sqluedo.data.local.dao.JeuProgressionDao
import com.sqluedo.data.local.entity.JeuProgression
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class JeuProgressionDaoInstrumentedTest {

    private lateinit var jeuProgressionDao: JeuProgressionDao
    private lateinit var db: SQLuedoDatabase

    @Before
    fun creerDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, SQLuedoDatabase::class.java
        ).build()
        jeuProgressionDao = db.jeuProgressionDao()
    }

    @After
    @Throws(IOException::class)
    fun fermerDb() {
        db.close()
    }

    @Test
    fun insererEtLireProgression() = runBlocking {
        val progression = JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 1,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = false,
            dateDerniereModification = Date()
        )

        val id = jeuProgressionDao.insert(progression)
        val progressionLue = jeuProgressionDao.getProgressionByUserAndEnquete("testUser", 1)

        assertNotNull(progressionLue)
        assertEquals(progression.nomUtilisateur, progressionLue!!.nomUtilisateur)
        assertEquals(progression.enqueteId, progressionLue.enqueteId)
        assertEquals(progression.nbTentatives, progressionLue.nbTentatives)
    }

    @Test
    fun mettreAJourProgression() = runBlocking {
        val progression = JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 1,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = false,
            dateDerniereModification = Date()
        )

        val id = jeuProgressionDao.insert(progression)
        val progressionLue = jeuProgressionDao.getProgressionByUserAndEnquete("testUser", 1)

        val progressionModifiee = progressionLue!!.copy(
            nbTentatives = 5,
            tempsPasse = 180,
            reussi = true
        )

        jeuProgressionDao.update(progressionModifiee)

        val progressionMiseAJour = jeuProgressionDao.getProgressionByUserAndEnquete("testUser", 1)

        assertEquals(5, progressionMiseAJour!!.nbTentatives)
        assertEquals(180L, progressionMiseAJour.tempsPasse)
        assertTrue(progressionMiseAJour.reussi)
    }

    @Test
    fun compterEnquetesReussies() = runBlocking {
        jeuProgressionDao.insert(JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 1,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = true
        ))

        jeuProgressionDao.insert(JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 2,
            nbTentatives = 5,
            tempsPasse = 200,
            reussi = true
        ))

        jeuProgressionDao.insert(JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 3,
            nbTentatives = 2,
            tempsPasse = 90,
            reussi = false
        ))

        val compteur = jeuProgressionDao.getCountEnquetesReussiesForUser("testUser")

        assertEquals(2, compteur)
    }

    @Test
    fun calculerTempsTotal() = runBlocking {
        jeuProgressionDao.insert(JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 1,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = true
        ))

        jeuProgressionDao.insert(JeuProgression(
            nomUtilisateur = "testUser",
            enqueteId = 2,
            nbTentatives = 5,
            tempsPasse = 200,
            reussi = true
        ))

        val tempsTotal = jeuProgressionDao.getTotalTempsPasseForUser("testUser")

        assertEquals(320L, tempsTotal)
    }


}