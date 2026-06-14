package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.JiuBeltBadge

@Composable
fun ChatScreen(viewModel: JiuSpeakViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val selectedFriend by viewModel.selectedChatFriend.collectAsState()

    var typedMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val currentProfile = profile ?: UserProfileEntity()

    // Let lists always scroll to bottom automatically when new message is registered
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "WAR ROOM CHATS",
            subtitle = "Active channel: ${selectedFriend ?: "Global Chat"}"
        )

        // ACTIVE CONVERSATION TAB BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val channels = listOf("Global Chat", "Coach Michael", "Carlos_BJJ")
            channels.forEach { name ->
                val isSelected = selectedFriend == name
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonBlue else DarkCard)
                        .clickable { viewModel.selectChatFriend(name) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else FontSecondary
                    )
                }
            }
        }

        // MESSAGES SCROLL DIALOGS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkCard)
                .padding(12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentProfile.username

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        // Speaker header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isMe) {
                                Text(
                                    text = msg.senderName,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                JiuBeltBadge(beltColor = msg.senderBelt)
                            } else {
                                Text(
                                    text = "YOU",
                                    color = NeonBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Message bubble
                        Box(
                            modifier = Modifier
                                .padding(vertical = 3.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = if (isMe) 8.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 8.dp
                                    )
                                )
                                .background(if (isMe) NeonBlue.copy(alpha = 0.2f) else DarkCard)
                                .border(
                                    1.dp,
                                    if (isMe) NeonBlue.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = if (isMe) 8.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 8.dp
                                    )
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = msg.messageText,
                                color = FontPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        // Timestamp text
                        Text(
                            text = if (isMe) "Delivered ✓" else "Online",
                            color = FontSecondary,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CHAT MESSAGE INPUT ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = typedMessage,
                onValueChange = { typedMessage = it },
                placeholder = { Text("Study, talk, share...", color = FontSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = DarkCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (typedMessage.isNotBlank()) {
                        viewModel.sendMessage(typedMessage)
                        typedMessage = ""
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonBlue)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
