package pl.edu.pja.kdudek.paintapp.ui.view

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.pja.kdudek.paintapp.model.Drawing
import pl.edu.pja.kdudek.paintapp.ui.viewmodel.DrawingScreenViewModel

@Composable
fun DrawingScreen(
    viewModel: DrawingScreenViewModel = viewModel(),
    uri: Uri
) {
    Column {
        val drawings by viewModel.drawings.collectAsStateWithLifecycle()
        val toolSettings by viewModel.currentToolSettings.collectAsStateWithLifecycle()
        var currentDrawing by remember { mutableStateOf<Drawing?>(null) }
        val image by viewModel.image.collectAsStateWithLifecycle()

        val ctx = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.loadImage(ctx, uri)
        }

        Button(onClick = {
            viewModel.save(ctx)
        }) {
            Text(text = "Save")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(toolSettings) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentDrawing = Drawing(
                                path = Path().apply { moveTo(offset.x, offset.y) },
                                toolSettings = toolSettings
                            )
                        },
                        onDrag = { change, offset ->
                            currentDrawing?.let {
                                val pathAfterNewLine = it.path.copy().apply {
                                    lineTo(change.position.x, change.position.y)
                                }
                                currentDrawing = it.copy(path = pathAfterNewLine)
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            currentDrawing?.let {
                                viewModel.addDrawing(it)
                            }
                            currentDrawing = null
                        },
                        onDragCancel = {
                            currentDrawing = null
                        }
                    )
                }
        ) {
            image?.let {
                drawImage(
                    image = it.asImageBitmap()
                )
            }
            drawings.forEach {
                drawPath(
                    path = it.path,
                    color = it.toolSettings.color,
                    style = Stroke(
                        width = it.toolSettings.size,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            currentDrawing?.let {
                drawPath(
                    path = it.path,
                    color = it.toolSettings.color,
                    style = Stroke(
                        width = it.toolSettings.size,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        ToolBox(
            availableColors = viewModel.availableColors,
            currentColor = toolSettings.color,
            selectColor = viewModel::setToolColor,
            currentSize = toolSettings.size,
            selectSize = viewModel::setToolSize,
            undo = viewModel::onUndo,
            clear = viewModel::onClearCanvas
        )
    }
}

@Composable
fun ToolBox(
    availableColors: List<Color>,
    currentColor: Color,
    selectColor: (Color) -> Unit,
    currentSize: Float,
    selectSize: (Float) -> Unit,
    undo: () -> Unit,
    clear: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row {
            availableColors.forEachIndexed { index, color ->
                IconButton(
                    onClick = { selectColor(color) }
                ) {
                    Icon(
                        modifier = Modifier.size(if (color == currentColor) 44.dp else 24.dp),
                        imageVector = Icons.Filled.Circle,
                        contentDescription = "color ${index.inc()}",
                        tint = color
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = undo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = clear,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Slider(
                value = currentSize,
                onValueChange = selectSize,
                valueRange = 1f..30f,
                steps = 28
            )
        }
    }
}