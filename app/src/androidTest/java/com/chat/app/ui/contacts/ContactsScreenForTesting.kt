package com.chat.app.ui.contacts

import android.Manifest
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
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreenForTesting(navController: NavController) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Sample contacts for testing
    val sampleContacts = remember {
        listOf(
            Contact("1", "Alice Johnson", "+1234567890", true),
            Contact("2", "Bob Smith", "+1234567891", false),
            Contact("3", "Charlie Brown", "+1234567892", true),
            Contact("4", "Diana Prince", "+1234567893", false),
            Contact("5", "Edward Norton", "+1234567894", true),
            Contact("6", "Fiona Green", "+1234567895", false),
            Contact("7", "George Wilson", "+1234567896", true),
            Contact("8", "Hannah Davis", "+1234567897", false)
        )
    }

    val filteredContacts = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            sampleContacts
        } else {
            sampleContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phone.contains(searchQuery)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts (${filteredContacts.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh action */ }) {
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
                onValueChange = { searchQuery = it },
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
                
                filteredContacts.isEmpty() -> {
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
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            ContactItem(
                                contact = contact,
                                onContactClick = { 
                                    // For testing, directly navigate with contact name
                                    navController.navigate("chat_detail/chat_${contact.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}