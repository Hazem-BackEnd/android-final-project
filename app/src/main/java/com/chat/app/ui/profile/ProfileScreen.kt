package com.chat.app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()

    // Load user data on first composition
    LaunchedEffect(Unit) {
        // Try to get current user ID (you might want to get this from AuthRepository)
        val currentUserId = "current_user" // TODO: Get from authentication
        viewModel.loadUserProfile(currentUserId)
        
        // Also load from SharedPreferences as fallback
        val savedUser = UserPrefs.loadUser(context)
        viewModel.loadFromSharedPreferences(savedUser)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateProfileImage(uri)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F2F2)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Profile") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${uiState.errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.clearErrorMessage() }) {
                                Text("Retry")
                            }
                        }
                    }
                    
                    else -> {
                        ProfileContent(
                            uiState = uiState,
                            isEditing = isEditing,
                            onUsernameChange = viewModel::updateUsername,
                            onPhoneChange = viewModel::updatePhone,
                            onEmailChange = viewModel::updateEmail,
                            onImagePick = { pickImageLauncher.launch("image/*") },
                            onToggleEdit = { 
                                viewModel.toggleEditMode()
                                // Save to SharedPreferences when saving
                                if (!isEditing) {
                                    val userData = viewModel.saveToSharedPreferences()
                                    UserPrefs.saveUser(context, userData.username, userData.phone, userData.email, userData.profileUri)
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Success message
                uiState.successMessage?.let { message ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    isEditing: Boolean,
    onUsernameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onImagePick: () -> Unit,
    onToggleEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable(enabled = isEditing) {
                    onImagePick()
                },
            contentAlignment = Alignment.Center
        ) {
            val imageModel = uiState.profileImageUri ?: uiState.profileImageUrl
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Username
        if (isEditing) {
            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                singleLine = true
            )
        } else {
            Text(
                text = uiState.username.ifEmpty { "No username" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Phone
        ProfileRow(
            label = "Phone: ",
            value = uiState.phone,
            isEditing = isEditing,
            onValueChange = onPhoneChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email
        ProfileRow(
            label = "Email: ",
            value = uiState.email,
            isEditing = isEditing,
            onValueChange = onEmailChange
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onToggleEdit,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = if (isEditing) "Save Changes" else "Edit Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileRow(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Spacer(modifier = Modifier.height(6.dp))


        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}


/*

 */
