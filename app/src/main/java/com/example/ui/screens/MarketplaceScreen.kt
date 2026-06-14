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
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.AvatarWithFrame
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.JiuBeltBadge

@Composable
fun MarketplaceScreen(viewModel: JiuSpeakViewModel) {
    val teachers by viewModel.teachersList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "BILINGUAL MASTER MARKETPLACE",
            subtitle = "Book premium classes from global BJJ professors and language coaches"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SEARCH BAR MOCK
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Filter professors by name, belt, country...", color = FontSecondary) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonBlue) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = DarkCard,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // TEACHERS CATALOG COLUMN
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(teachers) { teacher ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            AvatarWithFrame(
                                avatarId = teacher.imageUrl,
                                frameColorHex = "#FBBF24",
                                level = 35,
                                size = 52.dp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = teacher.name,
                                        color = FontPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    JiuBeltBadge(beltColor = teacher.belt)
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐ ${teacher.rating}", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("•   📍 ${teacher.country}", color = FontSecondary, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Teaches: ${teacher.language}",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Dynamic Pricing display
                            Column(horizontalAlignment = Alignment.End) {
                                Text("💎 ${teacher.hourlyRate}", color = GoldAccent, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                Text("per hour", color = FontSecondary, fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Coach Bio Description
                        Text(
                            text = teacher.bio,
                            color = FontSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = DarkCard)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Schedulers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Available times: Mon-Fri", color = FontSecondary, fontSize = 11.sp)

                            Button(
                                onClick = {
                                    viewModel.addLog("Booked appointment class with master ${teacher.name} for 💎 ${teacher.hourlyRate} Tickets.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("BOOK PRIVATE CLASS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
