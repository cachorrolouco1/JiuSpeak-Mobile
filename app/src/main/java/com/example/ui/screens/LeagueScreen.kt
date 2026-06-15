package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.LeagueEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueScreen(viewModel: JiuSpeakViewModel) {
    val leagueStatusState by viewModel.leagueStatus.collectAsState()

    val leagueStatus = leagueStatusState ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LIGA MUNDIAL (ELO)",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ELO division Crest Banner
            item {
                LeagueCrestCard(league = leagueStatus)
            }

            // Rank positions HUD
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("RANK GLOBAL", fontSize = 9.sp, color = FontSecondary, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("#${leagueStatus.globalRank}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = NeonBlue, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("RANK NACIONAL", fontSize = 9.sp, color = FontSecondary, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("#${leagueStatus.countryRank}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Statistics scoreboard card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "HISTÓRICO DA TEMPORADA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonBlue,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("VITÓRIAS", fontSize = 10.sp, color = FontSecondary)
                                Text(
                                    leagueStatus.winsCount.toString(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = Color.Green,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DERROTAS", fontSize = 10.sp, color = FontSecondary)
                                Text(
                                    leagueStatus.lossesCount.toString(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = Color.Red,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            val totalMatches = leagueStatus.winsCount + leagueStatus.lossesCount
                            val winRate = if (totalMatches > 0) (leagueStatus.winsCount.toFloat() / totalMatches * 100).toInt() else 0
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Aproveitamento", fontSize = 10.sp, color = FontSecondary)
                                Text(
                                    "$winRate%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = GoldAccent,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Division Rules Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            "REGRAS DE CLASSIFICAÇÃO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = FontSecondary,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "🧠 Vença combates de vocabulário ou de pronúncia na Arena PvP para subir de ELO. Seus erros diminuem sua pontuação.",
                            fontSize = 11.sp,
                            color = FontSecondary,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "🥋 Ao atingir o ELO de promoção, você sobe de subdivisão ou avança para a próxima liga mundial (ex: Ouro).",
                            fontSize = 11.sp,
                            color = FontSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueCrestCard(league: LeagueEntity) {
    val crestShadowColor = when (league.division) {
        "BRONZE" -> Color(0xFFCD7F32)
        "SILVER" -> Color(0xFFC0C0C0)
        "GOLD" -> Color(0xFFFFD700)
        "PLATINUM" -> Color(0xFFE5E4E2)
        "DIAMOND" -> NeonCyan
        "MASTER" -> Color(0xFF8B5CF6)
        else -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("league_crest_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050B14)),
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(listOf(crestShadowColor, Color.Transparent))
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // division Halo Ring and Badge
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(crestShadowColor.copy(alpha = 0.1f))
                    .border(2.5.dp, crestShadowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (league.division) {
                        "BRONZE" -> "🥉"
                        "SILVER" -> "🥈"
                        "GOLD" -> "🥇"
                        "PLATINUM" -> "🏅"
                        "DIAMOND" -> "💎"
                        "MASTER" -> "🥋"
                        else -> "🏆"
                    },
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${league.division} DIVISION ${league.subDivision}".uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${league.currentElo} ELO POINTS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = crestShadowColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ELO boundaries Slider HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Rebaixamento: ${league.rebaixamentoThresholdElo} ELO",
                    fontSize = 10.sp,
                    color = Color.Red.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "Meta Promoção: ${league.promotionGoalElo} ELO",
                    fontSize = 10.sp,
                    color = Color.Green.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ELO gauge
            val totalSpan = league.promotionGoalElo - league.rebaixamentoThresholdElo
            val progressPoint = league.currentElo - league.rebaixamentoThresholdElo
            val gaugeProgress = if (totalSpan > 0) progressPoint.toFloat() / totalSpan else 0f
            val boundedRatio = gaugeProgress.coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(DarkBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(boundedRatio)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Color.Red, crestShadowColor, Color.Green)))
                )
            }
        }
    }
}
