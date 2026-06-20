package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.GitaViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          GitaAppHost()
        }
      }
    }
  }
}

@Composable
fun GitaAppHost() {
  val navController = rememberNavController()
  val viewModel: GitaViewModel = viewModel()

  NavHost(
    navController = navController,
    startDestination = "home",
    modifier = Modifier.fillMaxSize()
  ) {
    composable("home") {
      HomeScreen(navController = navController, viewModel = viewModel)
    }
    composable("chat") {
      ChatScreen(navController = navController, viewModel = viewModel)
    }
    composable("camera") {
      CameraScreen(navController = navController, viewModel = viewModel)
    }
    composable("shloka_library") {
      ShlokaScreen(navController = navController, viewModel = viewModel)
    }
    composable("mood_solver") {
      MoodScreen(navController = navController, viewModel = viewModel)
    }
    composable("reflection_journal") {
      JournalScreen(navController = navController, viewModel = viewModel)
    }
    composable("settings") {
      SettingsScreen(navController = navController, viewModel = viewModel)
    }
  }
}

