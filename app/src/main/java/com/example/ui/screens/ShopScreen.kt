package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.widgets.AvatarWithFrame
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.GlowButton

@Composable
fun ShopScreen(viewModel: JiuSpeakViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val currentProfile = profile ?: UserProfileEntity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "SHŌGUN STORE",
            subtitle = "Settle aesthetic items in tournament styles using JiuTickets"
        )

        // BALANCE BAR CABINET
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("WARRIOR TICKETS IN STOCK", color = FontSecondary, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentProfile.jiuTickets.toString(),
                            color = GoldAccent,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldAccent)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("EARN IN ARENA", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // STORE SECTIONS
        Text(
            text = "LEGENDARY AVATARS & SHAPE FRAMES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonBlue,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val items = listOf(
            Triple("SHŌGUN GI SHIELD", "avatar_charles", Pair("#E11D48", 500)), // GI emoji
            Triple("ELITE SAMURAI MASK", "avatar_john", Pair("#FBBF24", 800)),
            Triple("COSMIC SPIRIT EAGLE", "avatar_igor", Pair("#009DFF", 400)),
            Triple("GLADIATOR CLAW", "avatar_fighter3", Pair("#10B981", 300)),
            Triple("BLITZ LIGHTNING STORM", "avatar_fighter1", Pair("#8B5CF6", 600)),
            Triple("NEON SPAR GLOVE", "avatar_fighter2", Pair("#EC4899", 250))
        )

        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { (name, id, options) ->
                    val (hexColor, cost) = options
                    val isPurchasable = currentProfile.jiuTickets >= cost

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = isPurchasable) {
                                viewModel.buyAestheticItem(id, hexColor, cost)
                            },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, if (isPurchasable) NeonCyan.copy(alpha = 0.3f) else DarkCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarWithFrame(
                                avatarId = id,
                                frameColorHex = hexColor,
                                level = currentProfile.level,
                                size = 56.dp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = name,
                                color = FontPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Cost Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("💎", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$cost Tickets",
                                    color = if (isPurchasable) GoldAccent else FontSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPurchasable) NeonBlue else DarkCard)
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentProfile.selectedAvatar == id) "EQUIPPED" else if (isPurchasable) "UNLOCK" else "LOCKED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPurchasable) Color.White else FontSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
