package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JiuSpeakDatabase
import com.example.data.model.UserProfileEntity
import com.example.data.model.DailyMissionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: JiuSpeakDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JiuSpeakDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDatabaseInitializationAndUserDao() = runBlocking {
        val userDao = db.userDao()
        
        // Ensure profile is originally null
        val initialProfile = userDao.getProfileDirect()
        assertNull(initialProfile)

        // Insert new profile
        val mockProfile = UserProfileEntity(
            id = "test_user_id_123",
            username = "GuerreiroTest",
            fullName = "Guerreiro Testador",
            email = "guerreiro@jiuspeak.com.br",
            beltColor = "BLUE",
            level = 2,
            xp = 150,
            jiuTickets = 200
        )
        userDao.insertProfile(mockProfile)

        // Retrieve and assert values
        val savedProfile = userDao.getProfileDirect()
        assertNotNull(savedProfile)
        assertEquals("test_user_id_123", savedProfile?.id)
        assertEquals("GuerreiroTest", savedProfile?.username)
        assertEquals("guerreiro@jiuspeak.com.br", savedProfile?.email)
        assertEquals("BLUE", savedProfile?.beltColor)
        assertEquals(2, savedProfile?.level)
        assertEquals(150, savedProfile?.xp)
        assertEquals(200, savedProfile?.jiuTickets)

        // Retrieve via Flow
        val profileFromFlow = userDao.getProfileFlow().first()
        assertNotNull(profileFromFlow)
        assertEquals("GuerreiroTest", profileFromFlow?.username)
    }

    @Test
    fun testMissionsAndClearAllTables() = runBlocking {
        val missionDao = db.missionDao()
        
        val initialMissions = missionDao.getAllMissions().first()
        assertEquals(0, initialMissions.size)

        val missionList = listOf(
            DailyMissionEntity("m1", "Review Guard Pull", "Read vocabulary instructions on pull", 50, 100, false, 0, 1),
            DailyMissionEntity("m2", "Complete 1 PvP Match", "Fight under rules", 100, 200, true, 1, 1)
        )
        missionDao.insertMissions(missionList)

        var savedMissions = missionDao.getAllMissions().first()
        assertEquals(2, savedMissions.size)
        assertEquals("Review Guard Pull", savedMissions[0].title)
        assertEquals(true, savedMissions[1].isCompleted)

        // Test updating mission progress
        missionDao.updateMissionProgress("m1", 1, true)
        savedMissions = missionDao.getAllMissions().first()
        val updatedMission1 = savedMissions.find { it.id == "m1" }
        assertNotNull(updatedMission1)
        assertEquals(true, updatedMission1?.isCompleted)
        assertEquals(1, updatedMission1?.progress)

        // Test clearAllTables
        db.clearAllTables()
        
        val clearedMissions = missionDao.getAllMissions().first()
        assertEquals(0, clearedMissions.size)
    }
}
