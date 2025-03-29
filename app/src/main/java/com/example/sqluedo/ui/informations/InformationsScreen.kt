package com.example.sqluedo.ui.informations

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sqluedo.R
import com.example.sqluedo.data.model.Statistiques
import com.example.sqluedo.data.model.Utilisateur
import com.example.sqluedo.data.model.Stub

@Composable
fun InformationsScreen(
    user: Utilisateur,
    stat: Statistiques,
    goHome: () -> Unit,
    goLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête avec bouton de retour et déconnexion
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

            // Bouton de déconnexion
            TextButton(
                onClick = goLogout,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Déconnexion",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        InformationFields(user)

        Spacer(modifier = Modifier.height(32.dp))

        StatistiquesSection(stat)

        Spacer(modifier = Modifier.height(64.dp))

        GroupButtons()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationFields(user: Utilisateur) {
    var nom by remember { mutableStateOf(user.nomUtilisateur) }
    var groupe by remember { mutableStateOf(user.nomGroupe?.nom ?: "") }

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
fun StatistiquesSection(stat: Statistiques) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.titre_statistiques),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .size(180.dp)
                    .padding(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.connexion), // Remplacez par votre icône de médaille
                        contentDescription = stringResource(id = R.string.badge_reussite),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.text_reussite),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(id = R.string.pourcentage_reussite),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun GroupButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { /* Action pour quitter groupe */ },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_quitter_groupe),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = { /* Action pour rejoindre groupe */ },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_rejoindre_groupe),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = { /* Action pour créer groupe */ },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_creer_groupe),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
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