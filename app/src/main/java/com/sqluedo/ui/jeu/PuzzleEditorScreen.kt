//package com.sqluedo.ui.jeu
//
//import android.content.ClipData
//import android.content.ClipDescription
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.dragAndDropSource
//import androidx.compose.foundation.dragAndDropTarget
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Density
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Outline
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.LayoutDirection
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.toPx
//
//// ====================================================================
//// 1. Composant PuzzleBlock : affiche un bloc SQL sous forme de puzzle
//// ====================================================================
//@Composable
//fun PuzzleBlock(
//    text: String,
//    color: Color,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .width(150.dp)
//            .height(50.dp)
//            .background(color = color, shape = PuzzlePieceShape)
//            .padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 12.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
//    }
//}
//
//// ====================================================================
//// 2. Définition de la forme personnalisée simulant une pièce de puzzle
//// ====================================================================
//val PuzzlePieceShape: Shape = object : Shape {
//    override fun createOutline(
//        size: Size,
//        layoutDirection: LayoutDirection,
//        density: Density
//    ): Outline {
//        with(density) {
//            val notchWidth = 20.dp.toPx()   // largeur de l'encoche / tenon
//            val notchHeight = 8.dp.toPx()   // hauteur de l'encoche / tenon
//
//            // Construction du chemin pour la pièce de puzzle :
//            // L'encoche (femelle) en haut à gauche et le tenon (mâle) en bas à gauche.
//            val path = Path().apply {
//                // Commence à gauche, légèrement en dessous du bord supérieur (pour former l'encoche)
//                moveTo(0f, notchHeight)
//                // Descend le long du bord gauche jusqu'au point de départ du tenon
//                lineTo(0f, size.height - notchHeight)
//                // Dessine le tenon en bas à gauche
//                lineTo(0f, size.height)
//                lineTo(notchWidth, size.height)
//                lineTo(notchWidth, size.height - notchHeight)
//                // Ligne du bas jusqu'au coin inférieur droit
//                lineTo(size.width, size.height - notchHeight)
//                // Remonte le long du bord droit
//                lineTo(size.width, 0f)
//                // Remonte en diagonale pour créer l'encoche en haut
//                lineTo(notchWidth, 0f)
//                lineTo(notchWidth, notchHeight)
//                // Retour au point de départ
//                lineTo(0f, notchHeight)
//                close()
//            }
//            return Outline.Generic(path)
//        }
//    }
//}
//
//// ====================================================================
//// 3. Écran d'édition en mode "Puzzle" avec palette et zone de construction
//// ====================================================================
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun PuzzleEditorScreen() {
//    // Liste mutable pour stocker les blocs déposés dans la zone de construction
//    val droppedBlocks = remember { mutableStateListOf<String>() }
//    // État pour signaler le survol de la zone de dépôt
//    val isDragOver = remember { mutableStateOf(false) }
//
//    // Définition du target pour le drag-and-drop
//    val dropTarget = remember {
//        object : DragAndDropTarget {
//            override fun onEntered(event: DragAndDropEvent) {
//                isDragOver.value = true
//            }
//            override fun onExited(event: DragAndDropEvent) {
//                isDragOver.value = false
//            }
//            override fun onEnded(event: DragAndDropEvent) {
//                isDragOver.value = false
//            }
//            override fun onDrop(event: DragAndDropEvent): Boolean {
//                val clipData = event.toAndroidDragEvent().clipData
//                if (clipData != null && clipData.itemCount > 0) {
//                    val droppedText = clipData.getItemAt(0).text.toString()
//                    droppedBlocks.add(droppedText)
//                    return true
//                }
//                return false
//            }
//        }
//    }
//
//    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        // Palette de blocs SQL (ex. "SELECT", "FROM", etc.) à gauche
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxHeight()
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            val paletteBlocks = listOf("SELECT", "FROM", "WHERE", "AND", "OR")
//            // Couleurs pour chaque bloc (vous pouvez adapter selon vos besoins)
//            val blockColors = listOf(
//                Color(0xFF4CAF50), // SELECT en vert
//                Color(0xFF2196F3), // FROM en bleu
//                Color(0xFFFFC107), // WHERE en orange
//                Color(0xFF9C27B0), // AND en violet
//                Color(0xFF009688)  // OR en teal
//            )
//            paletteBlocks.forEachIndexed { index, label ->
//                PuzzleBlock(
//                    text = label,
//                    color = blockColors.getOrElse(index) { Color.Gray },
//                    modifier = Modifier
//                        .padding(bottom = 8.dp)
//                        .dragAndDropSource {
//                            detectTapGestures(
//                                onLongPress = {
//                                    // Démarrer la session de drag-and-drop avec le texte du bloc
//                                    startTransfer(
//                                        DragAndDropTransferData(
//                                            ClipData.newPlainText("SQLClause", label)
//                                        )
//                                    )
//                                }
//                            )
//                        }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.width(16.dp))
//
//        // Zone de construction (drop target) pour déposer les blocs
//        Box(
//            modifier = Modifier
//                .weight(2f)
//                .fillMaxHeight()
//                .padding(8.dp)
//                .border(
//                    width = 2.dp,
//                    color = if (isDragOver.value) Color.Blue else Color.Gray,
//                    shape = MaterialTheme.shapes.medium
//                )
//                .padding(16.dp)
//                .dragAndDropTarget(
//                    shouldStartDragAndDrop = { event ->
//                        event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
//                    },
//                    target = dropTarget
//                )
//        ) {
//            if (droppedBlocks.isEmpty()) {
//                Text(
//                    text = "Glissez des éléments SQL ici pour construire votre requête",
//                    color = Color.DarkGray,
//                    style = MaterialTheme.typography.bodyMedium,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            } else {
//                // Affiche les blocs déposés sous forme de liste verticale
//                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                    droppedBlocks.forEach { clause ->
//                        PuzzleBlock(
//                            text = clause,
//                            color = Color.LightGray,
//                            modifier = Modifier.padding(4.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
