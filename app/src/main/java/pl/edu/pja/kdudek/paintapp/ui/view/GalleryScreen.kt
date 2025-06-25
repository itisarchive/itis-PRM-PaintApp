package pl.edu.pja.kdudek.paintapp.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import pl.edu.pja.kdudek.paintapp.Destination
import pl.edu.pja.kdudek.paintapp.R
import pl.edu.pja.kdudek.paintapp.ui.viewmodel.GalleryScreenViewModel

val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
} else {
    Manifest.permission.READ_EXTERNAL_STORAGE
}

@Composable
fun GalleryScreen(
    viewModel: GalleryScreenViewModel = viewModel(),
    controller: NavController
) {
    val ctx = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ctx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        hasPermissions = it
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            viewModel.loadImages(ctx)
        } else {
            requestPermission.launch(permission)
        }
    }

    if (hasPermissions) {
        val images by viewModel.images.collectAsStateWithLifecycle()
        GalleryView(images, { controller.navigate(Destination.Drawing.pathTo(it)) })
    } else {
        NoPermission(
            onClick = {
                requestPermission.launch(permission)
            }
        )
    }
}

@Composable
private fun NoPermission(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.no_permission))
        Button(onClick = onClick) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

@Composable
private fun GalleryView(
    uris: List<Uri>,
    onSelect: (Uri) -> Unit,
) {
    if (uris.isEmpty()) {
        Text(stringResource(R.string.no_images_found_in_gallery))
    }
    LazyVerticalGrid(
        modifier = Modifier.padding(
            horizontal = 8.dp,
        ),
        columns = GridCells.Adaptive(minSize = 120.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            uris,
            key = { it.toString() }
        ) {
            Image(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable(onClick = { onSelect(it) }),
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}