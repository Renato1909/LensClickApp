package com.example.lensclickapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lensclickapp.BuildConfig
import com.example.lensclickapp.LensClickApplication
import com.example.lensclickapp.data.LensClickRepository
import com.example.lensclickapp.data.HttpPublicPhotographersGateway
import com.example.lensclickapp.data.PublicPhotographerPage
import com.example.lensclickapp.data.PublicPhotographersGateway
import com.example.lensclickapp.data.User
import com.example.lensclickapp.data.auth.MobileAuthErrorCode
import com.example.lensclickapp.data.auth.MobileAuthException
import com.example.lensclickapp.data.auth.MobileAuthRepository
import com.example.lensclickapp.data.auth.MobileUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PublicSearchState {
    data object Loading : PublicSearchState
    data object Error : PublicSearchState
    data class Ready(val page: PublicPhotographerPage) : PublicSearchState
}

sealed interface AuthenticationState {
    data object Loading : AuthenticationState
    data object SignedOut : AuthenticationState
    data object LogoutCompleted : AuthenticationState
    data class Authenticated(val user: User) : AuthenticationState
    data class Error(val code: MobileAuthErrorCode) : AuthenticationState
    data object SessionExpired : AuthenticationState
}

class LensClickViewModel(
    private val repository: LensClickRepository,
    private val publicPhotographers: PublicPhotographersGateway = HttpPublicPhotographersGateway(),
    private val mobileAuthRepository: MobileAuthRepository? = null
) : ViewModel() {
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as LensClickApplication
                LensClickViewModel(application.repository, mobileAuthRepository = application.mobileAuthRepository)
            }
        }
    }
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val _authentication = MutableStateFlow<AuthenticationState>(AuthenticationState.Loading)
    val authentication: StateFlow<AuthenticationState> = _authentication.asStateFlow()
    private val _publicSearch = MutableStateFlow<PublicSearchState>(PublicSearchState.Loading)
    val publicSearch: StateFlow<PublicSearchState> = _publicSearch.asStateFlow()
    private var publicSearchJob: kotlinx.coroutines.Job? = null

    fun searchPublicPhotographers(city: String, specialty: String?, page: Int) {
        publicSearchJob?.cancel()
        publicSearchJob = viewModelScope.launch {
            _publicSearch.value = PublicSearchState.Loading
            _publicSearch.value = try {
                PublicSearchState.Ready(publicPhotographers.list(city, specialty, page))
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                PublicSearchState.Error
            }
        }
    }

    val photographers = repository.photographers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val budgets = repository.budgets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        viewModelScope.launch { repository.seedDemoData() }
        viewModelScope.launch { restoreSession() }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authentication.value = AuthenticationState.Loading
            try {
                val session = requireMobileAuthRepository().login(
                    email = email,
                    password = password,
                    deviceName = "Android",
                    appVersion = BuildConfig.VERSION_NAME
                )
                setAuthenticated(session.user)
            } catch (error: MobileAuthException) {
                _currentUser.value = null
                _authentication.value = AuthenticationState.Error(error.code)
            }
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            if (mobileAuthRepository == null) {
                _currentUser.value = null
                _authentication.value = AuthenticationState.SignedOut
                return@launch
            }
            try {
                val user = mobileAuthRepository.restore()
                if (user == null) {
                    _currentUser.value = null
                    _authentication.value = AuthenticationState.SignedOut
                } else {
                    setAuthenticated(user)
                }
            } catch (error: MobileAuthException) {
                _currentUser.value = null
                _authentication.value = if (error.code == MobileAuthErrorCode.UNAUTHENTICATED) {
                    AuthenticationState.SessionExpired
                } else {
                    AuthenticationState.Error(error.code)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (mobileAuthRepository == null) {
                _currentUser.value = null
                _authentication.value = AuthenticationState.SignedOut
                return@launch
            }
            _authentication.value = AuthenticationState.Loading
            try {
                requireMobileAuthRepository().logout()
            } catch (error: MobileAuthException) {
                _currentUser.value = null
                _authentication.value = AuthenticationState.Error(error.code)
                return@launch
            }
            _currentUser.value = null
            _authentication.value = AuthenticationState.LogoutCompleted
        }
    }

    private fun setAuthenticated(user: MobileUser) {
        val localUser = User(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role
        )
        _currentUser.value = localUser
        _authentication.value = AuthenticationState.Authenticated(localUser)
    }

    private fun requireMobileAuthRepository(): MobileAuthRepository =
        mobileAuthRepository ?: error("Mobile authentication repository is required")

    @Suppress("UNUSED_PARAMETER")
    fun register(name: String, email: String, password: String, result: (Boolean) -> Unit) {
        result(false)
    }

    @Suppress("UNUSED_PARAMETER")
    fun registerPhotographer(
        name: String,
        email: String,
        password: String,
        specialty: String,
        city: String,
        price: String,
        bio: String,
        result: (Boolean) -> Unit
    ) {
        result(false)
    }

    fun createBudget(
        type: String,
        date: String,
        place: String,
        duration: String,
        description: String,
        additionalInfo: String,
        done: () -> Unit
    ) {
        viewModelScope.launch {
            repository.createBudget(type, date, place, duration, description, additionalInfo)
            done()
        }
    }
}
