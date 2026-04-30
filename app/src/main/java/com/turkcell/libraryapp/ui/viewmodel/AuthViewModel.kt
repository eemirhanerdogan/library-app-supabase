package com.turkcell.libraryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.libraryapp.data.model.Profile
import com.turkcell.libraryapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object RegisterSuccess : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signUp(email: String, password: String, fullName: String, studentNo: String?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signUp(email, password, fullName, studentNo)
                .onSuccess {
                    _authState.value = AuthState.RegisterSuccess
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Kayıt başarısız")
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signIn(email, password)
                .onSuccess {
                    val userId = repository.getCurrentUserId()
                    if (userId != null) {
                        val profile = repository.getProfile(userId)
                        _profile.value = profile
                        _authState.value = AuthState.Success(profile?.role ?: "student")
                    } else {
                        _authState.value = AuthState.Error("Profil bulunamadı.")
                    }
                }
                .onFailure { ex ->
                    _authState.value = AuthState.Error(ex.message ?: "Giriş başarısız")
                }
        }
    }
}
