package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.viewmodel.PvpMatchState
import com.example.ui.widgets.*

@Composable
fun ArenaScreen(viewModel: JiuSpeakViewModel) {
    val pvpState by viewModel.pvpMatchState.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val battles by viewModel.pvpBattles.collectAsState()

    val currentProfile = profile ?: UserProfileEntity()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBg, Color(0xFF000814))))
    ) {
        when (val state = pvpState) {
            is PvpMatchState.Idle -> {
                ArenaIdleView(
                    viewModel = viewModel,
                    battlesHistory = battles,
                    currentProfile = currentProfile
                )
            }
            is PvpMatchState.Searching -> {
                ArenaSearchingView(matchType = state.matchType)
            }
            is PvpMatchState.InteractiveFight -> {
                ArenaFightView(viewModel = viewModel, state = state, currentProfile = currentProfile)
            }
            is PvpMatchState.Ending -> {
                ArenaEndingView(viewModel = viewModel, state = state)
            }
        }
    }
}

@Composable
fun ArenaIdleView(
    viewModel: JiuSpeakViewModel,
    battlesHistory: List<com.example.data.model.PvpBattleEntity>,
    currentProfile: UserProfileEntity
) {
    val pvpError by viewModel.pvpError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "NEON ARENA PVP",
            subtitle = "Challenge international athletes to live language battles"
        )

        Spacer(modifier = Modifier.height(8.dp))

        pvpError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("pvp_error_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = error,
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (currentProfile.jiuTickets < 50) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("insufficient_balance_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0x19F59E0B)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Acesso Limitado: Entradas PvP consomem 50 JiuTickets por combate. Seu saldo atual é de ${currentProfile.jiuTickets} JT.",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Big Gladiator banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OSS! ARE YOU READY?",
                    color = NeonBlue,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter live quick matchmaking to test your vocabulary, oral comprehension, and pronounciation. Correct answers give you direct tickets and boost your rank! Strikes give double rewards.",
                    color = FontSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // MATCHMAKING LAUNCH CHANNELS
        Text(
            text = "SELECT YOUR DISCIPLINE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val disciplines = listOf(
            Triple("PVP Conversação", "Defend positions under verbal queries, translate context scenarios", "Conversation"),
            Triple("PVP Vocabulário", "Select correct names for anatomical parts, joints, and positions", "Vocabulary"),
            Triple("PVP Pronúncia", "Speak clearly into simulated audio analyzer. Perfect tone beats opponent", "Pronunciation"),
            Triple("Desafio Relâmpago", "Sudden death 10-second drills. High stakes, maximum reward multiplier", "Lightning")
        )

        disciplines.forEach { (title, desc, key) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.searchAndLaunchPvpFight(key) }
                    .testTag("disciplina_$key"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .border(1.dp, NeonBlue.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (key) {
                            "Conversation" -> "💬"
                            "Vocabulary" -> "🥋"
                            "Pronunciation" -> "🗣️"
                            else -> "⚡"
                        }
                        Text(icon, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = FontPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = FontSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Fight",
                        tint = NeonBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HISTORIC COMBAT STATS ROW WITH NAVLINK TO RANKING
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT COMBAT HISTORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NeonBlue,
                letterSpacing = 1.sp
            )
            Text(
                text = "VER RANKING GERAL ➔",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { viewModel.navigateTo("LEADERBOARD") }
            )
        }

        if (battlesHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No arena battles fought yet. Start matchmaking!", color = FontSecondary, fontSize = 12.sp)
            }
        } else {
            battlesHistory.take(5).forEach { battle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarWithFrame(
                            avatarId = battle.opponentAvatar,
                            frameColorHex = "#94A3B8",
                            level = 15,
                            size = 40.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = battle.opponentName,
                                    color = FontPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                JiuBeltBadge(beltColor = battle.opponentBelt)
                            }
                            Text(
                                text = "Mode: ${battle.matchType} • ${battle.scoreMe} vs ${battle.scoreOpponent} pts",
                                color = FontSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Outcome indicator
                        val outcomeColor = when (battle.outcome) {
                            "WIN" -> Color.Green
                            "LOSS" -> Color.Red
                            else -> Color.Gray
                        }
                        Text(
                            text = battle.outcome,
                            color = outcomeColor,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArenaSearchingView(matchType: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadarAlpha"
    )
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadarScale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            // Ripple layer 1
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(2.dp, NeonBlue.copy(alpha = alphaAnim), CircleShape)
            )
            // Ripple layer 2
            Box(
                modifier = Modifier
                    .size(120.dp * scaleAnim)
                    .border(1.dp, NeonCyan.copy(alpha = alphaAnim * 0.5f), CircleShape)
            )

            // Center Ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(DarkCard, Color.Black))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚔️",
                    fontSize = 32.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LOOKING FOR SPARRING ATLETE...",
            color = FontPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Disciplines: $matchType • Connecting to server...",
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ArenaFightView(
    viewModel: JiuSpeakViewModel,
    state: PvpMatchState.InteractiveFight,
    currentProfile: UserProfileEntity
) {
    val question = state.questions[state.currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MATCH SCOREBOARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(1.dp, DarkCard, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Me Side
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                AvatarWithFrame(avatarId = currentProfile.selectedAvatar, frameColorHex = currentProfile.selectedFrameColor, level = currentProfile.level, size = 48.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(currentProfile.username, color = FontPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${state.myScore} pts", color = NeonBlue, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }

            // VS display
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE11D48))
                    .padding(8.dp)
            ) {
                Text("VS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }

            // Opponent Side
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                AvatarWithFrame(avatarId = "avatar_fighter2", frameColorHex = "#94A3B8", level = currentProfile.level + 1, size = 48.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.opponentName, color = FontPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${state.opponentScore} pts", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }

        // QUESTION COUNTER LEAGUE PROGRESS
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROUND ${state.currentQuestionIndex + 1} of ${state.questions.size}",
                color = FontSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            // Small indicator lights
            Row {
                repeat(state.questions.size) { index ->
                    val color = if (index < state.currentQuestionIndex) NeonBlue else if (index == state.currentQuestionIndex) NeonCyan else DarkCard
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        // QUIZ MAIN BOX
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // Prompt text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCard)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = question.text,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Options list
                Column {
                    question.options.forEachIndexed { optIndex, option ->
                        Button(
                            onClick = { viewModel.answerArenaQuestion(optIndex) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            border = BorderStroke(0.5.dp, FontSecondary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = option,
                                color = FontPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth().padding(6.dp),
                                textAlign = TextAlign.Left
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArenaEndingView(
    viewModel: JiuSpeakViewModel,
    state: PvpMatchState.Ending
) {
    val isWinner = state.myScore > state.opponentScore
    val isDraw = state.myScore == state.opponentScore

    val trophy = if (isWinner) "🏆 SUCCESS" else if (isDraw) "🤝 SPLIT DRAW" else "💀 DEFEAT"
    val trophyColor = if (isWinner) Color.Green else if (isDraw) Color.Gray else Color.Red

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = trophy,
            color = trophyColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isWinner) "OFFICIAL VERDICT: SUBMISSION WIN!" else if (isDraw) "JUDGES VERDICT: MATCH DRAW" else "OFFICIAL VERDICT: TAP OUT",
            color = FontSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Fight Scores Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, trophyColor.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SCORECARD SUMMARY",
                    fontSize = 11.sp,
                    color = FontSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOU", color = FontPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${state.myScore} pts", color = NeonBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }

                    Text("vs", color = FontSecondary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.opponentName, color = FontPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${state.opponentScore} pts", color = NeonCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rewards breakdown
                Divider(color = DarkCard)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mat Experience Gained:", color = FontSecondary, fontSize = 11.sp)
                    Text(if (isWinner) "+150 XP" else if (isDraw) "+80 XP" else "+40 XP", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("JiuTickets Reward:", color = FontSecondary, fontSize = 11.sp)
                    Text(if (isWinner) "+50 💎" else if (isDraw) "+25 💎" else "+10 💎", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        GlowButton(
            text = "CLAIM REWARDS",
            onClick = { viewModel.finalizeArenaFight() },
            modifier = Modifier.fillMaxWidth(),
            glowColor = trophyColor
        )
    }
}
