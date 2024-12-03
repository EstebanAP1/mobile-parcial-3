package com.example.parcial_3

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parcial_3.model.Album
import com.example.parcial_3.viewmodel.AlbumViewModel
import com.example.parcial_3.viewmodel.UiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private fun fetchLocation(onLocationReceived: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                onLocationReceived(location)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            var locationText by remember { mutableStateOf("Fetching location...") }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) {
                        fetchLocation { location ->
                            locationText = "Lat: ${location?.latitude}, Long: ${location?.longitude}"
                        }
                    } else {
                        locationText = "Permission denied"
                    }
                }
            )

            LaunchedEffect(Unit) {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    fetchLocation { location ->
                        locationText = "Lat: ${location?.latitude}, Long: ${location?.longitude}"
                    }
                }
            }

            App(locationText)
        }
    }
}

@Composable
fun App(locationText: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) { navController.navigate("home") } }
        composable("register") { RegisterScreen { navController.navigate("login") } }
        composable("home") { HomeScreen(navController) }
        composable("addSong") { AddSongScreen { navController.navigate("home") } }
        composable("albums") { AlbumsScreen {navController.navigate("home")} }
        composable("location") { LocationScreen(locationText) {navController.navigate("home")} }
    }
}

@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { errorMessage = it.message }
                }
            ) {
                Text("Login")
            }

            Button(
                onClick = {
                    navController.navigate("register")
                }
            ) {
                Text("Register")
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error)}
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onRegisterSuccess() }
                        .addOnFailureListener { errorMessage = it.message }
                }
            ) {
                Text("Register")
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error)}
        }
    }
}


@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { navController.navigate("addSong") }) {
                Text("Add song")
            }
            Button(onClick = { navController.navigate("albums") }) {
                Text("Albums")
            }
            Button(onClick = { navController.navigate("location") }) {
                Text("Location")
            }
        }
    }
}

@Composable
fun AddSongScreen(backToHome: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var inputAuthor by remember { mutableStateOf("") }
    var inputTitle by remember { mutableStateOf("") }
    var inputAlbum by remember { mutableStateOf("") }
    var inputYear by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                backToHome()
            }) {
                Text("Go Back")
            }
            TextField(
                value = inputAuthor,
                onValueChange = { inputAuthor = it },
                label = { Text("Author") }
            )
            TextField(
                value = inputTitle,
                onValueChange = { inputTitle = it },
                label = { Text("Title") }
            )
            TextField(
                value = inputAlbum,
                onValueChange = { inputAlbum = it },
                label = { Text("Album") }
            )
            TextField(
                value = inputYear,
                onValueChange = { inputYear = it },
                label = { Text("Year") }
            )
            Button(onClick = {
                val data = hashMapOf(
                    "author" to inputAuthor,
                    "title" to inputTitle,
                    "album" to inputAlbum,
                    "year" to inputYear
                )
                db.collection("songs")
                    .add(data)
                    .addOnSuccessListener { message = "Song added" }
                    .addOnFailureListener { message = it.message }
            }) {
                Text("Add song")
            }

            message?.let { Text(it) }
        }
    }
}

@Composable
fun AlbumsScreen(viewModel: AlbumViewModel = viewModel(), backToHome: () -> Unit) {
    val state by viewModel.uiState

    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = {
                        backToHome()
                    }) {
                        Text("Go Back")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AlbumsList(albums = (state as UiState.Success).albums)
                }
            }
        }

        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error ${(state as UiState.Error).message}")
            }
        }
    }
}

@Composable
fun AlbumsList(albums: List<Album>) {
    LazyColumn(modifier = Modifier.padding(20.dp)) {
        items(albums) { album -> AlbumItem(album = album) }
    }
}

@Composable
fun AlbumItem(album: Album) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "UserID: ${album.userId}", style = MaterialTheme.typography.titleMedium)
            Text("ID: ${album.id}")
            Text("Title: ${album.title}")
        }
    }
}

@Composable
fun LocationScreen(locationText: String, backToHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                backToHome()
            }) {
                Text("Go Back")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Location:",
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = locationText,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}