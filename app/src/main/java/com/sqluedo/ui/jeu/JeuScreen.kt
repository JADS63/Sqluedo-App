package com.sqluedo.ui.jeu

import android.content.ClipData
import android.content.ClipDescription
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.EnqueteResultViewModel
import com.sqluedo.ViewModel.PlayQueryViewModel
import com.sqluedo.ViewModel.QueryResult
import com.sqluedo.ViewModel.VerificationResult
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Stub
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.delay
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

@Composable
fun JeuScreen(
    goHome: () -> Unit,
    goResultat: () -> Unit,
    enquete: Enquete
) {
    // Création des ViewModels
    val repository = EnqueteRepository(createCodeFirstService())
    val queryViewModel = remember { PlayQueryViewModel(enquete, repository) }
    val resultViewModel = remember { EnqueteResultViewModel(enquete, repository) }

    // Observation des états des ViewModels
    val isLoadingQuery by queryViewModel.isLoading.collectAsState()
    val queryResult by queryViewModel.queryResult.collectAsState()
    val queryErrorMessage by queryViewModel.errorMessage.collectAsState()

    val isLoadingResult by resultViewModel.isLoading.collectAsState()
    val verificationResult by resultViewModel.verificationResult.collectAsState()
    val resultErrorMessage by resultViewModel.errorMessage.collectAsState()

    var requeteSQL by remember { mutableStateOf("") }
    var reponseText by remember { mutableStateOf("") }

    // Effet pour naviguer vers l'écran de résultat si la réponse est correcte
    LaunchedEffect(verificationResult) {
        if (verificationResult?.isCorrect == true) {
            // Attendre un court instant pour montrer le feedback de succès
            delay(1500)
            goResultat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barre d'outils en haut
        TopBar(goHome)

        Spacer(modifier = Modifier.height(24.dp))

        // Zone principale
        MainContent(
            requeteSQL = requeteSQL,
            onRequeteChanged = { requeteSQL = it },
            onValiderRequete = { queryViewModel.executeQuery(requeteSQL) },
            reponseText = reponseText,
            onReponseChanged = { reponseText = it },
            onValiderReponse = {
                resultViewModel.verifyAnswer(reponseText)
            },
            isLoadingQuery = isLoadingQuery,
            queryResult = queryResult,
            queryErrorMessage = queryErrorMessage,
            isLoadingResult = isLoadingResult,
            verificationResult = verificationResult
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Boutons d'aide SQL à droite
        SqlHelpButtons()
    }
}

@Composable
fun TopBar(goHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton retour
        IconButton(onClick = goHome) {
            Image(
                painter = painterResource(id = R.drawable.retour),
                contentDescription = stringResource(id = R.string.btn_retour),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Row {
            IconButton(onClick = { /* Action base de données */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Base de données",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = { /* Action profil */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.connexion),
                    contentDescription = "Profil",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    requeteSQL: String,
    onRequeteChanged: (String) -> Unit,
    onValiderRequete: () -> Unit,
    reponseText: String,
    onReponseChanged: (String) -> Unit,
    onValiderReponse: () -> Unit,
    isLoadingQuery: Boolean,
    queryResult: QueryResult?,
    queryErrorMessage: String?,
    isLoadingResult: Boolean,
    verificationResult: VerificationResult?
) {
    var txt by remember {
        mutableStateOf(
            TextFieldValue(
                text = requeteSQL,
                selection = TextRange(requeteSQL.length)
            )
        )
    }

    val callback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val clipData = event.toAndroidDragEvent().clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val draggedText = clipData.getItemAt(0).text.toString()
                    val newText = txt.text + " " + draggedText
                    txt = TextFieldValue(
                        text = newText,
                        selection = TextRange(newText.length)
                    )
                    onRequeteChanged(newText)
                }
                return true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Requêtes SQL
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Requêtes SQL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { event ->
                                event
                                    .mimeTypes()
                                    .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            }, target = callback
                        )
                ) {
                    OutlinedTextField(
                        value = txt,
                        onValueChange = {
                            txt = it
                            onRequeteChanged(it.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = { Text("Écrivez votre requête SQL ici...") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onValiderRequete,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !isLoadingQuery && requeteSQL.isNotBlank()
                    ) {
                        if (isLoadingQuery) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.btn_valider))
                    }
                }
            }
        }

        // Section Tableau de réponse
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .weight(1f),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Résultats de la requête",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Affichage des résultats
                when {
                    isLoadingQuery -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    queryErrorMessage != null -> {
                        ErrorMessage(queryErrorMessage)
                    }
                    queryResult != null -> {
                        if (queryResult.success && queryResult.rows.isNotEmpty()) {
                            ResultTable(queryResult)
                        } else if (queryResult.success && queryResult.rowCount == 0) {
                            EmptyResultMessage()
                        } else {
                            ErrorMessage(queryResult.errorMessage ?: "Erreur inconnue")
                        }
                    }
                    else -> {
                        EmptyStateMessage()
                    }
                }
            }
        }

        // Section Réponse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Champ de réponse
            OutlinedTextField(
                value = reponseText,
                onValueChange = onReponseChanged,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(id = R.string.label_reponse)) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = when {
                        verificationResult?.isCorrect == true -> Color.Green
                        verificationResult?.isCorrect == false -> Color.Red
                        else -> MaterialTheme.colorScheme.primary
                    }
                ),
                isError = verificationResult?.isCorrect == false
            )

            Button(
                onClick = onValiderReponse,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        verificationResult?.isCorrect == true -> Color(0xFF4CAF50)
                        isLoadingResult -> Color.Gray
                        else -> Color.DarkGray
                    }
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                enabled = !isLoadingResult && reponseText.isNotBlank()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoadingResult) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (verificationResult?.isCorrect == true) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = when {
                            verificationResult?.isCorrect == true -> "Correct !"
                            isLoadingResult -> "Vérification..."
                            else -> stringResource(id = R.string.btn_valider)
                        }
                    )
                }
            }
        }
        println("DEBUG - verificationResult: $verificationResult")
        println("DEBUG - isLoadingResult: $isLoadingResult")
        // Afficher un message de feedback pour la vérification
        verificationResult?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (it.isCorrect) Color(0xFFE0F7E6) else Color(0xFFFDE8E8)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (it.isCorrect) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (it.isCorrect) Color.Green else Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = it.message,
                        color = if (it.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ResultTable(queryResult: QueryResult) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // En-tête avec info de nombre de lignes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${queryResult.rowCount} résultat(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Table avec header et rows
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // En-tête de table
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .horizontalScroll(horizontalScrollState)
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    queryResult.columns.forEach { column ->
                        Text(
                            text = column.name,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .width(120.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Divider entre header et contenu
                Divider()

                // Lignes de données
                LazyColumn {
                    items(queryResult.rows) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            queryResult.columns.forEach { column ->
                                val value = row[column.name]?.toString() ?: "NULL"
                                Text(
                                    text = value,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .width(120.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Exécutez une requête SQL pour voir les résultats",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyResultMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Requête exécutée avec succès",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aucun résultat retourné",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Erreur lors de l'exécution de la requête",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SqlHelpButtons() {
    // Liste des commandes SQL à afficher comme boutons d'aide
    val sqlCommands = listOf(
        "SELECT", "FROM", "WHERE", "JOIN", "ON",
        "AND", "ORDER BY", "GROUP BY", "HAVING",
        "INNER JOIN", "LEFT JOIN", "RIGHT JOIN"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Boutons des commandes SQL
            sqlCommands.forEach { command ->
                Box(modifier = Modifier
                    .background(Color.LightGray)
                    .dragAndDropSource {
                        detectTapGestures(onPress = {
                            startTransfer(
                                DragAndDropTransferData(
                                    ClipData.newPlainText(
                                        command, command
                                    ),
                                    flags = View.DRAG_FLAG_GLOBAL
                                )
                            )
                        })
                    }
                    .padding(5.dp)
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color.LightGray))
                    .padding(10.dp)
                ) { Text(command, color = Color.Black) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JeuScreenPreview() {
    MaterialTheme {
        JeuScreen(
            goHome = {},
            goResultat = {},
            enquete = Stub.enquetes.first()
        )
    }
}