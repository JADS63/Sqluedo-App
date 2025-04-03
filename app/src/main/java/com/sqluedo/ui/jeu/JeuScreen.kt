@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package com.sqluedo.ui.jeu

import android.content.ClipData
import android.content.ClipDescription
import android.view.View
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.EnqueteResultViewModel
import com.sqluedo.ViewModel.JeuBlocViewModel
import com.sqluedo.ViewModel.PlayQueryViewModel
import com.sqluedo.ViewModel.QueryResult
import com.sqluedo.ViewModel.VerificationResult
import com.sqluedo.data.model.BlocSQL
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Stub
import com.sqluedo.data.model.getColorForType
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ---------------------------------------------------------
// Data class pour représenter un bloc de la palette
// ---------------------------------------------------------
data class PuzzleData(val label: String, val color: Color)

// ---------------------------------------------------------
// Composant PuzzleBlock : bloc de puzzle avec forme personnalisée
// ---------------------------------------------------------
@Composable
fun PuzzleBlock(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 120.dp, height = 50.dp)
            .background(color = color, shape = PuzzlePieceShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------
// Forme personnalisée pour simuler une pièce de puzzle
// ---------------------------------------------------------
val PuzzlePieceShape: Shape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        with(density) {
            val notchWidth = 20.dp.toPx()
            val notchHeight = 8.dp.toPx()
            val path = Path().apply {
                // Encoche en haut à gauche
                moveTo(0f, notchHeight)
                lineTo(0f, size.height - notchHeight)
                // Tenon en bas à gauche
                lineTo(0f, size.height)
                lineTo(notchWidth, size.height)
                lineTo(notchWidth, size.height - notchHeight)
                // Ligne de bas jusqu'au coin droit
                lineTo(size.width, size.height - notchHeight)
                // Bord droit
                lineTo(size.width, 0f)
                // Encoche en haut
                lineTo(notchWidth, 0f)
                lineTo(notchWidth, notchHeight)
                lineTo(0f, notchHeight)
                close()
            }
            return Outline.Generic(path)
        }
    }
}

