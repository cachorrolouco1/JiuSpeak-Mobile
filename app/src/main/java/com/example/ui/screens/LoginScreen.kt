package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.GlowButton
import com.example.ui.widgets.NeonBorderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: JiuSpeakViewModel) {
    var isRegisterState by remember { mutableStateOf(false) }
    var isRecoveryState by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("campeao@jiuspeak.com") }
    var username by remember { mutableStateOf("Fighter99") }
    var password by remember { mutableStateOf("bjj123") }
    var selectedBelt by remember { mutableStateOf("BLUE") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBg, Color(0xFF000814))))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cinematic Logo
            Text(
                text = "JIUSPEAK",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 4.sp,
                color = NeonBlue
            )
            Text(
                text = "TRAIN HARD. SPEAK GLOBALLY.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(24.dp))

            NeonBorderCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = NeonBlue
            ) {
                Text(
                    text = when {
                        isRecoveryState -> "FORGOT PASSWORD"
                        isRegisterState -> "CREATE CHAMPION PROFILE"
                        else -> "ATHLETE LOGIN"
                    },
                    style = Typography.titleLarge,
                    color = FontPrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                if (authError != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loginOffline() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300F0FF)),
                            border = BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ENTRAR NO MODO DEMO OFFLINE",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = FontSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonBlue) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isRegisterState) {
                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = FontSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonBlue) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Belt selector
                    Text(
                        text = "Select Your Belt Color",
                        color = FontSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val belts = listOf("WHITE", "BLUE", "PURPLE", "BROWN", "BLACK")
                        belts.forEach { belt ->
                            val isSelected = selectedBelt == belt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) NeonBlue else DarkCard)
                                    .border(1.dp, if (isSelected) NeonCyan else Color.Transparent)
                                    .clickable { selectedBelt = belt }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = belt.take(3),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else FontSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!isRecoveryState) {
                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = FontSecondary) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonBlue) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = FontSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isRegisterState) {
                        Text(
                            text = "Forgot password?",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { isRecoveryState = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlowButton(
                    text = when {
                        isRecoveryState -> "Send Recovery Link"
                        isRegisterState -> "S'enroller & Entrar"
                        else -> "Enter Arena"
                    },
                    onClick = {
                        if (isRecoveryState) {
                            viewModel.recoveryPassword(email)
                            isRecoveryState = false
                        } else if (isRegisterState) {
                            viewModel.register(email, username, password, selectedBelt)
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            isRecoveryState -> "Back to login"
                            isRegisterState -> "Already registered?"
                            else -> "New athlete?"
                        },
                        color = FontSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isRecoveryState -> "Login"
                            isRegisterState -> "Sign In"
                            else -> "Create Account"
                        },
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            if (isRecoveryState) {
                                isRecoveryState = false
                            } else {
                                isRegisterState = !isRegisterState
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DarkCard, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.loginOffline() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, NeonBlue),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EXPLORAR APP SEM CONEXÃO (MODO DEMO)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Brand footer
            Text(
                text = "FROM THE MATS TO THE WORLD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = FontSecondary.copy(alpha = 0.5f)
            )
        }
    }
}
