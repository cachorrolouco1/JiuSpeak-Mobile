package com.example.data.network

import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// API Payload Models
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val username: String, val password: String, val beltColor: String)
data class AuthResponse(
    @SerializedName("token", alternate = ["accessToken"])
    val token: String,
    val refreshToken: String,
    val user: UserProfileDto
)
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
    val avatar: String,
    val bio: String? = null,
    val city: String? = null,
    val country: String? = null,
    val nativeLanguage: String? = null,
    val studiedLanguages: String? = null,
    val learningGoals: String? = null,
    val instagram: String? = null,
    val youtube: String? = null,
    val facebook: String? = null,
    val website: String? = null,
    val coverPhoto: String? = null,
    val isVerified: Boolean? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null
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
    private var okHttpClient: OkHttpClient? = null
    private var configuredBaseUrl = ApiConfig.productionBaseUrl // Use production environment by default
    
    private var appContext: android.content.Context? = null
    private var cookieJar: okhttp3.CookieJar = InMemoryCookieJar()
    private var detectedCsrfToken: String? = null

    fun configure(baseUrl: String, context: android.content.Context? = null) {
        var formattedUrl = baseUrl.trim()
        if (!formattedUrl.endsWith("/")) {
            formattedUrl += "/"
        }
        configuredBaseUrl = formattedUrl
        if (context != null) {
            appContext = context.applicationContext
            cookieJar = PersistentCookieJar(context.applicationContext)
        }
        buildClient()
    }

    fun getApi(): JiuSpeakApi? {
        if (currentApi == null) {
            buildClient()
        }
        return currentApi
    }

    fun getBaseUrl(): String = configuredBaseUrl

    fun getOkHttpClient(): OkHttpClient? {
        if (okHttpClient == null) {
            buildClient()
        }
        return okHttpClient
    }

    suspend fun ensureCsrf() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val client = getOkHttpClient() ?: return@withContext
            val csrfUrl = if (configuredBaseUrl.endsWith("/")) {
                "${configuredBaseUrl}api/csrf-token"
            } else {
                "${configuredBaseUrl}/api/csrf-token"
            }
            println("CSRF Request Warm Up URL: $csrfUrl")
            val request = okhttp3.Request.Builder()
                .url(csrfUrl)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                println("Ensure CSRF response code: ${response.code}")
                val bodyText = response.body?.string() ?: ""
                println("Ensure CSRF response body: $bodyText")
                if (response.isSuccessful && bodyText.isNotEmpty()) {
                    try {
                        val json = com.google.gson.JsonParser.parseString(bodyText).asJsonObject
                        val csrfToken = json.get("csrfToken")?.asString
                        if (csrfToken != null && csrfToken.isNotBlank()) {
                            detectedCsrfToken = csrfToken
                            println("Parsed CSRF token successfully: $csrfToken")
                        }
                    } catch (e: Exception) {
                        println("Failed to parse CSRF json response: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("CSRF Warm Up Error: ${e.message}")
        }
    }

    private fun buildClient() {
        try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .addInterceptor(logging)
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val originalRequest = chain.request()
                        val requestBuilder = originalRequest.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")

                        // 1. Inject detected CSRF token to headers if available
                        detectedCsrfToken?.let { token ->
                            requestBuilder.header("X-XSRF-TOKEN", token)
                            requestBuilder.header("X-CSRF-TOKEN", token)
                            requestBuilder.header("x-xsrf-token", token)
                            requestBuilder.header("x-csrf-token", token)
                        }

                        // 2. Scan active cookies for CSRF/XSRF token and inject if found
                        val url = originalRequest.url
                        val savedCookies = cookieJar.loadForRequest(url)
                        val csrfCookie = savedCookies.find { 
                            it.name.contains("csrf", ignoreCase = true) || 
                            it.name.contains("xsrf", ignoreCase = true) 
                        }
                        csrfCookie?.let { cookie ->
                            val decodedValue = try {
                                java.net.URLDecoder.decode(cookie.value, "UTF-8")
                            } catch (e: Exception) {
                                cookie.value
                            }
                            requestBuilder.header("X-XSRF-TOKEN", decodedValue)
                            requestBuilder.header("X-CSRF-TOKEN", decodedValue)
                            requestBuilder.header("x-xsrf-token", decodedValue)
                            requestBuilder.header("x-csrf-token", decodedValue)
                        }

                        val request = requestBuilder.build()

                        // Explicitly log request headers and body details
                        println("=== JIUSPEAK API REQUEST START ===")
                        println("URL: ${request.url}")
                        println("METHOD: ${request.method}")
                        request.headers.forEach { pair ->
                            println("REQUEST HEADER: ${pair.first}: ${pair.second}")
                        }
                        request.body?.let { body ->
                            try {
                                val buffer = okio.Buffer()
                                body.writeTo(buffer)
                                val bodyStr = buffer.readUtf8()
                                println("REQUEST BODY: $bodyStr")
                            } catch (e: Exception) {
                                println("REQUEST BODY LOG ERROR: ${e.message}")
                            }
                        }
                        println("=== JIUSPEAK API REQUEST END ===")

                        val response = chain.proceed(request)

                        // 3. Inspect response header or set-cookie headers for fresh CSRF token
                        val csrfHeader = response.header("X-XSRF-TOKEN") 
                            ?: response.header("X-CSRF-TOKEN")
                            ?: response.header("x-csrf-token")
                            ?: response.header("x-xsrf-token")
                        if (csrfHeader != null && csrfHeader.isNotBlank()) {
                            detectedCsrfToken = csrfHeader
                            println("Fresh CSRF Token header: $csrfHeader")
                        }

                        val setCookieHeaders = response.headers("Set-Cookie")
                        for (header in setCookieHeaders) {
                            try {
                                val cookie = okhttp3.Cookie.parse(url, header)
                                if (cookie != null && (
                                    cookie.name.contains("csrf", ignoreCase = true) || 
                                    cookie.name.contains("xsrf", ignoreCase = true)
                                )) {
                                    val decodedValue = java.net.URLDecoder.decode(cookie.value, "UTF-8")
                                    detectedCsrfToken = decodedValue
                                    println("Fresh CSRF Token cookie: ${cookie.name}=$decodedValue")
                                }
                            } catch (e: Exception) {
                                // Ignore parsing errors
                            }
                        }

                        // Explicitly log response status code, headers and body details
                        println("=== JIUSPEAK API RESPONSE START ===")
                        println("HTTP CODE: ${response.code}")
                        response.headers.forEach { pair ->
                            println("RESPONSE HEADER: ${pair.first}: ${pair.second}")
                        }
                        try {
                            val responseBodyCopy = response.peekBody(1024 * 1024)
                            val bodyStr = responseBodyCopy.string()
                            println("RESPONSE BODY: $bodyStr")
                        } catch (e: Exception) {
                            println("RESPONSE BODY LOG ERROR: ${e.message}")
                        }
                        println("=== JIUSPEAK API RESPONSE END ===")

                        return response
                    }
                })
                .build()

            okHttpClient = client

            currentRetrofit = Retrofit.Builder()
                .baseUrl(configuredBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            currentApi = currentRetrofit?.create(JiuSpeakApi::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            currentApi = null
        }
    }
}

