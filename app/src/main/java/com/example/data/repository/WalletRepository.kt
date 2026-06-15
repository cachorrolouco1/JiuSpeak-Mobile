package com.example.data.repository

import com.example.data.network.JiuSpeakApiClient
import com.example.data.network.WalletDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepository {
    suspend fun fetchWallet(token: String): Result<WalletDto> = withContext(Dispatchers.IO) {
        try {
            val api = JiuSpeakApiClient.getApi() ?: return@withContext Result.failure(Exception("API client not initialized"))
            val wallet = api.getWallet("Bearer $token")
            Result.success(wallet)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
