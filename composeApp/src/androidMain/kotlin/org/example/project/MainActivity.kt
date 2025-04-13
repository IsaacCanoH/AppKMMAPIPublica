package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieApp()
        }
    }
}

@Serializable
data class MovieResponse(
    val Title: String,
    val Year: String,
    val Director: String,
    val Plot: String,
    val Poster: String
)

suspend fun fetchMovie(title: String): MovieResponse? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return try {
        val url = "https://www.omdbapi.com/?apikey=bf2ead91&t=$title"
        client.get(url).body()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        client.close()
    }
}

@Composable
fun MovieApp() {
    val backgroundColor = Color(0xFF101820)
    val primaryTextColor = Color(0xFFE0E0E0)
    val accentColor = Color(0xFF64B5F6) // Azul claro
    val inputBackground = Color(0xFF1E2A38)

    var searchQuery by remember { mutableStateOf("") }
    var movie by remember { mutableStateOf<MovieResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = backgroundColor,
            surface = inputBackground,
            onSurface = primaryTextColor,
            primary = accentColor
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        "🎬 Movie Explorer",
                        style = typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar película", color = primaryTextColor) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(inputBackground, shape = MaterialTheme.shapes.medium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = accentColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            movie = null
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = fetchMovie(searchQuery)
                                withContext(Dispatchers.Main) {
                                    movie = result
                                    isLoading = false
                                }
                            }
                        },
                        enabled = searchQuery.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Buscar")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = accentColor)
                    }

                    movie?.let {
                        Text(
                            it.Title,
                            style = typography.headlineSmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Año: ${it.Year}",
                            style = typography.bodyLarge.copy(color = primaryTextColor)
                        )
                        Text(
                            "Director: ${it.Director}",
                            style = typography.bodyLarge.copy(color = primaryTextColor)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Image(
                            painter = rememberAsyncImagePainter(it.Poster),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(400.dp)
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            it.Plot,
                            style = typography.bodyLarge.copy(
                                color = primaryTextColor,
                                fontFamily = FontFamily.Serif
                            ),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