class InMemoryCookieJar : okhttp3.CookieJar {
    private val cookieStore = java.util.concurrent.ConcurrentHashMap<String, List<okhttp3.Cookie>>()

    override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
        val host = url.host
        val current = cookieStore[host]?.toMutableList() ?: mutableListOf()
        cookies.forEach { newCookie ->
            current.removeAll { it.name == newCookie.name }
            current.add(newCookie)
        }
        cookieStore[host] = current
    }

    override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
        val host = url.host
        return cookieStore[host] ?: emptyList()
    }
}

class PersistentCookieJar(context: android.content.Context) : okhttp3.CookieJar {
    private val cookieStore = java.util.concurrent.ConcurrentHashMap<String, MutableList<okhttp3.Cookie>>()
    private val prefs = context.getSharedPreferences("jiuspeak_cookies", android.content.Context.MODE_PRIVATE)

    init {
        try {
            prefs.all.forEach { (host, serialized) ->
                if (serialized is String) {
                    val cookiesList = mutableListOf<okhttp3.Cookie>()
                    val parts = serialized.split("||")
                    parts.forEach { part ->
                        if (part.isNotBlank()) {
                            val parsed = parseCookieString(host, part)
                            if (parsed != null) {
                                cookiesList.add(parsed)
                            }
                        }
                    }
                    cookieStore[host] = cookiesList
                }
            }
        } catch (e: Exception) {
            println("PersistentCookieJar init error: ${e.message}")
        }
    }

    private fun parseCookieString(host: String, part: String): okhttp3.Cookie? {
        try {
            val segments = part.split(";")
            var name = ""
            var value = ""
            var domain = host
            var path = "/"
            var secure = false
            var httpOnly = false
            var expiresAt = Long.MAX_VALUE

            segments.forEach { segment ->
                val eq = segment.indexOf('=')
                if (eq != -1) {
                    val k = segment.substring(0, eq).trim()
                    val v = segment.substring(eq + 1).trim()
                    when (k) {
                        "domain" -> domain = v
                        "path" -> path = v
                        "secure" -> secure = v.toBoolean()
                        "httpOnly" -> httpOnly = v.toBoolean()
                        "expires" -> expiresAt = v.toLongOrNull() ?: Long.MAX_VALUE
                        else -> {
                            if (name.isEmpty()) {
                                name = k
                                value = v
                            }
                        }
                    }
                }
            }
            if (name.isNotEmpty()) {
                val builder = okhttp3.Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain(domain)
                    .path(path)
                if (secure) builder.secure()
                if (httpOnly) builder.httpOnly()
                if (expiresAt != Long.MAX_VALUE) builder.expiresAt(expiresAt)
                return builder.build()
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return null
    }

    private fun serializeCookie(cookie: okhttp3.Cookie): String {
        return "${cookie.name}=${cookie.value};domain=${cookie.domain};path=${cookie.path};secure=${cookie.secure};httpOnly=${cookie.httpOnly};expires=${cookie.expiresAt}"
    }

    override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
        val host = url.host
        val current = cookieStore[host] ?: mutableListOf()
        cookies.forEach { newCookie ->
            current.removeAll { it.name == newCookie.name }
            current.add(newCookie)
        }
        cookieStore[host] = current

        // Persist to preferences
        try {
            val serialized = current.joinToString("||") { serializeCookie(it) }
            prefs.edit().putString(host, serialized).apply()
        } catch (e: Exception) {
            println("PersistentCookieJar save error: ${e.message}")
        }
    }

    override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
        val host = url.host
        val originalList = cookieStore[host] ?: emptyList()
        val now = System.currentTimeMillis()
        val validList = originalList.filter { it.expiresAt > now }
        if (validList.size != originalList.size) {
            cookieStore[host] = validList.toMutableList()
            try {
                val serialized = validList.joinToString("||") { serializeCookie(it) }
                prefs.edit().putString(host, serialized).apply()
            } catch (e: Exception) {
                // Ignore
            }
        }
        return validList
    }
}
