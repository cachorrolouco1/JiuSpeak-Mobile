package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.UserProfileEntity
import com.example.data.repository.LeaderboardRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.viewmodel.LeaderboardViewModel
import com.example.ui.viewmodel.LeaderboardUiState
import com.example.ui.widgets.AvatarWithFrame
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.JiuBeltBadge

@Composable
fun LeaderboardScreen(
    viewModel: JiuSpeakViewModel,
    leaderboardViewModel: LeaderboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LeaderboardViewModel(LeaderboardRepository()) as T
            }
        }
    )
) {
    val activeType by viewModel.leaderboardType.collectAsState()
    val activeCountry by viewModel.leaderboardCountry.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val currentProfile = profile ?: UserProfileEntity()

    val types = listOf("WEEKLY", "MONTHLY", "GLOBAL")
    val countries = listOf("Global", "Brazil", "USA", "UAE", "UK")

    val state by leaderboardViewModel.leaderboardState.collectAsState()
    val token = viewModel.repository.currentToken ?: ""

    // Auto-fetch leaderboard from production backend on enter and filter updates
    LaunchedEffect(activeType, activeCountry, token) {
        if (token.isNotEmpty()) {
            leaderboardViewModel.loadLeaderboard(token)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // High contrast back action arrow to Arena
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("ARENA") }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Arena",
                    tint = NeonBlue
                )
            }
            Text(
                text = "VOLTAR PARA ARENA",
                color = FontSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        CinematicHeader(
            title = "WEEKLY LEAGUE CHAMPIONS",
            subtitle = "Settle the score. Best ranks receive physical gold belts!"
        )

        // FILTER TABS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { t ->
                val isSelected = activeType == t
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonBlue else DarkCard)
                        .clickable { viewModel.selectLeaderboardFilters(t, activeCountry) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t, fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else FontSecondary)
                }
            }
        }

        // COUNTRY SELECTOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            countries.forEach { c ->
                val isSelected = activeCountry == c
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkCard)
                        .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectLeaderboardFilters(activeType, c) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(c, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) NeonCyan else FontSecondary)
                }
            }
        }

        // LEADERBOARDS ROW TABLE DISPATCHER BASED ON PRODUCTION BACKEND STATE
        when (val uiState = state) {
            is LeaderboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonBlue)
                }
            }
            is LeaderboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Failed to sync leaderboard",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { leaderboardViewModel.loadLeaderboard(token) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                        ) {
                            Text("Retry Sync")
                        }
                    }
                }
            }
            is LeaderboardUiState.Success -> {
                val list = uiState.list
                if (list.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum atleta ranqueado no momento.",
                            color = FontSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(list) { index, athlete ->
                            val isMe = athlete.userId == currentProfile.id || athlete.username == currentProfile.username
                            val rank = athlete.rankPosition

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isMe) NeonBlue.copy(alpha = 0.15f) else DarkSurface),
                                border = BorderStroke(1.dp, if (isMe) NeonBlue else DarkCard)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank position indicator
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (rank) {
                                                    1 -> Color(0xFFFBBF24).copy(alpha = 0.15f)
                                                    2 -> Color(0xFFF8FAFC).copy(alpha = 0.1f)
                                                    3 -> Color(0xFF94A3B8).copy(alpha = 0.1f)
                                                    else -> Color.Transparent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val ringIcon = when (rank) {
                                            1 -> "🥇"
                                            2 -> "🥈"
                                            3 -> "🥉"
                                            else -> "$rank"
                                        }
                                        Text(
                                            text = ringIcon,
                                            color = when (rank) {
                                                1 -> Color(0xFFFBBF24)
                                                else -> FontPrimary
                                            },
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Dynamic Custom Avatar
                                    AvatarWithFrame(
                                        avatarId = athlete.avatar.ifEmpty { "avatar_fighter$rank" },
                                        frameColorHex = if (rank == 1) "#FBBF24" else "#94A3B8",
                                        level = if (athlete.xp > 0) (athlete.xp / 100) + 1 else 10,
                                        size = 40.dp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = athlete.username,
                                                color = if (isMe) NeonBlue else FontPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            JiuBeltBadge(beltColor = athlete.belt)
                                        }
                                        Text(
                                            text = "Weekly Wins: ${athlete.wins} • Losses: ${athlete.losses}",
                                            color = FontSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Accumulated XP points
                                    Text(
                                        text = "${athlete.xp} XP",
                                        color = if (rank == 1) GoldAccent else NeonCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
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
