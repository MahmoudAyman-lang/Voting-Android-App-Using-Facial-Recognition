package com.example.votingappnew
import android.app.Activity
import android.view.WindowManager
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (String) -> Unit  // Update to pass fullName to MainScreen
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

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

    var fullName by remember { mutableStateOf("") }
    var collegeId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Spacer(modifier = Modifier.height(20.dp))

            // Edge-to-edge red box
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "User Authentication",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // Full Name field
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFFD32F2F),
                        focusedTextColor = Color.Black,  // Add this
                        unfocusedTextColor = Color.Black,  // Add this
                        cursorColor = Color(0xFFD32F2F)  // Optional: Custom cursor color
                    )
                )


                // College ID field
                OutlinedTextField(
                    value = collegeId,
                    onValueChange = { collegeId = it },
                    label = { Text("College ID") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFFD32F2F),
                        focusedTextColor = Color.Black,  // Add this
                        unfocusedTextColor = Color.Black,  // Add this
                        cursorColor = Color(0xFFD32F2F)  // Optional: Custom cursor color
                    )
                )

                // Continue Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (fullName.isNotEmpty() && collegeId.isNotEmpty()) {
                            isLoading = true
                            val email = "$collegeId@ecu.edu.eg"
                            val password = "default password"

                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onLoginSuccess(fullName)  // Pass fullName to callback
                                    } else {
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { createTask ->
                                                if (createTask.isSuccessful) {
                                                    onLoginSuccess(fullName)  // Pass fullName to callback
                                                } else {
                                                    errorMessage = "Authentication failed: ${createTask.exception?.message}"
                                                }
                                            }
                                    }
                                }
                        } else {
                            errorMessage = "Please fill all fields"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Continue >", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

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
                    modifier = Modifier.size(300.dp) // Reduced size for better fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Secure Voting System • College Project • v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}