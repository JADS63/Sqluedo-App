package com.sqluedo

import com.sqluedo.ViewModel.CreateGroupeViewModel
import com.sqluedo.ViewModel.GroupeCreationState
import com.sqluedo.data.repository.GroupeRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CreateGroupeViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var repository: GroupeRepository
    private lateinit var viewModel: CreateGroupeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = CreateGroupeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `createGroupe doit valider les champs d'entrée`() = runTest {
        viewModel.createGroupe("", "CODE123", "creator")
        assertTrue(viewModel.creationState.value is GroupeCreationState.Error)

        viewModel.createGroupe("Groupe Test", "", "creator")
        assertTrue(viewModel.creationState.value is GroupeCreationState.Error)

        viewModel.createGroupe("Groupe Test", "CODE123", "")
        assertTrue(viewModel.creationState.value is GroupeCreationState.Error)
    }

    @Test
    fun `createGroupe doit afficher l'état de chargement puis succès lors d'une création réussie`() = runTest {
        val nomGroupe = "Groupe Test"
        val code = "CODE123"
        val nomCreateur = "creator"
        val successMessage = "Groupe créé avec succès"

        whenever(repository.createGroupe(nomGroupe, code, nomCreateur))
            .thenReturn(Result.success(successMessage))

        whenever(repository.joinGroupe(nomGroupe, nomCreateur))
            .thenReturn(Result.success("Utilisateur a rejoint le groupe"))

        viewModel.createGroupe(nomGroupe, code, nomCreateur)

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.creationState.value is GroupeCreationState.Success)
        val successState = viewModel.creationState.value as GroupeCreationState.Success
        assertEquals(nomGroupe, successState.nomGroupe)
        assertTrue(successState.message.contains("succès"))
    }

    @Test
    fun `createGroupe doit gérer les erreurs du repository`() = runTest {
        val nomGroupe = "Groupe Test"
        val code = "CODE123"
        val nomCreateur = "creator"
        val errorMessage = "Erreur lors de la création du groupe"

        whenever(repository.createGroupe(nomGroupe, code, nomCreateur))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.createGroupe(nomGroupe, code, nomCreateur)

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.creationState.value is GroupeCreationState.Error)
        val errorState = viewModel.creationState.value as GroupeCreationState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `createGroupe doit gérer les erreurs de jointure après création réussie`() = runTest {
        val nomGroupe = "Groupe Test"
        val code = "CODE123"
        val nomCreateur = "creator"
        val successMessage = "Groupe créé avec succès"
        val joinErrorMessage = "Erreur lors de la tentative de rejoindre le groupe"

        whenever(repository.createGroupe(nomGroupe, code, nomCreateur))
            .thenReturn(Result.success(successMessage))

        whenever(repository.joinGroupe(nomGroupe, nomCreateur))
            .thenReturn(Result.failure(Exception(joinErrorMessage)))

        viewModel.createGroupe(nomGroupe, code, nomCreateur)

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.creationState.value is GroupeCreationState.Success)
        val successState = viewModel.creationState.value as GroupeCreationState.Success
        assertEquals(nomGroupe, successState.nomGroupe)
        assertTrue(successState.message.contains("erreur"))
    }

    @Test
    fun `createGroupe doit gérer les exceptions pendant l'opération de jointure`() = runTest {
        val nomGroupe = "Groupe Test"
        val code = "CODE123"
        val nomCreateur = "creator"
        val successMessage = "Groupe créé avec succès"

        whenever(repository.createGroupe(nomGroupe, code, nomCreateur))
            .thenReturn(Result.success(successMessage))

        whenever(repository.joinGroupe(nomGroupe, nomCreateur))
            .thenThrow(RuntimeException("Test exception"))

        viewModel.createGroupe(nomGroupe, code, nomCreateur)

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.creationState.value is GroupeCreationState.Success)
        val successState = viewModel.creationState.value as GroupeCreationState.Success
        assertTrue(successState.message.contains("erreur"))
    }

    @Test
    fun `resetState doit définir l'état à Initial`() {
        viewModel.createGroupe("", "CODE123", "creator")
        assertTrue(viewModel.creationState.value is GroupeCreationState.Error)

        viewModel.resetState()

        assertTrue(viewModel.creationState.value is GroupeCreationState.Initial)
    }

    @Test
    fun `Factory doit créer une instance correcte de ViewModel`() {
        val factory = CreateGroupeViewModel.Factory(repository)
        val createdViewModel = factory.create(CreateGroupeViewModel::class.java)
        assertTrue(createdViewModel is CreateGroupeViewModel)
    }
}