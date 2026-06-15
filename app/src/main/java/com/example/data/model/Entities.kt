package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "logged_user",
    val username: String = "AtletaGuerreiro",
    val fullName: String = "Renzo Gracie Believer",
    val email: String = "campeao@jiuspeak.com",
    val beltColor: String = "BLUE", // WHITE, BLUE, PURPLE, BROWN, BLACK
    val level: Int = 18,
    val xp: Int = 3450,
    val xpNextLevel: Int = 5000,
    val dailyStreak: Int = 12,
    val jiuTickets: Int = 1450,
    val selectedAvatar: String = "avatar_fighter",
    val selectedFrameColor: String = "#009DFF",
    val currentWeeklyRankingRank: Int = 4,
    val activeToken: String = "jwt_premium_mock_token_jiuspeak_secret_key"
)

@Entity(tableName = "daily_missions")
data class DailyMissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val jiuTicketsReward: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1
)

@Entity(tableName = "social_posts")
data class SocialPostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorBelt: String,
    val authorLevel: Int,
    val authorAvatar: String,
    val content: String,
    val imageUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timeAgo: String,
    val isLikedByMe: Boolean = false
)

@Entity(tableName = "pvp_battles")
data class PvpBattleEntity(
    @PrimaryKey val id: String,
    val opponentName: String,
    val opponentBelt: String,
    val opponentAvatar: String,
    val matchType: String, // "Conversation", "Vocabulary", "Pronunciation", "Lightning"
    val scoreMe: Int,
    val scoreOpponent: Int,
    val outcome: String, // "WIN", "LOSS", "DRAW", "PENDING"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val senderBelt: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isGlobal: Boolean = true // true = Global, false = private with selected support
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "English for Competitors", "Academy Management", "Referee English"
    val totalLessons: Int,
    val completedLessons: Int,
    val isCertified: Boolean = false,
    val xpMultiplier: Float = 1.0f
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String,
    val name: String,
    val belt: String,
    val rating: Float,
    val hourlyRate: Int, // tickets per hour
    val bio: String,
    val country: String,
    val language: String = "English",
    val imageUrl: String
)

@Entity(tableName = "seasons")
data class SeasonEntity(
    @PrimaryKey val id: String,
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
    val hasProPass: Boolean
)

@Entity(tableName = "season_rewards")
data class SeasonRewardEntity(
    @PrimaryKey val id: String,
    val seasonId: String,
    val level: Int,
    val type: String, // "AVATAR", "FRAME", "MEDAL", "JIU_TICKETS", "BOOSTER", "TITLE"
    val name: String,
    val isPremium: Boolean, // Requere VIP ou PRO
    val rewardsValue: Int,
    val isClaimed: Boolean,
    val imageUrl: String
)

@Entity(tableName = "clans")
data class ClanEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "league_status")
data class LeagueEntity(
    @PrimaryKey val id: String,
    val currentElo: Int,
    val division: String, // "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "LEGEND"
    val subDivision: Int, // 1, 2, 3, 4
    val promotionGoalElo: Int,
    val rebaixamentoThresholdElo: Int,
    val globalRank: Int,
    val countryRank: Int,
    val winsCount: Int,
    val lossesCount: Int
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
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
    val unlockedAtTimestamp: Long
)

