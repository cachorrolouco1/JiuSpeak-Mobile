package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
}

@Dao
interface MissionDao {
    @Query("SELECT * FROM daily_missions")
    fun getAllMissions(): Flow<List<DailyMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<DailyMissionEntity>)

    @Update
    suspend fun updateMission(mission: DailyMissionEntity)

    @Query("UPDATE daily_missions SET progress = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateMissionProgress(id: String, progress: Int, completed: Boolean)
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM social_posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<SocialPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPostEntity)

    @Query("UPDATE social_posts SET likesCount = :likes, isLikedByMe = :liked WHERE id = :id")
    suspend fun updatePostLike(id: String, likes: Int, liked: Boolean)
}

@Dao
interface PvpDao {
    @Query("SELECT * FROM pvp_battles ORDER BY timestamp DESC")
    fun getAllBattles(): Flow<List<PvpBattleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattle(battle: PvpBattleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattles(battles: List<PvpBattleEntity>)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)
}

@Dao
interface SeasonDao {
    @Query("SELECT * FROM seasons LIMIT 1")
    fun getActiveSeason(): Flow<SeasonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity)

    @Query("SELECT * FROM season_rewards ORDER BY level ASC")
    fun getSeasonRewards(): Flow<List<SeasonRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<SeasonRewardEntity>)

    @Query("UPDATE season_rewards SET isClaimed = 1 WHERE id = :id")
    suspend fun claimReward(id: String)
}

@Dao
interface ClanDao {
    @Query("SELECT * FROM clans WHERE myRole != 'NONE' LIMIT 1")
    fun getMyClanFlow(): Flow<ClanEntity?>

    @Query("SELECT * FROM clans WHERE myRole != 'NONE' LIMIT 1")
    suspend fun getMyClanDirect(): ClanEntity?

    @Query("SELECT * FROM clans ORDER BY totalXp DESC")
    fun getAllClansFlow(): Flow<List<ClanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClans(clans: List<ClanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClan(clan: ClanEntity)

    @Query("UPDATE clans SET myRole = :role WHERE id = :clanId")
    suspend fun updateMyRole(clanId: String, role: String)

    @Query("UPDATE clans SET myRole = 'NONE' WHERE myRole != 'NONE'")
    suspend fun leaveCurrentClan()
}

@Dao
interface LeagueDao {
    @Query("SELECT * FROM league_status LIMIT 1")
    fun getLeagueStatusFlow(): Flow<LeagueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagueStatus(status: LeagueEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET progress = :progress, isCompleted = :completed, unlockedAtTimestamp = :unlockedAt WHERE id = :id")
    suspend fun updateAchievementProgress(id: String, progress: Int, completed: Boolean, unlockedAt: Long)
}

@Database(
    entities = [
        UserProfileEntity::class,
        DailyMissionEntity::class,
        SocialPostEntity::class,
        PvpBattleEntity::class,
        ChatMessageEntity::class,
        CourseEntity::class,
        TeacherEntity::class,
        SeasonEntity::class,
        SeasonRewardEntity::class,
        ClanEntity::class,
        LeagueEntity::class,
        AchievementEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class JiuSpeakDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun missionDao(): MissionDao
    abstract fun socialDao(): SocialDao
    abstract fun pvpDao(): PvpDao
    abstract fun chatDao(): ChatDao
    abstract fun courseDao(): CourseDao
    abstract fun teacherDao(): TeacherDao
    abstract fun seasonDao(): SeasonDao
    abstract fun clanDao(): ClanDao
    abstract fun leagueDao(): LeagueDao
    abstract fun achievementDao(): AchievementDao
}
