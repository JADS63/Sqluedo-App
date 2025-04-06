package com.sqluedo

import com.sqluedo.ViewModel.EnqueteListViewModel
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EnqueteListViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var repository: EnqueteRepository
    private lateinit var viewModel: EnqueteListViewModel

    private val mockEnquetes = listOf(
        Enquete(
            id = 1,
            nomCreateur = "admin_prof",
            nom = "Enquête 1",
            description = "Description 1",
            difficulteIntermediare = 2,
            difficulteDificile = 3,
            mld = "MLD 1",
            solution = "Solution 1",
            indice = "Indice 1",
            nomDatabase = "DB1"
        ),
        Enquete(
            id = 2,
            nomCreateur = "admin_prof",
            nom = "Enquête 2",
            description = "Description 2",
            difficulteIntermediare = 3,
            difficulteDificile = 4,
            mld = "MLD 2",
            solution = "Solution 2",
            indice = "Indice 2",
            nomDatabase = "DB2"
        ),
        Enquete(
            id = 3,
            nomCreateur = "admin_prof",
            nom = "Enquête 3",
            description = "Description 3",
            difficulteIntermediare = 4,
            difficulteDificile = 5,
            mld = "MLD 3",
            solution = "Solution 3",
            indice = "Indice 3",
            nomDatabase = "DB3"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `initialisation doit charger les enquêtes`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(mockEnquetes.size)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))

        viewModel = EnqueteListViewModel(repository)

        assertEquals(mockEnquetes, viewModel.enquetes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadEnquetes doit mettre à jour l'état correctement`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(mockEnquetes.size)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))
        viewModel = EnqueteListViewModel(repository)

        val field = EnqueteListViewModel::class.java.getDeclaredField("_enquetes")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(emptyList<Enquete>()))

        viewModel.loadEnquetes()

        assertEquals(mockEnquetes, viewModel.enquetes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadEnquetes doit gérer les erreurs correctement`() = runTest {
        val errorMessage = "Erreur réseau"
        whenever(repository.getTotalEnquetesCount()).thenReturn(mockEnquetes.size)
        whenever(repository.getEnquetes(0, 3)).thenThrow(RuntimeException(errorMessage))
        viewModel = EnqueteListViewModel(repository)

        viewModel.loadEnquetes()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `nextPage doit incrémenter la page et charger les enquêtes`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(6)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))

        val page2Enquetes = listOf(
            Enquete(
                id = 4,
                nomCreateur = "admin_prof",
                nom = "Enquête 4",
                description = "Description 4",
                difficulteIntermediare = 2,
                difficulteDificile = 3,
                mld = "MLD 4",
                solution = "Solution 4",
                indice = "Indice 4",
                nomDatabase = "DB4"
            ),
            Enquete(
                id = 5,
                nomCreateur = "admin_prof",
                nom = "Enquête 5",
                description = "Description 5",
                difficulteIntermediare = 3,
                difficulteDificile = 4,
                mld = "MLD 5",
                solution = "Solution 5",
                indice = "Indice 5",
                nomDatabase = "DB5"
            ),
            Enquete(
                id = 6,
                nomCreateur = "admin_prof",
                nom = "Enquête 6",
                description = "Description 6",
                difficulteIntermediare = 4,
                difficulteDificile = 5,
                mld = "MLD 6",
                solution = "Solution 6",
                indice = "Indice 6",
                nomDatabase = "DB6"
            )
        )

        whenever(repository.getEnquetes(3, 3)).thenReturn(flowOf(page2Enquetes))
        viewModel = EnqueteListViewModel(repository)

        viewModel.nextPage()

        assertEquals(1, viewModel.currentPage.value)
        assertEquals(page2Enquetes, viewModel.enquetes.value)
    }

    @Test
    fun `previousPage doit décrémenter la page et charger les enquêtes`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(6)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))
        val page2Enquetes = listOf(
            Enquete(
                id = 4,
                nomCreateur = "admin_prof",
                nom = "Enquête 4",
                description = "Description 4",
                difficulteIntermediare = 2,
                difficulteDificile = 3,
                mld = "MLD 4",
                solution = "Solution 4",
                indice = "Indice 4",
                nomDatabase = "DB4"
            )
        )
        whenever(repository.getEnquetes(3, 3)).thenReturn(flowOf(page2Enquetes))

        viewModel = EnqueteListViewModel(repository)
        viewModel.nextPage()

        viewModel.previousPage()

        assertEquals(0, viewModel.currentPage.value)
    }

    @Test
    fun `previousPage ne doit pas décrémenter en dessous de 0`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(mockEnquetes.size)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))
        viewModel = EnqueteListViewModel(repository)
        assertEquals(0, viewModel.currentPage.value)

        viewModel.previousPage()

        assertEquals(0, viewModel.currentPage.value)
    }

    @Test
    fun `refreshData doit réinitialiser la page et recharger les enquêtes`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(6)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))
        val page2Enquetes = listOf(
            Enquete(
                id = 4,
                nomCreateur = "admin_prof",
                nom = "Enquête 4",
                description = "Description 4",
                difficulteIntermediare = 2,
                difficulteDificile = 3,
                mld = "MLD 4",
                solution = "Solution 4",
                indice = "Indice 4",
                nomDatabase = "DB4"
            )
        )
        whenever(repository.getEnquetes(3, 3)).thenReturn(flowOf(page2Enquetes))

        viewModel = EnqueteListViewModel(repository)
        viewModel.nextPage()
        assertEquals(1, viewModel.currentPage.value)

        viewModel.refreshData()

        assertEquals(0, viewModel.currentPage.value)
    }

    @Test
    fun `authenticate doit appeler login sur repository`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(mockEnquetes.size)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))
        whenever(repository.login("admin@sqluedo.com", "Admin123!")).thenReturn(true)

        viewModel = EnqueteListViewModel(repository)

        viewModel.authenticate()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `authenticate doit gérer l'échec de connexion`() = runTest {
        whenever(repository.getTotalEnquetesCount()).thenReturn(mockEnquetes.size)
        whenever(repository.getEnquetes(0, 3)).thenReturn(flowOf(mockEnquetes))
        whenever(repository.login("admin@sqluedo.com", "Admin123!")).thenReturn(false)

        viewModel = EnqueteListViewModel(repository)

        viewModel.authenticate()

        assertEquals("Échec de l'authentification", viewModel.error.value)
    }
}