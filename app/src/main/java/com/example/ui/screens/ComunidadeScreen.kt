package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialPostEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.*

@Composable
fun ComunidadeScreen(viewModel: JiuSpeakViewModel) {
    val posts by viewModel.socialPosts.collectAsState()
    var isWritingPost by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    var mockImageAttach by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "BJJ GLOBAL STATION",
            subtitle = "Translate experiences with purple and black belts from all continents",
            actionIcon = Icons.Default.Add,
            onActionClick = { isWritingPost = !isWritingPost }
        )

        // CREATE NEW SOCIAL POST SHEET EXPOSURE
        if (isWritingPost) {
            NeonBorderCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                glowColor = NeonBlue
            ) {
                Text(
                    text = "POST MATS REPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = NeonBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = newPostText,
                    onValueChange = { newPostText = it },
                    placeholder = { Text("What positions did you study today? Share English vocabulary tips...", color = FontSecondary) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Optional preset Image indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Attach Photo presets: ", color = FontSecondary, fontSize = 10.sp)
                        val choices = listOf(
                            Pair("🥋 Gis", "https://images.unsplash.com/photo-1555597673-b21d5c935865?q=80&w=300"),
                            Pair("🥈 Arena", "https://media.istockphoto.com/id/1183188597/photo/man-ready-for-fighting.jpg")
                        )
                        choices.forEach { (label, url) ->
                            val isSelected = mockImageAttach == url
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) NeonCyan else DarkCard)
                                    .clickable { mockImageAttach = if (isSelected) null else url }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(label, fontSize = 9.sp, color = if (isSelected) Color.Black else FontSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { isWritingPost = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red)
                        }
                        IconButton(onClick = {
                            if (newPostText.isNotBlank()) {
                                viewModel.createPost(newPostText, mockImageAttach)
                                newPostText = ""
                                mockImageAttach = null
                                isWritingPost = false
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Post", tint = NeonBlue)
                        }
                    }
                }
            }
        }

        // POPULAR TOPIC CHANNELS / SUBGROUPS
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val topics = listOf("ALL FEEDS", "🇺🇸 ENGLISH ONLY", "🇧🇷 SEMINÁRIOS", "🥋 GUARD GAME")
            topics.forEach { topic ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (topic.startsWith("ALL")) NeonBlue else DarkCard)
                        .border(1.dp, if (topic.startsWith("ALL")) NeonCyan else Color.Transparent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable { viewModel.addLog("Filtered community feed topic by: $topic") }
                ) {
                    Text(
                        text = topic,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (topic.startsWith("ALL")) Color.White else FontSecondary
                    )
                }
            }
        }

        // FEED LAZY COLUMN
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Author block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarWithFrame(
                                avatarId = post.authorAvatar,
                                frameColorHex = "#94A3B8",
                                level = post.authorLevel,
                                size = 44.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = post.authorName,
                                        color = FontPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    JiuBeltBadge(beltColor = post.authorBelt)
                                }
                                Text(
                                    text = "Level ${post.authorLevel} • ${post.timeAgo}",
                                    color = FontSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = FontSecondary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Post raw text content
                        Text(
                            text = post.content,
                            color = FontPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        // Post Optional attached image
                        if (!post.imageUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = "Attached image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, DarkCard, RoundedCornerShape(8.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = DarkCard)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interaction row (Likes count, Comment count)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (post.isLikedByMe) NeonBlue.copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable { viewModel.likePost(post.id) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (post.isLikedByMe) Color.Red else FontSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${post.likesCount} Likes",
                                        color = if (post.isLikedByMe) NeonBlue else FontSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.addLog("Drafting comments for post: ${post.id}") }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Comment",
                                        tint = FontSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${post.commentsCount} Comments",
                                        color = FontSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.addLog("Sharing post link: ${post.id}") }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = FontSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
