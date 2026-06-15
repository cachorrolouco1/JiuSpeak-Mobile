package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.network.LeaderboardDto
import com.example.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(private val repository: LeaderboardRepository) : ViewModel() {
    private val _leaderboardState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val leaderboardState: StateFlow<LeaderboardUiState> = _leaderboardState

    fun loadLeaderboard(token: String) {
        viewModelScope.launch {
            _leaderboardState.value = LeaderboardUiState.Loading
            val result = repository.fetchLeaderboard(token)
            if (result.isSuccess) {
                _leaderboardState.value = LeaderboardUiState.Success(result.getOrDefault(emptyList()))
            } else {
                _leaderboardState.value = LeaderboardUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}

sealed class LeaderboardUiState {
    object Loading : LeaderboardUiState()
    data class Success(val list: List<LeaderboardDto>) : LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}
