package com.example.chatapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale


data class ChatItem(
    val name: String,
    val lastMessage: String,
    val time: String
)

val dummyChats = listOf(
    ChatItem("Ahmed", "Hey, how are you?", "10:45 AM"),
    ChatItem("Mariam", "Let's meet tomorrow", "9:30 AM"),
    ChatItem("Omar", "Thanks a lot!", "Yesterday"),
    ChatItem("Sara", "See you soon", "Yesterday")
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = if (searchQuery.isEmpty()) dummyChats
    else dummyChats.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(Modifier.width(250.dp)) {
                // Profile Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (CurrentUser.user.profileUri != null) {
                        AsyncImage(
                            model = CurrentUser.user.profileUri,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.Gray, shape = CircleShape),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CurrentUser.user.username.ifEmpty { "User" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                HorizontalDivider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )


                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    DrawerItem(Icons.Default.Person, "My Profile") {
                        navController.navigate("profile")
                    }
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DrawerItem(Icons.Default.Group, "New Group") { /* TODO */ }
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DrawerItem(Icons.Default.Contacts, "Contacts") { /* TODO */ }
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DrawerItem(Icons.Default.Settings, "Settings") { /* TODO */ }
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DrawerItem(Icons.Default.Logout, "Logout") {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (isSearching) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search chats") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Chats")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isSearching = !isSearching
                            if (!isSearching) searchQuery = ""
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(filteredChats) { chat ->
                    ChatRow(chat) {
                        // Create initial messages list for this chat
                        val chatMessages = arrayListOf(
                            Message(chat.lastMessage, isUser = false, time = chat.time)
                        )

                        // Pass messages via Bundle
                        navController.currentBackStackEntry?.arguments?.putParcelableArrayList("messages", chatMessages)
                        navController.navigate("chat_detail/${chat.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRow(chat: ChatItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(50.dp).background(Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(chat.name.first().toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(chat.lastMessage, color = Color.Gray, maxLines = 1)
        }

        Text(chat.time, color = Color.Gray, fontSize = 12.sp)
    }

    HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
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
        Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 18.sp)
    }
}

