package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sqluedo.data.model.Enquete
import com.sqluedo.data.repository.EnqueteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VerificationResult(
    val isCorrect: Boolean,
    val submittedAnswer: String,
    val correctSolution: String,
    val attemptCount: Int = 1,
    val timeTaken: Long = 0,
    val message: String = ""
)

class EnqueteResultViewModel(
    private val enquete: Enquete,
    private val repository: EnqueteRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _verificationResult = MutableStateFlow<VerificationResult?>(null)
    val verificationResult: StateFlow<VerificationResult?> = _verificationResult

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var attemptCount = 0
    private var startTime: Long = System.currentTimeMillis()

    init {
        startTime = System.currentTimeMillis()
    }

    fun verifyAnswer(userAnswer: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            attemptCount++

            try {
                // Utiliser directement la solution de l'enquête locale
                val correctSolution = enquete.solution

                val timeTaken = (System.currentTimeMillis() - startTime) / 1000

                val cleanedUserAnswer = cleanSolution(userAnswer)
                val cleanedCorrectSolution = cleanSolution(correctSolution)

                val isCorrect = cleanedUserAnswer == cleanedCorrectSolution

                val message = if (isCorrect) {
                    "Félicitations ! Votre réponse est correcte."
                } else {
                    "Votre réponse n'est pas correcte. Essayez encore."
                }

                _verificationResult.value = VerificationResult(
                    isCorrect = isCorrect,
                    submittedAnswer = userAnswer,
                    correctSolution = correctSolution,
                    attemptCount = attemptCount,
                    timeTaken = timeTaken,
                    message = message
                )

                // Logs détaillés pour débogage
                println("DEBUG - Submitted Answer: '$userAnswer'")
                println("DEBUG - Cleaned User Answer: '$cleanedUserAnswer'")
                println("DEBUG - Correct Solution: '$correctSolution'")
                println("DEBUG - Cleaned Solution: '$cleanedCorrectSolution'")
                println("DEBUG - Is Correct: $isCorrect")

            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la vérification : ${e.message}"
                println("ERROR - Verification Failed: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun cleanSolution(solution: String): String {
        return solution
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^a-z0-9 ]"), "")
    }

    fun resetVerification() {
        _verificationResult.value = null
        _errorMessage.value = null
    }

    fun getAttemptCount(): Int = attemptCount

    fun getElapsedTime(): Long = (System.currentTimeMillis() - startTime) / 1000
}