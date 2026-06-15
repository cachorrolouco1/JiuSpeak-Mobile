package com.example.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// API Payload Models
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val username: String, val password: String, val beltColor: String)
data class AuthResponse(val token: String, val refreshToken: String, val user: UserProfileDto)
data class UserProfileDto(
    val id: String,
    val username: String,
    val email: String,
    val beltColor: String,
    val level: Int,
    val xp: Int,
    val xpNextLevel: Int,
    val dailyStreak: Int,
    val jiuTickets: Int,
    val avatar: String
)

data class MissionDto(val id: String, val title: String, val description: String, val ticketsReward: Int, val xpReward: Int, val isCompleted: Boolean, val progress: Int, val maxProgress: Int)
data class PostDto(val id: String, val authorName: String, val authorBelt: String, val authorLevel: Int, val content: String, val imageUrl: String?, val likesCount: Int, val commentsCount: Int, val timestamp: Long)
data class CreatePostRequest(val content: String, val imageUrl: String?)
data class PvpMatchResultDto(val opponentName: String, val opponentBelt: String, val opponentAvatar: String, val matchType: String, val scoreMe: Int, val scoreOpponent: Int, val outcome: String)
data class CourseDto(val id: String, val title: String, val subtitle: String, val category: String, val totalLessons: Int, val completedLessons: Int, val isCertified: Boolean, val xpMultiplier: Float)
data class TeacherDto(val id: String, val name: String, val belt: String, val rating: Float, val hourlyRate: Int, val bio: String, val country: String, val imageUrl: String)
data class TokenRefreshRequest(val refreshToken: String)
data class TokenRefreshResponse(val token: String, val refreshToken: String)

data class LeaderboardDto(
    val userId: String,
    val username: String,
    val avatar: String,
    val belt: String,
    val xp: Int,
    val wins: Int,
    val losses: Int,
    val rankPosition: Int
)

data class WalletTransactionDto(
    val id: String,
    val amount: Int,
    val type: String, // "CREDIT", "DEBIT"
    val description: String,
    val date: String
)

data class WalletDto(
    val balance: Int,
    val jiuTickets: Int,
    val transactions: List<WalletTransactionDto>
)

data class SubscriptionPlanDto(
    val id: String,
    val name: String,
    val price: Double,
    val benefits: List<String>
)

data class UserSubscriptionStateDto(
    val planName: String,
    val price: Double,
    val benefits: List<String>,
    val status: String,
    val renewalDate: String,
    val expirationDate: String
)

data class SeasonRewardDto(
    val id: String,
    val seasonId: String,
    val level: Int,
    val type: String, // "AVATAR", "FRAME", "MEDAL", "JIU_TICKETS", "BOOSTER", "TITLE"
    val name: String,
    val isPremium: Boolean,
    val rewardsValue: Int,
    val isClaimed: Boolean,
    val imageUrl: String
)

data class SeasonDto(
    val id: String,
    val name: String,
    val description: String,
    val themeColorHex: String,
    val bannerUrl: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val maxLevel: Int,
    val currentLevel: Int,
    val currentXp: Int,
    val requiredXpNextLevel: Int,
    val hasVipPass: Boolean,
    val hasProPass: Boolean,
    val rewards: List<SeasonRewardDto>
)

data class ClaimRewardRequest(val rewardId: String)

data class LeagueStatusDto(
    val id: String,
    val currentElo: Int,
    val division: String, // "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "LEGEND"
    val subDivision: Int,
    val promotionGoalElo: Int,
    val rebaixamentoThresholdElo: Int,
    val globalRank: Int,
    val countryRank: Int,
    val winsCount: Int,
    val lossesCount: Int
)

data class ClanDto(
    val id: String,
    val name: String,
    val logoUrl: String,
    val country: String,
    val city: String,
    val gymName: String,
    val masterName: String,
    val description: String,
    val memberCount: Int,
    val maxMembers: Int,
    val totalXp: Int,
    val rankPosition: Int,
    val myRole: String // "LEADER", "CO_LEADER", "CAPTAIN", "MEMBER", "NONE"
)

data class CreateClanRequest(
    val name: String,
    val gymName: String,
    val city: String,
    val country: String,
    val description: String
)

data class ClanWarChallengeRequest(val targetClanId: String)

