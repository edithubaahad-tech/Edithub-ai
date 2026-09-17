package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.EditHubTheme
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeViewModel: HomeViewModel = viewModel()
            val editorViewModel: EditorViewModel = viewModel()
            val themeMode by homeViewModel.themeMode.collectAsState()

            EditHubTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkCanvas
                ) {
                    EditHubNavGraph(
                        homeViewModel = homeViewModel,
                        editorViewModel = editorViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun EditHubNavGraph(
    homeViewModel: HomeViewModel,
    editorViewModel: EditorViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                homeViewModel = homeViewModel,
                onCreateNewProject = { ratio ->
                    editorViewModel.initNewProject(ratio)
                    navController.navigate("editor")
                },
                onOpenProject = { projectId ->
                    editorViewModel.loadProject(projectId)
                    navController.navigate("editor")
                },
                onNavigateScriptToVideo = {
                    navController.navigate("script_to_video")
                },
                onNavigateThumbnailGenerator = {
                    navController.navigate("thumbnail_generator")
                },
                onNavigateSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("editor") {
            EditorScreen(
                editorViewModel = editorViewModel,
                onNavigateBack = {
                    editorViewModel.saveProjectToDatabase()
                    navController.popBackStack()
                },
                onNavigateExport = {
                    navController.navigate("export")
                }
            )
        }

        composable("script_to_video") {
            ScriptToVideoScreen(
                geminiService = homeViewModel.geminiService,
                onBack = { navController.popBackStack() },
                onOpenGeneratedProject = { title, scenes, style ->
                    editorViewModel.initProjectFromScenes(title, scenes, style)
                    navController.navigate("editor")
                }
            )
        }

        composable("thumbnail_generator") {
            ThumbnailGeneratorScreen(
                geminiService = homeViewModel.geminiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable("export") {
            ExportScreen(
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                homeViewModel = homeViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
