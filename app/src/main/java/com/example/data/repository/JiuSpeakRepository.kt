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

    val isLoggedIn: Boolean
        get() = prefs.getString("auth_token", null) != null

    val currentToken: String?
        get() = prefs.getString("auth_token", null)

    init {
        JiuSpeakApiClient.configure(apiBaseUrl, context)
    }

    suspend fun setApiUrl(url: String) {
        prefs.edit().putString("api_url", url).apply()
        JiuSpeakApiClient.configure(url, context)
    }

    // Reactive Flows for UI
    val userProfileFlow: Flow<UserProfileEntity?> = userDao.getProfileFlow()

    suspend fun getProfileDirect(): UserProfileEntity? {
        return userDao.getProfileDirect()
    }
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

    // Network & Sync logic with full offline fallbacks (to support local emulator playground)

    companion object {
        fun mapDtoToEntity(dto: com.example.data.network.UserProfileDto, token: String): UserProfileEntity {
            val rawBelt = dto.beltColor?.uppercase()?.trim() ?: "WHITE"
            val finalBelt = if (rawBelt.isEmpty()) "WHITE" else rawBelt
            
            val mappedId = dto.id ?: ""
            val mappedEmail = dto.email ?: ""
            
            println("=== MAP DTO TO ENTITY ===")
            println("Mapped ID: $mappedId")
            println("Mapped Email: $mappedEmail")
            
            return UserProfileEntity(
                id = mappedId,
                email = mappedEmail,
                username = dto.username ?: "",
                fullName = dto.username ?: "",
                beltColor = finalBelt,
                level = dto.level ?: 1,
                xp = dto.xp ?: 0,
                xpNextLevel = dto.xpNextLevel ?: 1000,
                dailyStreak = dto.dailyStreak ?: 0,
                jiuTickets = dto.jiuTickets ?: 0,
                selectedAvatar = dto.avatar ?: "avatar_fighter",
                selectedFrameColor = dto.frameColor ?: "#009DFF",
                activeToken = token,
                bio = dto.bio ?: "",
                city = dto.city ?: "",
                country = dto.country ?: "",
                nativeLanguage = dto.nativeLanguage ?: "Portuguese",
                studiedLanguages = dto.studiedLanguages ?: "English",
                learningGoals = dto.learningGoals ?: "",
                instagram = dto.instagram ?: "",
                youtube = dto.youtube ?: "",
                facebook = dto.facebook ?: "",
                website = dto.website ?: "",
                coverPhoto = dto.coverPhoto ?: "",
                isVerified = dto.isVerified ?: false,
                followersCount = dto.followersCount ?: 0,
                followingCount = dto.followingCount ?: 0
            )
        }
    }

    fun getAuthUserId(): String? {
        return prefs.getString("auth_user_id", null)
    }

    fun getAuthUserEmail(): String? {
        return prefs.getString("auth_user_email", null)
    }

    suspend fun login(email: String, prepopulatedPass: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            // Limpeza completa de sessão anterior antes de novo login
            println("=== PURGING PRIOR SESSION AND LOGGING OUT BEFORE NEW LOGIN ===")
            logout()
            
            JiuSpeakApiClient.ensureCsrf()
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val response = api.login(LoginRequest(email, prepopulatedPass))
            
            // LOGS CORRESPONDENTES EXIGIDOS
            println("=== LOGIN RESPONSE RECEIVED ===")
            println("Token: ${response.token}")
            println("User DTO: ${response.user}")
            
            val fetchedUser = response.user ?: throw Exception("Login response body has no user profile")
            val fetchedToken = response.token ?: ""
            
            val authUserId = fetchedUser.id ?: ""
            val authUserEmail = fetchedUser.email ?: ""
            val authUsername = fetchedUser.username ?: ""
            
            println("=== CURRENT USER ID: $authUserId ===")
            println("=== CURRENT USER EMAIL: $authUserEmail ===")
            println("=== CURRENT USERNAME: $authUsername ===")
            
            prefs.edit()
                .putString("auth_token", fetchedToken)
                .putString("auth_user_id", authUserId)
                .putString("auth_user_email", authUserEmail)
                .putString("auth_username", authUsername)
                .apply()

            // database.clearAllTables() antes de persistir novo usuário
            println("=== TRUNCATING LOCAL DATABASE TABLES ===")
            database.clearAllTables()

            val entity = mapDtoToEntity(fetchedUser, fetchedToken)
            println("=== ROOM INSERT USER PROFILE ===")
            println("Entity: $entity")
            
            userDao.insertProfile(entity)
            
            // ROOM SELECT LOG
            val selectProfile = userDao.getProfileDirect()
            println("=== ROOM SELECT USER PROFILE ===")
            println("Selected: $selectProfile")
            
            Result.success(entity)
        } catch (e: Exception) {
            println("=== LOGIN RESPONSE ERROR ===")
            e.printStackTrace()
            Result.failure(mapHttpException(e))
        }
    }

    suspend fun register(email: String, username: String, pass: String, belt: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            println("=== PURGING PRIOR SESSION AND LOGGING OUT BEFORE NEW REGISTER ===")
            logout()
            
            JiuSpeakApiClient.ensureCsrf()
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val response = api.register(RegisterRequest(email, username, pass, belt))
            
            println("=== REGISTER RESPONSE RECEIVED ===")
            println("Token: ${response.token}")
            println("User DTO: ${response.user}")
            
            val fetchedUser = response.user ?: throw Exception("Register response body has no user profile")
            val fetchedToken = response.token ?: ""
            
            val authUserId = fetchedUser.id ?: ""
            val authUserEmail = fetchedUser.email ?: ""
            val authUsername = fetchedUser.username ?: ""
            
            println("=== CURRENT USER ID: $authUserId ===")
            println("=== CURRENT USER EMAIL: $authUserEmail ===")
            println("=== CURRENT USERNAME: $authUsername ===")
            
            prefs.edit()
                .putString("auth_token", fetchedToken)
                .putString("auth_user_id", authUserId)
                .putString("auth_user_email", authUserEmail)
                .putString("auth_username", authUsername)
                .apply()

            println("=== TRUNCATING LOCAL DATABASE TABLES ===")
            database.clearAllTables()

            val entity = mapDtoToEntity(fetchedUser, fetchedToken)
            println("=== ROOM INSERT USER PROFILE ===")
            println("Entity: $entity")
            
            userDao.insertProfile(entity)
            
            val selectProfile = userDao.getProfileDirect()
            println("=== ROOM SELECT USER PROFILE ===")
            println("Selected: $selectProfile")
            
            Result.success(entity)
        } catch (e: Exception) {
            println("=== REGISTER RESPONSE ERROR ===")
            e.printStackTrace()
            Result.failure(mapHttpException(e))
        }
    }

    suspend fun fetchRemoteProfile(): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")

            // 1. Fetch Remote Profile
            val userDto = api.getProfile("Bearer $token")
            val profileId = userDto.id ?: ""
            val profileEmail = userDto.email ?: ""

            // 2. Fetch Remote Wallet
            val walletDto = try {
                api.getWallet("Bearer $token")
            } catch (e: Exception) {
                println("=== WALLET SYNC ERROR ===")
                throw Exception("Failed to sync wallet balance with backend: ${e.message}")
            }
            
            val finalTickets = walletDto.jiuTickets

            // 3. Authenticate sync
            val authUserId = prefs.getString("auth_user_id", "") ?: ""
            val authUserEmail = prefs.getString("auth_user_email", "") ?: ""
            val authUsername = prefs.getString("auth_username", "") ?: ""
            val profileUsername = userDto.username ?: ""

            // Validate Identity Discrepancies
            if ((authUserId.isNotEmpty() && authUserId != profileId) || (authUserEmail.isNotEmpty() && authUserEmail != profileEmail)) {
                println("=== CRITICAL IDENTITY DISCREPANCY DETECTED ===")
                println("Purging entire local state, cache and preferences...")
                logout()
                try {
                    context.cacheDir.deleteRecursively()
                } catch (ignored: Exception) {}
                throw Exception("Identity discrepancy detected! Local cache, database, and JWT have been cleared. Please login again.")
            }

            prefs.edit()
                .putString("auth_user_id", profileId)
                .putString("auth_user_email", profileEmail)
                .putString("auth_username", profileUsername)
                .apply()

            val mappedEntity = mapDtoToEntity(userDto, token)
            val entity = mappedEntity.copy(jiuTickets = finalTickets)
            
            userDao.insertProfile(entity)
            val selectProfile = userDao.getProfileDirect()
            
            val roomId = selectProfile?.id ?: ""
            val roomEmail = selectProfile?.email ?: ""

            // Standard logs with specified labels
            println("=== AUTHENTICATION IDENTITY AUDIT ===")
            println("AUTH USER ID: $authUserId")
            println("PROFILE USER ID: $profileId")
            println("AUTH EMAIL: $authUserEmail")
            println("PROFILE EMAIL: $profileEmail")
            println("AUTH USERNAME: $authUsername")
            println("PROFILE USERNAME: $profileUsername")
            println("ROOM USER ID: $roomId")
            println("ROOM EMAIL: $roomEmail")
            println("=====================================")

            Result.success(entity)
        } catch (e: Exception) {
            println("=== PROFILE SYNC RESPONSE ERROR ===")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun syncRemoteMissions(): Result<List<DailyMissionEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            if (JiuSpeakApiClient.getApi() == null) {
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
        val token = currentToken ?: return@withContext
        val api = JiuSpeakApiClient.getApi() ?: return@withContext
        try {
            val updatedUserDto = api.completeMission("Bearer $token", id)
            
            // Fetch fresh wallet balance
            val walletDto = try {
                api.getWallet("Bearer $token")
            } catch (e: Exception) {
                null
            }
            
            val finalTickets = walletDto?.jiuTickets 
                ?: walletDto?.balance 
                ?: updatedUserDto.jiuTickets 
                ?: 0
                
            val initialEntity = mapDtoToEntity(updatedUserDto, token)
            val entity = initialEntity.copy(jiuTickets = finalTickets)
            
            userDao.insertProfile(entity)
            
            // Refresh list of missions after completion
            syncRemoteMissions()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun addSocialPost(content: String, imageUrl: String?) = withContext(Dispatchers.IO) {
        val profile = userDao.getProfileDirect() ?: return@withContext
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
        if (token != null && JiuSpeakApiClient.getApi() != null) {
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
        if (token != null && JiuSpeakApiClient.getApi() != null) {
            try {
                JiuSpeakApiClient.getApi()!!.likePost("Bearer $token", postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun recordLocalBattle(matchType: String, scoreMe: Int, scoreOpponent: Int, outcome: String, opponentName: String) = withContext(Dispatchers.IO) {
        val id = "b_${System.currentTimeMillis()}"

        val pvpBattle = PvpBattleEntity(
            id = id,
            opponentName = opponentName,
            opponentBelt = "WHITE",
            opponentAvatar = "avatar_fighter",
            matchType = matchType,
            scoreMe = scoreMe,
            scoreOpponent = scoreOpponent,
            outcome = outcome
        )
        pvpDao.insertBattle(pvpBattle)

        // Remote sync call
        val token = currentToken
        if (token != null && JiuSpeakApiClient.getApi() != null) {
            try {
                val updatedProfileDto = JiuSpeakApiClient.getApi()!!.recordPvpMatch(
                    "Bearer $token",
                    PvpMatchResultDto(opponentName, pvpBattle.opponentBelt, pvpBattle.opponentAvatar, matchType, scoreMe, scoreOpponent, outcome)
                )
                
                // Let's also fetch fresh wallet to maintain perfect sync
                val walletDto = try {
                    JiuSpeakApiClient.getApi()!!.getWallet("Bearer $token")
                } catch (e: Exception) {
                    null
                }
                
                val finalTickets = walletDto?.jiuTickets 
                    ?: walletDto?.balance 
                    ?: updatedProfileDto.jiuTickets 
                    ?: 0
                    
                val initialEntity = mapDtoToEntity(updatedProfileDto, token)
                val entity = initialEntity.copy(jiuTickets = finalTickets)
                
                userDao.insertProfile(entity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun writeChatMessage(messageText: String) = withContext(Dispatchers.IO) {
        val profile = userDao.getProfileDirect() ?: return@withContext
        val newMsg = ChatMessageEntity(
            senderId = profile.username,
            senderName = profile.username,
            senderBelt = profile.beltColor,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(newMsg)
    }

    suspend fun fetchShopItems(): Result<List<ShopItemDto>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val items = api.getShopItems("Bearer $token")
            Result.success(items)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun purchaseShopItemRemote(itemId: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            
            val updatedUserDto = api.purchaseShopItem("Bearer $token", PurchaseRequest(itemId))
            
            val walletDto = try {
                api.getWallet("Bearer $token")
            } catch (e: Exception) {
                null
            }
            
            val finalTickets = walletDto?.jiuTickets 
                ?: walletDto?.balance 
                ?: updatedUserDto.jiuTickets 
                ?: 0
                
            val initialEntity = mapDtoToEntity(updatedUserDto, token)
            val entity = initialEntity.copy(jiuTickets = finalTickets)
            
            userDao.insertProfile(entity)
            Result.success(entity)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun fetchInventory(): Result<List<InventoryItemDto>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val items = api.getInventory("Bearer $token")
            Result.success(items)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun equipItemRemote(itemId: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            
            val updatedUserDto = api.equipItem("Bearer $token", EquipRequest(itemId))
            
            val walletDto = try {
                api.getWallet("Bearer $token")
            } catch (e: Exception) {
                null
            }
            
            val finalTickets = walletDto?.jiuTickets 
                ?: walletDto?.balance 
                ?: updatedUserDto.jiuTickets 
                ?: 0
                
            val initialEntity = mapDtoToEntity(updatedUserDto, token)
            val entity = initialEntity.copy(jiuTickets = finalTickets)
            
            userDao.insertProfile(entity)
            Result.success(entity)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun syncRemotePvpHistory(): Result<List<PvpBattleEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            
            val remoteMatches = api.getPvpMatches("Bearer $token")
            val entities = remoteMatches.map { dto ->
                PvpBattleEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    opponentName = dto.opponentName ?: "Elite Atleta",
                    opponentBelt = dto.opponentBelt ?: "WHITE",
                    opponentAvatar = dto.opponentAvatar ?: "avatar_fighter",
                    matchType = dto.matchType ?: "Vocabulary",
                    scoreMe = dto.scoreMe ?: 0,
                    scoreOpponent = dto.scoreOpponent ?: 0,
                    outcome = dto.outcome ?: "DRAW",
                    timestamp = System.currentTimeMillis()
                )
            }
            if (entities.isNotEmpty()) {
                pvpDao.insertBattles(entities)
            }
            Result.success(entities)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dto = api.getActiveSeason("Bearer $token")
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
            if (token != null && JiuSpeakApiClient.getApi() != null) {
                try {
                    val profileDto = JiuSpeakApiClient.getApi()!!.claimReward("Bearer $token", ClaimRewardRequest(rewardId))
                    
                    println("=== CLAIM REWARD PROFILE RESPONSE ===")
                    println("User DTO: $profileDto")
                    
                    val entity = mapDtoToEntity(profileDto, token)
                    println("=== ROOM INSERT USER PROFILE (REWARD UPDATE) ===")
                    println("Entity: $entity")
                    
                    userDao.insertProfile(entity)
                } catch (e: Exception) {
                    println("=== CLAIM REWARD ERROR ===")
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
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dto = api.getLeagueStatus("Bearer $token")
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
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dtos = api.getAllClans("Bearer $token")
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
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in. Conecte-se para criar equipe."))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dto = api.createClan(
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
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in. Conecte-se para declarar guerra."))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val response = api.challengeClanWar("Bearer $token", ClanWarChallengeRequest(targetClanId))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Guerra não pôde ser declarada pelo servidor."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAchievements(): Result<List<AchievementEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dtos = api.getAchievements("Bearer $token")
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
        println("=== FULL SESSION PURGE: CLEARING PREFERENCES, ROOM DB, AND CACHE DIR ===")
        prefs.edit().clear().apply()
        try {
            database.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            context.cacheDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        com.example.data.network.JiuSpeakApiClient.clearSession()
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

    suspend fun syncCourses(): Result<List<com.example.data.model.CourseEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dtos = api.getCourses("Bearer $token")
            val entities = dtos.map { dto ->
                com.example.data.model.CourseEntity(
                    id = dto.id,
                    title = dto.title,
                    subtitle = dto.subtitle,
                    category = dto.category,
                    totalLessons = dto.totalLessons,
                    completedLessons = dto.completedLessons,
                    isCertified = dto.isCertified,
                    xpMultiplier = dto.xpMultiplier
                )
            }
            courseDao.insertCourses(entities)
            Result.success(entities)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun syncTeachers(): Result<List<TeacherEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = currentToken ?: return@withContext Result.failure(Exception("Not logged in"))
            val api = JiuSpeakApiClient.getApi() ?: throw Exception("API client not initialized")
            val dtos = api.getTeachers("Bearer $token")
            val entities = dtos.map { dto ->
                TeacherEntity(
                    id = dto.id,
                    name = dto.name,
                    belt = dto.belt,
                    rating = dto.rating,
                    hourlyRate = dto.hourlyRate,
                    bio = dto.bio,
                    country = dto.country,
                    imageUrl = dto.imageUrl
                )
            }
            teacherDao.insertTeachers(entities)
            Result.success(entities)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
