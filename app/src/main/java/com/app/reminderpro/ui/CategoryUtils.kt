package com.app.reminderpro.ui

import androidx.compose.ui.graphics.Color

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "personal" -> Color(0xFFD1C4E9) // Light purple
        "work" -> Color(0xFFBBDEFB)     // Light blue
        "health" -> Color(0xFFC8E6C9)   // Light green
        "finance" -> Color(0xFFFFF9C4)  // Light yellow
        else -> Color.LightGray         // Default color
    }
}

fun getCategoryTextColor(category: String): Color {
    return when (category.lowercase()) {
        "personal" -> Color(0xFF4A148C)
        "work" -> Color(0xFF0D47A1)
        "health" -> Color(0xFF1B5E20)
        "finance" -> Color(0xFFF57F17)
        else -> Color.DarkGray
    }
}
