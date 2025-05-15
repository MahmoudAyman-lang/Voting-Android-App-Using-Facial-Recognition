package com.example.votingappnew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.votingappnew.ui.theme.VotingAppNewTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VotingAppNewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    VotingAppNavigation(
                        navController = navController,
                        auth = auth
                    )
                }
            }
        }
    }
}

@Composable
fun VotingAppNavigation(navController: NavHostController, auth: FirebaseAuth) {
    NavHost(
        navController = navController,
        startDestination = if (auth.currentUser == null) "login" else "main"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = { fullName ->
                    // Navigate to camera instruction screen instead of directly to main
                    navController.navigate("cameraInstruction/$fullName") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Add the new Camera Instruction screen
        composable(
            route = "cameraInstruction/{fullName}",
            arguments = listOf(
                navArgument("fullName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val fullName = backStackEntry.arguments?.getString("fullName") ?: ""
            CameraInstructionScreen(
                navController = navController,
                fullName = fullName
            )
        }

        // Update main route to include fullName parameter
        composable(
            route = "main/{fullName}",
            arguments = listOf(
                navArgument("fullName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val fullName = backStackEntry.arguments?.getString("fullName") ?: ""
            MainScreen(
                navController = navController,
                fullName = fullName
            )
        }

        // Add a fallback route for main without parameters (for when coming from auth check)
        composable("main") {
            MainScreen(
                navController = navController,
                fullName = "" // Ensure this matches the function signature
            )
        }

        composable(
            route = "thankyou/{name}/{id}/{candidate}",
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = "Anonymous"
                },
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("candidate") {
                    type = NavType.StringType
                    defaultValue = "Candidate"
                }
            )
        ) { backStackEntry ->
            ThankYouScreen(
                navController = navController,
                voterName = backStackEntry.arguments?.getString("name") ?: "Anonymous",
                voterId = backStackEntry.arguments?.getString("id") ?: "",
                votedFor = backStackEntry.arguments?.getString("candidate") ?: "Candidate"
            )
        }
    }
}