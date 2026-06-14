package com.example.ui.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NeonBorderCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonBlue,
    backgroundColor: Color = DarkSurface,
    borderWidth: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                border = BorderStroke(borderWidth, Brush.verticalGradient(listOf(glowColor, Color.Transparent))),
                shape = shape
            )
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = shape
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun JiuBeltBadge(
    beltColor: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (beltColor.uppercase()) {
        "WHITE" -> Pair(BeltWhite, Color(0xFF1E293B))
        "BLUE" -> Pair(BeltBlue, Color.White)
        "PURPLE" -> Pair(BeltPurple, Color.White)
        "BROWN" -> Pair(BeltBrown, Color.White)
        "BLACK" -> Pair(BeltBlack, Color.Red)
        else -> Pair(Color.Gray, Color.White)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = beltColor.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        // BJJ belt black stripe on the right (or red for black belt)
        val stripeColor = if (beltColor.uppercase() == "BLACK") Color.Red else Color.Black
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(8.dp)
                .background(stripeColor)
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // White stripes representing degrees
                repeat(2) {
                    Box(modifier = Modifier.width(1.5.dp).fillMaxHeight().background(Color.White))
                }
            }
        }
    }
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = NeonBlue,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            ),
        colors = colors,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 1.sp,
            color = Color.White
        )
    }
}

@Composable
fun AvatarWithFrame(
    avatarId: String,
    frameColorHex: String,
    level: Int,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(frameColorHex))
    } catch (e: Exception) {
        NeonBlue
    }

    val finalModifier = if (onClick != null) modifier.clickable { onClick() } else modifier

    Box(
        modifier = finalModifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing frame circle
        Box(
            modifier = Modifier
                .size(size)
                .border(2.5.dp, Brush.sweepGradient(listOf(parsedColor, NeonCyan, parsedColor)), CircleShape)
                .padding(4.dp)
        ) {
            // Simulated Avatar Drawings with Brazilian MMA details
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(DarkCard, DarkBg))),
                contentAlignment = Alignment.Center
            ) {
                val symbolText = when (avatarId) {
                    "avatar_fighter" -> "🥋"
                    "avatar_charles" -> "🦁"
                    "avatar_igor" -> "🦅"
                    "avatar_john" -> "💀"
                    "avatar_fighter1" -> "⚡"
                    "avatar_fighter2" -> "⚔️"
                    "avatar_fighter3" -> "🔥"
                    else -> "🥋"
                }
                Text(symbolText, fontSize = (size.value * 0.45).sp)
            }
        }

        // Small level badge floating at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239))))
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .padding(horizontal = 5.dp, vertical = 1.5.dp)
        ) {
            Text(
                text = "LV$level",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CinematicHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                title.uppercase(),
                style = Typography.displayMedium,
                color = FontPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subtitle,
                color = FontSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (actionIcon != null && onActionClick != null) {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkCard)
                    .border(1.dp, NeonBlue.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(actionIcon, contentDescription = "Header action", tint = NeonBlue)
            }
        }
    }
}