// ---------------------------------------------------------
// Composant PuzzleDragDropEditor : éditeur drag & drop de blocs puzzle
// ---------------------------------------------------------
@Composable
fun PuzzleDragDropEditor(
    blocsSQL: List<BlocSQL>,
    onAjouterBloc: (BlocSQL) -> Unit
) {
    // Palette de blocs
    val palette = listOf(
        PuzzleData("SELECT", getColorForType("SELECT")),
        PuzzleData("FROM", getColorForType("FROM")),
        PuzzleData("WHERE", getColorForType("WHERE"))
    )
    // Gestion du drag & drop
    var draggedBlock by remember { mutableStateOf<PuzzleData?>(null) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dropZoneBounds by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Palette de blocs en haut
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Palette :", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                palette.forEach { data ->
                    Box(
                        modifier = Modifier.pointerInput(data) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    draggedBlock = data
                                    dragStart = offset
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                },
                                onDragEnd = {
                                    val finalPos = dragStart + dragOffset
                                    if (dropZoneBounds?.contains(finalPos) == true && draggedBlock != null) {
                                        onAjouterBloc(
                                            BlocSQL(
                                                type = draggedBlock!!.label,
                                                couleur = draggedBlock!!.color
                                            )
                                        )
                                    }
                                    draggedBlock = null
                                    dragOffset = Offset.Zero
                                },
                                onDragCancel = {
                                    draggedBlock = null
                                    dragOffset = Offset.Zero
                                }
                            )
                        }
                    ) {
                        PuzzleBlock(text = data.label, color = data.color)
                    }
                }
            }
        }
        // Zone de dépôt en bas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .onGloballyPositioned { coords ->
                    dropZoneBounds = coords.boundsInWindow()
                },
            border = BorderStroke(2.dp, Color.DarkGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (blocsSQL.isEmpty()) {
                    Text(
                        text = "Déposez ici vos blocs",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(blocsSQL) { bloc ->
                            PuzzleBlock(
                                text = bloc.type,
                                color = bloc.couleur,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
        // Overlay du bloc en cours de drag
        if (draggedBlock != null) {
            val currentPos = dragStart + dragOffset
            val offsetX = currentPos.x.roundToInt()
            val offsetY = currentPos.y.roundToInt()
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .wrapContentSize()
            ) {
                PuzzleBlock(text = draggedBlock!!.label, color = draggedBlock!!.color)
            }
        }
    }
}

// ---------------------------------------------------------
// Composant InfoJeu : affiche le nombre de tentatives et le temps écoulé
// ---------------------------------------------------------
@Composable
fun InfoJeu(attempts: Int, elapsedSeconds: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Tentatives : $attempts", style = MaterialTheme.typography.bodyMedium)
        Text("Temps : ${elapsedSeconds}s", style = MaterialTheme.typography.bodyMedium)
    }
}

// ---------------------------------------------------------
// Adaptation de JeuScreen pour intégrer l'éditeur puzzle, le compteur, le timer et l'indice
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuScreen(
    goHome: () -> Unit,
    goResultat: (Int, Long) -> Unit,
    enquete: Enquete
) {
    // Création des ViewModels (supposés définis dans votre projet)
    val repository = EnqueteRepository(createCodeFirstService())
    val queryViewModel = remember { PlayQueryViewModel(enquete, repository) }
    val resultViewModel = remember { EnqueteResultViewModel(enquete, repository) }
    val blocViewModel = remember { JeuBlocViewModel() }

    // États observés
    val isLoadingQuery by queryViewModel.isLoading.collectAsState()
    val queryResult by queryViewModel.queryResult.collectAsState()
    val queryErrorMessage by queryViewModel.errorMessage.collectAsState()
    val isLoadingResult by resultViewModel.isLoading.collectAsState()
    val verificationResult by resultViewModel.verificationResult.collectAsState()
    val resultErrorMessage by resultViewModel.errorMessage.collectAsState()
    val modeBlocActif by blocViewModel.modeBlocActif.collectAsState()
    val blocsSQL by blocViewModel.blocsSQL.collectAsState()
    val requeteSQL by blocViewModel.requeteSQL.collectAsState()

    // Variable pour le champ de réponse
    var reponseText by remember { mutableStateOf("") }

    // Nouveaux états pour le compteur de tentatives et le timer
    var attemptCount by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0L) }
    // État pour afficher l'indice
    var showHint by remember { mutableStateOf(false) }

    // Timer : incrémente le temps chaque seconde
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedTime++
        }
    }

    // Navigation vers l'écran de résultat
    LaunchedEffect(verificationResult) {
        if (verificationResult?.isCorrect == true) {
            delay(1500)
            goResultat(attemptCount, elapsedTime)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopBar(goHome)
        Spacer(modifier = Modifier.height(8.dp))
        // Affichage du compteur et du timer
        InfoJeu(attempts = attemptCount, elapsedSeconds = elapsedTime)
        Spacer(modifier = Modifier.height(16.dp))
        // Switch pour basculer entre mode puzzle et mode texte
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (modeBlocActif) "Mode puzzle" else "Mode texte", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = modeBlocActif, onCheckedChange = { blocViewModel.basculerMode() })
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Choix de l'éditeur en fonction du mode
        if (modeBlocActif) {
            PuzzleDragDropEditor(
                blocsSQL = blocsSQL,
                onAjouterBloc = { blocViewModel.ajouterBloc(it) }
            )
        } else {
            TextEditorUI(
                requeteSQL = requeteSQL,
                onRequeteChanged = { blocViewModel.mettreAJourRequeteTexte(it) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Section de réponse utilisateur avec icône de loupe pour afficher l'indice
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Champ de réponse
            OutlinedTextField(
                value = reponseText,
                onValueChange = { reponseText = it },
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

            // Icône loupe pour afficher l'indice (si attemptCount > enquete.difficulteDificile)
            IconButton(
                onClick = {
                    if (attemptCount > enquete.difficulteDificile) {
                        showHint = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Afficher indice"
                )
            }
        }

// ---------------------------------------------------------
// Boutons d'action : "Exécuter la requête" et "Valider l'enquête"
// ---------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton pour exécuter la requête SQL
            Button(
                onClick = {
                    attemptCount++
                    queryViewModel.executeQuery(blocViewModel.getRequeteSQL())
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
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
                Text(text = "Exécuter la requête")
            }

            // Bouton pour valider l'enquête (vérifier la réponse)
            Button(
                onClick = {
                    resultViewModel.verifyAnswer(reponseText)
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                enabled = !isLoadingResult && reponseText.isNotBlank()
            ) {
                if (isLoadingResult) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Valider l'enquête")
            }
        }
        // Affichage de l'indice si demandé
        if (showHint) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { showHint = false },
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Indice : ${enquete.indice}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Tableau de résultats
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Résultats de la requête",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                when {
                    isLoadingQuery -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    queryErrorMessage != null -> {
                        queryErrorMessage?.let { ErrorMessage(message = it) }
                    }
                    queryResult != null -> {
                        queryResult?.let { result ->
                            if (result.success && result.rows.isNotEmpty()) {
                                ResultTable(queryResult = result)
                            } else if (result.success && result.rowCount == 0) {
                                EmptyResultMessage()
                            } else {
                                ErrorMessage(message = result.errorMessage ?: "Erreur inconnue")
                            }
                        }
                    }
                    else -> {
                        EmptyStateMessage()
                    }
                }
            }
        }
        // Section de réponse utilisateur (optionnelle, ici déjà incluse ci-dessus)
    }
}

@Composable
fun TopBar(goHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = goHome) {
            Image(
                painter = painterResource(id = R.drawable.retour),
                contentDescription = stringResource(id = R.string.btn_retour),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
            )
        }
        Text(text = "SQLuedo - Mode Jeu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row {
            IconButton(onClick = { /* Action base de données */ }) {
                Icon(painter = painterResource(id = R.drawable.menu), contentDescription = "Base de données", modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = { /* Action profil */ }) {
                Icon(painter = painterResource(id = R.drawable.connexion), contentDescription = "Profil", modifier = Modifier.size(32.dp))
            }
        }
    }
}

