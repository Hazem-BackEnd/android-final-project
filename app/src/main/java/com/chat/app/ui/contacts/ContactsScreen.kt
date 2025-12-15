package com.chat.app.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.chat.app.data.repository.ContactsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(navController: NavController) {
    // 🔥 SETUP VIEWMODEL WITH FACTORY
    val context = LocalContext.current
    val contactsRepository = ContactsRepository(context)
    val viewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(contactsRepository)
    )
    
    // 🔥 COLLECT UI STATE FROM VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadDeviceContacts()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadDeviceContacts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (uiState.searchQuery.isNotEmpty()) {
                        Text("Search Results (${uiState.filteredContacts.size})")
                    } else {
                        Text("Contacts (${uiState.contacts.size})")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshContacts() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.updateSearchQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )

            when {
                !hasPermission -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Contacts permission is required to view your contacts",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
                
                uiState.shouldShowLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshContacts() }) {
                            Text("Retry")
                        }
                    }
                }
                
                uiState.shouldShowEmpty || (uiState.searchQuery.isNotEmpty() && uiState.filteredContacts.isEmpty()) -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No contacts found" else "No contacts available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
                
                uiState.shouldShowContacts -> {
                    // Get current user ID from Firebase Auth
                    val authManager = FirebaseAuthManager()
                    val currentUserId = authManager.currentUserId ?: "current_user"
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.filteredContacts) { contact ->
                            ContactItemFromEntity(
                                contact = contact,
                                onContactClick = { 
                                    // Navigate to chat with this contact
                                    // Use contact.uid (phone number digits only) for chatId
                                    val otherUserId = contact.uid
                                    val chatId = generateChatId(currentUserId, otherUserId)
                                    // URL encode the contact name to handle special characters (spaces, slashes, etc.)
                                    val encodedName = Uri.encode(contact.fullName)
                                    navController.navigate("chat_detail/$chatId/$encodedName")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🔥 NEW: ContactItem that works with UserEntity from device contacts
 */
@Composable
fun ContactItemFromEntity(
    contact: UserEntity,
    onContactClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onContactClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = getColorForContact(contact.uid),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 🔥 Display profile picture or initial
                if (contact.profilePictureUrl != null) {
                    // TODO: Load profile picture using Coil
                    // For now, show initial
                    Text(
                        text = contact.fullName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = contact.fullName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 🔥 Display full name from device contacts
                Text(
                    text = contact.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                // 🔥 Display phone number from device contacts
                Text(
                    text = contact.phoneNumber,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Contact",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getColorForContact(contactId: String): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green  
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336), // Red
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF795548), // Brown
        Color(0xFF009688)  // Teal
    )
    return colors[contactId.hashCode().mod(colors.size)]
}

/**
 * Generate consistent chatId for two users
 */
private fun generateChatId(userId1: String, userId2: String): String {
    // Sort user IDs to ensure consistent chatId regardless of order
    val sortedIds = listOf(userId1, userId2).sorted()
    return "${sortedIds[0]}_${sortedIds[1]}"
}