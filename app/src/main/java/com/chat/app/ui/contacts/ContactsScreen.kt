package com.chat.app.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val isOnline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(navController: NavController) {
    // Sample contacts data
    val contacts = remember {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts (${contacts.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(contacts) { contact ->
                ContactItem(
                    contact = contact,
                    onContactClick = { 
                        // TODO: Navigate to chat with this contact
                        navController.navigate("chat_detail/${contact.name}")
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
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
                        color = getColorForContact(contact.id),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (contact.isOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Color.Green,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                Text(
                    text = contact.phone,
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