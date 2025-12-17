package com.chat.app.ui.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.chat.app.data.local.entities.ChatEntity
import com.chat.app.navigation.Routes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 🔥 REMOVED: ChatItem and dummyChats - now using real ChatEntity from database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    
    // 🔥 COLLECT UI STATE FROM VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                userName = uiState.currentUser.fullName,
                profilePictureUrl = uiState.currentUser.profilePictureUrl,
                onProfileClick = {
                    navController?.navigate(Routes.PROFILE)
                    scope.launch { drawerState.close() }
                },
                onContactsClick = {
                    navController?.navigate(Routes.CONTACTS)
                    scope.launch { drawerState.close() }
                },
                onSettingsClick = {
                    navController?.navigate(Routes.SETTINGS)
                    scope.launch { drawerState.close() }
                },
                onLogoutClick = {
                    viewModel.logout()
                    navController?.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // 🔥 USE VIEWMODEL SEARCH STATE
                        if (uiState.isSearching) {
                            TextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { 
                                    Text(
                                        "Search chats...",
                                        color = Color.Gray
                                    ) 
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color.Gray
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.updateSearchQuery("") }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }
                            )
                        } else {
                            Text("Chats (${uiState.chats.size})")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // 🔥 USE VIEWMODEL SEARCH TOGGLE
                        IconButton(
                            onClick = { viewModel.toggleSearch() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (uiState.isSearching) "Close search" else "Search"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            
            // 🔥 USE VIEWMODEL UI STATE FOR DIFFERENT SCREENS
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    // Loading state
                    uiState.shouldShowLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading chats...",
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // Empty state
                    uiState.shouldShowEmpty -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (uiState.searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.AutoMirrored.Filled.Chat,
                                contentDescription = if (uiState.searchQuery.isNotEmpty()) "No search results" else "No chats",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) {
                                    "No chats found for \"${uiState.searchQuery}\""
                                } else {
                                    "No chats yet"
                                },
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) {
                                    "Try a different search term"
                                } else {
                                    "Start a conversation!"
                                },
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // Error state
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error loading chats",
                                fontSize = 18.sp,
                                color = Color.Red
                            )
                            Text(
                                text = uiState.errorMessage ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // Show chats
                    uiState.shouldShowChats -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.chats) { chatEntity ->
                                // 🔥 USE NEW CHAT ROW FOR ENTITIES
                                ChatRowFromEntity(
                                    chatEntity = chatEntity,
                                    onChatClick = { chatId ->
                                        viewModel.onChatClicked(chatId)
                                        // Navigate to chat details with otherUserId and otherUserName
                                        // TODO: Get actual display name from UserRepository
                                        val encodedUserName = Uri.encode(chatEntity.otherUserName)
                                        navController?.navigate("${Routes.CHAT_DETAIL}/${chatEntity.otherUserId}/$encodedUserName")
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 🔥 SEARCH RESULTS INFO
                if (uiState.isSearching && uiState.searchQuery.isNotEmpty() && uiState.chats.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search results",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${uiState.chats.size} result${if (uiState.chats.size != 1) "s" else ""} for \"${uiState.searchQuery}\"",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDrawer(
    userName: String = "",
    profilePictureUrl: String? = null,
    onProfileClick: () -> Unit,
    onContactsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        // Header with proper status bar padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profilePictureUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Show initials or default icon
                        val initials = userName.split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                        
                        if (initials.isNotEmpty()) {
                            Text(
                                text = initials,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName.ifEmpty { "Chat App" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (userName.isNotEmpty()) "Welcome back!" else "Welcome!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("My Profile") },
            selected = false,
            onClick = onProfileClick,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
            label = { Text("Contacts") },
            selected = false,
            onClick = onContactsClick,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = onSettingsClick,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout") },
            label = { Text("Logout") },
            selected = false,
            onClick = onLogoutClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
fun ChatRowFromEntity(
    chatEntity: ChatEntity,
    onChatClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick(chatEntity.chatId) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture placeholder
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = getColorForUser(chatEntity.otherUserId),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chatEntity.otherUserId.firstOrNull()?.toString()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chatEntity.otherUserName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = chatEntity.lastMessage ?: "No messages yet",
                color = Color.Gray,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(chatEntity.timestamp),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * Generate consistent color for each user
 */
private fun getColorForUser(userId: String): Color {
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
    return colors[userId.hashCode().mod(colors.size)]
}

/**
 * Format timestamp for display
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        diff < 604800_000 -> {
            val formatter = SimpleDateFormat("EEE", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        else -> {
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}

// 🔥 REMOVED: Old ChatRow function - now using ChatRowFromEntity with real ChatEntity data
@Preview
@Composable
fun HomeScreenPreview() {
    // 🔥 Preview now uses real ViewModel with database
    HomeScreen()
}