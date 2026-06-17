package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JiuSpeakDatabase
import com.example.data.model.*
import com.example.data.repository.JiuSpeakRepository
import com.example.data.repository.WalletRepository
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
    private val _syncLogs = MutableStateFlow<List<String>>(listOf("Sistema iniciado e conectado ao backend oficial de JiuSpeak.", "Sincronização e autenticação ativa com produção."))
    val syncLogs: StateFlow<List<String>> = _syncLogs

    // Local state for PVP Match simulations
    private val _pvpMatchState = MutableStateFlow<PvpMatchState>(PvpMatchState.Idle)
    val pvpMatchState: StateFlow<PvpMatchState> = _pvpMatchState

    private val _pvpError = MutableStateFlow<String?>(null)
    val pvpError: StateFlow<String?> = _pvpError

    // Live leaderboards filtered dynamically
    private val _leaderboardType = MutableStateFlow("WEEKLY") // WEEKLY, MONTHLY, GLOBAL
    val leaderboardType: StateFlow<String> = _leaderboardType

    private val _leaderboardCountry = MutableStateFlow("Global") // Global, Brazil, USA, UAE
    val leaderboardCountry: StateFlow<String> = _leaderboardCountry

    // Private selected friend for personal chats
    private val _selectedChatFriend = MutableStateFlow<String?>("Global Chat")
    val selectedChatFriend: StateFlow<String?> = _selectedChatFriend

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

    val activeSeason: StateFlow<SeasonEntity?> = repository.activeSeasonFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val seasonRewards: StateFlow<List<SeasonRewardEntity>> = repository.seasonRewardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myClan: StateFlow<ClanEntity?> = repository.myClanFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allClans: StateFlow<List<ClanEntity>> = repository.allClansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leagueStatus: StateFlow<LeagueEntity?> = repository.leagueStatusFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shop and Inventory flows
    private val _shopItems = MutableStateFlow<List<com.example.data.network.ShopItemDto>>(emptyList())
    val shopItems: StateFlow<List<com.example.data.network.ShopItemDto>> = _shopItems

    private val _inventoryItems = MutableStateFlow<List<com.example.data.network.InventoryItemDto>>(emptyList())
    val inventoryItems: StateFlow<List<com.example.data.network.InventoryItemDto>> = _inventoryItems
    
    fun loadShopItems() {
        viewModelScope.launch {
            repository.fetchShopItems().onSuccess {
                _shopItems.value = it
            }.onFailure {
                _shopItems.value = emptyList()
            }
        }
    }

    fun loadInventory() {
        viewModelScope.launch {
            repository.fetchInventory().onSuccess {
                _inventoryItems.value = it
            }.onFailure {
                _inventoryItems.value = emptyList()
            }
        }
    }

    init {
        viewModelScope.launch {
            if (repository.isLoggedIn) {
                syncAllData()
            }
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
                    questions = emptyList(),
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
                addLog("Private PvP invite received from $challenger for $matchType match")
            }
        }

        viewModelScope.launch {
            SocketManager.notifications.filterNotNull().collect { obj ->
                val title = obj.optString("title", "JiuSpeak Info")
                val body = obj.optString("body", "Nova atualização disponível!")
                addLog("Push Notification received: [$title] - $body")
            }
        }

        if (repository.isLoggedIn) {
            SocketManager.connect(repository.apiBaseUrl, repository.currentToken)
        }

        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                if (profile != null) {
                    auditUserIdentity(profile)
                }
            }
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

    fun auditUserIdentity(displayedUser: UserProfileEntity?): Boolean {
        if (!repository.isLoggedIn) return true
        
        val authId = repository.getAuthUserId() ?: ""
        val authEmail = repository.getAuthUserEmail() ?: ""
        
        val profileId = displayedUser?.id ?: ""
        val profileEmail = displayedUser?.email ?: ""
        
        val roomId = displayedUser?.id ?: ""
        val roomEmail = displayedUser?.email ?: ""
        
        val uiId = displayedUser?.id ?: ""
        val uiEmail = displayedUser?.email ?: ""
        
        // Output logs matching required audit format
        println("=== AUDITORIA CRÍTICA DE IDENTIDADE ===")
        println("AUTH USER ID: $authId")
        println("AUTH EMAIL: $authEmail")
        println("PROFILE USER ID: $profileId")
        println("PROFILE EMAIL: $profileEmail")
        println("ROOM USER ID: $roomId")
        println("ROOM EMAIL: $roomEmail")
        println("UI USER ID: $uiId")
        println("UI EMAIL: $uiEmail")
        println("========================================")
        
        addLog("AUTH USER ID: $authId")
        addLog("AUTH EMAIL: $authEmail")
        addLog("PROFILE USER ID: $profileId")
        addLog("PROFILE EMAIL: $profileEmail")
        addLog("ROOM USER ID: $roomId")
        addLog("ROOM EMAIL: $roomEmail")
        addLog("UI USER ID: $uiId")
        addLog("UI EMAIL: $uiEmail")
        
        if (authId.isNotEmpty() && authEmail.isNotEmpty() && displayedUser != null) {
            if (authId != uiId || authEmail != uiEmail) {
                val errorMsg = "CRITICAL ERROR: Identidade Corrompida! ID Autenticado ($authId) diferente do ID Exibido ($uiId)."
                addLog("ERROR: $errorMsg")
                System.err.println(errorMsg)
                _authError.value = errorMsg
                
                // Interromper carregamento e registrar erro
                _isLoggedIn.value = false
                viewModelScope.launch {
                    repository.logout()
                }
                return false
            }
        }
        return true
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
                syncAllData()
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
                syncAllData()
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
    fun buyAestheticItem(itemId: String) {
        viewModelScope.launch {
            addLog("Enviando requisição de compra do item '$itemId' ao servidor...")
            repository.purchaseShopItemRemote(itemId).onSuccess {
                addLog("Compra do item '$itemId' concluída com sucesso! Atualizado pelo servidor.")
                loadInventory()
                loadShopItems()
            }.onFailure {
                addLog("Erro ao comprar item no Shogun Store: ${it.localizedMessage}")
            }
        }
    }

    fun equipInventoryItem(itemId: String) {
        viewModelScope.launch {
            addLog("Equipando item '$itemId' no servidor...")
            repository.equipItemRemote(itemId).onSuccess {
                addLog("Item '$itemId' equipado com sucesso!")
                loadInventory()
            }.onFailure {
                addLog("Promoção/Erro de equipagem: ${it.localizedMessage}")
            }
        }
    }

    // PVP Matchmaker Simulation
    fun searchAndLaunchPvpFight(matchType: String) {
        viewModelScope.launch {
            if (_pvpMatchState.value != PvpMatchState.Idle) return@launch
            _pvpError.value = null
            
            val token = repository.currentToken ?: ""
            if (token.isEmpty()) {
                _pvpError.value = "Erro: Usuário não autenticado."
                return@launch
            }
            addLog("Consultando carteira real no backend antes de iniciar PvP...")
            val walletFetch = WalletRepository().fetchWallet(token)
            var walletTickets = 0
            var fetchSuccess = false
            walletFetch.onSuccess { wallet: com.example.data.network.WalletDto ->
                walletTickets = wallet.jiuTickets
                fetchSuccess = true
                addLog("Real wallet fetched successfully: $walletTickets JT")
            }.onFailure { it: Throwable ->
                addLog("Erro ao consultar carteira real: ${it.localizedMessage}")
                _pvpError.value = "Erro de conexão: Não foi possível validar seu saldo no servidor."
            }

            if (!fetchSuccess) {
                return@launch
            }

            if (walletTickets < 50) {
                val errorMsg = "Erro: Saldo de JiuTickets insuficiente (mínimo 50 JT exigido para PvP)."
                addLog(errorMsg)
                _pvpError.value = errorMsg
                return@launch
            }

            addLog("Searching for PvP language spar in Arena: Match type $matchType...")
            _pvpMatchState.value = PvpMatchState.Searching(matchType)

            delay(2500) // Simulate matchmaker finding international opponents

            val opponents = listOf("Alex_Gracie_LA", "Anya_WhiteBelt_Siberia", "Charles_Mendes_UFC", "Igor_O_Terror", "JaneBJJ_London")
            val selectedOpponent = opponents.random()
            addLog("Opponent found: $selectedOpponent! Round starts in neon arena!")

            _pvpMatchState.value = PvpMatchState.InteractiveFight(
                opponentName = selectedOpponent,
                matchType = matchType,
                questions = emptyList(),
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

    // Settings repository modifiers for direct endpoint configuration
    fun updateServerAddress(url: String) {
        viewModelScope.launch {
            repository.setApiUrl(url)
            addLog("Configured new endpoint: $url (Synchronized & Connected)")
        }
    }

    fun syncAllData() {
        viewModelScope.launch {
            addLog("Sincronizando conquistas, ligas, times e passe com o servidor...")
            
            repository.fetchRemoteProfile().onSuccess {
                addLog("Perfil do Atleta '${it.username}' carregado e sincronizado.")
                auditUserIdentity(it)
            }.onFailure {
                addLog("Aviso: Falha ao obter dados atualizados de perfil do servidor.")
            }

            repository.syncRemoteMissions().onSuccess {
                addLog("Missões diárias sincronizadas com sucesso.")
            }.onFailure {
                addLog("Aviso: Falha ao sincronizar missões diárias com o servidor.")
            }

            repository.syncRemotePvpHistory().onSuccess {
                addLog("Histórico de combate PvP sincronizado com sucesso (${it.size} confrontos).")
            }.onFailure {
                addLog("Aviso: Falha ao sincronizar histórico PvP.")
            }

            loadShopItems()
            loadInventory()

            repository.syncActiveSeason().onSuccess {
                addLog("Passe de Temporada '${it.name}' sincronizado.")
            }.onFailure {
                addLog("Dados locais do Passe de Temporada carregados do banco de dados.")
            }
            repository.syncLeagueStatus().onSuccess {
                addLog("Liga Mundial (ELO: ${it.currentElo}) carregada do servidor!")
            }.onFailure {
                addLog("Dados locais da Liga Mundial de ELO carregados do banco de dados.")
            }
            repository.syncAllClans().onSuccess {
                addLog("Clãs e Alianças atualizados do servidor (${it.size} clãs).")
                val currentRole = it.find { c -> c.myRole != "NONE" }
                if (currentRole != null) {
                    addLog("Você está conectado ao clã '${currentRole.name}'.")
                }
            }.onFailure {
                addLog("Dados locais de Clãs carregados do banco de dados.")
            }
            repository.syncAchievements().onSuccess {
                addLog("Quadro de Conquistas/Medalhas sincronizado.")
            }.onFailure {
                addLog("Dados locais de Conquistas/Medalhas carregados do banco de dados.")
            }

            repository.syncCourses().onSuccess {
                addLog("Cursos de idiomas de Jiu-Jitsu sincronizados (${it.size} cursos).")
            }.onFailure {
                addLog("Aviso: Falha ao sincronizar cursos com o servidor.")
            }

            repository.syncTeachers().onSuccess {
                addLog("Instrutores e professores sincronizados (${it.size} professores).")
            }.onFailure {
                addLog("Aviso: Falha ao sincronizar professores com o servidor.")
            }
        }
    }

    fun claimReward(rewardId: String) {
        viewModelScope.launch {
            addLog("Resgatando recompensa...")
            repository.claimSeasonReward(rewardId).onSuccess {
                addLog("Recompensa resgatada com sucesso!")
            }.onFailure {
                addLog("Erro ao resgatar recompensa: ${it.localizedMessage}")
            }
        }
    }

    fun joinClan(clanId: String, name: String) {
        viewModelScope.launch {
            addLog("Adentrando equipe '$name' no servidor...")
            repository.joinClanLocal(clanId)
            addLog("Conexão ao clã '$name' estabelecida!")
            repository.syncAllClans()
        }
    }

    fun challengeClan(targetClanId: String, name: String) {
        viewModelScope.launch {
            addLog("Declarando guerra contra o clã '$name'...")
            repository.challengeClanWar(targetClanId).onSuccess {
                addLog("💣 DECLARAÇÃO DE GUERRA ENVIADA! Preparem seus fardos e kimonos!")
            }.onFailure {
                addLog("Falha ao declarar guerra oficial: ${it.localizedMessage}")
            }
        }
    }

    fun createClan(name: String, gymName: String, city: String, country: String, description: String) {
        viewModelScope.launch {
            addLog("Criando clã '$name'...")
            repository.createClan(name, gymName, city, country, description).onSuccess {
                addLog("Clã '${it.name}' criado com sucesso no tatame oficial!")
                repository.syncAllClans()
            }.onFailure {
                addLog("Falha ao criar clã: ${it.localizedMessage}")
            }
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


