package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: JiuSpeakViewModel) {
    val achievementsList by viewModel.achievements.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "STUDY", "PVP", "COMMUNITY", "MARKETPLACE")

    val filteredAchievements = if (selectedCategory == "ALL") {
        achievementsList
    } else {
        achievementsList.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CONQUISTAS & MEDALHAS",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("HOME") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncAllData() }) {
                        Text("🔄", fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
        ) {
            // Stats Summary Panel
            AchievementsStatsPanel(achievements = achievementsList)

            // Categories horizontal filter list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonBlue else DarkSurface)
                            .border(
                                BorderStroke(1.dp, if (isSelected) NeonCyan else DarkCard),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) Color.White else FontSecondary
                        )
                    }
                }
            }

            // Achievements main vertical scrolling list or empty state checker
            if (filteredAchievements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🏅", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NENHUMA CONQUISTA DISPONÍVEL",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "O servidor de produção não possui conquistas configuradas ou elas estão indisponíveis no momento.",
                            color = FontSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredAchievements) { achievement ->
                        AchievementCardItem(achievement = achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsStatsPanel(achievements: List<AchievementEntity>) {
    val total = achievements.size
    val completed = achievements.count { it.isCompleted }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCard)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "CONQUISTAS DESBLOQUEADAS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonBlue,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completed / $total",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Mostre seu domínio no inglês e no jiu-jitsu!",
                    fontSize = 11.sp,
                    color = FontSecondary
                )
            }

            // Circular progress ratio meter
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                val completionRatio = if (total > 0) completed.toFloat() / total else 0f
                CircularProgressIndicator(
                    progress = { completionRatio },
                    modifier = Modifier.fillMaxSize(),
                    color = NeonCyan,
                    strokeWidth = 6.dp,
                    trackColor = DarkBg,
                )
                Text(
                    text = "${(completionRatio * 100).toInt()}%",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AchievementCardItem(achievement: AchievementEntity) {
    val baseGlowColor = when (achievement.rarity) {
        "COMMON" -> Color.Gray
        "RARE" -> NeonBlue
        "EPIC" -> Color(0xFF8B5CF6) // Purple
        "LEGENDARY" -> Color(0xFFFFD700) // Gold
        "MYTHIC" -> Color(0xFFEF4444) // Deep Red
        else -> NeonCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("achievement_card_${achievement.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isCompleted) Color(0xFF0F172A) else DarkSurface
        ),
        border = BorderStroke(
            1.dp,
            if (achievement.isCompleted) baseGlowColor.copy(alpha = 0.6f) else DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement Medal Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isCompleted) baseGlowColor.copy(alpha = 0.15f) else DarkBg)
                    .border(
                        BorderStroke(
                            2.dp,
                            if (achievement.isCompleted) baseGlowColor else Color.Gray.copy(alpha = 0.5f)
                        ), CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (achievement.rarity) {
                        "COMMON" -> "🥉"
                        "RARE" -> "🥈"
                        "EPIC" -> "🥇"
                        "LEGENDARY" -> "🏆"
                        "MYTHIC" -> "👑"
                        else -> "🏅"
                    },
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Metadata
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = achievement.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(baseGlowColor.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, baseGlowColor), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = achievement.rarity,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = baseGlowColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    color = FontSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar or Unlocked Timestamps
                if (achievement.isCompleted) {
                    val dateFormatted = remember(achievement.unlockedAtTimestamp) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        sdf.format(Date(achievement.unlockedAtTimestamp))
                    }
                    Text(
                        text = "🔓 Conquistado em: $dateFormatted",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small ProgressBar tracker
                        val progressRatio = achievement.progress.toFloat() / achievement.targetProgress
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(DarkBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .background(baseGlowColor)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${achievement.progress}/${achievement.targetProgress}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = FontSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rewards listing HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recompensas:  ", fontSize = 10.sp, color = FontSecondary)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkBg)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("⭐", fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "${achievement.xpReward} XP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkBg)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("💎", fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "${achievement.jiuTicketsReward} Tickets",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
