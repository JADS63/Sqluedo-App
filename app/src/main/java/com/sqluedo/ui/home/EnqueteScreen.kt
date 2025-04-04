package com.sqluedo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sqluedo.R
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Stub
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.service.createCodeFirstService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


@Composable
fun EnqueteScreen(
    goHome: () -> Unit,
    goJeu: () -> Unit,
    goConnection: () -> Unit,
    enquete: Enquete
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TopBar moderne avec actions
            TopAppBar(
                goHome = goHome,
                goConnection = goConnection
            )

            // Contenu principal avec défilement
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EnqueteHeader(enquete = enquete)

                Spacer(modifier = Modifier.height(24.dp))

                EnqueteDescription(enquete = enquete)

                Spacer(modifier = Modifier.height(16.dp))

                EnqueteSchemaData(enquete = enquete)
            }

            // Bouton d'action principal
            EnqueteActionButton(
                goJeu = goJeu
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    goHome: () -> Unit,
    goConnection: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = goHome) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.btn_retour)
                )
            }
        },
        actions = {
            IconButton(onClick = goConnection) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.btn_reessayer)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun EnqueteHeader(enquete: Enquete) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = enquete.nom,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Créée par ${enquete.nomCreateur}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyChip(
                label = "Intermédiaire: ${enquete.difficulteIntermediare}/10",
                color = MaterialTheme.colorScheme.tertiary
            )

            DifficultyChip(
                label = "Difficile: ${enquete.difficulteDificile}/10",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun DifficultyChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EnqueteDescription(enquete: Enquete) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.titre_description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = enquete.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EnqueteSchemaData(enquete: Enquete) {
    var showDatabaseDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Schéma de base de données",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDatabaseDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voir les tables")
            }

            Spacer(modifier = Modifier.height(8.dp))

//            // Indice sous forme d'élément extensible
//            OutlinedButton(
//                onClick = { expanded = !expanded },
//                modifier = Modifier.align(Alignment.End),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = MaterialTheme.colorScheme.primary
//                )
//            ) {
//                Text(text = if (expanded) "Masquer l'indice" else "Afficher l'indice")
//            }
//
//            if (expanded) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        text = enquete.indice,
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.padding(12.dp),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
        }
    }

    if (showDatabaseDialog) {
        MldDialog(
            enquete = enquete,
            onDismiss = { showDatabaseDialog = false }
        )
    }
}

@Composable
fun MldDialog(
    enquete: Enquete,
    onDismiss: () -> Unit
) {
    val repository = EnqueteRepository(createCodeFirstService())

    // État pour stocker les tables de la base de données actuelle
    var tables by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Charger les tables de la base de données au démarrage
    LaunchedEffect(enquete.nomDatabase) {
        isLoading = true
        error = null

        try {
            // S'assurer que nous avons un token d'authentification
            if (repository.getAuthToken() == null) {
                repository.login("admin@sqluedo.com", "Admin123!")
            }

            // Exécuter la requête pour obtenir les tables
            val service = createCodeFirstService()
            val requestBody = JSONObject().apply {
                put("databaseName", enquete.nomDatabase)
                put("sqlQuery", "SELECT table_name FROM information_schema.tables WHERE table_schema = '${enquete.nomDatabase}';")
            }.toString().toRequestBody("application/json".toMediaType())

            val response = repository.getAuthToken()?.let { token ->
                service.executeQuery(requestBody, token)
            }

            // Parser la réponse
            if (response != null) {
                val responseJson = JSONObject(response.string())
                if (responseJson.getBoolean("success")) {
                    val dataArray = responseJson.getJSONArray("data")
                    val tableList = mutableListOf<String>()

                    for (i in 0 until dataArray.length()) {
                        val tableObj = dataArray.getJSONObject(i)
                        tableList.add(tableObj.getString("table_name"))
                    }

                    tables = tableList
                } else {
                    error = "Impossible de récupérer la structure de la base de données"
                }
            }
        } catch (e: Exception) {
            error = "Erreur: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modèle Logique de Données (MLD)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Text(
                    text = "Base de données: ${enquete.nomDatabase}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    Text(
                        text = "Erreur lors du chargement des tables: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // Liste simple des tables
                    Text(
                        text = "Tables disponibles:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            tables.forEach { tableName ->
                                Text(
                                    text = "• $tableName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun EnqueteActionButton(goJeu: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = goJeu,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.btn_jouer),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnqueteScreenPreview() {
    MaterialTheme {
        EnqueteScreen(
            goHome = {},
            goJeu = {},
            goConnection = {},
            enquete = Stub.enquetes.first()
        )
    }
}