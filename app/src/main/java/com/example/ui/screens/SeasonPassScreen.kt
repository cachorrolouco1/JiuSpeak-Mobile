package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import com.example.data.model.SeasonRewardEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.GlowButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonPassScreen(viewModel: JiuSpeakViewModel) {
    val activeSeason by viewModel.activeSeason.collectAsState()
    val rewards by viewModel.seasonRewards.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PASSE DE TEMPORADA",
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
        val season = activeSeason
        if (season == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(NeonBlue, NeonCyan))
                            ), RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🛡️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TEMPORADAS EM IMPLANTAÇÃO",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "O sistema de Temporadas e Passe de Elite (Season Pass) está em fase de implantação no servidor oficial de produção. Dados locais fictícios do passe foram removidos em conformidade com as regras de auditoria.",
                            fontSize = 12.sp,
                            color = FontSecondary,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.syncAllData() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("VERIFICAR CONEXÃO REAL", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
            // Season Epic Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(NeonBlue, NeonCyan))
                            ), RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEA580C))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "SEASON 4",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "TEMPO LIMITADO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = FontSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = season.name.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color.White
                        )

                        Text(
                            text = season.description,
                            fontSize = 12.sp,
                            color = FontSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Level Progress HUD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "NÍVEL DA TEMPORADA: ${season.currentLevel}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Progresso para próximo nível:",
                                    fontSize = 11.sp,
                                    color = FontSecondary
                                )
                            }
                            Text(
                                text = "${season.currentXp}/${season.requiredXpNextLevel} XP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Linear Progress Indicator
                        val progressRatio = season.currentXp.toFloat() / season.requiredXpNextLevel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(DarkBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                NeonBlue,
                                                NeonCyan
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // Status passes badges VIP/PRO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (season.hasVipPass) Color(0xFFFFD700) else Color.Transparent
                                ), RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (season.hasVipPass) Color(0xFF1E1A05) else DarkSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (season.hasVipPass) "👑" else "🔒", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    "VIP PASS CAPTURED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (season.hasVipPass) Color(0xFFFFD700) else FontSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    if (season.hasVipPass) "Sessão Ativa" else "Comprar VIP",
                                    fontSize = 10.sp,
                                    color = FontSecondary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (season.hasProPass) NeonBlue else Color.Transparent
                                ), RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (season.hasProPass) Color(0xFF030D1A) else DarkSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (season.hasProPass) "🥋" else "🔒", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    "PRO CHAMP PASS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (season.hasProPass) NeonBlue else FontSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    if (season.hasProPass) "Sessão Ativa" else "Indisponível",
                                    fontSize = 10.sp,
                                    color = FontSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Rewards List Titled Header
            item {
                Text(
                    text = "RECOMPENSAS DA TEMPORADA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonBlue,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Scrollable rewards levels list
            items(rewards) { reward ->
                RewardRow(
                    reward = reward,
                    currentLevel = season.currentLevel,
                    hasVip = season.hasVipPass || season.hasProPass,
                    onClaim = { viewModel.claimReward(reward.id) }
                )
            }
        }
    }
}
}

@Composable
fun RewardRow(
    reward: SeasonRewardEntity,
    currentLevel: Int,
    hasVip: Boolean,
    onClaim: () -> Unit
) {
    val unlocked = currentLevel >= reward.level
    val isPremiumReward = reward.isPremium
    val accessible = !isPremiumReward || hasVip

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reward_row_${reward.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isClaimed) Color(0xFF070B14) else DarkSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isPremiumReward) Color(0xFFFFD700).copy(alpha = 0.4f) else DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Level constraint Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) NeonBlue else DarkBg)
                    .border(BorderStroke(1.5.dp, if (unlocked) NeonCyan else Color.Gray), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "LVL",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (unlocked) Color.White else FontSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        reward.level.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Reward description Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconText = when (reward.type) {
                        "JIU_TICKETS" -> "💎"
                        "FRAME" -> "🖼️"
                        "BOOSTER" -> "⚡"
                        "AVATAR" -> "👤"
                        "TITLE" -> "🥋"
                        else -> "🏅"
                    }
                    Text(iconText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reward.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPremiumReward) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFD97706))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "VIP PREMIUM",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (unlocked) "Desbloqueado" else "Bloqueado na Trilha",
                        fontSize = 11.sp,
                        color = if (unlocked) NeonCyan else FontSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action section button
            when {
                reward.isClaimed -> {
                    IconButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Green.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Claimed",
                            tint = Color.Green
                        )
                    }
                }
                unlocked && accessible -> {
                    GlowButton(
                        text = "RESGATAR",
                        onClick = onClaim,
                        glowColor = NeonCyan,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    )
                }
                else -> {
                    IconButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
