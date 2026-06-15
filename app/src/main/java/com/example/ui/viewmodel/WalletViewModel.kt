package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.network.WalletDto
import com.example.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WalletViewModel(private val repository: WalletRepository) : ViewModel() {
    private val _walletState = MutableStateFlow<WalletUiState>(WalletUiState.Loading)
    val walletState: StateFlow<WalletUiState> = _walletState

    fun loadWallet(token: String) {
        viewModelScope.launch {
            _walletState.value = WalletUiState.Loading
            val result = repository.fetchWallet(token)
            if (result.isSuccess) {
                _walletState.value = WalletUiState.Success(result.getOrThrow())
            } else {
                _walletState.value = WalletUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}

sealed class WalletUiState {
    object Loading : WalletUiState()
    data class Success(val wallet: WalletDto) : WalletUiState()
    data class Error(val message: String) : WalletUiState()
}
