package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.JiuSpeakApiClient
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.GlowButton

@Composable
fun SettingsScreen(viewModel: JiuSpeakViewModel) {
    val syncLogs by viewModel.syncLogs.collectAsState()
    var editBaseUrl by remember { mutableStateOf(JiuSpeakApiClient.getBaseUrl()) }
    var mockOfflineToggle by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "CONTROL PANEL",
            subtitle = "Settle custom endpoints and test live server events"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // NODE ENDPOINT SWITCHER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sync Node.js API Address",
                    color = FontPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "If you have a Node.js API configured, enter the URL below to automatically synchronize profiles and scores.",
                    color = FontSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = editBaseUrl,
                    onValueChange = { editBaseUrl = it },
                    label = { Text("Base URL", color = FontSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.updateServerAddress(editBaseUrl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("SAVE & CONNECT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QA UTILITIES SECTION
        Text(
            text = "DEVELOPMENT QA TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Toggle offline mode simulator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Persist Database Locally (Offline Play)", color = FontPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Force applet to store and run battles local-first, avoiding server timeouts.", color = FontSecondary, fontSize = 11.sp)
                    }

                    Switch(
                        checked = mockOfflineToggle,
                        onCheckedChange = {
                            mockOfflineToggle = it
                            viewModel.toggleOfflineSetting(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonBlue.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = DarkCard)

                Spacer(modifier = Modifier.height(16.dp))

                // FCM push launcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate FCM Push Notifications", color = FontPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Simulate a remote background push notification arriving from the Firebase Console.", color = FontSecondary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.simulatePushNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("FIRE PUSH", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE TERMINAL LOGS CONSOLE DISPLAY
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STREAMING CONNECTION LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "LIVE FEED",
                color = Color.Green,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, DarkCard, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(syncLogs) { log ->
                    Text(
                        text = log,
                        color = if (log.contains("ERROR")) Color.Red else Color.Green,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
