package com.chat.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chat.app.data.repository.AuthRepository
import com.chat.app.navigation.Routes
import com.chat.app.ui.profile.CurrentUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    // 🔥 SETUP VIEWMODEL WITH FACTORY
    val context = LocalContext.current
    val authRepository = AuthRepository()
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(authRepository)
    )
    
    // 🔥 COLLECT UI STATE FROM VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()
    
    var showAboutQuote by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // 🔥 HANDLE LOGOUT SUCCESS
    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
            viewModel.resetLogoutState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp)
        ) {

            // Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("profile") }
                    .padding(vertical = 16.dp)
            ) {
                if (CurrentUser.user.profileUri != null) {
                    AsyncImage(
                        model = CurrentUser.user.profileUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Gray, shape = CircleShape),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = CurrentUser.user.username.ifEmpty { "User" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "View and edit profile",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 DARK THEME - USING VIEWMODEL STATE
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Theme", fontSize = 18.sp)
                Switch(
                    checked = uiState.isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme() }
                )
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 🔥 NOTIFICATIONS - USING VIEWMODEL STATE
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notifications", fontSize = 18.sp)
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() }
                )
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Privacy & Security
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO */ }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Privacy",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Privacy & Security", fontSize = 18.sp)
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // About section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutQuote = !showAboutQuote }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "About",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("About", fontSize = 18.sp)
                }

                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (showAboutQuote) {
                    Text(
                        text = "\"Communication is the key to connection.\"",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 🔥 ERROR MESSAGE DISPLAY
            if (uiState.shouldShowError) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 🔥 LOGOUT BUTTON WITH VIEWMODEL
            Button(
                onClick = { showLogoutDialog = true },
                enabled = !uiState.shouldShowLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (uiState.shouldShowLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Logout", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }

    // 🔥 LOGOUT CONFIRMATION DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { 
                Text(
                    "Logout Confirmation",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text("Are you sure you want to logout? You will need to login again to access your chats.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(NavController(LocalContext.current))
}

