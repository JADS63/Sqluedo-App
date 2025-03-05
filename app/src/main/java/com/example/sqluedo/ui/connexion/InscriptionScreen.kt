package com.example.sqluedo.ui.connexion

import android.telecom.Connection
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sqluedo.R

@Composable
fun InscriptionScreen(
    goConnection: ()->Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        affichageEnTete(goConnection)
        Text(
            text = "Nouveau Compte",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(100.dp))

        affichageLabel()
        Spacer(modifier = Modifier.height(100.dp))
        affichageBoutonsCreation()
    }
}

@Composable
fun affichageBoutonsCreation(){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.CreerCompte))
        }
    }
}
