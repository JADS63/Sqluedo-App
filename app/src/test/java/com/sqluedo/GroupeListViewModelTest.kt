package com.sqluedo

import com.sqluedo.ViewModel.GroupeListViewModel
import com.sqluedo.ViewModel.OperationType
import com.sqluedo.data.model.Group
import com.sqluedo.data.model.Utilisateur
import com.sqluedo.data.repository.GroupeRepository
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class GroupeListViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var repository: GroupeRepository
    private lateinit var viewModel: GroupeListViewModel

    private val mockCreator = Utilisateur(
        nomUtilisateur = "createur1",
        nomGroupe = null,
        mdp = "password",
        role = "Membre"
    )

    private val mockGroupes = listOf(
        Group(
            nom = "Groupe A",
            code = "GA123",
            nbUtilisateur = 5,
            nomCreator = mockCreator
        ),
        Group(
            nom = "Groupe B",
            code = "GB456",
            nbUtilisateur = 3,
            nomCreator = mockCreator
        ),
        Group(
            nom = "Groupe C",
            code = "GC789",
            nbUtilisateur = 7,
            nomCreator = null
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.getGroupes()).thenReturn(flowOf(mockGroupes))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `initialisation doit charger les groupes`() = runTest {
        viewModel = GroupeListViewModel(repository)

        assertEquals(mockGroupes, viewModel.groupes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadGroupes doit mettre à jour l'état correctement`() = runTest {
        viewModel = GroupeListViewModel(repository)

        viewModel.loadGroupes()

        assertEquals(mockGroupes, viewModel.groupes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadGroupes doit gérer les erreurs correctement`() = runTest {
        val errorMessage = "Erreur réseau"
        whenever(repository.getGroupes()).thenThrow(RuntimeException(errorMessage))

        viewModel = GroupeListViewModel(repository)

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `joinGroupe doit mettre à jour l'état en cas de succès`() = runTest {
        val nomGroupe = "Groupe A"
        val nomUtilisateur = "testUser"
        val successMessage = "L'utilisateur a rejoint le groupe avec succès"

        whenever(repository.joinGroupe(nomGroupe, nomUtilisateur))
            .thenReturn(Result.success(successMessage))

        viewModel = GroupeListViewModel(repository)

        viewModel.joinGroupe(nomGroupe, nomUtilisateur)

        assertNotNull(viewModel.operationMessage.value)
        val operationMessage = viewModel.operationMessage.value!!
        assertEquals(OperationType.JOIN, operationMessage.type)
        assertTrue(operationMessage.isSuccess)
        assertEquals(successMessage, operationMessage.message)
    }

    @Test
    fun `joinGroupe doit gérer l'échec`() = runTest {
        val nomGroupe = "Groupe X"
        val nomUtilisateur = "testUser"
        val errorMessage = "Groupe non trouvé"

        whenever(repository.joinGroupe(nomGroupe, nomUtilisateur))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = GroupeListViewModel(repository)

        viewModel.joinGroupe(nomGroupe, nomUtilisateur)

        assertEquals(errorMessage, viewModel.error.value)
        assertNotNull(viewModel.operationMessage.value)
        val operationMessage = viewModel.operationMessage.value!!
        assertEquals(OperationType.JOIN, operationMessage.type)
        assertFalse(operationMessage.isSuccess)
    }

    @Test
    fun `leaveGroupe doit mettre à jour l'état en cas de succès`() = runTest {
        val nomGroupe = "Groupe A"
        val nomUtilisateur = "testUser"
        val successMessage = "L'utilisateur a quitté le groupe avec succès"

        whenever(repository.leaveGroupe(nomGroupe, nomUtilisateur))
            .thenReturn(Result.success(successMessage))

        viewModel = GroupeListViewModel(repository)

        viewModel.leaveGroupe(nomGroupe, nomUtilisateur)

        assertNotNull(viewModel.operationMessage.value)
        val operationMessage = viewModel.operationMessage.value!!
        assertEquals(OperationType.LEAVE, operationMessage.type)
        assertTrue(operationMessage.isSuccess)
        assertEquals(successMessage, operationMessage.message)
    }

    @Test
    fun `leaveGroupe doit gérer l'échec`() = runTest {
        val nomGroupe = "Groupe X"
        val nomUtilisateur = "testUser"
        val errorMessage = "Erreur lors de la tentative de quitter le groupe"

        whenever(repository.leaveGroupe(nomGroupe, nomUtilisateur))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = GroupeListViewModel(repository)

        viewModel.leaveGroupe(nomGroupe, nomUtilisateur)

        assertEquals(errorMessage, viewModel.error.value)
        assertNotNull(viewModel.operationMessage.value)
        val operationMessage = viewModel.operationMessage.value!!
        assertEquals(OperationType.LEAVE, operationMessage.type)
        assertFalse(operationMessage.isSuccess)
    }

    @Test
    fun `clearOperationMessage doit réinitialiser le message d'opération`() = runTest {
        val nomGroupe = "Groupe A"
        val nomUtilisateur = "testUser"
        val successMessage = "L'utilisateur a rejoint le groupe avec succès"

        whenever(repository.joinGroupe(nomGroupe, nomUtilisateur))
            .thenReturn(Result.success(successMessage))

        viewModel = GroupeListViewModel(repository)
        viewModel.joinGroupe(nomGroupe, nomUtilisateur)
        assertNotNull(viewModel.operationMessage.value)

        viewModel.clearOperationMessage()

        assertNull(viewModel.operationMessage.value)
    }

    @Test
    fun `Factory doit créer une instance correcte de ViewModel`() {
        val factory = GroupeListViewModel.Factory(repository)
        val createdViewModel = factory.create(GroupeListViewModel::class.java)
        assertTrue(createdViewModel is GroupeListViewModel)
    }
}