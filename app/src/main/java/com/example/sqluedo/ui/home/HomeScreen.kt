package com.example.sqluedo.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sqluedo.data.model.Enquete
import com.example.sqluedo.data.model.Stub
import com.example.sqluedo.R

@Composable
fun HomeScreen(
    goConnexion: () -> Unit,
    goEnquete: (Enquete) -> Unit,
    enquetes: List<Enquete>,
    isLoading: Boolean = false,
    error: String? = null
) {
    Column {
        Spacer(modifier = Modifier.height(30.dp))

        AffichageBoutonNavigation(goConnexion)

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                    Button(onClick = { /* Fonctionnalité à implémenter */ }) {
                        Text(stringResource(R.string.btn_reessayer))
                    }
                }
            }
        } else {
            AffichageAllEnquetes(
                enquetes = enquetes.sortedBy { it.nom },
                goEnquete = goEnquete
            )
        }
    }
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
            Image(
                painter = painterResource(R.drawable.livre),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
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
    if (enquetes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.ErrEnqueteVide),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
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