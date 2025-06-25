package pl.edu.pja.kdudek.paintapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.pja.kdudek.paintapp.ui.theme.PaintAppTheme
import pl.edu.pja.kdudek.paintapp.ui.view.DrawingScreen
import pl.edu.pja.kdudek.paintapp.ui.view.GalleryScreen
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaintAppTheme {
                val controller = rememberNavController()
                Scaffold {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        NavHost(
                            navController = controller,
                            startDestination = Destination.Gallery.path
                        ) {
                            composable(Destination.Gallery.path) {
                                GalleryScreen(
                                    controller = controller
                                )
                            }
                            composable(Destination.Drawing.path) { backStackEntry ->
                                val encodedUri = backStackEntry.arguments?.getString("uri")
                                    ?: throw IllegalArgumentException("URI argument is required")
                                DrawingScreen(
                                    uri = encodedUri.decodeUri()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun String.decodeUri(): Uri {
    return URLDecoder.decode(this, "UTF-8").toUri()
}

sealed class Destination(
    val path: String,
) {
    data object Gallery : Destination("/home")
    data class Drawing(
        val uri: String
    ) : Destination(path) {
        companion object {
            const val path = "/drawing/{uri}"
            fun pathTo(uri: Uri): String {
                val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                return "/drawing/$encodedUri"
            }
        }
    }
}
