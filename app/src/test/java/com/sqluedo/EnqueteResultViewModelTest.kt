package com.sqluedo

import com.sqluedo.ViewModel.EnqueteResultViewModel
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EnqueteResultViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var repository: EnqueteRepository
    private lateinit var viewModel: EnqueteResultViewModel

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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = EnqueteResultViewModel(mockEnquete, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `verifyAnswer doit identifier correctement une réponse correcte`() = runTest {
        val userAnswer = "SELECT * FROM utilisateur WHERE age > 18"

        viewModel.verifyAnswer(userAnswer)

        assertNotNull(viewModel.verificationResult.value)
        val result = viewModel.verificationResult.value!!
        assertTrue(result.isCorrect)
        assertEquals(userAnswer, result.submittedAnswer)
        assertEquals(mockEnquete.solution, result.correctSolution)
        assertEquals(1, result.attemptCount)
        assertTrue(result.message.contains("Félicitations"))
    }

    @Test
    fun `verifyAnswer doit identifier correctement une réponse incorrecte`() = runTest {
        val userAnswer = "SELECT * FROM utilisateur"

        viewModel.verifyAnswer(userAnswer)

        assertNotNull(viewModel.verificationResult.value)
        val result = viewModel.verificationResult.value!!
        assertFalse(result.isCorrect)
        assertEquals(userAnswer, result.submittedAnswer)
        assertEquals(mockEnquete.solution, result.correctSolution)
        assertEquals(1, result.attemptCount)
        assertTrue(result.message.contains("pas correcte"))
    }

    @Test
    fun `verifyAnswer doit gérer la casse et les espaces blancs`() = runTest {
        val userAnswer = "select   *   FROM    utilisateur    WHERE    age > 18"

        viewModel.verifyAnswer(userAnswer)

        assertNotNull(viewModel.verificationResult.value)
        val result = viewModel.verificationResult.value!!
        assertTrue(result.isCorrect)
    }

    @Test
    fun `verifyAnswer doit incrémenter le compteur de tentatives`() = runTest {
        viewModel.verifyAnswer("SELECT * FROM utilisateur")
        assertEquals(1, viewModel.verificationResult.value?.attemptCount)

        viewModel.verifyAnswer("SELECT * FROM utilisateur WHERE id = 1")
        assertEquals(2, viewModel.verificationResult.value?.attemptCount)

        viewModel.verifyAnswer("SELECT * FROM utilisateur WHERE age > 18")
        assertEquals(3, viewModel.verificationResult.value?.attemptCount)
    }

    @Test
    fun `resetVerification doit effacer le résultat et l'erreur`() = runTest {
        viewModel.verifyAnswer("SELECT * FROM utilisateur")
        assertNotNull(viewModel.verificationResult.value)

        viewModel.resetVerification()

        assertNull(viewModel.verificationResult.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `getAttemptCount doit retourner le nombre actuel de tentatives`() = runTest {
        assertEquals(0, viewModel.getAttemptCount())

        viewModel.verifyAnswer("attempt 1")
        viewModel.verifyAnswer("attempt 2")

        assertEquals(2, viewModel.getAttemptCount())
    }

    @Test
    fun `getElapsedTime doit retourner une valeur supérieure ou égale à 0`() = runTest {
        val elapsedTime = viewModel.getElapsedTime()
        assertTrue(elapsedTime >= 0)
    }
}