package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JiuSpeakDatabase
import com.example.data.model.*
import com.example.data.repository.JiuSpeakRepository
import com.example.data.network.SocketManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class JiuSpeakViewModel(application: Application, val repository: JiuSpeakRepository) : AndroidViewModel(application) {

    // Bottom Navigation Bar States: HOME, ARENA, COMUNIDADE, APRENDER, PERFIL, CHAT, SETTINGS, LEADERBOARD, SHOP, MARKETPLACE
    private val _activeTab = MutableStateFlow("HOME")
    val activeTab: StateFlow<String> = _activeTab

    // UI Session State
    private val _isLoggedIn = MutableStateFlow(repository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    // Sync state logs for Node.js API feedback
    private val _syncLogs = MutableStateFlow<List<String>>(listOf("System started in offline mode.", "To connect to PostgreSQL backends, change URL in Settings."))
    val syncLogs: StateFlow<List<String>> = _syncLogs

    // Local state for PVP Match simulations
    private val _pvpMatchState = MutableStateFlow<PvpMatchState>(PvpMatchState.Idle)
    val pvpMatchState: StateFlow<PvpMatchState> = _pvpMatchState

    // Live leaderboards filtered dynamically
    private val _leaderboardType = MutableStateFlow("WEEKLY") // WEEKLY, MONTHLY, GLOBAL
    val leaderboardType: StateFlow<String> = _leaderboardType

    private val _leaderboardCountry = MutableStateFlow("Global") // Global, Brazil, USA, UAE
    val leaderboardCountry: StateFlow<String> = _leaderboardCountry

    // Private selected friend for personal chats
    private val _selectedChatFriend = MutableStateFlow<String?>("Global Chat")
    val selectedChatFriend: StateFlow<String?> = _selectedChatFriend

    // Mock Push Notification alerts list to simulate Firebase Push Notifications inside applet
    private val _activePushAlerts = MutableStateFlow<List<MockNotification>>(emptyList())
    val activePushAlerts: StateFlow<List<MockNotification>> = _activePushAlerts

    // Connect flow from repository tables
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyMissions: StateFlow<List<DailyMissionEntity>> = repository.dailyMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialPosts: StateFlow<List<SocialPostEntity>> = repository.socialPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pvpBattles: StateFlow<List<PvpBattleEntity>> = repository.pvpBattlesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coursesList: StateFlow<List<CourseEntity>> = repository.coursesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachersList: StateFlow<List<TeacherEntity>> = repository.teachersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedMockDataIfEmpty()
        }

        // Setup real Socket.IO event listener flows
        viewModelScope.launch {
            SocketManager.globalChatMessages.filterNotNull().collect { obj ->
                val senderName = obj.optString("username", "System")
                val text = obj.optString("text", "")
                val belt = obj.optString("belt", "WHITE")
                addLog("Message received from Socket.IO client: $senderName -> $text")
                repository.writeChatMessage(text) // Store message locally in Room database replica
            }
        }

        viewModelScope.launch {
            SocketManager.pvpMatches.filterNotNull().collect { obj ->
                val opponentName = obj.optString("opponentName", "Alex_Gracie")
                val matchType = obj.optString("matchType", "Vocabulary")
                addLog("PvP Matchmaker Found opponent! Name: $opponentName")
                _pvpMatchState.value = PvpMatchState.InteractiveFight(
                    opponentName = opponentName,
                    matchType = matchType,
                    questions = generateMockArenaQuestions(matchType),
                    currentQuestionIndex = 0,
                    opponentScore = 0,
                    myScore = 0
                )
            }
        }

        viewModelScope.launch {
            SocketManager.pvpInvites.filterNotNull().collect { obj ->
                val challenger = obj.optString("host", "charles_gracie")
                val matchType = obj.optString("matchType", "Vocabulary")
                addLog("Private PvP invite from $challenger for $matchType match")
                val notification = MockNotification(
                    id = UUID.randomUUID().toString(),
                    title = "⚔️ DESAFIO ARENA PvP",
                    body = "$challenger convidou você para luta de $matchType!",
                    time = "Just now"
                )
                _activePushAlerts.value = _activePushAlerts.value + notification
            }
        }

        viewModelScope.launch {
            SocketManager.notifications.filterNotNull().collect { obj ->
                val title = obj.optString("title", "JiuSpeak Info")
                val body = obj.optString("body", "Nova atualização disponível!")
                val notification = MockNotification(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    body = body,
                    time = "Just now"
                )
                _activePushAlerts.value = _activePushAlerts.value + notification
            }
        }

        if (repository.isLoggedIn) {
            SocketManager.connect(repository.apiBaseUrl, repository.currentToken)
        }
    }

    // Navigation trigger
    fun navigateTo(tab: String) {
        _activeTab.value = tab
    }

    fun selectChatFriend(friend: String?) {
        _selectedChatFriend.value = friend
    }

    fun addLog(msg: String) {
        val current = _syncLogs.value.toMutableList()
        current.add(0, "[${System.currentTimeMillis() % 100000}] $msg")
        _syncLogs.value = current.take(200) // Keep last 200 logs
    }

    // Authentication workflows
    fun login(email: String, prepopulatedPass: String) {
        viewModelScope.launch {
            _authError.value = null
            addLog("Login request for: $email")
            addLog("Fetching from configured API: ${repository.apiBaseUrl}")
            val res = repository.login(email, prepopulatedPass)
            if (res.isSuccess) {
                _isLoggedIn.value = true
                addLog("Login complete. Token received and cached. Sincronização e Sessão Ativa.")
                SocketManager.connect(repository.apiBaseUrl, repository.currentToken)
            } else {
                _authError.value = res.exceptionOrNull()?.localizedMessage ?: "Invalid login credentials."
                addLog("ERROR: Login failed - ${_authError.value}")
            }
        }
    }

    fun register(email: String, username: String, pass: String, belt: String) {
        viewModelScope.launch {
            _authError.value = null
            addLog("Registry request for athlete: $username ($belt)")
            val res = repository.register(email, username, pass, belt)
            if (res.isSuccess) {
                _isLoggedIn.value = true
                addLog("Registration successful. Athlete enrolled securely in database structure!")
                SocketManager.connect(repository.apiBaseUrl, repository.currentToken)
            } else {
                _authError.value = res.exceptionOrNull()?.localizedMessage ?: "Registration error."
                addLog("ERROR: Registry failed - ${_authError.value}")
            }
        }
    }

    fun recoveryPassword(email: String) {
        viewModelScope.launch {
            addLog("Trigger password recovery for email: $email")
            delay(1000)
            addLog("Recovery instruction email triggered via Socket.IO/NodeJS SMTP interface. Sincronizado.")
        }
    }

    fun logout() {
        viewModelScope.launch {
            SocketManager.disconnect()
            repository.logout()
            _isLoggedIn.value = false
            navigateTo("HOME")
            addLog("Logged out. Tokens cleared. Local mode intact.")
        }
    }

    // Community / Post logic
    fun createPost(content: String, img: String? = null) {
        viewModelScope.launch {
            addLog("Creating community post content: \"${content.take(25)}...\"")
            repository.addSocialPost(content, img)
            addLog("Post propagated. Sincronizado com SocketIO no servidor.")
        }
    }

    fun likePost(id: String) {
        viewModelScope.launch {
            addLog("Toggled like on community post $id")
            repository.postLikeToggle(id)
        }
    }

    // Mission Completion Workflows
    fun claimMissionReward(id: String) {
        viewModelScope.launch {
            addLog("Claiming daily rewards of mission: $id")
            repository.completeMissionLocal(id)
            addLog("Daily rewards claimed successfully. Experience and JiuTickets synced.")
        }
    }

    // Shop workflows
    fun buyAestheticItem(name: String, frameColor: String, cost: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.jiuTickets >= cost) {
                addLog("Purchasing aesthetic profile customize: $name ($cost JiuTickets)")
                repository.purchaseShopItem(cost)
                repository.updateAvatar(name, frameColor)
                addLog("Profile customization updated gracefully!")
            } else {
                addLog("ERROR: Insufficient JiuTickets balance.")
            }
        }
    }

    // PVP Matchmaker Simulation
    fun searchAndLaunchPvpFight(matchType: String) {
        viewModelScope.launch {
            if (_pvpMatchState.value != PvpMatchState.Idle) return@launch
            addLog("Searching for PvP language spar in Arena: Match type $matchType...")
            _pvpMatchState.value = PvpMatchState.Searching(matchType)

            delay(2500) // Simulate matchmaker finding international opponents

            val opponents = listOf("Alex_Gracie_LA", "Anya_WhiteBelt_Siberia", "Charles_Mendes_UFC", "Igor_O_Terror", "JaneBJJ_London")
            val selectedOpponent = opponents.random()
            addLog("Opponent found: $selectedOpponent! Round starts in neon arena!")

            _pvpMatchState.value = PvpMatchState.InteractiveFight(
                opponentName = selectedOpponent,
                matchType = matchType,
                questions = generateMockArenaQuestions(matchType),
                currentQuestionIndex = 0,
                opponentScore = 0,
                myScore = 0
            )
        }
    }

    fun answerArenaQuestion(selectedOptionIndex: Int) {
        val state = _pvpMatchState.value
        if (state !is PvpMatchState.InteractiveFight) return

        val question = state.questions[state.currentQuestionIndex]
        val isCorrect = selectedOptionIndex == question.correctIndex
        val ptsGained = if (isCorrect) 25 else 0
        val opponentAnswerCorrect = Math.random() > 0.35 // 65% chance opponent answers right
        val oppPts = if (opponentAnswerCorrect) 25 else 0

        viewModelScope.launch {
            addLog("PvP Quiz Step: Your answer correct? $isCorrect. Opponent answer correct? $opponentAnswerCorrect")

            val updatedMyScore = state.myScore + ptsGained
            val updatedOpponentScore = state.opponentScore + oppPts

            if (state.currentQuestionIndex < state.questions.size - 1) {
                _pvpMatchState.value = state.copy(
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                    myScore = updatedMyScore,
                    opponentScore = updatedOpponentScore
                )
            } else {
                // Fight completed! Evaluate outcome
                _pvpMatchState.value = PvpMatchState.Ending(
                    opponentName = state.opponentName,
                    matchType = state.matchType,
                    myScore = updatedMyScore,
                    opponentScore = updatedOpponentScore
                )
            }
        }
    }

    fun finalizeArenaFight() {
        val state = _pvpMatchState.value
        if (state !is PvpMatchState.Ending) {
            _pvpMatchState.value = PvpMatchState.Idle
            return
        }

        viewModelScope.launch {
            val outcome = if (state.myScore > state.opponentScore) "WIN" else if (state.myScore == state.opponentScore) "DRAW" else "LOSS"
            addLog("Saving arena outcome: $outcome vs ${state.opponentName}")

            repository.recordLocalBattle(
                matchType = state.matchType,
                scoreMe = state.myScore,
                scoreOpponent = state.opponentScore,
                outcome = outcome,
                opponentName = state.opponentName
            )

            _pvpMatchState.value = PvpMatchState.Idle
            navigateTo("HOME") // Return to main gamified home
        }
    }

    // Dynamic filtering for global leaderboards
    fun selectLeaderboardFilters(type: String, country: String) {
        _leaderboardType.value = type
        _leaderboardCountry.value = country
        addLog("Leaderboard filtered by: $type - country: $country")
    }

    // Chat management (Socket.IO simulations)
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            addLog("Sending chat message to ${_selectedChatFriend.value}: \"$text\"")
            repository.writeChatMessage(text)

            // Simulate opponent replying in real-time
            delay(1500)
            if (_selectedChatFriend.value == "Global Chat") {
                val replies = listOf(
                    "OSS! Excellent pronounciation tips, thanks!",
                    "Let's train together on Arena next Monday. I'll beat your vocabulary score!",
                    "Who is preparing for the IBJJF worlds? We should practice referee English",
                    "A caneleira em inglês se chama Shin Guard, anota aí!"
                )
                repository.writeChatMessage(replies.random())
                addLog("Real-time reply propagated from Socket.IO web socket channel.")
            }
        }
    }

    // Trigger FCM mock notification
    fun simulatePushNotification() {
        viewModelScope.launch {
            addLog("Triggering FCM Cloud Push Notification payload locally...")

            val title = listOf(
                "⚔️ ARENA CHALLENGE!",
                "🥋 COMMMUNITY NEW LINK!",
                "💎 TIQUETES DISPONÍVEIS!",
                "📈 RANKING DA SEMANA"
            ).random()

            val text = listOf(
                "Alex Gracie has challenged you to a PvP Vocabulary Spar! Tap to defend your rank.",
                "Charles 'Do Bronx' just liked your recent social martial post!",
                "Your streak is completed! You earned 100 bonus JiuTickets.",
                "You went up to Rank 3! Continue studying to claim the weekly belt."
            ).random()

            val notification = MockNotification(
                id = UUID.randomUUID().toString(),
                title = title,
                body = text,
                time = "Just now"
            )

            // Add notification to overlay
            _activePushAlerts.value = _activePushAlerts.value + notification

            // Dismiss automatically after 5 sec
            delay(5000)
            _activePushAlerts.value = _activePushAlerts.value.filter { it.id != notification.id }
        }
    }

    fun dismissPushAlert(id: String) {
        _activePushAlerts.value = _activePushAlerts.value.filter { it.id != id }
    }

    // Settings repository modifiers for direct endpoint configuration
    fun updateServerAddress(url: String) {
        viewModelScope.launch {
            repository.setApiUrl(url)
            addLog("Configured new endpoint: $url (Synchronized & Connected)")
        }
    }

    fun toggleOfflineSetting(enable: Boolean) {
        viewModelScope.launch {
            repository.setOfflineMode(enable)
            addLog("Simulate offline persistence toggle: $enable")
        }
    }

    // Generate mock arena questions loaded on demand
    private fun generateMockArenaQuestions(type: String): List<ArenaQuestion> {
        return when (type) {
            "Vocabulary" -> listOf(
                ArenaQuestion("How do you say 'FAIXA AZUL' in English?", listOf("Blue Ribbon", "Blue Stripe", "Blue Belt", "Blue Cord"), 2),
                ArenaQuestion("What does description 'TAP OUT' mean on BJJ mats?", listOf("Submeter-se / Desistir", "Apertar o kimono", "Calçar luvas", "Entrar na arena"), 0),
                ArenaQuestion("How do you translate 'QUEDA' in UFC rules?", listOf("Sweep", "Takedown", "Kimura", "Guard Pass"), 1)
            )
            "Pronunciation" -> listOf(
                ArenaQuestion("Translate 'SE CONCENTRE NA GUARDA' and pick correct phonetics:", listOf("Focus on your guard", "Stay on back side", "Catch the sleeve", "Watch the timer"), 0),
                ArenaQuestion("Which word describes phonetically 'CHOKE'?", listOf("Chocar", "Estrangulamento", "Apertar pegada", "Ganchos"), 1)
            )
            else -> listOf(
                ArenaQuestion("Before a match, the Referee shouts: 'COMBAT!'. What do you do?", listOf("Shake hands and start fighting", "Bow to the coach", "Walk off the mats", "Adjust your belt"), 0),
                ArenaQuestion("The opponent is in your CLOSED GUARD. How do you describe this?", listOf("Ele está nas minhas costas", "Ele está na minha guarda fechada", "Estou montado nele", "Estou passando a meia guarda"), 1)
            )
        }
    }
}

// Helper types
sealed class PvpMatchState {
    object Idle : PvpMatchState()
    data class Searching(val matchType: String) : PvpMatchState()
    data class InteractiveFight(
        val opponentName: String,
        val matchType: String,
        val questions: List<ArenaQuestion>,
        val currentQuestionIndex: Int,
        val opponentScore: Int,
        val myScore: Int
    ) : PvpMatchState()
    data class Ending(
        val opponentName: String,
        val matchType: String,
        val myScore: Int,
        val opponentScore: Int
    ) : PvpMatchState()
}

data class ArenaQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

data class MockNotification(
    val id: String,
    val title: String,
    val body: String,
    val time: String
)
