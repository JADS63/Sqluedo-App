package com.sqluedo

import com.sqluedo.data.local.dao.JeuProgressionDao
import com.sqluedo.data.local.entity.JeuProgression
import com.sqluedo.data.repository.JeuProgressionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import kotlin.test.assertEquals

class JeuProgressionRepositoryTest {

    private lateinit var jeuProgressionDao: JeuProgressionDao
    private lateinit var repository: JeuProgressionRepository

    @Before
    fun setUp() {
        jeuProgressionDao = mock()
        repository = JeuProgressionRepository(jeuProgressionDao)
    }

    @Test
    fun `getProgressionForUserAndEnquete doit retourner la progression depuis le dao`(): Unit = runBlocking {
        val nomUtilisateur = "testUser"
        val enqueteId = 1
        val expectedProgression = JeuProgression(
            id = 1,
            nomUtilisateur = nomUtilisateur,
            enqueteId = enqueteId,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = true,
            bestTempsPasse = 100,
            minTentatives = 2,
            dateDerniereModification = Date()
        )

        whenever(jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId))
            .thenReturn(expectedProgression)

        val result = repository.getProgressionForUserAndEnquete(nomUtilisateur, enqueteId)

        assertEquals(expectedProgression, result)
        verify(jeuProgressionDao).getProgressionByUserAndEnquete(nomUtilisateur, enqueteId)
    }

    @Test
    fun `saveProgression doit mettre à jour la progression existante quand elle existe`() = runBlocking {
        val nomUtilisateur = "testUser"
        val enqueteId = 1
        val nbTentatives = 5
        val tempsPasse = 300L

        val existingProgression = JeuProgression(
            id = 1,
            nomUtilisateur = nomUtilisateur,
            enqueteId = enqueteId,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = false
        )

        whenever(jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId))
            .thenReturn(existingProgression)

        repository.saveProgression(nomUtilisateur, enqueteId, nbTentatives, tempsPasse)

        verify(jeuProgressionDao).update(any())
    }

    @Test
    fun `saveProgression doit insérer une nouvelle progression quand elle n'existe pas`(): Unit = runBlocking {
        val nomUtilisateur = "testUser"
        val enqueteId = 1
        val nbTentatives = 5
        val tempsPasse = 300L

        whenever(jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId))
            .thenReturn(null)

        repository.saveProgression(nomUtilisateur, enqueteId, nbTentatives, tempsPasse)

        verify(jeuProgressionDao).insert(any())
    }

    @Test
    fun `saveSuccessfulAttempt doit mettre à jour correctement la progression existante quand déjà réussie`() = runBlocking {
        val nomUtilisateur = "testUser"
        val enqueteId = 1
        val nbTentatives = 2
        val tempsPasse = 90L

        val existingProgression = JeuProgression(
            id = 1,
            nomUtilisateur = nomUtilisateur,
            enqueteId = enqueteId,
            nbTentatives = 3,
            tempsPasse = 120,
            reussi = true,
            bestTempsPasse = 100,
            minTentatives = 3
        )

        whenever(jeuProgressionDao.getProgressionByUserAndEnquete(nomUtilisateur, enqueteId))
            .thenReturn(existingProgression)

        repository.saveSuccessfulAttempt(nomUtilisateur, enqueteId, nbTentatives, tempsPasse)

        verify(jeuProgressionDao).update(any())
    }

    @Test
    fun `getCountEnquetesReussiesForUser doit retourner le compte depuis le dao`(): Unit = runBlocking {
        val nomUtilisateur = "testUser"
        val expectedCount = 5

        whenever(jeuProgressionDao.getCountEnquetesReussiesForUser(nomUtilisateur))
            .thenReturn(expectedCount)

        val result = repository.getCountEnquetesReussiesForUser(nomUtilisateur)

        assertEquals(expectedCount, result)
        verify(jeuProgressionDao).getCountEnquetesReussiesForUser(nomUtilisateur)
    }

    @Test
    fun `getTotalTempsPasseForUser doit retourner le temps correct ou 0 quand null`(): Unit = runBlocking {
        val nomUtilisateur = "testUser"
        val expectedTime = 500L

        whenever(jeuProgressionDao.getTotalTempsPasseForUser(nomUtilisateur))
            .thenReturn(expectedTime)

        val result = repository.getTotalTempsPasseForUser(nomUtilisateur)

        assertEquals(expectedTime, result)
        verify(jeuProgressionDao).getTotalTempsPasseForUser(nomUtilisateur)
    }

    @Test
    fun `getTotalTempsPasseForUser doit retourner 0 quand le dao retourne null`(): Unit = runBlocking {
        val nomUtilisateur = "testUser"

        whenever(jeuProgressionDao.getTotalTempsPasseForUser(nomUtilisateur))
            .thenReturn(null)

        val result = repository.getTotalTempsPasseForUser(nomUtilisateur)

        assertEquals(0L, result)
        verify(jeuProgressionDao).getTotalTempsPasseForUser(nomUtilisateur)
    }
}