data class AchievementDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "STUDY", "PVP", "COMMUNITY", "MARKETPLACE", "SEASONS"
    val rarity: String, // "COMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"
    val progress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean,
    val xpReward: Int,
    val jiuTicketsReward: Int,
    val iconUrl: String,
    val unlockedAtTimestamp: Long?
)

interface JiuSpeakApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): TokenRefreshResponse

    @GET("api/user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): UserProfileDto

    @GET("api/missions")
    suspend fun getMissions(@Header("Authorization") token: String): List<MissionDto>

    @POST("api/missions/{id}/complete")
    suspend fun completeMission(@Header("Authorization") token: String, @Path("id") id: String): UserProfileDto

    @GET("api/feed")
    suspend fun getFeed(@Header("Authorization") token: String): List<PostDto>

    @POST("api/feed/post")
    suspend fun createPost(@Header("Authorization") token: String, @Body request: CreatePostRequest): PostDto

    @POST("api/feed/post/{id}/like")
    suspend fun likePost(@Header("Authorization") token: String, @Path("id") id: String): PostDto

    @GET("api/arena/history")
    suspend fun getPvpMatches(@Header("Authorization") token: String): List<PvpMatchResultDto>

    @POST("api/arena/match")
    suspend fun recordPvpMatch(@Header("Authorization") token: String, @Body result: PvpMatchResultDto): UserProfileDto

    @GET("api/courses")
    suspend fun getCourses(@Header("Authorization") token: String): List<CourseDto>

    @GET("api/marketplace/teachers")
    suspend fun getTeachers(@Header("Authorization") token: String): List<TeacherDto>

    @GET("api/pvp/leaderboard")
    suspend fun getLeaderboard(@Header("Authorization") token: String): List<LeaderboardDto>

    @GET("api/finance/wallet")
    suspend fun getWallet(@Header("Authorization") token: String): WalletDto

    @GET("api/subscriptions/plans")
    suspend fun getSubscriptionPlans(@Header("Authorization") token: String): List<SubscriptionPlanDto>

    // === NEW V4 FEATURES ===
    @GET("api/seasons/active")
    suspend fun getActiveSeason(@Header("Authorization") token: String): SeasonDto

    @POST("api/seasons/claim-reward")
    suspend fun claimReward(@Header("Authorization") token: String, @Body request: ClaimRewardRequest): UserProfileDto

    @GET("api/leagues/status")
    suspend fun getLeagueStatus(@Header("Authorization") token: String): LeagueStatusDto

    @GET("api/clans/all")
    suspend fun getAllClans(@Header("Authorization") token: String): List<ClanDto>

    @GET("api/clans/my-clan")
    suspend fun getMyClan(@Header("Authorization") token: String): ClanDto

    @POST("api/clans/create")
    suspend fun createClan(@Header("Authorization") token: String, @Body request: CreateClanRequest): ClanDto

    @POST("api/clans/war/challenge")
    suspend fun challengeClanWar(@Header("Authorization") token: String, @Body request: ClanWarChallengeRequest): retrofit2.Response<Void>

    @GET("api/achievements")
    suspend fun getAchievements(@Header("Authorization") token: String): List<AchievementDto>
}

object JiuSpeakApiClient {
    private var currentRetrofit: Retrofit? = null
    private var currentApi: JiuSpeakApi? = null
    private var configuredBaseUrl = ApiConfig.productionBaseUrl // Use production environment by default

    fun configure(baseUrl: String) {
        var formattedUrl = baseUrl.trim()
        if (!formattedUrl.endsWith("/")) {
            formattedUrl += "/"
        }
        if (formattedUrl != configuredBaseUrl || currentApi == null) {
            configuredBaseUrl = formattedUrl
            buildClient()
        }
    }

    fun getApi(): JiuSpeakApi? {
        if (currentApi == null) {
            buildClient()
        }
        return currentApi
    }

    fun getBaseUrl(): String = configuredBaseUrl

    private fun buildClient() {
        try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val request = chain.request().newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .build()
                        return chain.proceed(request)
                    }
                })
                .build()

            currentRetrofit = Retrofit.Builder()
                .baseUrl(configuredBaseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            currentApi = currentRetrofit?.create(JiuSpeakApi::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            currentApi = null
        }
    }
}
