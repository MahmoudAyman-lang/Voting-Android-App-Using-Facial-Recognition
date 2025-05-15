package com.example.votingappnew


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.app.Activity
import androidx.compose.material.icons.Icons
//mport androidx.compose.material.icons.filled.Camera
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

@Composable
fun CameraInstructionScreen(
    navController: NavController,
    fullName: String? = null,
    autoNavigateAfter: Long = 7000 // 7 seconds in milliseconds
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Hide system UI (status bar and navigation bar)
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, view)

        // Hide both system bars
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // Make them appear transiently when swiped
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // Optional: Show bars again when leaving screen
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Auto-navigate after the specified delay
    LaunchedEffect(Unit) {
        delay(autoNavigateAfter)
        // Navigate to main screen after delay
        navController.navigate("main/${fullName ?: ""}") {
            popUpTo("cameraInstruction") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Red header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Voting System",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Camera Icon
//            Icon(
//                painter = rememberVectorPainter(Icons.Default.Camera),
//                contentDescription = "Camera",
//                modifier = Modifier.size(120.dp),
//                tint = Color(0xFFD32F2F)
//            )

            Spacer(modifier = Modifier.height(40.dp))

            // Instruction text
            Text(
                text = "Please Stand In Front Of The Camera",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-instruction
            Text(
                text = "Proceeding to voting screen in a few seconds...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )

                Image(
                    painter = painterResource(id = R.drawable.ecu),
                    contentDescription = "Voting System Logo",
                    modifier = Modifier.size(240.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Secure Voting System • College Project • v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Progress indicator
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter),
            color = Color(0xFFD32F2F)
        )
    }
}