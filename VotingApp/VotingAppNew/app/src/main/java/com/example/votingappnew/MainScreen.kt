package com.example.votingappnew

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowInsets
import android.net.Uri
import androidx.activity.ComponentActivity
import android.annotation.TargetApi
import androidx.compose.animation.*
import android.app.Activity
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.votingappnew.models.Candidate
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder

private const val EMPTY_PARAM_PLACEHOLDER = "_EMPTY_PARAM_PLACEHOLDER_" // Generic placeholder

@TargetApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(
    navController: NavController,
    fullName: String? = null // Received from LoginScreen
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: run {
        navController.navigate("login") { popUpTo("main") { inclusive = true } }
        return
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    var candidates by remember { mutableStateOf<List<Candidate>>(emptyList()) }
    var selectedCandidateId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        db.collection("candidates")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    isLoading = false
                    errorMessage = "Failed to load candidates: ${e.message}"
                    Log.w("VotingApp", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    candidates = snapshots.map { doc ->
                        Candidate(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            age = doc.getLong("age") ?: 0,
                            totalVotes = doc.getLong("total-votes") ?: 0,
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    }
                }
                isLoading = false
            }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = selectedCandidateId != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Button(
                    onClick = {
                        selectedCandidateId?.let { candidateIdValue ->
                            val selectedCandidateObject = candidates.find { it.id == candidateIdValue }
                            val user = auth.currentUser
                            val userEmail = user?.email

                            // 1. Determine voterName, ensuring it's not blank for the path
                            val voterNameForPath = fullName?.takeIf { it.isNotBlank() }
                                ?: user?.displayName?.takeIf { it.isNotBlank() }
                                ?: userEmail?.substringBefore("@")?.takeIf { it.isNotBlank() }
                                ?: "Anonymous" // Fallback to NavHost default for 'name'

                            // 2. Determine collegeId, ensuring it's not blank for the path
                            var collegeIdForPath = userEmail?.substringBefore("@")?.takeIf { it.isNotBlank() }
                                ?: user?.uid?.takeIf { it.isNotBlank() }
                            if (collegeIdForPath.isNullOrBlank()) {
                                // NavHost default for 'id' is "", if ThankYouScreen handles it, fine.
                                // But for path construction, an empty segment is bad.
                                collegeIdForPath = EMPTY_PARAM_PLACEHOLDER
                            }

                            // 3. Determine candidateName, ensuring it's not blank for the path
                            val candidateNameForPath = selectedCandidateObject?.name?.takeIf { it.isNotBlank() }
                                ?: "Candidate" // Fallback to NavHost default for 'candidate'

                            // URL Encode all parameters that will form path segments
                            val encodedName = URLEncoder.encode(voterNameForPath, "UTF-8")
                            val encodedCollegeId = URLEncoder.encode(collegeIdForPath, "UTF-8")
                            val encodedCandidate = URLEncoder.encode(candidateNameForPath, "UTF-8")

                            val navigationRoute = "thankyou/$encodedName/$encodedCollegeId/$encodedCandidate"
                            Log.d("VotingApp", "Attempting to navigate to: $navigationRoute")

                            db.collection("votes")
                                .add(hashMapOf(
                                    "user_ID" to userId,
                                    "vote_Choice" to candidateIdValue,
                                    "timestamp" to FieldValue.serverTimestamp()
                                ))
                                .addOnSuccessListener {
                                    db.collection("candidates")
                                        .document(candidateIdValue)
                                        .update("total-votes", FieldValue.increment(1))
                                        .addOnSuccessListener {
                                            try {
                                                navController.navigate(navigationRoute) {
                                                    popUpTo("main") { inclusive = true }
                                                }
                                            } catch (navEx: Exception) {
                                                Log.e("VotingApp", "Navigation error after vote success", navEx)
                                                errorMessage = "Navigation error: ${navEx.message}"
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("VotingApp", "Failed to update candidate votes", e)
                                            errorMessage = "Failed to update vote count: ${e.message}"
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("VotingApp", "Failed to submit vote", e)
                                    errorMessage = "Failed to submit vote: ${e.message}"
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Confirm Vote")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (candidates.isEmpty()) {
                Text(
                    "No candidates available",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "Select Candidate",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(candidates, key = { it.id }) { candidate ->
                            CandidateItem(
                                candidate = candidate,
                                isSelected = candidate.id == selectedCandidateId,
                                onSelect = { selectedCandidateId = candidate.id }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CandidateItem(candidate: Candidate, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect() }
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = candidate.imageUrl,
                contentDescription = candidate.name + " image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop,
                loading = null,
                failure = null
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Age: ${candidate.age}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Votes: ${candidate.totalVotes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                )
            }
        }
    }
}

