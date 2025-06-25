package pl.edu.pja.kdudek.paintapp.ui.viewmodel

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryScreenViewModel : ViewModel() {

    val images = MutableStateFlow(listOf<Uri>())

    fun loadImages(context: Context) {
        viewModelScope.launch {
            val uris = mutableListOf<Uri>()
            withContext(Dispatchers.IO) {
                val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                context.contentResolver.query(
                    contentUri,
                    arrayOf(MediaStore.Images.Media._ID),
                    null,
                    null,
                    "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
                )?.use { cursor ->
                    val noOfIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(noOfIdColumn)
                        val imageUri = ContentUris.withAppendedId(contentUri, id)
                        uris.add(imageUri)
                    }
                }
            }
            images.update { uris }
        }
    }
}