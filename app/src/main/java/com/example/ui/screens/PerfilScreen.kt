package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.AvatarWithFrame
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.JiuBeltBadge
import com.example.ui.widgets.NeonBorderCard

@Composable
fun PerfilScreen(viewModel: JiuSpeakViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val battles by viewModel.pvpBattles.collectAsState()

    val currentProfile = profile ?: UserProfileEntity()

    val totalFights = battles.size
    val winCount = battles.count { it.outcome == "WIN" }
    val drawCount = battles.count { it.outcome == "DRAW" }
    val lossCount = battles.count { it.outcome == "LOSS" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CinematicHeader(
            title = "WARRIOR RECORD",
            subtitle = "Your comprehensive stats and prestige trophies",
            actionIcon = Icons.Default.Settings,
            onActionClick = { viewModel.navigateTo("SETTINGS") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BIG PROFILE AVATAR BLOCK
        AvatarWithFrame(
            avatarId = currentProfile.selectedAvatar,
            frameColorHex = currentProfile.selectedFrameColor,
            level = currentProfile.level,
            size = 96.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentProfile.fullName,
            style = Typography.titleLarge,
            color = FontPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "@${currentProfile.username}",
            color = NeonCyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        JiuBeltBadge(beltColor = currentProfile.beltColor)

        Spacer(modifier = Modifier.height(20.dp))

        // GRID SPECTACULAR STATS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stat 1: Fight Record
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ARENA RECORD", color = FontSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$winCount - $drawCount - $lossCount",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("W-D-L", color = FontSecondary, fontSize = 8.sp)
                }
            }

            // Stat 2: Success Ratio
            val successRate = if (totalFights == 0) 100 else ((winCount.toFloat() / totalFights) * 100).toInt()
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SUBMISSION RATE", color = FontSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$successRate%",
                        color = NeonBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("Accuracy index", color = FontSecondary, fontSize = 8.sp)
                }
            }

            // Stat 3: Accumulated Tickets
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("JIUTICKETS", color = FontSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentProfile.jiuTickets.toString(),
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("💎 balance", color = FontSecondary, fontSize = 8.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACHIEVEMENT MEDALS BOX
        Text(
            text = "MEDAL CABINET",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkCard)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val medals = listOf(
                        Triple("🥇", "First Submission", "Win a PvP match"),
                        Triple("🔥", "Daily Warmup", "Launch 5-day streak"),
                        Triple("💎", "High Roller", "Collect first 1k tickets"),
                        Triple("🎓", "Syllabus Pro", "Complete all level courses"),
                        Triple("⚔️", "Ring Veteran", "Conduct 30 matches"),
                        Triple("🛡️", "Guard Master", "Conquer 5 wins on defensive mode")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        medals.take(4).forEach { (icon, name, desc) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.addLog("Badge checked: $name - $desc") }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(DarkCard)
                                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(icon, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name.split(" ").first(), color = FontPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // INVENTORY BUTTONS FOR SHOP REDIRECTIONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val menuOptions = listOf(
                Pair("🎒 INVENTORY", "SHOP"),
                Pair("📜 CERTIFICATES", "APRENDER"),
                Pair("⚔️ COMBAT LOGS", "ARENA")
            )
            menuOptions.forEach { (label, route) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCard)
                        .clickable { viewModel.navigateTo(route) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = FontPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SIGN OUT BUTTON
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOGOUT SESSION", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}
