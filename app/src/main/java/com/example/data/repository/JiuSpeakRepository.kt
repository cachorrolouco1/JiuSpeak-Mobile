package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.JiuSpeakDatabase
import com.example.data.model.*
import com.example.data.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class JiuSpeakRepository(
    private val context: Context,
    private val database: JiuSpeakDatabase
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jiuspeak_prefs", Context.MODE_PRIVATE)

    // DAOs
    private val userDao = database.userDao()
    private val missionDao = database.missionDao()
    private val socialDao = database.socialDao()
    private val pvpDao = database.pvpDao()
    private val chatDao = database.chatDao()
    private val courseDao = database.courseDao()
    private val teacherDao = database.teacherDao()
    private val seasonDao = database.seasonDao()
    private val clanDao = database.clanDao()
    private val leagueDao = database.leagueDao()
    private val achievementDao = database.achievementDao()

    // Configuration keys
    val apiBaseUrl: String
        get() = prefs.getString("api_url", com.example.data.network.ApiConfig.productionBaseUrl) ?: com.example.data.network.ApiConfig.productionBaseUrl

    val isOfflineMode: Boolean
        get() = false

    val isLoggedIn: Boolean
        get() = prefs.getString("auth_token", null) != null

    val currentToken: String?
        get() = prefs.getString("auth_token", null)

    init {
        JiuSpeakApiClient.configure(apiBaseUrl)
    }

    suspend fun setApiUrl(url: String) {
        prefs.edit().putString("api_url", url).apply()
        JiuSpeakApiClient.configure(url)
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_offline_mode", enabled).apply()
    }

    // Reactive Flows for UI
    val userProfileFlow: Flow<UserProfileEntity?> = userDao.getProfileFlow()
    val dailyMissionsFlow: Flow<List<DailyMissionEntity>> = missionDao.getAllMissions()
    val socialPostsFlow: Flow<List<SocialPostEntity>> = socialDao.getAllPosts()
    val pvpBattlesFlow: Flow<List<PvpBattleEntity>> = pvpDao.getAllBattles()
    val chatMessagesFlow: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val coursesFlow: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val teachersFlow: Flow<List<TeacherEntity>> = teacherDao.getAllTeachers()
    val activeSeasonFlow: Flow<SeasonEntity?> = seasonDao.getActiveSeason()
    val seasonRewardsFlow: Flow<List<SeasonRewardEntity>> = seasonDao.getSeasonRewards()
    val myClanFlow: Flow<ClanEntity?> = clanDao.getMyClanFlow()
    val allClansFlow: Flow<List<ClanEntity>> = clanDao.getAllClansFlow()
    val leagueStatusFlow: Flow<LeagueEntity?> = leagueDao.getLeagueStatusFlow()
    val achievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()

    // Core Business Operations

    suspend fun seedMockDataIfEmpty() {
        withContext(Dispatchers.IO) {
            // Check if profile exists, if not seed it
            val profile = userDao.getProfileDirect()
            if (profile == null) {
                userDao.insertProfile(UserProfileEntity())
            }

            // Seed missions
            val existingMissions = missionDao.getAllMissions().firstOrNull() ?: emptyList()
            if (existingMissions.isEmpty()) {
                missionDao.insertMissions(listOf(
                    DailyMissionEntity("m1", "PRE-MATCH PREPARATION", "Watch an English lesson about Guard Passes", 150, 100, false, 0, 1),
                    DailyMissionEntity("m2", "SPARRING PRONUNCIATION", "Perform 3 perfect tap-out pronunciation exercises", 200, 150, false, 1, 3),
                    DailyMissionEntity("m3", "COMMUNITY GLADIATOR", "Create a discussion post describing your favorite escape technique", 100, 80, false, 0, 1),
                    DailyMissionEntity("m4", "ARENA WARMUP", "Participate in 1 PvP Vocabulary Arena fight", 300, 250, false, 0, 1)
                ))
            }

            // Seed Social posts
            val existingPosts = socialDao.getAllPosts().firstOrNull() ?: emptyList()
            if (existingPosts.isEmpty()) {
                val mockPosts = listOf(
                    SocialPostEntity(
                        "post_3", "Charles 'Do Bronx' Oliveira", "BLACK", 42, "avatar_charles",
                        "The training in English never stops! If you want to challenge the elite, you have to talk like the elite. Train hard. Speak globally! Let's get this weekly belt! ⚔️",
                        "https://media.istockphoto.com/id/1183188597/photo/man-ready-for-fighting.jpg",
                        234, 18, "2 hours ago", false
                    ),
                    SocialPostEntity(
                        "post_2", "John Jones Pro", "PURPLE", 21, "avatar_john",
                        "Just finished the vocabulary challenge about guard escapes. In BJJ, detail is everything; in English, it's the exact same! Focus on the details and dominate! #JiuSpeak",
                        null, 89, 5, "5 hours ago", true
                    ),
                    SocialPostEntity(
                        "post_1", "Prof. Igor Gracie", "BLACK", 51, "avatar_igor",
                        "I have been teaching English and Jiu-Jitsu in California for 5 years. I recommend all Brazilian purple belts to start studying English vocabulary immediately. Opportunities abroad come fast!",
                        null, 156, 12, "Yesterday", false
                    )
                )
                socialDao.insertPosts(mockPosts)
            }

            // Seed Pvp matches
            val existingPvp = pvpDao.getAllBattles().firstOrNull() ?: emptyList()
            if (existingPvp.isEmpty()) {
                pvpDao.insertBattles(listOf(
                    PvpBattleEntity("b3", "Ryan_The_King", "BROWN", "avatar_fighter3", "Conversation", 85, 78, "WIN"),
                    PvpBattleEntity("b2", "Gabi-Garcia-Fan", "BLUE", "avatar_fighter2", "Vocabulary", 92, 92, "DRAW"),
                    PvpBattleEntity("b1", "BJJ_Master_99", "BLACK", "avatar_fighter1", "Pronunciation", 70, 95, "LOSS")
                ))
            }

            // Seed courses
            val existingCourses = courseDao.getAllCourses().firstOrNull() ?: emptyList()
            if (existingCourses.isEmpty()) {
                courseDao.insertCourses(listOf(
                    CourseEntity("c1", "International Seminars", "Learn to teach classes & answer student questions in fluent English", "English for Competitors", 12, 5, false, 1.5f),
                    CourseEntity("c2", "MMA Press Conference", "Master interview vocabulary, call-outs, and post-fight speeches", "English for Competitors", 8, 8, true, 1.2f),
                    CourseEntity("c3", "Referee Commands", "Learn the essential rules, penalties, and ring direction terminology", "Referee English", 10, 2, false, 1.0f)
                ))
            }

            // Seed teachers
            val existingTeachers = teacherDao.getAllTeachers().firstOrNull() ?: emptyList()
            if (existingTeachers.isEmpty()) {
                teacherDao.insertTeachers(listOf(
                    TeacherEntity("t1", "Coach Michael", "BLACK", 4.9f, 250, "Black belt under Gracie Barra. Specializes in teaching BJJ terminology, academy management, and seminar delivery in English.", "USA", "English", "avatar_t1"),
                    TeacherEntity("t2", "Prof. Isabella", "BROWN", 4.8f, 200, "BJJ coach & certified ESL teacher. Expert in post-fight interview preparations and high-pressure media speech training.", "Brazil", "English / Portuguese", "avatar_t2"),
                    TeacherEntity("t3", "Dean G.", "PURPLE", 4.6f, 150, "Expert referee. Explains Referee commands, international federation standards (IBJJF), and rules translations seamlessly.", "UK", "English", "avatar_t3")
                ))
            }

            // Seed chat messages
            val existingChat = chatDao.getAllMessages().firstOrNull() ?: emptyList()
            if (existingChat.isEmpty()) {
                chatDao.insertMessage(ChatMessageEntity(0, "system", "SYSTEM", "WHITE", "Welcome to JiuSpeak Global Chat! Talk to fellow BJJ athletes studying languages. ⚔️🥋", System.currentTimeMillis() - 1000 * 60 * 10))
                chatDao.insertMessage(ChatMessageEntity(0, "u1", "MarcusBJJ", "BLUE", "E aí galera! Quem quer treinar pronúncia na Arena hoje?", System.currentTimeMillis() - 1000 * 60 * 5))
                chatDao.insertMessage(ChatMessageEntity(0, "u2", "Sensei_Arthur", "BLACK", "Estudar inglês salvou meus seminários na Europa. Mandem ver nos treinos!", System.currentTimeMillis() - 1000 * 60 * 2))
            }

            // Seed Active Season Pass
            val activeSeason = seasonDao.getActiveSeason().firstOrNull()
            if (activeSeason == null) {
                seasonDao.insertSeason(
                    SeasonEntity(
                        id = "season_v4",
                        name = "Road To Blue Belt",
                        description = "Trilha oficial para dominar o inglês e o tatame! Complete missões e vença na Arena para resgatar recompensas especiais.",
                        themeColorHex = "#009DFF",
                        bannerUrl = "https://images.unsplash.com/photo-1517649763962-0c623066013b?q=80&w=600",
                        startTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5, // started 5 days ago
                        endTimestamp = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 25, // ends in 25 days
                        maxLevel = 5,
                        currentLevel = 1,
                        currentXp = 250,
                        requiredXpNextLevel = 1000,
                        hasVipPass = true,
                        hasProPass = false
                    )
                )

                seasonDao.insertRewards(listOf(
                    SeasonRewardEntity("r1", "season_v4", 1, "JIU_TICKETS", "100 JiuTickets", false, 100, true, "ic_tickets"),
                    SeasonRewardEntity("r2", "season_v4", 2, "FRAME", "Moldura Neon Blue", false, 0, false, "frame_neon"),
                    SeasonRewardEntity("r3", "season_v4", 3, "BOOSTER", "XP Booster +50%", true, 1, false, "ic_booster"),
                    SeasonRewardEntity("r4", "season_v4", 4, "AVATAR", "Avatar Guerreiro de Bronze", false, 0, false, "avatar_fighter3"),
                    SeasonRewardEntity("r5", "season_v4", 5, "TITLE", "Título 'Gladiador de Oxford'", true, 0, false, "ic_title")
                ))
            }

            // Seed Clans (Equipes)
            val existingClans = clanDao.getAllClansFlow().firstOrNull() ?: emptyList()
            if (existingClans.isEmpty()) {
                clanDao.insertClans(listOf(
                    ClanEntity("clan_1", "Gracie Barra Headquarter", "https://logo.png", "Brazil", "Rio de Janeiro", "Gracie Barra Headquarter", "Mestre Carlos Gracie Jr", "A maior equipe de Jiu-Jitsu do mundo, unida no tatame e no inglês!", 15, 30, 48500, 1, "MEMBER"),
                    ClanEntity("clan_2", "Alliance Global", "https://logo.png", "USA", "New York", "Alliance NY", "Mestre Fabio Gurgel", "Multacampeões mundiais focados na excelência e conversação internacional.", 12, 30, 42000, 2, "NONE"),
                    ClanEntity("clan_3", "Atos Jiu-Jitsu Arena", "https://logo.png", "USA", "San Diego", "Atos HQ", "Mestre André Galvão", "Modernidade, explosão e estudo diário do vocabulário competitivo.", 8, 25, 35000, 3, "NONE")
                ))
            }

            // Seed League ELO Status
            val leagueStatus = leagueDao.getLeagueStatusFlow().firstOrNull()
            if (leagueStatus == null) {
                leagueDao.insertLeagueStatus(
                    LeagueEntity(
                        id = "league_state",
                        currentElo = 1350,
                        division = "SILVER",
                        subDivision = 2,
                        promotionGoalElo = 1500,
                        rebaixamentoThresholdElo = 1200,
                        globalRank = 422,
                        countryRank = 154,
                        winsCount = 28,
                        lossesCount = 12
                    )
                )
            }

            // Seed Achievements (Conquistas)
            val existingAchievements = achievementDao.getAllAchievementsFlow().firstOrNull() ?: emptyList()
            if (existingAchievements.isEmpty()) {
                achievementDao.insertAchievements(listOf(
                    AchievementEntity("ach1", "Primeira Vitória", "Vença seu primeiro sparring PvP na Arena de Vocabulário", "PVP", "COMMON", 1, 1, true, 50, 10, "ic_medal_1", System.currentTimeMillis() - 1000 * 60 * 60 * 12),
                    AchievementEntity("ach2", "Guerreiro Consistente", "Complete missões diárias por 7 dias consecutivos", "STUDY", "RARE", 4, 7, false, 250, 50, "ic_medal_2", 0L),
                    AchievementEntity("ach3", "Dominador da Fala", "Complete 10 lições de conversação com pronúncia excelente", "STUDY", "EPIC", 8, 10, false, 500, 100, "ic_medal_3", 0L),
                    AchievementEntity("ach4", "Campeão de Oxford", "Alcance a Divisão Ouro na Liga Mundial de ELO", "PVP", "LEGENDARY", 1350, 2000, false, 1000, 250, "ic_medal_4", 0L),
                    AchievementEntity("ach5", "Mentor Favorito", "Contrate 5 mentorias premium no marketplace", "MARKETPLACE", "MYTHIC", 2, 5, false, 1500, 500, "ic_medal_5", 0L)
                ))
            }
        }
    }

    // Network & Sync logic with full offline fallbacks (to support local emulator playground)

    suspend fun loginOffline() = withContext(Dispatchers.IO) {
        prefs.edit().putString("auth_token", "demo_token").apply()
        val profile = userDao.getProfileDirect()
        if (profile == null) {
            userDao.insertProfile(UserProfileEntity(
                email = "offline-champion@jiuspeak.com",
                username = "ChampionOffline",
                beltColor = "BLUE",
                level = 1,
                xp = 120,
                xpNextLevel = 1000,
                dailyStreak = 3,
                jiuTickets = 1500,
                activeToken = "demo_token"
            ))
        }
    }

    suspend fun login(email: String, prepopulatedPass: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val response = api.login(LoginRequest(email, prepopulatedPass))
            prefs.edit().putString("auth_token", response.token).apply()

            val entity = UserProfileEntity(
                email = response.user.email,
                username = response.user.username,
                level = response.user.level,
                xp = response.user.xp,
                xpNextLevel = response.user.xpNextLevel,
                dailyStreak = response.user.dailyStreak,
                jiuTickets = response.user.jiuTickets,
                beltColor = response.user.beltColor,
                activeToken = response.token
            )
            userDao.insertProfile(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(mapHttpException(e))
        }
    }

    suspend fun register(email: String, username: String, pass: String, belt: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val response = api.register(RegisterRequest(email, username, pass, belt))
            prefs.edit().putString("auth_token", response.token).apply()

            val entity = UserProfileEntity(
                email = response.user.email,
                username = response.user.username,
                beltColor = response.user.beltColor,
                level = response.user.level,
                xp = response.user.xp,
                xpNextLevel = response.user.xpNextLevel,
                dailyStreak = response.user.dailyStreak,
                jiuTickets = response.user.jiuTickets,
                activeToken = response.token
            )
            userDao.insertProfile(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(mapHttpException(e))
        }
    }

    suspend fun fetchRemoteProfile(): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                return@withContext Result.success(userDao.getProfileDirect() ?: UserProfileEntity())
            }

            val userDto = JiuSpeakApiClient.getApi()!!.getProfile("Bearer $token")
            val entity = UserProfileEntity(
                email = userDto.email,
                username = userDto.username,
                beltColor = userDto.beltColor,
                level = userDto.level,
                xp = userDto.xp,
                xpNextLevel = userDto.xpNextLevel,
                dailyStreak = userDto.dailyStreak,
                jiuTickets = userDto.jiuTickets,
                activeToken = token
            )
            userDao.insertProfile(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncRemoteMissions(): Result<List<DailyMissionEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                return@withContext Result.success(emptyList())
            }

            val dtos = JiuSpeakApiClient.getApi()!!.getMissions("Bearer $token")
            val entities = dtos.map {
                DailyMissionEntity(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    jiuTicketsReward = it.ticketsReward,
                    xpReward = it.xpReward,
                    isCompleted = it.isCompleted,
                    progress = it.progress,
                    maxProgress = it.maxProgress
                )
            }
            missionDao.insertMissions(entities)
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeMissionLocal(id: String) = withContext(Dispatchers.IO) {
        val missions = missionDao.getAllMissions().firstOrNull() ?: return@withContext
        val target = missions.find { it.id == id } ?: return@withContext
        if (!target.isCompleted) {
            val completed = target.copy(progress = target.maxProgress, isCompleted = true)
            missionDao.updateMission(completed)

            // Reward tickets & XP
            val profile = userDao.getProfileDirect()
            if (profile != null) {
                var newXp = profile.xp + target.xpReward
                var newLevel = profile.level
                if (newXp >= profile.xpNextLevel) {
                    newXp -= profile.xpNextLevel
                    newLevel += 1
                }
                userDao.insertProfile(profile.copy(
                    xp = newXp,
                    level = newLevel,
                    jiuTickets = profile.jiuTickets + target.jiuTicketsReward
                ))
            }
        }
    }

    suspend fun addSocialPost(content: String, imageUrl: String?) = withContext(Dispatchers.IO) {
        val profile = userDao.getProfileDirect() ?: UserProfileEntity()
        val customPost = SocialPostEntity(
            id = "sys_post_local_${System.currentTimeMillis()}",
            authorName = profile.fullName,
            authorBelt = profile.beltColor,
            authorLevel = profile.level,
            authorAvatar = profile.selectedAvatar,
            content = content,
            imageUrl = imageUrl,
            likesCount = 0,
            commentsCount = 0,
            timeAgo = "Just now",
            isLikedByMe = false
        )
        socialDao.insertPost(customPost)

        // Sync to remote if we can
        val token = currentToken
        if (!isOfflineMode && token != null && JiuSpeakApiClient.getApi() != null) {
            try {
                JiuSpeakApiClient.getApi()!!.createPost("Bearer $token", CreatePostRequest(content, imageUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun postLikeToggle(postId: String) = withContext(Dispatchers.IO) {
        val posts = socialDao.getAllPosts().firstOrNull() ?: return@withContext
        val target = posts.find { it.id == postId } ?: return@withContext
        val nowLiked = !target.isLikedByMe
        val newCount = if (nowLiked) target.likesCount + 1 else Math.max(0, target.likesCount - 1)
        socialDao.updatePostLike(postId, newCount, nowLiked)

        // Sync to remote
        val token = currentToken
        if (!isOfflineMode && token != null && JiuSpeakApiClient.getApi() != null) {
            try {
                JiuSpeakApiClient.getApi()!!.likePost("Bearer $token", postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun recordLocalBattle(matchType: String, scoreMe: Int, scoreOpponent: Int, outcome: String, opponentName: String) = withContext(Dispatchers.IO) {
        val id = "b_${System.currentTimeMillis()}"
        val sampleAvatars = listOf("avatar_fighter1", "avatar_fighter2", "avatar_fighter3", "avatar_charles")
        val sampleBelts = listOf("WHITE", "BLUE", "PURPLE", "BROWN", "BLACK")

        val pvpBattle = PvpBattleEntity(
            id = id,
            opponentName = opponentName,
            opponentBelt = sampleBelts.random(),
            opponentAvatar = sampleAvatars.random(),
            matchType = matchType,
            scoreMe = scoreMe,
            scoreOpponent = scoreOpponent,
            outcome = outcome
        )
        pvpDao.insertBattle(pvpBattle)

        // Gain/Loss dynamic tickets/experience from the match outcomes
        val profile = userDao.getProfileDirect()
        if (profile != null) {
            val xpEarned = if (outcome == "WIN") 150 else if (outcome == "DRAW") 80 else 40
            val ticketsEarned = if (outcome == "WIN") 50 else if (outcome == "DRAW") 25 else 10

            var newXp = profile.xp + xpEarned
            var newLvl = profile.level
            if (newXp >= profile.xpNextLevel) {
                newXp -= profile.xpNextLevel
                newLvl += 1
            }

            userDao.insertProfile(profile.copy(
                xp = newXp,
                level = newLvl,
                jiuTickets = profile.jiuTickets + ticketsEarned
            ))
        }

        // Remote sync call
        val token = currentToken
        if (!isOfflineMode && token != null && JiuSpeakApiClient.getApi() != null) {
            try {
                JiuSpeakApiClient.getApi()!!.recordPvpMatch(
                    "Bearer $token",
                    PvpMatchResultDto(opponentName, pvpBattle.opponentBelt, pvpBattle.opponentAvatar, matchType, scoreMe, scoreOpponent, outcome)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun writeChatMessage(messageText: String) = withContext(Dispatchers.IO) {
        val profile = userDao.getProfileDirect() ?: UserProfileEntity()
        val newMsg = ChatMessageEntity(
            senderId = profile.username,
            senderName = profile.username,
            senderBelt = profile.beltColor,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(newMsg)
    }

    suspend fun purchaseShopItem(costs: Int) = withContext(Dispatchers.IO) {
        val profile = userDao.getProfileDirect() ?: return@withContext
        if (profile.jiuTickets >= costs) {
            userDao.insertProfile(profile.copy(jiuTickets = profile.jiuTickets - costs))
        }
    }

    suspend fun updateAvatar(avatar: String, frameColor: String) = withContext(Dispatchers.IO) {
        val profile = userDao.getProfileDirect() ?: return@withContext
        userDao.insertProfile(profile.copy(selectedAvatar = avatar, selectedFrameColor = frameColor))
    }

    suspend fun syncActiveSeason(): Result<SeasonEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                val current = seasonDao.getActiveSeason().firstOrNull() ?: throw Exception("Offline mode")
                return@withContext Result.success(current)
            }
            val dto = JiuSpeakApiClient.getApi()!!.getActiveSeason("Bearer $token")
            val seasonEntity = SeasonEntity(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                themeColorHex = dto.themeColorHex,
                bannerUrl = dto.bannerUrl,
                startTimestamp = dto.startTimestamp,
                endTimestamp = dto.endTimestamp,
                maxLevel = dto.maxLevel,
                currentLevel = dto.currentLevel,
                currentXp = dto.currentXp,
                requiredXpNextLevel = dto.requiredXpNextLevel,
                hasVipPass = dto.hasVipPass,
                hasProPass = dto.hasProPass
            )
            seasonDao.insertSeason(seasonEntity)
            
            val rewardEntities = dto.rewards.map {
                SeasonRewardEntity(
                    id = it.id,
                    seasonId = it.seasonId,
                    level = it.level,
                    type = it.type,
                    name = it.name,
                    isPremium = it.isPremium,
                    rewardsValue = it.rewardsValue,
                    isClaimed = it.isClaimed,
                    imageUrl = it.imageUrl
                )
            }
            seasonDao.insertRewards(rewardEntities)
            Result.success(seasonEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun claimSeasonReward(rewardId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Update local Room database immediately (Offline-first)
            seasonDao.claimReward(rewardId)
            
            val token = currentToken
            if (!isOfflineMode && token != null && JiuSpeakApiClient.getApi() != null) {
                try {
                    val profileDto = JiuSpeakApiClient.getApi()!!.claimReward("Bearer $token", ClaimRewardRequest(rewardId))
                    // Update user profile with rewards updated
                    val entity = UserProfileEntity(
                        email = profileDto.email,
                        username = profileDto.username,
                        beltColor = profileDto.beltColor,
                        level = profileDto.level,
                        xp = profileDto.xp,
                        xpNextLevel = profileDto.xpNextLevel,
                        dailyStreak = profileDto.dailyStreak,
                        jiuTickets = profileDto.jiuTickets,
                        activeToken = token
                    )
                    userDao.insertProfile(entity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncLeagueStatus(): Result<LeagueEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                val current = leagueDao.getLeagueStatusFlow().firstOrNull() ?: throw Exception("Offline mode")
                return@withContext Result.success(current)
            }
            val dto = JiuSpeakApiClient.getApi()!!.getLeagueStatus("Bearer $token")
            val entity = LeagueEntity(
                id = dto.id,
                currentElo = dto.currentElo,
                division = dto.division,
                subDivision = dto.subDivision,
                promotionGoalElo = dto.promotionGoalElo,
                rebaixamentoThresholdElo = dto.rebaixamentoThresholdElo,
                globalRank = dto.globalRank,
                countryRank = dto.countryRank,
                winsCount = dto.winsCount,
                lossesCount = dto.lossesCount
            )
            leagueDao.insertLeagueStatus(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllClans(): Result<List<ClanEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                return@withContext Result.success(clanDao.getAllClansFlow().firstOrNull() ?: emptyList())
            }
            val dtos = JiuSpeakApiClient.getApi()!!.getAllClans("Bearer $token")
            val entities = dtos.map {
                ClanEntity(
                    id = it.id,
                    name = it.name,
                    logoUrl = it.logoUrl,
                    country = it.country,
                    city = it.city,
                    gymName = it.gymName,
                    masterName = it.masterName,
                    description = it.description,
                    memberCount = it.memberCount,
                    maxMembers = it.maxMembers,
                    totalXp = it.totalXp,
                    rankPosition = it.rankPosition,
                    myRole = it.myRole
                )
            }
            clanDao.insertClans(entities)
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createClan(name: String, gymName: String, city: String, country: String, description: String): Result<ClanEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken
            if (isOfflineMode || token == null || JiuSpeakApiClient.getApi() == null) {
                // Offline fallback creation
                val newClan = ClanEntity(
                    id = "clan_local_${System.currentTimeMillis()}",
                    name = name,
                    logoUrl = "https://logo.png",
                    country = country,
                    city = city,
                    gymName = gymName,
                    masterName = userDao.getProfileDirect()?.username ?: "Me",
                    description = description,
                    memberCount = 1,
                    maxMembers = 30,
                    totalXp = 100,
                    rankPosition = 10,
                    myRole = "LEADER"
                )
                clanDao.leaveCurrentClan()
                clanDao.insertClan(newClan)
                return@withContext Result.success(newClan)
            }
            val dto = JiuSpeakApiClient.getApi()!!.createClan(
                "Bearer $token",
                CreateClanRequest(name, gymName, city, country, description)
            )
            val clan = ClanEntity(
                id = dto.id,
                name = dto.name,
                logoUrl = dto.logoUrl,
                country = dto.country,
                city = dto.city,
                gymName = dto.gymName,
                masterName = dto.masterName,
                description = dto.description,
                memberCount = dto.memberCount,
                maxMembers = dto.maxMembers,
                totalXp = dto.totalXp,
                rankPosition = dto.rankPosition,
                myRole = dto.myRole
            )
            clanDao.leaveCurrentClan()
            clanDao.insertClan(clan)
            Result.success(clan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinClanLocal(clanId: String) = withContext(Dispatchers.IO) {
        clanDao.leaveCurrentClan()
        clanDao.updateMyRole(clanId, "MEMBER")
    }

    suspend fun challengeClanWar(targetClanId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken
            if (!isOfflineMode && token != null && JiuSpeakApiClient.getApi() != null) {
                val response = JiuSpeakApiClient.getApi()!!.challengeClanWar("Bearer $token", ClanWarChallengeRequest(targetClanId))
                if (response.isSuccessful) {
                    return@withContext Result.success(true)
                } else {
                    return@withContext Result.failure(Exception("War declaration API error"))
                }
            }
            Result.success(true) // offline success
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAchievements(): Result<List<AchievementEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                return@withContext Result.success(achievementDao.getAllAchievementsFlow().firstOrNull() ?: emptyList())
            }
            val dtos = JiuSpeakApiClient.getApi()!!.getAchievements("Bearer $token")
            val entities = dtos.map {
                AchievementEntity(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    rarity = it.rarity,
                    progress = it.progress,
                    targetProgress = it.targetProgress,
                    isCompleted = it.isCompleted,
                    xpReward = it.xpReward,
                    jiuTicketsReward = it.jiuTicketsReward,
                    iconUrl = it.iconUrl,
                    unlockedAtTimestamp = it.unlockedAtTimestamp ?: 0L
                )
            }
            achievementDao.insertAchievements(entities)
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        prefs.edit().remove("auth_token").apply()
        try {
            database.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapHttpException(e: Exception): Exception {
        if (e is retrofit2.HttpException) {
            val code = e.code()
            var errorBodyStr = ""
            try {
                errorBodyStr = e.response()?.errorBody()?.string() ?: ""
            } catch (ignored: Exception) {}
            val message = if (errorBodyStr.isNotBlank()) {
                "Erro do Servidor ($code): $errorBodyStr"
            } else {
                "Erro de Conexão ($code): ${e.message()}"
            }
            return Exception(message)
        }
        return e
    }
}
