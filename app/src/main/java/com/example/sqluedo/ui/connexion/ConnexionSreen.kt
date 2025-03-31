package com.example.sqluedo.ui.connexion

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sqluedo.R
import com.example.sqluedo.ViewModel.ConnexionState
import com.example.sqluedo.ViewModel.UserConnexionViewModel

@Composable
fun ConnectionScreen(
    goHome: () -> Unit,
    goInscription: () -> Unit,
    goInformations: () -> Unit,
    viewModel: UserConnexionViewModel
) {
    val connexionState by viewModel.connexionState.collectAsState()

    LaunchedEffect(connexionState) {
        if (connexionState is ConnexionState.Success) {
            goInformations()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        AffichageEnTete(goHome)
        Spacer(modifier = Modifier.height(60.dp))

        // Afficher un message d'erreur si nécessaire
        when (connexionState) {
            is ConnexionState.Error -> {
                Text(
                    text = (connexionState as ConnexionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            else -> {}
        }

        AffichageFields(viewModel)
        Spacer(modifier = Modifier.height(80.dp))
        AffichageBoutonsConnexion(goInscription, viewModel)
    }
}

@Composable
fun AffichageEnTete(goHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.connexion),
                contentDescription = stringResource(id = R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun AffichageFields(viewModel: UserConnexionViewModel) {
    val connexionState by viewModel.connexionState.collectAsState()
    val isLoading = connexionState is ConnexionState.Loading

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(id = R.string.nom)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.mdp)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading,
            singleLine = true
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        } else {
            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.SeConnecter))
            }
        }

        LaunchedEffect(username, password) {
            viewModel.resetState()
        }
    }
}

@Composable
fun AffichageBoutonsConnexion(
    goInscription: () -> Unit,
    viewModel: UserConnexionViewModel
) {
    val connexionState by viewModel.connexionState.collectAsState()
    val isLoading = connexionState is ConnexionState.Loading

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Vous n'avez pas de compte ?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        OutlinedButton(
            onClick = goInscription,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(text = stringResource(id = R.string.NouveauCompte))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    MaterialTheme {
        ConnectionScreen(
            goHome = {},
            goInscription = {},
            goInformations = {},
            viewModel = UserConnexionViewModel()
        )
    }
}