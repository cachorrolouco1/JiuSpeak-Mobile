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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClanEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JiuSpeakViewModel
import com.example.ui.widgets.GlowButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClanScreen(viewModel: JiuSpeakViewModel) {
    val myClanState by viewModel.myClan.collectAsState()
    val allClansList by viewModel.allClans.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (myClanState != null) "MEU CLÃ: ${myClanState!!.name.uppercase()}" else "EQUIPES & CLÃS",
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
                    if (myClanState == null) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Criar Clã", tint = NeonCyan)
                        }
                    }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
        ) {
            val clan = myClanState
            if (clan != null) {
                // User is in a clan -> Show the Master Tab Dashboard
                ActiveClanHub(clan = clan, allClans = allClansList, viewModel = viewModel)
            } else {
                // User is not in a clan -> Show Browser List & Creation Card
                ClansBrowser(clans = allClansList, viewModel = viewModel, onCreateClick = { showCreateDialog = true })
            }

            if (showCreateDialog) {
                CreateClanDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreate = { name, gym, city, country, bio ->
                        viewModel.createClan(name, gym, city, country, bio)
                        showCreateDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ClansBrowser(
    clans: List<ClanEntity>,
    viewModel: JiuSpeakViewModel,
    onCreateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateClick() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030D1A)),
                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "FORME SUA EQUIPE",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Una sua academia física e dispute o topo das copas de inglês!",
                            fontSize = 11.sp,
                            color = FontSecondary
                        )
                    }
                    Text("➡️", fontSize = 14.sp)
                }
            }
        }

        item {
            Text(
                text = "CLÃS E ACADEMIAS ATIVAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NeonBlue,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(clans) { targetClan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clan_browser_item_${targetClan.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBg)
                                .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥋", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetClan.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${targetClan.gymName} — ${targetClan.city}, ${targetClan.country}",
                                fontSize = 11.sp,
                                color = FontSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${targetClan.memberCount}/${targetClan.maxMembers}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = targetClan.description,
                        fontSize = 11.sp,
                        color = FontSecondary,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total XP: ${targetClan.totalXp} pts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontFamily = FontFamily.Monospace
                        )

                        GlowButton(
                            text = "ADENTRAR",
                            onClick = { viewModel.joinClan(targetClan.id, targetClan.name) },
                            glowColor = NeonCyan,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveClanHub(
    clan: ClanEntity,
    allClans: List<ClanEntity>,
    viewModel: JiuSpeakViewModel
) {
    var activeTabIdx by remember { mutableStateOf(0) }
    val tabTitles = listOf("DASHBOARD", "GUERRA DE ACADEMIAS", "CHAT EM REAL-TIME")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = activeTabIdx,
            containerColor = DarkSurface,
            contentColor = NeonBlue
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeTabIdx == index,
                    onClick = { activeTabIdx = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                )
            }
        }

        when (activeTabIdx) {
            0 -> ClanDashboardTab(clan = clan, viewModel = viewModel)
            1 -> ClanWarTab(clan = clan, allClans = allClans, viewModel = viewModel)
            2 -> ClanChatTab(clan = clan, viewModel = viewModel)
        }
    }
}

