package com.example.data.repository

import com.example.data.network.JiuSpeakApiClient
import com.example.data.network.LeaderboardDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LeaderboardRepository {
    suspend fun fetchLeaderboard(token: String): Result<List<LeaderboardDto>> = withContext(Dispatchers.IO) {
        try {
            val api = JiuSpeakApiClient.getApi() ?: return@withContext Result.failure(Exception("API client not initialized"))
            val list = api.getLeaderboard("Bearer $token")
            Result.success(list)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
