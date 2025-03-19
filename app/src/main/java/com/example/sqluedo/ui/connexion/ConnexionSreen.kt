package com.example.sqluedo.ui.connexion

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sqluedo.R

@Composable
fun ConnectionScreen(
    goHome: () -> Unit,
    goInscription: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        affichageEnTete(goHome)
        Spacer(modifier = Modifier.height(100.dp))
        affichageLabel()
        Spacer(modifier = Modifier.height(100.dp))
        affichageBoutonsConnexion(goInscription)
    }
}

@Composable
fun affichageEnTete(goHome: () -> Unit) {
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
        Spacer(modifier = Modifier.height(100.dp))
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
fun affichageLabel() {
    var nom by remember { mutableStateOf("") }
    var mdp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text(stringResource(id = R.string.nom)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = mdp,
            onValueChange = { mdp = it },
            label = { Text(stringResource(id = R.string.mdp)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun affichageBoutonsConnexion(goInscription: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = goInscription,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.NouveauCompte))
        }
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.SeConnecter))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    MaterialTheme {
        ConnectionScreen(
            goHome = {},
            goInscription = {}
        )
    }
}