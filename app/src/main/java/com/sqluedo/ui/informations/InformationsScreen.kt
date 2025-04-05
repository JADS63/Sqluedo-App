package com.sqluedo.ui.informations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.CreateGroupeViewModel
import com.sqluedo.ViewModel.GroupeCreationState
import com.sqluedo.ViewModel.GroupeDetailViewModel
import com.sqluedo.ViewModel.MembershipState
import com.sqluedo.data.model.Statistiques
import com.sqluedo.data.model.Utilisateur
import com.sqluedo.data.model.Stub
import com.sqluedo.data.repository.GroupeRepository
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InformationsScreen(
    user: Utilisateur,
    stat: Statistiques,
    goHome: () -> Unit,
    goLogout: () -> Unit
) {
    val service = createCodeFirstService()
    val groupeRepository = remember { GroupeRepository(service) }
    val utilisateurRepository = remember { UtilisateurRepository(service) }

    val createGroupeViewModel = viewModel { CreateGroupeViewModel(groupeRepository) }
    val groupeDetailViewModel = viewModel {
        GroupeDetailViewModel(groupeRepository, utilisateurRepository)
    }

    var utilisateur by remember { mutableStateOf(user) }
    val coroutineScope = rememberCoroutineScope()

    val refreshUserInfo = {
        coroutineScope.launch {
            try {
                delay(500)
                utilisateurRepository.getUserByName(user.nomUtilisateur).collect { updatedUser ->
                    if (updatedUser != null) {
                        utilisateur = updatedUser
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors du rafraîchissement des informations utilisateur: ${e.message}")
            }
        }
    }

    val showJoinDialog = remember { mutableStateOf(false) }
    val showCreateDialog = remember { mutableStateOf(false) }
    val groupeToJoin = remember { mutableStateOf("") }
    val showSuccessMessage = remember { mutableStateOf(false) }
    val successMessage = remember { mutableStateOf("") }
    val showErrorMessage = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    val membershipState by groupeDetailViewModel.membershipState.collectAsState()
    val creationState by createGroupeViewModel.creationState.collectAsState()

    LaunchedEffect(membershipState) {
        when (membershipState) {
            is MembershipState.Member -> {
                successMessage.value = "Vous êtes maintenant membre du groupe."
                showSuccessMessage.value = true
                refreshUserInfo()
            }
            is MembershipState.NotMember -> {
                successMessage.value = "Vous avez quitté le groupe."
                showSuccessMessage.value = true
                refreshUserInfo()
            }
            else -> {}
        }
    }

    val errorState by groupeDetailViewModel.error.collectAsState()
    LaunchedEffect(errorState) {
        if (!errorState.isNullOrEmpty()) {
            errorMessage.value = errorState ?: "Une erreur s'est produite"
            showErrorMessage.value = true
            groupeDetailViewModel.resetError()
        }
    }

    LaunchedEffect(creationState) {
        when (creationState) {
            is GroupeCreationState.Success -> {
                val message = (creationState as GroupeCreationState.Success).message
                successMessage.value = message
                showSuccessMessage.value = true
                showCreateDialog.value = false
                refreshUserInfo()
            }
            is GroupeCreationState.Error -> {
                errorMessage.value = (creationState as GroupeCreationState.Error).message
                showErrorMessage.value = true
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = goHome,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.retour),
                    contentDescription = stringResource(id = R.string.btn_retour),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            TextButton(
                onClick = goLogout,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.deco),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (showSuccessMessage.value) {
            AlertCard(
                message = successMessage.value,
                color = Color(0xFFDFF0D8),
                textColor = Color(0xFF3C763D),
                onDismiss = { showSuccessMessage.value = false }
            )
        }

        if (showErrorMessage.value) {
            AlertCard(
                message = errorMessage.value,
                color = Color(0xFFF2DEDE),
                textColor = Color(0xFFA94442),
                onDismiss = { showErrorMessage.value = false }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        InformationFields(utilisateur)

        Spacer(modifier = Modifier.height(32.dp))


        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { refreshUserInfo() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(id = R.string.refresh))
        }

        Spacer(modifier = Modifier.height(16.dp))

        GroupButtons(
            hasGroup = utilisateur.nomGroupe != null,
            onJoinGroup = { showJoinDialog.value = true },
            onLeaveGroup = {
                utilisateur.nomGroupe?.let { groupe ->
                    groupeDetailViewModel.leaveGroupe(groupe.nom, utilisateur.nomUtilisateur)
                }
            },
            onCreateGroup = { showCreateDialog.value = true }
        )
    }

    if (showJoinDialog.value) {
        JoinGroupDialog(
            onDismiss = { showJoinDialog.value = false },
            onJoin = { nomGroupe ->
                groupeDetailViewModel.joinGroupe(nomGroupe, utilisateur.nomUtilisateur)
                showJoinDialog.value = false
            }
        )
    }

    if (showCreateDialog.value) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog.value = false },
            onCreate = { nom, code ->
                createGroupeViewModel.createGroupe(nom, code, utilisateur.nomUtilisateur)
            },
            isLoading = creationState is GroupeCreationState.Loading
        )
    }
}

@Composable
fun AlertCard(
    message: String,
    color: Color,
    textColor: Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Text(
                    text = "×",
                    fontSize = 24.sp,
                    color = textColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationFields(user: Utilisateur) {
    var nom by remember { mutableStateOf(user.nomUtilisateur) }
    var groupe by remember { mutableStateOf(user.nomGroupe?.nom ?: "") }

    LaunchedEffect(user) {
        nom = user.nomUtilisateur
        groupe = user.nomGroupe?.nom ?: ""
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text(stringResource(id = R.string.label_nom)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledBorderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTextColor = Color.DarkGray
            )
        )

        OutlinedTextField(
            value = groupe,
            onValueChange = { groupe = it },
            label = { Text(stringResource(id = R.string.label_groupe)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledBorderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTextColor = Color.DarkGray
            )
        )
    }
}
@Composable
fun GroupButtons(
    hasGroup: Boolean,
    onJoinGroup: () -> Unit,
    onLeaveGroup: () -> Unit,
    onCreateGroup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onLeaveGroup,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp),
            enabled = hasGroup
        ) {
            Text(
                text = stringResource(id = R.string.btn_quitter_groupe),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = onJoinGroup,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp),
            enabled = !hasGroup
        ) {
            Text(
                text = stringResource(id = R.string.btn_rejoindre_groupe),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = onCreateGroup,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp),
            enabled = !hasGroup
        ) {
            Text(
                text = stringResource(id = R.string.btn_creer_groupe),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var nomGroupe by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.btn_rejoindre_groupe),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nomGroupe,
                    onValueChange = { nomGroupe = it },
                    label = { Text(stringResource(id = R.string.nomgroupe)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(id = R.string.annuler))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onJoin(nomGroupe) },
                        enabled = nomGroupe.isNotBlank()
                    ) {
                        Text(stringResource(id = R.string.btn_rejoindre_groupe))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    isLoading: Boolean
) {
    var nomGroupe by remember { mutableStateOf("") }
    var codeGroupe by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.btn_creer_groupe),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nomGroupe,
                    onValueChange = { nomGroupe = it },
                    label = { Text(stringResource(id = R.string.nomgroupe)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = codeGroupe,
                    onValueChange = { codeGroupe = it },
                    label = { Text(stringResource(id = R.string.code)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(stringResource(id = R.string.refresh))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onCreate(nomGroupe, codeGroupe) },
                        enabled = nomGroupe.isNotBlank() && codeGroupe.isNotBlank() && !isLoading
                    ) {
                        Text(stringResource(id = R.string.CreerCompte))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InformationsScreenPreview() {
    val utilisateur = Stub.utilisateur1
    val statistiques = Stub.statistiques

    MaterialTheme {
        InformationsScreen(
            user = utilisateur,
            stat = statistiques,
            goHome = { },
            goLogout = { }
        )
    }
}