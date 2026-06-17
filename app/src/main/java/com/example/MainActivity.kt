package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.local.JiuSpeakDatabase
import com.example.data.repository.JiuSpeakRepository
import com.example.ui.screens.*
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.FontPrimary
import com.example.ui.theme.FontSecondary
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.JiuSpeakViewModel

class MainActivity : ComponentActivity() {

    private val database: JiuSpeakDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            JiuSpeakDatabase::class.java,
            "jiuspeak_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val repository: JiuSpeakRepository by lazy {
        JiuSpeakRepository(applicationContext, database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Instantiating custom ViewModel with standard factory to inject repository parameter
                val model: JiuSpeakViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return JiuSpeakViewModel(application, repository) as T
                        }
                    }
                )

                MainScaffoldLayout(viewModel = model)
            }
        }
    }
}

@Composable
fun MainScaffoldLayout(viewModel: JiuSpeakViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            LoginScreen(viewModel = viewModel)
        } else {
            Scaffold(
                bottomBar = {
                    BottomNavWithIndicator(
                        activeTab = activeTab,
                        onTabSelect = { viewModel.navigateTo(it) }
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBg)
                        .padding(paddingValues)
                ) {
                    // TAB NAVIGATION DISPATCHER WITH SLIDE-FADE TRANSITION FLOW
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(animationSpec = tween(220), initialOffsetY = { 40 }))
                                .togetherWith(fadeOut(tween(140)))
                        },
                        label = "MainTabsTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            "HOME" -> HomeScreen(viewModel = viewModel)
                            "ARENA" -> ArenaScreen(viewModel = viewModel)
                            "COMUNIDADE" -> ComunidadeScreen(viewModel = viewModel)
                            "APRENDER" -> AprenderScreen(viewModel = viewModel)
                            "PERFIL" -> PerfilScreen(viewModel = viewModel)
                            "WALLET" -> WalletScreen(viewModel = viewModel)
                            "CHAT" -> ChatScreen(viewModel = viewModel)
                            "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                            "SHOP" -> ShopScreen(viewModel = viewModel)
                            "MARKETPLACE" -> MarketplaceScreen(viewModel = viewModel)
                            "LEADERBOARD" -> LeaderboardScreen(viewModel = viewModel)
                            "SEASON_PASS" -> SeasonPassScreen(viewModel = viewModel)
                            "CLANS" -> ClanScreen(viewModel = viewModel)
                            "LEAGUE" -> LeagueScreen(viewModel = viewModel)
                            "ACHIEVEMENTS" -> AchievementsScreen(viewModel = viewModel)
                            else -> HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavWithIndicator(
    activeTab: String,
    onTabSelect: (String) -> Unit
) {
    // We enforce solid jet dark background with neon lights borders as per Apple/Directives!
    val tabs = listOf(
        Triple("HOME", "Home", Icons.Default.Home),
        Triple("ARENA", "Arena", Icons.Default.Security), // Fighting icon representation
        Triple("COMUNIDADE", "Comunidade", Icons.Default.People),
        Triple("APRENDER", "Aprender", Icons.Default.MenuBook),
        Triple("PERFIL", "Perfil", Icons.Default.AccountCircle)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(BorderStroke(1.dp, Brush.verticalGradient(listOf(DarkCard, Color.Transparent))))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (route, label, icon) ->
            val isSelected = activeTab == route || (route == "HOME" && activeTab == "SETTINGS") || (route == "PERFIL" && activeTab == "SHOP") || (route == "APRENDER" && activeTab == "MARKETPLACE")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelect(route) }
                    .padding(vertical = 4.dp)
            ) {
                // Interactive indicator pill on icons
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) NeonBlue else FontSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) NeonBlue else FontSecondary
                )
            }
        }
    }
}
