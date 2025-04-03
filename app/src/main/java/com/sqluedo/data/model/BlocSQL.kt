package com.sqluedo.data.model

import androidx.compose.ui.graphics.Color

/**
 * Représente un bloc SQL utilisé dans l'interface de construction de requêtes.
 */
data class BlocSQL(
    val type: String,
    val valeur: String = "",
    val couleur: Color = getColorForType(type)
)

/**
 * Renvoie une couleur en fonction du type de bloc SQL.
 */
fun getColorForType(type: String): Color {
    return when (type.uppercase()) {
        "SELECT" -> Color(0xFF2196F3) // Bleu
        "FROM" -> Color(0xFF2196F3) // Bleu
        "WHERE" -> Color(0xFFE91E63) // Rose
        "AND" -> Color(0xFF4CAF50) // Vert
        "OR" -> Color(0xFF4CAF50) // Vert
        "JOIN" -> Color(0xFFFF9800) // Orange
        "ON" -> Color(0xFFFF9800) // Orange
        "GROUP BY" -> Color(0xFF9C27B0) // Violet
        "ORDER BY" -> Color(0xFF9C27B0) // Violet
        "HAVING" -> Color(0xFF9C27B0) // Violet
        else -> Color(0xFF607D8B) // Bleu gris
    }
}