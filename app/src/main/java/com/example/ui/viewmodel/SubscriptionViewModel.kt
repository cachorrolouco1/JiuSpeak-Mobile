package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.network.SubscriptionPlanDto
import com.example.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(private val repository: SubscriptionRepository) : ViewModel() {
    private val _subscriptionState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val subscriptionState: StateFlow<SubscriptionUiState> = _subscriptionState

    fun loadPlans(token: String) {
        viewModelScope.launch {
            _subscriptionState.value = SubscriptionUiState.Loading
            val result = repository.fetchSubscriptionPlans(token)
            if (result.isSuccess) {
                _subscriptionState.value = SubscriptionUiState.Success(result.getOrDefault(emptyList()))
            } else {
                _subscriptionState.value = SubscriptionUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}

sealed class SubscriptionUiState {
    object Loading : SubscriptionUiState()
    data class Success(val plans: List<SubscriptionPlanDto>) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
}
