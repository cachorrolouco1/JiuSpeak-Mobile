package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyMissionEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.*

@Composable
fun HomeScreen(viewModel: JiuSpeakViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val posts by viewModel.socialPosts.collectAsState()

    val activeSeason by viewModel.activeSeason.collectAsState()
    val myClanState by viewModel.myClan.collectAsState()
    val leagueStatusState by viewModel.leagueStatus.collectAsState()
    val achievementsState by viewModel.achievements.collectAsState()

    val currentProfile = profile ?: UserProfileEntity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // UI PROFILE HEADER WITH JIU-JITSU BRANDING
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarWithFrame(
                avatarId = currentProfile.selectedAvatar,
                frameColorHex = currentProfile.selectedFrameColor,
                level = currentProfile.level,
                size = 68.dp,
                onClick = { viewModel.navigateTo("PERFIL") }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentProfile.username,
                        style = Typography.titleLarge,
                        color = FontPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    JiuBeltBadge(beltColor = currentProfile.beltColor)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // XP Progress Bar
                val progressRatio = currentProfile.xp.toFloat() / currentProfile.xpNextLevel
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressRatio)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(NeonBlue, NeonCyan)))
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${currentProfile.xp}/${currentProfile.xpNextLevel} XP",
                    color = FontSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Streak indicator & JiuTickets
            Column(horizontalAlignment = Alignment.End) {
                // Streak Fire
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🔥", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${currentProfile.dailyStreak}d",
                        color = Color(0xFFD97706),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // JiuTickets Wallet
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.navigateTo("WALLET") }
                ) {
                    Text("💎", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = currentProfile.jiuTickets.toString(),
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // GAMIFIED ACTIVE V4 SHORTCUTS DASHBOARD (Seasons, ELO Leagues, Teams, Achievements)
        Text(
            text = "JIUSPEAK ATLETA PASSPORT (V4)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonBlue,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Season Pass widget card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("SEASON_PASS") },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("👑 SEASON PASS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeonBlue, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        activeSeason?.name?.uppercase() ?: "ROAD TO BLUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Nível ${activeSeason?.currentLevel ?: 1}",
                        fontSize = 10.sp,
                        color = FontSecondary
                    )
                }
            }

            // World League ELO card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("LEAGUE") },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🥈 LIGA MUNDIAL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeonCyan, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${leagueStatusState?.division ?: "SILVER"} ${leagueStatusState?.subDivision ?: 2}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${leagueStatusState?.currentElo ?: 1350} ELO pts",
                        fontSize = 10.sp,
                        color = FontSecondary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Clan or Team dashboard card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("CLANS") },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🛡️ EQUIPE / CLÃ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = GoldAccent, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        myClanState?.name ?: "SEM EQUIPE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (myClanState != null) "Rank #${myClanState!!.rankPosition}" else "Buscar Aliança",
                        fontSize = 10.sp,
                        color = FontSecondary
                    )
                }
            }

            // Achievements bulletin card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("ACHIEVEMENTS") },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, Color(0xFFC084FC).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🏅 MEDALHA REAL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFC084FC), fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "CONQUISTAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val completeCount = achievementsState.count { it.isCompleted }
                    Text(
                        "$completeCount / ${achievementsState.size} Concluídas",
                        fontSize = 10.sp,
                        color = FontSecondary
                    )
                }
            }
        }

        // QUICK PVP ARENA CARD
        NeonBorderCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            glowColor = NeonCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2,450 ONLINE NOW",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ARENA PVP CHALLENGE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Spar against global players. Train vocabulary & speaking rules in 3-round matches.",
                        fontSize = 11.sp,
                        color = FontSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                GlowButton(
                    text = "FIGHT",
                    onClick = { viewModel.navigateTo("ARENA") },
                    glowColor = NeonCyan,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                )
            }
        }

        // DAILY MISSIONS (DAILY QUESTS) WITH DIRECT REWARD DRAWER
        CinematicHeader(
            title = "Daily Quests",
            subtitle = "Complete daily combat drills on mats to earn tickets and XP"
        )
        dailyMissions.forEach { mission ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = if (mission.isCompleted) DarkCard.copy(alpha = 0.5f) else DarkSurface),
                border = BorderStroke(1.dp, if (mission.isCompleted) Color.Green.copy(alpha = 0.3f) else DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!mission.isCompleted) {
                                viewModel.claimMissionReward(mission.id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (mission.isCompleted) Color.Green else FontSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mission.title,
                            color = if (mission.isCompleted) FontSecondary else FontPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = mission.description,
                            color = FontSecondary,
                            fontSize = 11.sp
                        )

                        // Quest Progress Bar
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(DarkCard)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(mission.progress.toFloat() / mission.maxProgress)
                                        .fillMaxHeight()
                                        .background(if (mission.isCompleted) Color.Green else NeonBlue)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${mission.progress}/${mission.maxProgress}",
                                color = FontSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text("+${mission.xpReward} XP", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("+${mission.jiuTicketsReward} 💎", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CHAMPION SHIP & WORLD BRACKETS EVENTS
        Text(
            text = "LIVE ACTIVE WORLD EVENTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonBlue,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        NeonBorderCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            glowColor = Color(0xFFEF4444) // Red glow for special tournaments
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("LIVE CHAMPIONSHIP", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EUROPEAN OPEN LINGUISTICS 🏆", color = FontPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Earn up to 1,500 JiuTickets and a legendary RED BELT profile item tag. Competition finishes in 2 days. 3 matches required to qualify.",
                    fontSize = 11.sp,
                    color = FontSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.navigateTo("ARENA") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("JOIN TOURNAMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // PERSISTENT NEXT TROPHY ACHIEVEMENT PROGRESS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCard)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏆", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("NEXT ACHIEVEMENT: WORLD TRAVELER", color = FontPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Conduct 5 PvP Conversation matches with overseas opponent", color = FontSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { 0.6f },
                        color = GoldAccent,
                        trackColor = DarkCard,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("60%", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
