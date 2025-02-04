package com.example.sqluedo.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.sqluedo.data.Enquete
import com.example.sqluedo.R
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(
    navController: NavController,
    enquetes: List<Enquete>
) {
    var selectedEnquete by remember { mutableStateOf<Enquete?>(null) }

    Column {
        if (selectedEnquete == null) {
            AffichageBoutonNavigation(navController = navController)
            AffichageAllEnquetes(
                enquetes = enquetes.sortedBy { it.nom },
                onEnqueteClick = { enquete ->
                    selectedEnquete = enquete
                }
            )
        } else {
            AffichageEnqueteClickee(
                enquete = selectedEnquete!!,
                onBack = { selectedEnquete = null }
            )
        }
    }
}

@Composable
fun AffichageBoutonNavigation(navController: NavController) {
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
                painter = painterResource(id = R.drawable.livre),
                contentDescription = stringResource(id = R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            IconButton(
                onClick = { navController.navigate("connexion") },
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.connexion_moyen),
                    contentDescription = stringResource(id = R.string.image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Sqluedo",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun AffichageAllEnquetes(
    enquetes: List<Enquete>,
    onEnqueteClick: (Enquete) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        items(enquetes) { enquete ->
            AffichageEnquetes(
                enquete = enquete,
                onClick = { onEnqueteClick(enquete) }
            )
        }
    }
}

@Composable
fun AffichageEnquetes(
    enquete: Enquete,
    onClick: () -> Unit
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
            .clickable { onClick() }
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

@Composable
fun AffichageEnqueteClickee(
    enquete: Enquete,
    onBack: () -> Unit
) {
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
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.retour),
                    contentDescription = stringResource(id = R.string.image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Image(
                painter = painterResource(id = R.drawable.connexion_moyen),
                contentDescription = stringResource(id = R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = enquete.nom,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Description",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = enquete.description,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(2.dp, Color.Gray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.mcd),
                contentDescription = stringResource(id = R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(100.dp))

        Button(
            onClick = {  },
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = "Jouer",
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAffichageBoutonNavigation() {
    val navController = rememberNavController()
    AffichageBoutonNavigation(navController = navController)
}