// ---------------------------------------------------------
// Fonctions complémentaires pour éditeur texte et affichage des résultats
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun TextEditorUI(
    requeteSQL: String,
    onRequeteChanged: (String) -> Unit
) {
    var txt by remember {
        mutableStateOf(
            TextFieldValue(
                text = requeteSQL,
                selection = TextRange(requeteSQL.length)
            )
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        border = BorderStroke(1.dp, Color.Gray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text(
                text = "Requêtes SQL",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = txt,
                onValueChange = {
                    txt = it
                    onRequeteChanged(it.text)
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Écrivez votre requête SQL ici...") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocSQLItem(
    bloc: BlocSQL,
    onDelete: () -> Unit,
    onUpdate: (BlocSQL) -> Unit
) {
    var valeur by remember { mutableStateOf(bloc.valeur) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bloc.couleur.copy(alpha = 0.2f))
            .border(1.dp, bloc.couleur, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = bloc.couleur,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = bloc.type,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 14.sp
            )
        }
        OutlinedTextField(
            value = valeur,
            onValueChange = {
                valeur = it
                onUpdate(bloc.copy(valeur = it))
            },
            modifier = Modifier.weight(1f).height(40.dp),
            placeholder = { Text("Valeur...") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                containerColor = Color.White.copy(alpha = 0.5f)
            ),
            singleLine = true
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray)
        }
    }
}

@Composable
fun ResultTable(queryResult: QueryResult) {
    val horizontalScrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${queryResult.rowCount} résultat(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                            modifier = Modifier.padding(end = 16.dp).width(120.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Divider()
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
                                    modifier = Modifier.padding(end = 16.dp).width(120.dp),
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
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Exécutez une requête SQL pour voir les résultats",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyResultMessage() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
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
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
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


