package com.example.sqluedo.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sqluedo.data.Enquete
import com.example.sqluedo.R


@Composable
fun HomeScreen(
    enquetes: List<Enquete>,
    onEnqueteClick: (Enquete) -> Unit
) {
    Column {
        AffichageBoutonNavigation()

//        AffichageAllEnquetes(enquetes.sortedBy { it.nom })


    }
}
@Preview(showBackground = true)
@Composable
fun AffichageBoutonNavigation(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(22.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Image(
                painter = painterResource(id = R.drawable.livre),
                contentDescription = stringResource(id = R.string.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
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
            text = "Sqluedo",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

