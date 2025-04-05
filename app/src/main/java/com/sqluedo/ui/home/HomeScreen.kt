package com.sqluedo.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.EnqueteListViewModel
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.model.Stub
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.service.createCodeFirstService

@Composable
fun HomeScreen(
    goConnexion: () -> Unit,
    goEnquete: (Enquete) -> Unit,
    enquetes: List<Enquete>,
    isLoading: Boolean = false,
    error: String? = null,
    onNextPage: () -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onRefresh: () -> Unit = {},
    currentPage: Int = 0,
    hasNextPage: Boolean = true
) {
    Column {
        Spacer(modifier = Modifier.height(30.dp))

        AffichageBoutonNavigation(goConnexion)

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.ErrChargement),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text(stringResource(R.string.btn_reessayer))
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (enquetes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ErrEnqueteVide),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    AffichageAllEnquetes(
                        enquetes = enquetes.sortedBy { it.nom },
                        goEnquete = goEnquete
                    )
                }
            }

            PaginationControls(
                currentPage = currentPage,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                hasPreviousPage = currentPage > 0,
                hasNextPage = hasNextPage && enquetes.isNotEmpty()
            )
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    hasPreviousPage: Boolean,
    hasNextPage: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPreviousPage,
                enabled = hasPreviousPage,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (hasPreviousPage)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.retour),
                    contentDescription = "Précédent",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Précédent")
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "${currentPage + 1}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Button(
                onClick = onNextPage,
                enabled = hasNextPage,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            ) {
                Text(stringResource(id = R.string.suivant))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = stringResource(id = R.string.suivant),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreenWithViewModel(
    goConnexion: () -> Unit,
    goEnquete: (Enquete) -> Unit
) {
    val service = createCodeFirstService()
    val repository = EnqueteRepository(service)
    val viewModel: EnqueteListViewModel = viewModel(
        factory = EnqueteListViewModel.Factory(repository)
    )

    val enquetes by viewModel.enquetes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val hasNextPage by viewModel.hasNextPage.collectAsState()

    HomeScreen(
        goConnexion = goConnexion,
        goEnquete = goEnquete,
        enquetes = enquetes,
        isLoading = isLoading,
        error = error,
        onNextPage = { viewModel.nextPage() },
        onPreviousPage = { viewModel.previousPage() },
        onRefresh = { viewModel.refreshData() },
        currentPage = currentPage,
        hasNextPage = hasNextPage
    )
}

@Composable
fun AffichageBoutonNavigation(goConnexion: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val context = LocalContext.current
            Image(
                painter = painterResource(R.drawable.livre),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://codefirst.iut.uca.fr/containers/sqluedo-documentation"))
                        context.startActivity(intent)
                    }
            )

            IconButton(
                onClick = goConnexion,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.connexion_moyen),
                    contentDescription = stringResource(R.string.image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = stringResource(R.string.titre_app),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun AffichageAllEnquetes(
    enquetes: List<Enquete>,
    goEnquete: (Enquete) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        items(enquetes) { enquete ->
            AffichageEnquetes(
                enquete = enquete,
                goEnquete = goEnquete
            )
        }
    }
}

@Composable
fun AffichageEnquetes(
    enquete: Enquete,
    goEnquete: (Enquete) -> Unit
) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(2.dp, Color.Gray),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .clickable {
                goEnquete(enquete)
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = enquete.nom,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = enquete.description,
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            goConnexion = {},
            goEnquete = {},
            enquetes = Stub.enquetes
        )
    }
}