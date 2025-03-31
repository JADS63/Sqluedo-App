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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sqluedo.R
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Stub

@Composable
fun JeuScreen(
    goHome: () -> Unit,
    goResultat: () -> Unit,
    enquete: Enquete
) {
    var requeteSQL by remember { mutableStateOf("") }
    var reponseText by remember { mutableStateOf("") }

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
            onValiderRequete = { /* Traiter la requête SQL */ },
            reponseText = reponseText,
            onReponseChanged = { reponseText = it },
            onValiderReponse = goResultat
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
                    painter = painterResource(id = R.drawable.menu), // Remplacer par une icône de base de données
                    contentDescription = "Base de données",
                    modifier = Modifier.size(32.dp)
                )
            }
            // A faire quand on aura plus de problème sur le jeu
//            IconButton(onClick = { /* Action recherche */ }) {
//                Icon(
//                    painter = painterResource(id = R.drawable.menu), // Remplacer par une icône de recherche
//                    contentDescription = "Recherche",
//                    modifier = Modifier.size(32.dp)
//                )
//            }

            IconButton(onClick = { /* Action profil */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.connexion), // Remplacer par une icône de profil
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
    onValiderReponse: () -> Unit
) {
    var txt  by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange(0)
            )
        )
    }
    val txtVide  =
        TextFieldValue(
            text = "",
            selection = TextRange(0)
        )

    val callback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {



                val clipData = event.toAndroidDragEvent().clipData
                val txtDrop  =
                    TextFieldValue(
                        text = txt.text+clipData.getItemAt(0).text.toString(),
                        selection = TextRange(txt.text.length+clipData.getItemAt(0).text.toString().length)
                    )

                if (clipData.itemCount > 0) {
                    txt = txtDrop;
                    println("Données reçues : $txt")
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
                ){OutlinedTextField(
                    value = txt,
                    onValueChange = { txt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("Écrivez votre requête SQL ici...") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )}


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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
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
                .padding(vertical = 8.dp),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tableau de réponse",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Zone de résultat (remplacer par un tableau réel si nécessaire)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp)
                ) {
                    // Ici vous pourriez afficher les résultats de la requête SQL
                    Text(
                        text = "Les résultats de votre requête s'afficheront ici.",
                        color = Color.Gray
                    )
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
                singleLine = true
            )

            // Bouton Valider
            Button(
                onClick = onValiderReponse,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray
                ),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(stringResource(id = R.string.btn_valider))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SqlHelpButtons() {
    // Liste des commandes SQL à afficher comme boutons d'aide
    val sqlCommands = listOf(
        "Select", "Where", "From", "Join", "On",
        "And", "Order By", "Group By", "Having",
        "Full Join", "Left Join", "Right Join"
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
                                        command, command.uppercase()
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