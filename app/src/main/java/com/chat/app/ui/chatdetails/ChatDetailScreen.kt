package com.chat.app.ui.chatdetails

import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chat.app.R
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.local.dao.ChatDao
import com.chat.app.data.local.entities.MessageEntity
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.MessageRepository
import com.chat.app.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val text: String,
    val isUser: Boolean,
    val time: String
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    otherUserId: String,
    otherUserName: String,
    navController: NavController
) {
    // 🔥 SETUP VIEWMODEL WITH FACTORY
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val chatRepository = ChatRepository(database.chatDao())
    val messageRepository = MessageRepository(context)
    val userRepository = UserRepository(database.userDao())
    
    // 🔥 Get Firebase Auth Manager
    val authManager = FirebaseAuthManager()
    
    val viewModel: ChatDetailsViewModel = viewModel(
        factory = ChatDetailsViewModelFactory(
            chatRepository = chatRepository,
            messageRepository = messageRepository,
            userRepository = userRepository,
            authManager = authManager,
            otherUserName = otherUserName,
            otherUserId = otherUserId
        )
    )
    
    // 🔥 COLLECT UI STATE FROM VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // 🔥 AUTO-SCROLL TO BOTTOM WHEN NEW MESSAGES ARRIVE
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            // Scroll to the last message (bottom of the list)
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    // 🔥 SCROLL TO BOTTOM WHEN SCREEN FIRST LOADS
    LaunchedEffect(uiState.shouldShowMessages) {
        if (uiState.shouldShowMessages && uiState.messages.isNotEmpty()) {
            // Initial scroll to bottom when messages first load
            listState.scrollToItem(uiState.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background image
        Image(
            painter = painterResource( R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = uiState.otherUserName, 
                            fontSize = 20.sp, 
                            color = Color.White
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = { },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        placeholder = {
                            Text(
                                "Message...",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    IconButton(
                        onClick = {
                            if (uiState.inputText.isNotEmpty()) {
                                viewModel.sendMessage(uiState.inputText)
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
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
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading messages...",
                                color = Color.White
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
                                text = "Error loading messages",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = uiState.errorMessage ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.clearError() }) {
                                Text("Retry")
                            }
                        }
                    }
                    
                    // Empty state
                    uiState.shouldShowEmpty -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "No messages",
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No messages yet",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Start the conversation!",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Show messages
                    uiState.shouldShowMessages -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                state = listState,
                                reverseLayout = false // Keep normal layout (top to bottom)
                            ) {
                                items(uiState.messages) { messageEntity ->
                                    MessageBubbleFromEntity(
                                        message = messageEntity,
                                        formatTime = { viewModel.formatMessageTime(it) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            
                            // 🔥 SCROLL TO BOTTOM BUTTON (shows when not at bottom)
                            val isAtBottom by remember {
                                derivedStateOf {
                                    val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    lastVisibleIndex >= uiState.messages.size - 2 // Show button when not near bottom
                                }
                            }
                            
                            if (!isAtBottom && uiState.messages.isNotEmpty()) {
                                FloatingActionButton(
                                    onClick = {
                                        scope.launch {
                                            listState.animateScrollToItem(uiState.messages.size - 1)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Scroll to bottom"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🔥 NEW: MessageBubble that works with MessageEntity from database
 */
@Composable
fun MessageBubbleFromEntity(
    message: MessageEntity,
    formatTime: (Long) -> String
) {
    val bubbleColor = if (message.isFromMe) Color.Black else Color(0xFFE0E0E0)
    val textColor = if (message.isFromMe) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    bubbleColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 250.dp)
        ) {
            Column {
                Text(
                    text = message.content, 
                    color = textColor, 
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatTime(message.timestamp),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

// Keep old MessageBubble for compatibility
@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isUser) Color.Black else Color(0xFFE0E0E0)
    val textColor = if (message.isUser) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    bubbleColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 250.dp)
        ) {
            Column {
                Text(message.text, color = textColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        message.time,
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}
