package com.sqluedo.ui.jeu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.EnqueteResultViewModel
import com.sqluedo.ViewModel.JeuPersistenceViewModel
import com.sqluedo.ViewModel.StatistiquesFinDePartieViewModel
import com.sqluedo.data.local.SQLuedoDatabase
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.repository.JeuProgressionRepository
import com.sqluedo.data.repository.StatistiquesRepository
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.launch

@Composable
fun ResultatScreen(
    enquete: Enquete,
    goHome: () -> Unit,
    attempts: Int = 1,
    timeTaken: Long = 0
) {
    var attemptCount by remember { mutableStateOf(attempts) }
    var elapsedTime by remember { mutableStateOf(timeTaken) }

    val context = LocalContext.current
    val codeFirstService = createCodeFirstService()
    val repository = EnqueteRepository(codeFirstService)
    val utilisateurRepository = UtilisateurRepository(codeFirstService)
    val statistiquesRepository = StatistiquesRepository(codeFirstService, utilisateurRepository)

    val database = SQLuedoDatabase.getDatabase(context)
    val jeuProgressionRepository = JeuProgressionRepository(database.jeuProgressionDao())

    val statsViewModel = remember {
        StatistiquesFinDePartieViewModel(
            enquete = enquete,
            utilisateurRepository = utilisateurRepository,
            statistiquesRepository = statistiquesRepository
        )
    }
    val persistenceViewModel: JeuPersistenceViewModel = viewModel(
        factory = JeuPersistenceViewModel.Factory(jeuProgressionRepository, utilisateurRepository)
    )

    val viewModel = remember { EnqueteResultViewModel(enquete, repository) }

    val statsError by statsViewModel.error.collectAsState()
    val persistenceError by persistenceViewModel.error.collectAsState()
    val totalAttempts by persistenceViewModel.nbTentatives.collectAsState()
    val tempsTotalUser by persistenceViewModel.tempsTotal.collectAsState()
    val enquetesReussies by persistenceViewModel.enquetesReussies.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        persistenceViewModel.initEnquete(enquete)

        if (attempts == 1 && timeTaken == 0L) {
            attemptCount = viewModel.getAttemptCount()
            elapsedTime = viewModel.getElapsedTime()
        }

        persistenceViewModel.recordSuccess()

        scope.launch {
            statsViewModel.envoyerStatistiquesFinPartie(
                nomUtilisateur = utilisateurRepository.getCurrentUsername() ?: "admin_prof",
                nbTentatives = attemptCount,
                tempsPasse = elapsedTime.toInt()
            ).collect { response ->
                if (!response.success) {
                    println("Échec de l'envoi des statistiques: ${response.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = goHome,
                modifier = Modifier.align(Alignment.TopStart)
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
        }

        persistenceError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        statsError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            modifier = Modifier
                .padding(16.dp)
                .size(120.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.success_label),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = enquete.nom,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.statistics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.ThumbUp,
                        value = attemptCount.toString(),
                        label = stringResource(id = R.string.attempts_label)
                    )

                    StatItem(
                        icon = Icons.Default.Check,
                        value = formatTime(elapsedTime),
                        label = stringResource(id = R.string.time_label)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Vos statistiques globales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        value = enquetesReussies.toString(),
                        label = "Enquêtes réussies"
                    )

                    StatItem(
                        icon = Icons.Default.Check,
                        value = formatTime(tempsTotalUser),
                        label = "Temps total"
                    )
                }
            }
        }

        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(2.dp, Color.Gray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.investigation_solution),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = enquete.solution,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = goHome,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.return_to_home),
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}