package com.sqluedo

import com.sqluedo.ViewModel.JeuPersistenceViewModel
import com.sqluedo.data.local.entity.JeuProgression
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.JeuProgressionRepository
import com.sqluedo.data.repository.UtilisateurRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class JeuPersistenceViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var jeuProgressionRepository: JeuProgressionRepository
    private lateinit var utilisateurRepository: UtilisateurRepository
    private lateinit var viewModel: JeuPersistenceViewModel

    private val mockEnquete = Enquete(
        id = 1,
        nomCreateur = "admin_prof",
        nom = "Enquête Test",
        description = "Description test",
        difficulteIntermediare = 2,
        difficulteDificile = 3,
        mld = "Table utilisateur(id, nom, age)",
        solution = "SELECT * FROM utilisateur WHERE age > 18",
        indice = "Filtrer par âge",
        nomDatabase = "DB_Test"
    )

    private val mockUsername = "testUser"
    private val mockProgression = JeuProgression(
        id = 1,
        nomUtilisateur = mockUsername,
        enqueteId = mockEnquete.id,
        nbTentatives = 5,
        tempsPasse = 300L,
        reussi = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        jeuProgressionRepository = mock()
        utilisateurRepository = mock()
        whenever(utilisateurRepository.getCurrentUsername()).thenReturn(mockUsername)
        viewModel = JeuPersistenceViewModel(jeuProgressionRepository, utilisateurRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `initEnquete doit charger les données de progression`() = runTest {
        whenever(jeuProgressionRepository.getProgressionForUserAndEnquete(mockUsername, mockEnquete.id))
            .thenReturn(mockProgression)
        whenever(jeuProgressionRepository.getCountEnquetesReussiesForUser(mockUsername))
            .thenReturn(3)
        whenever(jeuProgressionRepository.getTotalTempsPasseForUser(mockUsername))
            .thenReturn(1500L)

        viewModel.initEnquete(mockEnquete)

        assertEquals(mockEnquete, viewModel.enqueteActuelle.value)
        assertEquals(mockProgression.nbTentatives, viewModel.nbTentatives.value)
        assertEquals(mockProgression.tempsPasse, viewModel.tempsPasse.value)
        assertEquals(0, viewModel.tentativesLocales.value)
        assertEquals(0L, viewModel.tempsLocal.value)
        assertEquals(3, viewModel.enquetesReussies.value)
        assertEquals(1500L, viewModel.tempsTotal.value)
    }

    @Test
    fun `initEnquete doit gérer une progression nulle`() = runTest {
        whenever(jeuProgressionRepository.getProgressionForUserAndEnquete(mockUsername, mockEnquete.id))
            .thenReturn(null)
        whenever(jeuProgressionRepository.getCountEnquetesReussiesForUser(mockUsername))
            .thenReturn(0)
        whenever(jeuProgressionRepository.getTotalTempsPasseForUser(mockUsername))
            .thenReturn(0L)

        viewModel.initEnquete(mockEnquete)

        assertEquals(mockEnquete, viewModel.enqueteActuelle.value)
        assertEquals(0, viewModel.nbTentatives.value)
        assertEquals(0L, viewModel.tempsPasse.value)
        assertEquals(0, viewModel.tentativesLocales.value)
        assertEquals(0L, viewModel.tempsLocal.value)
    }

    @Test
    fun `initEnquete doit gérer les erreurs`() = runTest {
        whenever(jeuProgressionRepository.getProgressionForUserAndEnquete(mockUsername, mockEnquete.id))
            .thenThrow(RuntimeException("Test error"))

        viewModel.initEnquete(mockEnquete)

        assertEquals(mockEnquete, viewModel.enqueteActuelle.value)
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Test error"))
    }

    @Test
    fun `recordAttempt doit incrémenter tentativesLocales`() = runTest {
        viewModel.initEnquete(mockEnquete)
        assertEquals(0, viewModel.tentativesLocales.value)

        viewModel.recordAttempt()

        assertEquals(1, viewModel.tentativesLocales.value)
    }

    @Test
    fun `updateTime doit mettre à jour tempsLocal`() = runTest {
        viewModel.initEnquete(mockEnquete)
        assertEquals(0L, viewModel.tempsLocal.value)

        viewModel.updateTime(120L)

        assertEquals(120L, viewModel.tempsLocal.value)
    }

    @Test
    fun `saveCurrentProgress doit appeler le repository avec les valeurs mises à jour`() = runTest {
        whenever(jeuProgressionRepository.getProgressionForUserAndEnquete(mockUsername, mockEnquete.id))
            .thenReturn(mockProgression)
        whenever(jeuProgressionRepository.getCountEnquetesReussiesForUser(mockUsername))
            .thenReturn(3)
        whenever(jeuProgressionRepository.getTotalTempsPasseForUser(mockUsername))
            .thenReturn(1500L)

        viewModel.initEnquete(mockEnquete)
        viewModel.recordAttempt()
        viewModel.updateTime(150L)

        verify(jeuProgressionRepository).saveProgression(
            nomUtilisateur = mockUsername,
            enqueteId = mockEnquete.id,
            nbTentatives = 6,
            tempsPasse = 450L
        )
    }

    @Test
    fun `recordSuccess doit appeler saveSuccessfulAttempt`() = runTest {
        whenever(jeuProgressionRepository.getProgressionForUserAndEnquete(mockUsername, mockEnquete.id))
            .thenReturn(mockProgression)
        whenever(jeuProgressionRepository.getCountEnquetesReussiesForUser(mockUsername))
            .thenReturn(3)
        whenever(jeuProgressionRepository.getTotalTempsPasseForUser(mockUsername))
            .thenReturn(1500L)

        viewModel.initEnquete(mockEnquete)
        viewModel.recordAttempt()
        viewModel.updateTime(150L)

        viewModel.recordSuccess()

        verify(jeuProgressionRepository).saveSuccessfulAttempt(
            nomUtilisateur = mockUsername,
            enqueteId = mockEnquete.id,
            nbTentatives = 1,
            tempsPasse = 150L
        )
    }

    @Test
    fun `updateTime et recordAttempt doivent mettre à jour l'état sans attendre l'IO`() = runTest {
        whenever(jeuProgressionRepository.getProgressionForUserAndEnquete(mockUsername, mockEnquete.id))
            .thenReturn(mockProgression)
        whenever(jeuProgressionRepository.getCountEnquetesReussiesForUser(mockUsername))
            .thenReturn(3)

        viewModel.initEnquete(mockEnquete)

        viewModel.recordAttempt()
        viewModel.updateTime(100L)

        assertEquals(1, viewModel.tentativesLocales.value)
        assertEquals(100L, viewModel.tempsLocal.value)
    }

    @Test
    fun `Factory doit créer une instance correcte de ViewModel`() {
        val factory = JeuPersistenceViewModel.Factory(jeuProgressionRepository, utilisateurRepository)
        val createdViewModel = factory.create(JeuPersistenceViewModel::class.java)
        assertTrue(createdViewModel is JeuPersistenceViewModel)
    }
}