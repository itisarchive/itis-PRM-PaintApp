package pl.edu.pja.kdudek.paintapp.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.edu.pja.kdudek.paintapp.model.Drawing
import pl.edu.pja.kdudek.paintapp.model.ToolSettings

class DrawingScreenViewModel : ViewModel() {
    val availableColors = listOf(
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Blue
    )

    var currentToolSettings = MutableStateFlow(ToolSettings())
    val drawings = MutableStateFlow(listOf<Drawing>())
    val image = MutableStateFlow<Bitmap?>(null)

    fun setToolColor(newColor: Color) {
        currentToolSettings.update { it.copy(color = newColor) }
    }

    fun setToolSize(newSize: Float) {
        currentToolSettings.update { it.copy(size = newSize) }
    }

    fun addDrawing(drawing: Drawing) {
        drawings.update { it + drawing }
    }

    fun onUndo() {
        drawings.update { it.dropLast(1) }
    }

    fun onClearCanvas() {
        drawings.update { emptyList() }
    }

    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    image.update { BitmapFactory.decodeStream(inputStream) }
                }
            }
        }
    }

    fun save(context: Context) {
        viewModelScope.launch {
            val currentImage = image.value ?: return@launch
            val newBitmap = createBitmap(currentImage.width, currentImage.height)
            val canvas = Canvas(newBitmap)
            withContext(Dispatchers.Default) {
                with(canvas) {
                    drawBitmap(currentImage, 0f, 0f, Paint())
                    drawings.value.forEach { drawing ->
                        val paint = Paint().apply {
                            color = drawing.toolSettings.color.toArgb()
                            strokeWidth = drawing.toolSettings.size
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            isAntiAlias = true
                        }
                        drawPath(drawing.path.asAndroidPath(), paint)
                    }
                }
            }
            withContext(Dispatchers.IO) {
                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        "edited_${System.currentTimeMillis()}.jpg"
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val imageUri = context.contentResolver.insert(
                    contentUri,
                    contentValues
                )
                imageUri?.let {
                    context.contentResolver.openOutputStream(it)?.use {
                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, contentValues, null, null)
                }
            }
        }
    }
}