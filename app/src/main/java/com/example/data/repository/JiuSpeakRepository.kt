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

    // Configuration keys
    val apiBaseUrl: String
        get() = prefs.getString("api_url", com.example.data.network.ApiConfig.productionBaseUrl) ?: com.example.data.network.ApiConfig.productionBaseUrl

    val isOfflineMode: Boolean
        get() = prefs.getBoolean("is_offline_mode", false) // Default false to integrate real production API immediately

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
        }
    }

    // Network & Sync logic with full offline fallbacks (to support local emulator playground)

    suspend fun login(email: String, prepopulatedPass: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                // Offline fallback - simulate success
                val profile = userDao.getProfileDirect() ?: UserProfileEntity()
                val updated = profile.copy(email = email, username = email.substringBefore("@"))
                userDao.insertProfile(updated)
                prefs.edit().putString("auth_token", "jwt_offline_mock").apply()
                return@withContext Result.success(updated)
            }

            val response = JiuSpeakApiClient.getApi()!!.login(LoginRequest(email, prepopulatedPass))
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
            Result.failure(e)
        }
    }

    suspend fun register(email: String, username: String, pass: String, belt: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            if (isOfflineMode || JiuSpeakApiClient.getApi() == null) {
                val profile = UserProfileEntity(email = email, username = username, beltColor = belt)
                userDao.insertProfile(profile)
                prefs.edit().putString("auth_token", "jwt_offline_mock").apply()
                return@withContext Result.success(profile)
            }

            val response = JiuSpeakApiClient.getApi()!!.register(RegisterRequest(email, username, pass, belt))
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
            Result.failure(e)
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

    suspend fun logout() {
        prefs.edit().remove("auth_token").apply()
        // We do not clear the profile completely, so that stats are preserved of local player
    }
}
