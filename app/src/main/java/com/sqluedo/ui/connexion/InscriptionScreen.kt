package com.sqluedo.ui.connexion

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.InscriptionState
import com.sqluedo.ViewModel.UserInscriptionViewModel

@Composable
fun InscriptionScreen(
    goConnection: () -> Unit,
    viewModel: UserInscriptionViewModel = viewModel()
) {
    val inscriptionState by viewModel.inscriptionState.collectAsState()

    var nom by remember { mutableStateOf("") }
    var mdp by remember { mutableStateOf("") }
    var confirmMdp by remember { mutableStateOf("") }

    LaunchedEffect(inscriptionState) {
        if (inscriptionState is InscriptionState.Success) {
            goConnection()
        }
    }

    LaunchedEffect(inscriptionState) {
        println("État actuel de l'inscription: $inscriptionState")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        AffichageEnTeteInscription(goConnection)

        Text(
            text = stringResource(id = R.string.titre_nouveau_compte),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(50.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            when (inscriptionState) {
                is InscriptionState.Error -> {
                    Text(
                        text = (inscriptionState as InscriptionState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is InscriptionState.UserExists -> {
                    Text(
                        text = "Le nom d'utilisateur '${(inscriptionState as InscriptionState.UserExists).username}' est déjà pris",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is InscriptionState.UserAvailable -> {
                    Text(
                        text = "Le nom d'utilisateur '${(inscriptionState as InscriptionState.UserAvailable).username}' est disponible",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is InscriptionState.Loading -> {
                    Text(
                        text = stringResource(id = R.string.Chargement),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nom,
                onValueChange = {
                    nom = it
                    viewModel.resetState()
                },
                label = { Text(stringResource(id = R.string.nom)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = inscriptionState !is InscriptionState.Loading,
                singleLine = true,
                trailingIcon = {
                    if (nom.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.checkUserExists(nom)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu),
                                contentDescription = stringResource(id = R.string.mdp)
                            )
                        }
                    }
                }
            )

            OutlinedTextField(
                value = mdp,
                onValueChange = {
                    mdp = it
                    viewModel.resetState()
                },
                label = { Text(stringResource(id = R.string.mdp)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = inscriptionState !is InscriptionState.Loading,
                singleLine = true
            )

            val passwordsMatch = mdp == confirmMdp
            val showPasswordError = mdp.isNotEmpty() && confirmMdp.isNotEmpty() && !passwordsMatch

            OutlinedTextField(
                value = confirmMdp,
                onValueChange = {
                    confirmMdp = it
                    viewModel.resetState()
                },
                label = { Text(stringResource(id = R.string.connfirmermdp)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = inscriptionState !is InscriptionState.Loading,
                isError = showPasswordError,
                singleLine = true
            )

            if (showPasswordError) {
                Text(
                    text = stringResource(id = R.string.mdppasegaux),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isFormValid = nom.isNotBlank() && mdp.isNotBlank() && confirmMdp.isNotBlank() && mdp == confirmMdp
            val isLoading = inscriptionState is InscriptionState.Loading

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            } else {
                Button(
                    onClick = {
                        println("Tentative d'inscription avec: nom=$nom, mdp=$mdp")
                        viewModel.registerUser(nom, mdp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !isLoading
                ) {
                    Text(text = stringResource(id = R.string.CreerCompte))
                }
            }

            TextButton(
                onClick = goConnection,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(id = R.string.compteexistant))
            }
        }
    }
}

@Composable
fun AffichageEnTeteInscription(goConnection: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = goConnection,
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

@Preview(showBackground = true)
@Composable
fun InscriptionScreenPreview() {
    MaterialTheme {
        InscriptionScreen(
            goConnection = {}
        )
    }
}