package com.chat.app.ui.chatdetails

import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chat.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    name: String,
    messages: List<Message>,
    navController: NavController
) {
    val messagesState = remember { mutableStateListOf<Message>().apply { addAll(messages) } }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

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
                    title = { Text(name, fontSize = 20.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
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
                        value = inputText,
                        onValueChange = { inputText = it },
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
                            if (inputText.isNotEmpty()) {
                                val currentTime = timeFormatter.format(Date())
                                messagesState.add(
                                    Message(
                                        inputText,
                                        isUser = true,
                                        time = currentTime
                                    )
                                )
                                val sentText = inputText
                                inputText = ""
                                scope.launch {
                                    listState.animateScrollToItem(messagesState.size - 1)
                                }

                                // Optional: Simulate auto-reply
                                scope.launch {
                                    delay(500)
                                    messagesState.add(
                                        Message(
                                            text = "Auto-reply to: $sentText",
                                            isUser = false,
                                            time = timeFormatter.format(Date())
                                        )
                                    )
                                    listState.animateScrollToItem(messagesState.size - 1)
                                }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(10.dp),
                state = listState
            ) {
                items(messagesState) { msg ->
                    MessageBubble(msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

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
