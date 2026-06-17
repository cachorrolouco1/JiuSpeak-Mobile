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
import androidx.compose.material.icons.filled.Refresh
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

    val shopItems by viewModel.shopItems.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadShopItems()
        viewModel.loadInventory()
    }

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

                IconButton(
                    onClick = {
                        viewModel.loadShopItems()
                        viewModel.loadInventory()
                        viewModel.syncAllData()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recarregar Loja",
                        tint = GoldAccent
                    )
                }
            }
        }

        // STORE SECTION
        Text(
            text = "SHOP BOUTIQUE CATALOG",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonBlue,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (shopItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .border(1.dp, DarkCard, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "O Shōgun catálogo de vendas está vazio no servidor.",
                        color = FontSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Não há itens cadastrados ou você já possui todos.",
                        color = FontSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            shopItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        val isPurchasable = currentProfile.jiuTickets >= item.cost

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.buyAestheticItem(item.id)
                                },
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, if (isPurchasable) NeonCyan.copy(alpha = 0.3f) else DarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarWithFrame(
                                    avatarId = item.avatarId,
                                    frameColorHex = item.frameColor,
                                    level = currentProfile.level,
                                    size = 56.dp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.name,
                                    color = FontPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("💎", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.cost} Tickets",
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
                                        text = if (currentProfile.selectedAvatar == item.avatarId) "EQUIPPED" else if (isPurchasable) "UNLOCK" else "COMPRAR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPurchasable) Color.White else FontSecondary
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PERSONAL INVENTORY SECTION
        Text(
            text = "SEU INVENTÓRIO (ARMORY)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (inventoryItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .border(1.dp, DarkCard, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum item encontrado no inventário da conta. Acesse a Loja para equipar o seu lutador",
                    color = FontSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            inventoryItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        val isEquipped = item.isEquipped || currentProfile.selectedAvatar == item.avatarId

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.equipInventoryItem(item.id)
                                },
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, if (isEquipped) GoldAccent.copy(alpha = 0.5f) else DarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarWithFrame(
                                    avatarId = item.avatarId,
                                    frameColorHex = item.frameColor,
                                    level = currentProfile.level,
                                    size = 56.dp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.name,
                                    color = FontPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isEquipped) GoldAccent else DarkCard)
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEquipped) "EQUIPPED" else "EQUIPAR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEquipped) Color.Black else FontSecondary
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
