package com.chat.app.ui.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chat.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenForTesting(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    // Simple validation for testing
    fun validateField(fieldName: String, value: String) {
        val errors = validationErrors.toMutableMap()
        when (fieldName) {
            "username" -> {
                if (value.isBlank()) errors["username"] = "Username is required"
                else if (value.length < 3) errors["username"] = "Username must be at least 3 characters"
                else errors.remove("username")
            }
            "phone" -> {
                if (value.isBlank()) errors["phone"] = "Phone number is required"
                else if (value.length < 10) errors["phone"] = "Phone number must be at least 10 digits"
                else errors.remove("phone")
            }
            "email" -> {
                if (value.isBlank()) errors["email"] = "Email is required"
                else if (!value.contains("@")) errors["email"] = "Invalid email format"
                else errors.remove("email")
            }
            "password" -> {
                if (value.isBlank()) errors["password"] = "Password is required"
                else if (value.length < 6) errors["password"] = "Password must be at least 6 characters"
                else errors.remove("password")
            }
        }
        validationErrors = errors
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(192, 192, 192)
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(192, 192, 192)),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // PROFILE IMAGE PICKER
                Box(
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color(192, 192, 192))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.icon),
                                contentDescription = "Default Profile Icon",
                                modifier = Modifier.size(210.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Select Image",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // TEXT FIELDS
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        validateField("username", it)
                    },
                    label = { Text("Username") },
                    singleLine = true,
                    isError = validationErrors.containsKey("username"),
                    supportingText = {
                        validationErrors["username"]?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        validateField("phone", it)
                    },
                    label = { Text("Phone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = validationErrors.containsKey("phone"),
                    supportingText = {
                        validationErrors["phone"]?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        validateField("email", it)
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = validationErrors.containsKey("email"),
                    supportingText = {
                        validationErrors["email"]?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        validateField("password", it)
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = validationErrors.containsKey("password"),
                    supportingText = {
                        validationErrors["password"]?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (username.isNotBlank() && phone.isNotBlank() && 
                            email.isNotBlank() && password.isNotBlank() &&
                            validationErrors.isEmpty()) {
                            // Simple navigation for testing
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    },
                    enabled = username.isNotBlank() && 
                              phone.isNotBlank() && 
                              email.isNotBlank() && 
                              password.isNotBlank() &&
                              validationErrors.isEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(
                        "Create Account",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("This app needs access to your contacts to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}