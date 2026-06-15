package com.example.data.repository

import com.example.data.network.JiuSpeakApiClient
import com.example.data.network.SubscriptionPlanDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionRepository {
    suspend fun fetchSubscriptionPlans(token: String): Result<List<SubscriptionPlanDto>> = withContext(Dispatchers.IO) {
        try {
            val api = JiuSpeakApiClient.getApi() ?: return@withContext Result.failure(Exception("API client not initialized"))
            val plans = api.getSubscriptionPlans("Bearer $token")
            Result.success(plans)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
