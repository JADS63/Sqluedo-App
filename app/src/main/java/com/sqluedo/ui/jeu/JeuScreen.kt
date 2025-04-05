package com.sqluedo.ui.jeu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sqluedo.R
import com.sqluedo.ViewModel.EnqueteResultViewModel
import com.sqluedo.ViewModel.JeuPersistenceViewModel
import com.sqluedo.ViewModel.PlayQueryViewModel
import com.sqluedo.ViewModel.QueryResult
import com.sqluedo.data.local.SQLuedoDatabase
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.repository.JeuProgressionRepository
import com.sqluedo.data.repository.UtilisateurRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun InfoJeu(attempts: Int, elapsedSeconds: Long, totalAttempts: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                stringResource(id = R.string.attempts_count, attempts),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Total: $totalAttempts tentatives",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            stringResource(id = R.string.elapsed_time, elapsedSeconds),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuScreen(
    goHome: () -> Unit,
    goResultat: (Int, Long) -> Unit,
    enquete: Enquete
) {
    val context = LocalContext.current
    val codeFirstService = createCodeFirstService()
    val repository = EnqueteRepository(codeFirstService)
    val utilisateurRepository = UtilisateurRepository(codeFirstService)

    val database = SQLuedoDatabase.getDatabase(context)
    val jeuProgressionRepository = JeuProgressionRepository(database.jeuProgressionDao())

    val queryViewModel = remember { PlayQueryViewModel(enquete, repository) }
    val resultViewModel = remember { EnqueteResultViewModel(enquete, repository) }
    val persistenceViewModel: JeuPersistenceViewModel = viewModel(
        factory = JeuPersistenceViewModel.Factory(jeuProgressionRepository, utilisateurRepository)
    )

    val isLoadingQuery by queryViewModel.isLoading.collectAsState()
    val queryResult by queryViewModel.queryResult.collectAsState()
    val queryErrorMessage by queryViewModel.errorMessage.collectAsState()
    val isLoadingResult by resultViewModel.isLoading.collectAsState()
    val verificationResult by resultViewModel.verificationResult.collectAsState()
    val resultErrorMessage by resultViewModel.errorMessage.collectAsState()

    val totalAttempts by persistenceViewModel.nbTentatives.collectAsState()
    val persistenceError by persistenceViewModel.error.collectAsState()

    var requeteSQL by remember { mutableStateOf("") }
    var reponseText by remember { mutableStateOf("") }
    var attemptCount by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var showHint by remember { mutableStateOf(false) }

    LaunchedEffect(enquete) {
        persistenceViewModel.initEnquete(enquete)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedTime++
            persistenceViewModel.updateTime(elapsedTime)
        }
    }

    LaunchedEffect(verificationResult) {
        if (verificationResult?.isCorrect == true) {
            persistenceViewModel.recordSuccess()
            delay(1500)
            goResultat(attemptCount, elapsedTime)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopBar(goHome, enquete)
        Spacer(modifier = Modifier.height(8.dp))

        persistenceError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        InfoJeu(
            attempts = attemptCount,
            elapsedSeconds = elapsedTime,
            totalAttempts = totalAttempts
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextEditorUI(
            requeteSQL = requeteSQL,
            onRequeteChanged = { requeteSQL = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            IconButton(
                onClick = {
                    if (attemptCount > enquete.difficulteDificile) {
                        showHint = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.show_hint)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    attemptCount++
                    persistenceViewModel.recordAttempt()
                    queryViewModel.executeQuery(requeteSQL)
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
                Text(text = stringResource(id = R.string.btn_execute_query))
            }

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
                Text(text = stringResource(id = R.string.btn_validate_investigation))
            }
        }
        if (showHint) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { showHint = false },
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.hint_prefix, enquete.indice),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.query_results),
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
                                ErrorMessage(message = result.errorMessage ?: stringResource(id = R.string.unknown_error))
                            }
                        }
                    }
                    else -> {
                        EmptyStateMessage()
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(goHome: () -> Unit, enquete: Enquete) {
    var showMLDDialog by remember { mutableStateOf(false) }

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
        Text(text = stringResource(id = R.string.title_jeu_screen), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row {

            IconButton(onClick = { showMLDDialog = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.database),
                    contentDescription = stringResource(id = R.string.database_icon_desc),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (showMLDDialog) {
        MLDDialog(
            enquete = enquete,
            onDismiss = { showMLDDialog = false }
        )
    }
}

@Composable
fun MLDDialog(
    enquete: Enquete,
    onDismiss: () -> Unit
) {
    val repository = EnqueteRepository(createCodeFirstService())

    var tables by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(enquete.nomDatabase) {
        isLoading = true
        error = null

        try {
            if (repository.getAuthToken() == null) {
                repository.login("admin@sqluedo.com", "Admin123!")
            }

            val service = createCodeFirstService()
            val requestBody = JSONObject().apply {
                put("databaseName", enquete.nomDatabase)
                put("sqlQuery", "SELECT table_name FROM information_schema.tables WHERE table_schema = '${enquete.nomDatabase}';")
            }.toString().toRequestBody("application/json".toMediaType())

            val response = repository.getAuthToken()?.let { token ->
                service.executeQuery(requestBody, token)
            }

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
                    error = "erreur"
                }
            }
        } catch (e: Exception) {
            error = "erreur"
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.mld_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.database_name, enquete.nomDatabase),
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
                        text = stringResource(id = R.string.error_loading_tables, error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.available_tables),
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
                                    text = stringResource(id = R.string.table_bullet_item, tableName),
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
                Text(stringResource(id = R.string.btn_close))
            }
        }
    )
}

@Composable
fun TableCard(tableName: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = tableName.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

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
                text = stringResource(id = R.string.sql_queries),
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
                placeholder = { Text(stringResource(id = R.string.sql_query_placeholder)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
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
                text = stringResource(id = R.string.result_count, queryResult.rowCount),
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
                                val value = row[column.name]?.toString() ?: stringResource(id = R.string.null_value)
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
            text = stringResource(id = R.string.empty_state_message),
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
                text = stringResource(id = R.string.query_success),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.no_results_returned),
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
                text = stringResource(id = R.string.query_execution_error),
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