package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import com.sqluedo.data.service.createCodeFirstService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.collections.HashMap

data class QueryResultColumn(val name: String)

data class QueryResult(
    val columns: List<QueryResultColumn>,
    val rows: List<Map<String, Any>>,
    val rowCount: Int,
    val success: Boolean,
    val errorMessage: String? = null
)

class PlayQueryViewModel(
    private val enquete: Enquete,
    private val repository: EnqueteRepository = EnqueteRepository(createCodeFirstService())
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _queryResult = MutableStateFlow<QueryResult?>(null)
    val queryResult: StateFlow<QueryResult?> = _queryResult

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        authenticate()
    }

    private fun authenticate() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.login("admin@sqluedo.com", "Admin123!")
            } catch (e: Exception) {
                _errorMessage.value = "Erreur d'authentification: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun executeQuery(sqlQuery: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val authToken = repository.getAuthToken()
                if (authToken == null) {
                    authenticate()
                    return@launch
                }

                val service = createCodeFirstService()

                val jsonObject = JSONObject()
                jsonObject.put("databaseName", enquete.nomDatabase)
                jsonObject.put("sqlQuery", sqlQuery)
                val jsonString = jsonObject.toString()
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())

                val response = service.executeQuery(requestBody, authToken)

                val responseJson = JSONObject(response.string())
                val success = responseJson.getBoolean("success")

                if (success) {
                    val dataArray = responseJson.getJSONArray("data")
                    val rowCount = responseJson.getInt("rowCount")

                    val rows = mutableListOf<Map<String, Any>>()
                    val columnsSet = mutableSetOf<String>()

                    if (dataArray.length() > 0) {
                        val firstRow = dataArray.getJSONObject(0)
                        val keys = firstRow.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            columnsSet.add(key)
                        }

                        for (i in 0 until dataArray.length()) {
                            val rowObj = dataArray.getJSONObject(i)
                            val rowMap = HashMap<String, Any>()

                            columnsSet.forEach { colName ->
                                if (rowObj.has(colName)) {
                                    val value = when {
                                        rowObj.isNull(colName) -> "NULL"
                                        else -> rowObj.get(colName)
                                    }
                                    rowMap[colName] = value
                                } else {
                                    rowMap[colName] = ""
                                }
                            }

                            rows.add(rowMap)
                        }
                    }

                    val columns = columnsSet.map { QueryResultColumn(it) }
                    _queryResult.value = QueryResult(
                        columns = columns,
                        rows = rows,
                        rowCount = rowCount,
                        success = true
                    )
                } else {
                    val errorMsg = if (responseJson.has("detail")) {
                        responseJson.getString("detail")
                    } else {
                        "Erreur lors de l'exécution de la requête"
                    }
                    _queryResult.value = QueryResult(
                        columns = emptyList(),
                        rows = emptyList(),
                        rowCount = 0,
                        success = false,
                        errorMessage = errorMsg
                    )
                    _errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _queryResult.value = QueryResult(
                    columns = emptyList(),
                    rows = emptyList(),
                    rowCount = 0,
                    success = false,
                    errorMessage = e.message
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetQueryResult() {
        _queryResult.value = null
        _errorMessage.value = null
    }
}