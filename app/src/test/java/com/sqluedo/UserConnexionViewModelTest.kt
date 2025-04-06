package com.sqluedo

import com.sqluedo.ViewModel.ConnexionState
import com.sqluedo.ViewModel.UserConnexionViewModel
import com.sqluedo.data.model.Group
import com.sqluedo.data.model.Utilisateur
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.CodeFirstService
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class UserConnexionViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var utilisateurRepository: UtilisateurRepository
    private lateinit var apiService: CodeFirstService
    private lateinit var viewModel: UserConnexionViewModel

    private val mockUser = Utilisateur(
        nomUtilisateur = "testUser",
        nomGroupe = Group(
            nom = "Groupe A",
            code = "GA123",
            nbUtilisateur = 5,
            nomCreator = null
        ),
        mdp = "password",
        role = "Membre"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mock()
        utilisateurRepository = mock()

        viewModel = UserConnexionViewModel()

        val field = UserConnexionViewModel::class.java.getDeclaredField("utilisateurRepository")
        field.isAccessible = true
        field.set(viewModel, utilisateurRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `login doit valider le nom d'utilisateur et le mot de passe`() = runTest {
        viewModel.login("", "password")

        assertTrue(viewModel.connexionState.value is ConnexionState.Error)
        val error = viewModel.connexionState.value as ConnexionState.Error
        assertEquals("Veuillez remplir tous les champs", error.message)

        viewModel.login("username", "")

        assertTrue(viewModel.connexionState.value is ConnexionState.Error)
    }

    @Test
    fun `login doit mettre à jour l'état vers succès quand l'authentification réussit`() = runTest {
        val username = "testUser"
        val password = "password"

        whenever(utilisateurRepository.authenticateUser(username, password))
            .thenReturn(true)
        whenever(utilisateurRepository.getUserByName(username))
            .thenReturn(flowOf(mockUser))

        viewModel.login(username, password)

        assertTrue(viewModel.connexionState.value is ConnexionState.Success)
        verify(utilisateurRepository).setCurrentUser(mockUser)
    }

    @Test
    fun `login doit mettre à jour l'état vers erreur quand l'authentification échoue`() = runTest {
        val username = "testUser"
        val password = "wrongPassword"

        whenever(utilisateurRepository.authenticateUser(username, password))
            .thenReturn(false)

        viewModel.login(username, password)

        assertTrue(viewModel.connexionState.value is ConnexionState.Error)
        assertEquals("Identifiants incorrects", (viewModel.connexionState.value as ConnexionState.Error).message)
    }

    @Test
    fun `checkUserExists doit valider le nom d'utilisateur`() = runTest {
        viewModel.checkUserExists("")

        assertTrue(viewModel.connexionState.value is ConnexionState.Error)
        assertEquals("Veuillez entrer un nom d'utilisateur",
            (viewModel.connexionState.value as ConnexionState.Error).message)
    }

    @Test
    fun `checkUserExists doit mettre à jour l'état correctement quand l'utilisateur existe`() = runTest {
        val username = "existingUser"

        whenever(utilisateurRepository.checkUserExists(username))
            .thenReturn(true)

        viewModel.checkUserExists(username)

        assertTrue(viewModel.connexionState.value is ConnexionState.UserExists)
        assertEquals(username, (viewModel.connexionState.value as ConnexionState.UserExists).username)
    }

    @Test
    fun `checkUserExists doit mettre à jour l'état correctement quand l'utilisateur n'existe pas`() = runTest {
        val username = "nonExistingUser"

        whenever(utilisateurRepository.checkUserExists(username))
            .thenReturn(false)

        viewModel.checkUserExists(username)

        assertTrue(viewModel.connexionState.value is ConnexionState.UserNotFound)
        assertEquals(username, (viewModel.connexionState.value as ConnexionState.UserNotFound).username)
    }

    @Test
    fun `logout doit réinitialiser l'état et appeler logout du repository`() = runTest {
        whenever(utilisateurRepository.authenticateUser(any(), any()))
            .thenReturn(true)
        whenever(utilisateurRepository.getUserByName(any()))
            .thenReturn(flowOf(mockUser))

        viewModel.login("testUser", "password")

        viewModel.logout()

        assertTrue(viewModel.connexionState.value is ConnexionState.Initial)
        assertNull(viewModel.currentUser.value)
        verify(utilisateurRepository).logout()
    }

    @Test
    fun `getCurrentUsername doit déléguer au repository`() {
        val expectedUsername = "testUser"
        whenever(utilisateurRepository.getCurrentUsername())
            .thenReturn(expectedUsername)

        val result = viewModel.getCurrentUsername()

        assertEquals(expectedUsername, result)
        verify(utilisateurRepository).getCurrentUsername()
    }

    @Test
    fun `isUserLoggedIn doit déléguer au repository`() {
        whenever(utilisateurRepository.isUserLoggedIn())
            .thenReturn(true)

        val result = viewModel.isUserLoggedIn()

        assertTrue(result)
        verify(utilisateurRepository).isUserLoggedIn()
    }
}