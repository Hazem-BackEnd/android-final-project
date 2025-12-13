package com.example.chatapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    val context = LocalContext.current

    // Load saved data
    LaunchedEffect(Unit) {
        val savedUser = UserPrefs.loadUser(context)
        CurrentUser.user.username = savedUser.username
        CurrentUser.user.phone = savedUser.phone
        CurrentUser.user.email = savedUser.email
        CurrentUser.user.profileUri = savedUser.profileUri
    }
    var isEditing by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf(CurrentUser.user.username) }
    var phone by remember { mutableStateOf(CurrentUser.user.phone) }
    var email by remember { mutableStateOf(CurrentUser.user.email) }
    var profileUri by remember { mutableStateOf(CurrentUser.user.profileUri) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileUri = uri
            CurrentUser.user.profileUri = uri
        }
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable(enabled = isEditing) {
                            pickImageLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileUri != null) {
                        AsyncImage(
                            model = profileUri,
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
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true
                    )
                } else {
                    Text(username, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Phone
                ProfileRow(
                    label = "Phone: ",
                    value = phone,
                    isEditing = isEditing,
                    onValueChange = { phone = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                ProfileRow(
                    label = "Email: ",
                    value = email,
                    isEditing = isEditing,
                    onValueChange = { email = it }
                )

                Spacer(modifier = Modifier.height(30.dp))


                Button(
                    onClick = {
                        if (isEditing) {
                            // Save changes
                            CurrentUser.user.username = username
                            CurrentUser.user.phone = phone
                            CurrentUser.user.email = email

                            CurrentUser.save(context)
                        }
                        isEditing = !isEditing
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) Color.Black else Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isEditing) "Save Changes" else "Edit Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
            )        }
    }
}
