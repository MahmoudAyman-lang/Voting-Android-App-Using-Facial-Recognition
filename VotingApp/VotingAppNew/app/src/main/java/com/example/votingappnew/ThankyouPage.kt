package com.example.votingappnew
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import android.annotation.TargetApi
import androidx.compose.animation.*
import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import com.example.votingappnew.models.Candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.privacysandbox.tools.core.model.Type
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ThankYouScreen(
    navController: NavController,
    voterName: String,
    voterId: String,
    votedFor: String
) {
    val view = LocalView.current
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // System UI handling
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }

    // Main container with gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD32F2F), Color.White),
                    startY = 0f,
                    endY = 400f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with app title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Voting System",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Main content card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Thank you message
                    Text(
                        text = "Thank You For Voting!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    // Horizontal gray empty frame for picture
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(200.dp)
                            .background(Color(0xFFF5F5F5))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF9E9E9E),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Empty space for picture
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Receipt section
                    Text(
                        text = "Voting Receipt",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ReceiptItem("Voter Name:", voterName)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ReceiptItem("College ID:", voterId)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ReceiptItem("Voted For:", votedFor)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your vote has been recorded securely",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Done button
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )

                Image(
                    painter = painterResource(id = R.drawable.ecu),
                    contentDescription = "College Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "ECU EDUCATION NEW ERA",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Secure Voting System • College Project • v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ReceiptItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF424242)
            )
        )
    }
}