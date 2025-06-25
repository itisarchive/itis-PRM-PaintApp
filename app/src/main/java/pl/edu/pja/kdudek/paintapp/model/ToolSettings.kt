package pl.edu.pja.kdudek.paintapp.model

import androidx.compose.ui.graphics.Color

data class ToolSettings(
    val color: Color = Color.Black,
    val size: Float = 5f
)
