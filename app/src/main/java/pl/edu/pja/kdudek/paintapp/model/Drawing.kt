package pl.edu.pja.kdudek.paintapp.model

import androidx.compose.ui.graphics.Path

data class Drawing(
    val path: Path,
    val toolSettings: ToolSettings
)
