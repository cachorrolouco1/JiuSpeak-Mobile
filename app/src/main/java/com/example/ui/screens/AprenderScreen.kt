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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.CinematicHeader
import com.example.ui.widgets.GlowButton
import com.example.ui.widgets.NeonBorderCard

@Composable
fun AprenderScreen(viewModel: JiuSpeakViewModel) {
    val courses by viewModel.coursesList.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "English for Competitors", "Referee English")
    val filteredCourses = if (selectedCategory == "ALL") courses else courses.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CinematicHeader(
            title = "MARTIAL SYLLABUS",
            subtitle = "Syllabus tracks synchronized continuously with PostgreSQL backend"
        )

        // SYNC STATUS REPORT BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = NeonBlue)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("SYNCHRONIZED METRICS", color = FontPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Lessons progress is automatically verified with existing Node.js APIs.", color = FontSecondary, fontSize = 10.sp)
                }
            }
        }

        // CATEGORIES TABS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonBlue else DarkCard)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (cat == "ALL") "ALL TRACKS" else cat.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else FontSecondary
                    )
                }
            }
        }

        // TRILHAS / COURSES TRACKS LIST
        Text(
            text = "AVAILABLE COURSES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        filteredCourses.forEach { course ->
            val ratio = course.completedLessons.toFloat() / course.totalLessons
            val isCompleted = course.completedLessons == course.totalLessons

            NeonBorderCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                glowColor = if (isCompleted) GoldAccent else NeonBlue
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DarkCard)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(course.category.uppercase(), color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(course.title, color = FontPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(course.subtitle, color = FontSecondary, fontSize = 11.sp)
                        }

                        if (isCompleted) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f))
                                    .padding(6.dp)
                            ) {
                                Icon(Icons.Default.LightbulbCircle, contentDescription = "Certified", tint = GoldAccent, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${course.completedLessons}/${course.totalLessons} LESSONS COMPLETED",
                            fontSize = 10.sp,
                            color = FontSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = if (isCompleted) GoldAccent else NeonBlue,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(NeonBlue, if (isCompleted) GoldAccent else NeonCyan)))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action elements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Multiplier: ", fontSize = 10.sp, color = FontSecondary)
                            Text("x${course.xpMultiplier} XP", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.addLog("Entered classroom for syllabus: ${course.title}")
                                viewModel.navigateTo("ARENA") // Direct link to arena PVP drills as core gamified approach!
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isCompleted) DarkCard else NeonBlue),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isCompleted) "REVIEW SYLLABUS" else "CONTINUE LECTURE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CERTIFICATIONS METRICS CABINET
        Text(
            text = "EARNED CERTIFICATES",
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎓", fontSize = 36.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("INTERNATIONAL CERTIFICATE: LEVEL 1 APPROVED", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Approved by IBJJF Linguistics Board • UUID-893JKS", color = FontSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Allows you to apply for international coach registrations worldwide.", color = FontSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}