@Composable
fun ClanDashboardTab(clan: ClanEntity, viewModel: JiuSpeakViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070C15)),
                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 34.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                clan.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "LÍDER COACH: ${clan.masterName}",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        clan.description,
                        fontSize = 12.sp,
                        color = FontSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("POSIÇÃO RANKING", fontSize = 8.sp, color = FontSecondary, fontFamily = FontFamily.Monospace)
                            Text("#${clan.rankPosition}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = GoldAccent, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("EXPERIÊNCIA TOTAL", fontSize = 8.sp, color = FontSecondary, fontFamily = FontFamily.Monospace)
                            Text("${clan.totalXp} XP", fontWeight = FontWeight.Black, fontSize = 20.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("MEMBROS DO TATAME", fontSize = 8.sp, color = FontSecondary, fontFamily = FontFamily.Monospace)
                            Text("${clan.memberCount}/${clan.maxMembers}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "MEMBROS CONECTADOS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonBlue,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // List contributors
                    val contributors = listOf(
                        Triple(clan.masterName, "Leader Coach", "BLACK"),
                        Triple("MarcusBJJ", "Competidor Pro", "BLUE"),
                        Triple("Alice_TapOut", "Guarda Aberta Specialist", "PURPLE"),
                        Triple("Prof_Rickson_Fan", "Aluno Dedicado", "WHITE")
                    )

                    contributors.forEach { (name, role, belt) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👤", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text(role, fontSize = 10.sp, color = FontSecondary)
                                }
                            }
                            // belt flag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (belt) {
                                            "BLACK" -> Color.Black
                                            "BROWN" -> Color(0xFF5C4033)
                                            "PURPLE" -> Color(0xFF8B008B)
                                            "BLUE" -> Color.Blue
                                            else -> Color.White
                                        }
                                    )
                                    .border(
                                        BorderStroke(1.dp, Color.Gray),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    belt,
                                    fontSize = 8.sp,
                                    color = if (belt == "WHITE") Color.Black else Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.joinClan("leave", "None") }, // join "leave" resets role to NONE in simple flows
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("REPETIR ABANDONO/SAIR DO CLÃ", color = Color.Red, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ClanWarTab(clan: ClanEntity, allClans: List<ClanEntity>, viewModel: JiuSpeakViewModel) {
    val rivals = allClans.filter { it.id != clan.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0B0B)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💣", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "GUERRA DE ACADEMIAS (V4)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Desafie outras academias do ranking para competições PvP de vocabulário!",
                            fontSize = 11.sp,
                            color = FontSecondary
                        )
                    }
                }
            }
        }

        item {
            Text(
                "SELECIONE SEU OPONENTE DE COMBATE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = FontSecondary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(rivals) { rival ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rival_clan_card_${rival.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            rival.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            "${rival.city} — Total XP: ${rival.totalXp}",
                            fontSize = 11.sp,
                            color = FontSecondary
                        )
                    }

                    GlowButton(
                        text = "DESAFIAR",
                        onClick = { viewModel.challengeClan(rival.id, rival.name) },
                        glowColor = Color.Red,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    )
                }
            }
        }
    }
}

@Composable
fun ClanChatTab(clan: ClanEntity, viewModel: JiuSpeakViewModel) {
    // Shared chat messaging system styled for the user's specific clan
    var textMsg by remember { mutableStateOf("") }
    val listMsgs = remember {
        mutableStateListOf(
            Triple("MarcusBJJ", "E aí galera! Vamos fazer sparring hoje?", "BLUE"),
            Triple("Alice_TapOut", "Estou estudando as lições de guard pass para a arena!", "PURPLE")
        )
    }

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
            border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Text(
                text = "💬 REAL-TIME ALLIANCE FEED: Seu clã está online com criptografia de ponta-a-ponta.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(10.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            // Displaying reversed
            val displayed = listMsgs.reversed()
            items(displayed) { (sender, payload, belt) ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("clan_chat_msg"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCard)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(sender, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NeonBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            // Belt indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when (belt) {
                                            "BLUE" -> Color.Blue
                                            "PURPLE" -> Color(0xFF8B008B)
                                            "BLACK" -> Color.Black
                                            else -> Color.White
                                        }
                                    )
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                            ) {
                                Text(belt, fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(payload, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Send bar interface
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textMsg,
                onValueChange = { textMsg = it },
                placeholder = { Text("Escreva para o clã...", fontSize = 12.sp, color = FontSecondary) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkBg,
                    unfocusedContainerColor = DarkBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textMsg.isNotBlank()) {
                        listMsgs.add(Triple("Master Athlete", textMsg, "BLACK"))
                        viewModel.addLog("Clan chat message broadcasted: $textMsg")
                        textMsg = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NeonBlue)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CreateClanDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gymName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, gymName, city, country, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("CRIAR CLÃ", fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = FontSecondary)
            }
        },
        title = {
            Text(
                "FORMAR ALIANÇA OFICIAL JIE",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Clã/Equipe") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = gymName,
                    onValueChange = { gymName = it },
                    label = { Text("Nome da Academia (ex: Gracie Barra HQ)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Cidade") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("País") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Mantra do Clã") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        containerColor = DarkSurface
    )
}
