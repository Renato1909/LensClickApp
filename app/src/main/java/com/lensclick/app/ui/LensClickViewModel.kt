package com.lensclick.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lensclick.app.BuildConfig
import com.lensclick.app.LensClickApplication
import com.lensclick.app.data.LensClickRepository
import com.lensclick.app.data.HttpPublicPhotographersGateway
import com.lensclick.app.data.PublicPhotographerPage
import com.lensclick.app.data.PublicPhotographerProfile
import com.lensclick.app.data.PublicPhotographerNotFoundException
import com.lensclick.app.data.PublicPhotographersGateway
import com.lensclick.app.data.User
import com.lensclick.app.data.auth.MobileAuthErrorCode
import com.lensclick.app.data.auth.MobileAuthException
import com.lensclick.app.data.auth.MobileAuthRepository
import com.lensclick.app.data.auth.MobileUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface PublicSearchState {
    data object Loading : PublicSearchState
    data object Error : PublicSearchState
    data class Empty(val page: PublicPhotographerPage) : PublicSearchState
    data class Ready(val page: PublicPhotographerPage) : PublicSearchState
}

sealed interface PublicProfileState {
    data object Loading : PublicProfileState
    data object Error : PublicProfileState
    data object NotFoundOrUnpublished : PublicProfileState
    data class Ready(val profile: PublicPhotographerProfile) : PublicProfileState
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
    private val _publicProfile = MutableStateFlow<PublicProfileState>(PublicProfileState.Loading)
    val publicProfile: StateFlow<PublicProfileState> = _publicProfile.asStateFlow()
    private var publicSearchJob: kotlinx.coroutines.Job? = null
    private var publicProfileJob: kotlinx.coroutines.Job? = null
    private val authenticationMutex = Mutex()

    fun searchPublicPhotographers(city: String, specialty: String?, page: Int) {
        publicSearchJob?.cancel()
        publicSearchJob = viewModelScope.launch {
            _publicSearch.value = PublicSearchState.Loading
            _publicSearch.value = try {
                publicPhotographers.list(city, specialty, page).let {
                    if (it.photographers.isEmpty()) PublicSearchState.Empty(it) else PublicSearchState.Ready(it)
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                PublicSearchState.Error
            }
        }
    }

    fun loadPublicPhotographer(slug: String) {
        publicProfileJob?.cancel()
        publicProfileJob = viewModelScope.launch {
            _publicProfile.value = PublicProfileState.Loading
            _publicProfile.value = try {
                publicPhotographers.get(slug).let { profile ->
                    if (profile.isPublished) {
                        PublicProfileState.Ready(profile)
                    } else {
                        PublicProfileState.NotFoundOrUnpublished
                    }
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: PublicPhotographerNotFoundException) {
                PublicProfileState.NotFoundOrUnpublished
            } catch (_: Exception) {
                PublicProfileState.Error
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
            authenticationMutex.withLock {
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
    }

    fun restoreSession() {
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (mobileAuthRepository == null) {
                    _currentUser.value = null
                    _authentication.value = AuthenticationState.SignedOut
                    return@withLock
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
    }

    fun logout() {
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (mobileAuthRepository == null) {
                    _currentUser.value = null
                    _authentication.value = AuthenticationState.SignedOut
                    return@withLock
                }
                _authentication.value = AuthenticationState.Loading
                try {
                    requireMobileAuthRepository().logout()
                } catch (error: MobileAuthException) {
                    _currentUser.value = null
                    _authentication.value = AuthenticationState.Error(error.code)
                    return@withLock
                }
                _currentUser.value = null
                _authentication.value = AuthenticationState.LogoutCompleted
            }
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

    fun register(name: String, email: String, password: String, result: (Boolean) -> Unit) {
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (mobileAuthRepository == null) {
                    result(false)
                    return@withLock
                }
                _authentication.value = AuthenticationState.Loading
                try {
                    val session = requireMobileAuthRepository().register(
                        name = name,
                        email = email,
                        password = password,
                        deviceName = "Android",
                        appVersion = BuildConfig.VERSION_NAME
                    )
                    setAuthenticated(session.user)
                    result(true)
                } catch (error: MobileAuthException) {
                    _authentication.value = AuthenticationState.Error(error.code)
                    result(false)
                }
            }
        }
    }

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
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (mobileAuthRepository == null) {
                    result(false)
                    return@withLock
                }
                _authentication.value = AuthenticationState.Loading
                try {
                    val session = requireMobileAuthRepository().registerPhotographer(
                        name = name,
                        email = email,
                        password = password,
                        specialty = specialty,
                        city = city,
                        price = price,
                        bio = bio,
                        deviceName = "Android",
                        appVersion = BuildConfig.VERSION_NAME
                    )
                    setAuthenticated(session.user)
                    result(true)
                } catch (error: MobileAuthException) {
                    _authentication.value = AuthenticationState.Error(error.code)
                    result(false)
                }
            }
        }
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
