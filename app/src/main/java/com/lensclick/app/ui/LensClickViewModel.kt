package com.lensclick.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lensclick.app.BuildConfig
import com.lensclick.app.LensClickApplication
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.local.User
import com.lensclick.app.data.remote.HttpPublicPhotographersGateway
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.MobileLoginResult
import com.lensclick.app.domain.AccountGateway
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.BudgetRequestsGateway
import com.lensclick.app.domain.Conversation
import com.lensclick.app.domain.ConversationsGateway
import com.lensclick.app.domain.EngagementAction
import com.lensclick.app.domain.EngagementDraft
import com.lensclick.app.domain.EngagementGateway
import com.lensclick.app.domain.EngagementOffer
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.FavoritesGateway
import com.lensclick.app.domain.Message
import com.lensclick.app.domain.MobileUser
import com.lensclick.app.domain.PortfolioGateway
import com.lensclick.app.domain.PortfolioItem
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.ProfessionalProfileGateway
import com.lensclick.app.domain.PublicPhotographerNotFoundException
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.domain.PublicReviews
import com.lensclick.app.domain.ReviewRequest
import com.lensclick.app.domain.ReviewsGateway
import com.lensclick.app.domain.TwoFactorGateway
import com.lensclick.app.domain.TwoFactorSetup
import com.lensclick.app.domain.TwoFactorStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
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

data class PhotographerRankingState(
    val photographers: List<PublicPhotographer> = emptyList(),
    val nextPage: Int? = 1,
    val loading: Boolean = false,
    val failed: Boolean = false
)

data class PhotographerReviewsState(
    val slug: String,
    val data: PublicReviews? = null,
    val failed: Boolean = false
)

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
    data class TwoFactorRequired(
        val email: String,
        val challengeToken: String,
        val verifying: Boolean = false,
        val error: MobileAuthErrorCode? = null
    ) : AuthenticationState
    data class Error(val code: MobileAuthErrorCode) : AuthenticationState
    data object SessionExpired : AuthenticationState
}

sealed interface TwoFactorEnrollmentState {
    data object Idle : TwoFactorEnrollmentState
    data object Loading : TwoFactorEnrollmentState
    data object Error : TwoFactorEnrollmentState
    data class Ready(
        val setup: TwoFactorSetup,
        val verifying: Boolean = false,
        val error: MobileAuthErrorCode? = null
    ) : TwoFactorEnrollmentState
}

sealed interface ProfessionalProfileState {
    data object Loading : ProfessionalProfileState
    data object Error : ProfessionalProfileState
    data class Ready(
        val profile: ProfessionalProfile,
        val saving: Boolean = false,
        val saved: Boolean = false,
        val error: MobileAuthErrorCode? = null
    ) : ProfessionalProfileState
}

sealed interface PortfolioState {
    data object Loading : PortfolioState
    data object Error : PortfolioState
    data class Ready(
        val items: List<PortfolioItem>,
        val busy: Boolean = false,
        val error: MobileAuthErrorCode? = null,
        val message: String? = null
    ) : PortfolioState
}

data class ChatState(
    val conversationId: Long,
    val messages: List<Message>? = null,
    val loadFailed: Boolean = false
)

class LensClickViewModel(
    private val repository: LensClickRepository,
    private val publicPhotographers: PublicPhotographersGateway = HttpPublicPhotographersGateway(),
    private val mobileAuthRepository: MobileAuthRepository? = null,
    private val professionalProfileGateway: ProfessionalProfileGateway? = null,
    private val portfolioGateway: PortfolioGateway? = null,
    private val budgetRequestsGateway: BudgetRequestsGateway? = null,
    private val favoritesGateway: FavoritesGateway? = null,
    private val twoFactorGateway: TwoFactorGateway? = null,
    private val conversationsGateway: ConversationsGateway? = null,
    private val reviewsGateway: ReviewsGateway? = null,
    private val accountGateway: AccountGateway? = null,
    private val engagementGateway: EngagementGateway? = null
) : ViewModel() {
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as LensClickApplication
                LensClickViewModel(
                    application.repository,
                    mobileAuthRepository = application.mobileAuthRepository,
                    professionalProfileGateway = application.professionalProfileGateway,
                    portfolioGateway = application.portfolioGateway,
                    budgetRequestsGateway = application.budgetRequestsGateway,
                    favoritesGateway = application.favoritesGateway,
                    twoFactorGateway = application.twoFactorGateway,
                    conversationsGateway = application.conversationsGateway,
                    reviewsGateway = application.reviewsGateway,
                    accountGateway = application.accountGateway,
                    engagementGateway = application.engagementGateway
                )
            }
        }
    }
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val _authentication = MutableStateFlow<AuthenticationState>(AuthenticationState.Loading)
    val authentication: StateFlow<AuthenticationState> = _authentication.asStateFlow()
    private val _publicSearch = MutableStateFlow<PublicSearchState>(PublicSearchState.Loading)
    val publicSearch: StateFlow<PublicSearchState> = _publicSearch.asStateFlow()
    private val _photographerRanking = MutableStateFlow(PhotographerRankingState())
    val photographerRanking = _photographerRanking.asStateFlow()
    private var rankingJob: Job? = null
    private val _publicProfile = MutableStateFlow<PublicProfileState>(PublicProfileState.Loading)
    val publicProfile: StateFlow<PublicProfileState> = _publicProfile.asStateFlow()
    private var publicSearchJob: kotlinx.coroutines.Job? = null
    private var publicProfileJob: kotlinx.coroutines.Job? = null
    private val authenticationMutex = Mutex()
    private var pendingPhotographerEmail: String? = null
    private val _finishingPhotographerRegistration = MutableStateFlow(false)
    val finishingPhotographerRegistration = _finishingPhotographerRegistration.asStateFlow()

    fun searchPublicPhotographers(city: String, specialty: String?, page: Int) {
        publicSearchJob?.cancel()
        publicSearchJob = viewModelScope.launch {
            _publicSearch.value = PublicSearchState.Loading
            _publicSearch.value = try {
                publicPhotographers.list(city, specialty, page).let {
                    if (it.photographers.isEmpty()) PublicSearchState.Empty(it) else PublicSearchState.Ready(it)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                PublicSearchState.Error
            }
        }
    }

    fun loadPhotographerRanking(excludedSlugs: Set<String>, refresh: Boolean = false) {
        if (_photographerRanking.value.loading) return
        val current = if (refresh) PhotographerRankingState() else _photographerRanking.value
        val page = current.nextPage ?: return
        _photographerRanking.value = current.copy(loading = true, failed = false)
        rankingJob = viewModelScope.launch {
            try {
                val result = publicPhotographers.listTopRated(page)
                val eligible = result.photographers.filter { photographer ->
                    val average = photographer.ratingAverage
                    average != null && average in 1.0..5.0 && photographer.reviewCount > 0 &&
                        photographer.slug != TestPhotographerSlug && photographer.slug != DemoPhotographerSlug &&
                        photographer.slug !in excludedSlugs
                }
                // A API ordena os perfis sem nota depois de todos os perfis avaliados.
                val reachedUnrated = result.photographers.any { it.ratingAverage == null }
                val nextPage = (page + 1).takeIf {
                    !reachedUnrated && result.photographers.isNotEmpty() && page < result.totalPages
                }
                _photographerRanking.value = PhotographerRankingState(
                    photographers = (current.photographers + eligible).distinctBy { it.slug },
                    nextPage = nextPage
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _photographerRanking.value = current.copy(failed = true)
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
            } catch (cancelled: CancellationException) {
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
    private val _budgets = MutableStateFlow<List<BudgetRequest>>(emptyList())
    val budgets: StateFlow<List<BudgetRequest>> = _budgets.asStateFlow()
    private val _agenda = MutableStateFlow<List<BudgetRequest>>(emptyList())
    val agenda: StateFlow<List<BudgetRequest>> = _agenda.asStateFlow()
    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()
    private val _budgetBusy = MutableStateFlow(false)
    val budgetBusy: StateFlow<Boolean> = _budgetBusy.asStateFlow()
    private val _budgetError = MutableStateFlow<MobileAuthErrorCode?>(null)
    val budgetError: StateFlow<MobileAuthErrorCode?> = _budgetError.asStateFlow()
    private val _budgetMessage = MutableStateFlow<String?>(null)
    val budgetMessage: StateFlow<String?> = _budgetMessage.asStateFlow()

    // 2FA State
    private val _twoFactorStatus = MutableStateFlow<TwoFactorStatus?>(null)
    val twoFactorStatus: StateFlow<TwoFactorStatus?> = _twoFactorStatus.asStateFlow()
    private val _twoFactorStatusLoading = MutableStateFlow(false)
    val twoFactorStatusLoading: StateFlow<Boolean> = _twoFactorStatusLoading.asStateFlow()
    private val _twoFactorEnrollment = MutableStateFlow<TwoFactorEnrollmentState>(TwoFactorEnrollmentState.Idle)
    val twoFactorEnrollment: StateFlow<TwoFactorEnrollmentState> = _twoFactorEnrollment.asStateFlow()

    // Conversations & Messages State
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    private val _conversationsLoading = MutableStateFlow(false)
    val conversationsLoading: StateFlow<Boolean> = _conversationsLoading.asStateFlow()
    private val _conversationsFailed = MutableStateFlow(false)
    val conversationsFailed: StateFlow<Boolean> = _conversationsFailed.asStateFlow()
    private val _chat = MutableStateFlow<ChatState?>(null)
    val chat: StateFlow<ChatState?> = _chat.asStateFlow()
    private var conversationsJob: Job? = null
    private var messagesJob: Job? = null

    // Reviews State
    private val _publicReviews = MutableStateFlow<PhotographerReviewsState?>(null)
    val publicReviews = _publicReviews.asStateFlow()
    private var reviewsJob: Job? = null

    init {
        viewModelScope.launch { repository.seedDemoData() }
        restoreSession()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authenticationMutex.withLock {
                _authentication.value = AuthenticationState.Loading
                try {
                    when (val result = requireMobileAuthRepository().login(
                        email = email,
                        password = password,
                        deviceName = "Android",
                        appVersion = BuildConfig.VERSION_NAME
                    )) {
                        is MobileLoginResult.Success -> setAuthenticated(result.session.user)
                        is MobileLoginResult.TwoFactorRequired -> {
                            _currentUser.value = null
                            _authentication.value = AuthenticationState.TwoFactorRequired(
                                email = email,
                                challengeToken = result.challengeToken
                            )
                        }
                    }
                } catch (error: MobileAuthException) {
                    _currentUser.value = null
                    _authentication.value = AuthenticationState.Error(error.code)
                }
            }
        }
    }

    fun verifyTwoFactorChallenge(code: String) {
        val gateway = twoFactorGateway ?: return
        val challenge = _authentication.value as? AuthenticationState.TwoFactorRequired ?: return
        if (challenge.verifying || code.isBlank()) return
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (_authentication.value !== challenge) return@withLock
                _authentication.value = challenge.copy(verifying = true, error = null)
                try {
                    val session = gateway.verifyChallenge(challenge.challengeToken, code)
                    setAuthenticated(session.user)
                } catch (error: MobileAuthException) {
                    _authentication.value = if (error.code == MobileAuthErrorCode.INVALID_TWO_FACTOR_CHALLENGE) {
                        AuthenticationState.Error(error.code)
                    } else {
                        challenge.copy(error = error.code)
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _authentication.value = challenge.copy(error = MobileAuthErrorCode.SERVER)
                }
            }
        }
    }

    fun cancelTwoFactorChallenge() {
        val challenge = _authentication.value as? AuthenticationState.TwoFactorRequired ?: return
        if (!challenge.verifying) _authentication.value = AuthenticationState.SignedOut
    }

    fun loadTwoFactorStatus() {
        val gateway = twoFactorGateway ?: return
        val userId = _currentUser.value?.id ?: return
        if (_twoFactorStatusLoading.value) return
        _twoFactorStatusLoading.value = true
        viewModelScope.launch {
            try {
                val status = gateway.getStatus()
                if (_currentUser.value?.id == userId) _twoFactorStatus.value = status
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (_currentUser.value?.id == userId) _twoFactorStatus.value = null
            } finally {
                _twoFactorStatusLoading.value = false
            }
        }
    }

    fun setupTwoFactor() {
        val gateway = twoFactorGateway ?: return
        if (_authentication.value !is AuthenticationState.Authenticated) return
        if (_twoFactorEnrollment.value !in setOf(TwoFactorEnrollmentState.Idle, TwoFactorEnrollmentState.Error)) return
        _twoFactorEnrollment.value = TwoFactorEnrollmentState.Loading
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (_authentication.value !is AuthenticationState.Authenticated ||
                    _twoFactorEnrollment.value != TwoFactorEnrollmentState.Loading
                ) return@withLock
                _twoFactorEnrollment.value = try {
                    TwoFactorEnrollmentState.Ready(gateway.setup())
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    TwoFactorEnrollmentState.Error
                }
            }
        }
    }

    fun closeTwoFactorSetup() {
        val state = _twoFactorEnrollment.value
        if (state != TwoFactorEnrollmentState.Loading && (state as? TwoFactorEnrollmentState.Ready)?.verifying != true) {
            _twoFactorEnrollment.value = TwoFactorEnrollmentState.Idle
        }
    }

    fun enableTwoFactor(code: String) {
        val gateway = twoFactorGateway ?: return
        val enrollment = _twoFactorEnrollment.value as? TwoFactorEnrollmentState.Ready ?: return
        if (enrollment.verifying) {
            return
        }
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (_twoFactorEnrollment.value !== enrollment) return@withLock
                _twoFactorEnrollment.value = enrollment.copy(verifying = true, error = null)
                try {
                    if (gateway.enable(code)) {
                        _twoFactorStatus.value = TwoFactorStatus(enabled = true)
                        _twoFactorEnrollment.value = TwoFactorEnrollmentState.Idle
                    } else {
                        _twoFactorEnrollment.value = enrollment.copy(error = MobileAuthErrorCode.SERVER)
                    }
                } catch (error: MobileAuthException) {
                    _twoFactorEnrollment.value = enrollment.copy(error = error.code)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _twoFactorEnrollment.value = enrollment.copy(error = MobileAuthErrorCode.SERVER)
                }
            }
        }
    }

    fun disableTwoFactor(password: String, code: String, result: (MobileAuthErrorCode?) -> Unit) {
        val gateway = twoFactorGateway ?: run { result(MobileAuthErrorCode.SERVER); return }
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (_authentication.value !is AuthenticationState.Authenticated) {
                    result(MobileAuthErrorCode.UNAUTHENTICATED)
                    return@withLock
                }
                try {
                    if (!gateway.disable(password, code)) {
                        result(MobileAuthErrorCode.SERVER)
                        return@withLock
                    }
                    _twoFactorStatus.value = null
                    _twoFactorEnrollment.value = TwoFactorEnrollmentState.Idle
                    _currentUser.value = null
                    clearAccountLists()
                    _authentication.value = AuthenticationState.LogoutCompleted
                    mobileAuthRepository?.clearSession()
                    result(null)
                } catch (error: MobileAuthException) {
                    result(error.code)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    result(MobileAuthErrorCode.SERVER)
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
                _twoFactorStatus.value = null
                _twoFactorEnrollment.value = TwoFactorEnrollmentState.Idle
                clearAccountLists()
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
                pendingPhotographerEmail = null
                _finishingPhotographerRegistration.value = false
                _authentication.value = AuthenticationState.LogoutCompleted
            }
        }
    }

    private fun setAuthenticated(user: MobileUser) {
        val previous = _currentUser.value
        if (previous?.id != user.id || previous.role != user.role) clearAccountLists()
        val localUser = User(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role
        )
        _twoFactorStatus.value = null
        _twoFactorEnrollment.value = TwoFactorEnrollmentState.Idle
        _currentUser.value = localUser
        _authentication.value = AuthenticationState.Authenticated(localUser)
        loadTwoFactorStatus()
        loadConversations()
    }

    private fun clearAccountLists() {
        conversationsJob?.cancel()
        conversationsJob = null
        messagesJob?.cancel()
        _chat.value = null
        _conversations.value = emptyList()
        _conversationsLoading.value = false
        _conversationsFailed.value = false
        _budgets.value = emptyList()
        _favorites.value = emptyList()
        _agenda.value = emptyList()
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
        phone: String,
        profile: ProfessionalProfile,
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
                    val normalizedEmail = email.trim().lowercase()
                    val existing = if (pendingPhotographerEmail == normalizedEmail) mobileAuthRepository.restore() else null
                    val registered = existing?.takeIf { it.email == normalizedEmail }
                        ?: mobileAuthRepository.register(
                            name, normalizedEmail, password, deviceName = "Android", appVersion = BuildConfig.VERSION_NAME
                        ).user
                    pendingPhotographerEmail = registered.email
                    _finishingPhotographerRegistration.value = true
                    val photographer = if (registered.role == "photographer") registered
                    else checkNotNull(accountGateway).enableProfessionalAccount(phone.trim())
                    val gateway = checkNotNull(professionalProfileGateway)
                    val saved = gateway.load()
                    gateway.save(saved.copy(
                        bio = profile.bio,
                        city = profile.city,
                        specialties = profile.specialties,
                        startingPriceCents = profile.startingPriceCents
                    ))
                    pendingPhotographerEmail = null
                    _finishingPhotographerRegistration.value = false
                    setAuthenticated(photographer)
                    result(true)
                } catch (error: MobileAuthException) {
                    _authentication.value = AuthenticationState.Error(error.code)
                    result(false)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _authentication.value = AuthenticationState.Error(MobileAuthErrorCode.SERVER)
                    result(false)
                }
            }
        }
    }

    fun enableProfessionalAccount(phone: String, result: (Result<Unit>) -> Unit) {
        runRemoteAction(result) {
            authenticationMutex.withLock {
                if (_authentication.value !is AuthenticationState.Authenticated) {
                    throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
                }
                val user = checkNotNull(accountGateway).enableProfessionalAccount(phone.trim())
                setAuthenticated(user)
            }
        }
    }

    /** Evita que um erro de cadastro apareça como erro de login ao navegar para a tela de entrada. */
    fun clearAuthenticationError() {
        if (_authentication.value is AuthenticationState.Error) {
            _authentication.value = AuthenticationState.SignedOut
        }
    }

    fun loadConversations() {
        val gateway = conversationsGateway ?: return
        if (conversationsJob?.isActive == true) return
        val userId = _currentUser.value?.id
        _conversationsLoading.value = true
        _conversationsFailed.value = false
        conversationsJob = viewModelScope.launch {
            try {
                val rows = gateway.list()
                if (_currentUser.value?.id == userId) _conversations.value = rows
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _conversationsFailed.value = true
            } finally {
                if (isActive) _conversationsLoading.value = false
            }
        }
    }

    fun loadMessages(conversationId: Long) {
        val gateway = conversationsGateway ?: return
        messagesJob?.cancel()
        _chat.value = ChatState(conversationId)
        messagesJob = viewModelScope.launch {
            val loaded = try {
                gateway.getMessages(conversationId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _chat.value = ChatState(conversationId, loadFailed = true)
                return@launch
            }
            _chat.value = ChatState(conversationId, messages = loaded)
            try {
                gateway.markAsRead(conversationId)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(unreadCount = 0) else it
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Keep messages visible. The inbox will refresh its unread counts on return.
            }
        }
    }

    fun sendMessage(conversationId: Long, text: String, result: (MobileAuthErrorCode?) -> Unit) {
        val gateway = conversationsGateway ?: run { result(MobileAuthErrorCode.SERVER); return }
        val userId = _currentUser.value?.id
        viewModelScope.launch {
            val newMessage = try {
                gateway.sendMessage(conversationId, text)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: MobileAuthException) {
                result(failure.code)
                return@launch
            } catch (_: Exception) {
                result(MobileAuthErrorCode.SERVER)
                return@launch
            }
            if (_currentUser.value?.id != userId) return@launch
            val current = _chat.value
            if (current?.conversationId == conversationId && current.messages != null) {
                _chat.value = current.copy(messages = (current.messages + newMessage).distinctBy { it.id })
            }
            result(null)
            loadConversations()
        }
    }

    suspend fun findPortfolioConversation(photographerSlug: String): Long? {
        val rows = checkNotNull(conversationsGateway).list()
        _conversations.value = rows
        return rows.firstOrNull { it.photographerSlug == photographerSlug }?.id
    }

    fun startConversation(photographerSlug: String, text: String, result: (Result<Long>) -> Unit) {
        runRemoteAction(result) {
            val conversation = checkNotNull(conversationsGateway).startConversation(photographerSlug, text)
            _conversations.value = listOf(conversation) + _conversations.value.filterNot { it.id == conversation.id }
            conversation.id
        }
    }

    fun loadReviews(photographerSlug: String) {
        reviewsJob?.cancel()
        val loading = PhotographerReviewsState(photographerSlug)
        _publicReviews.value = loading
        reviewsJob = viewModelScope.launch {
            try {
                val reviews = checkNotNull(reviewsGateway).getReviews(photographerSlug)
                _publicReviews.value = loading.copy(data = reviews)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _publicReviews.value = loading.copy(failed = true)
            }
        }
    }

    private fun invalidateRanking() {
        rankingJob?.cancel()
        _photographerRanking.value = PhotographerRankingState()
    }

    suspend fun getEngagementOffer(slug: String, budgetId: Long?): EngagementOffer {
        if (budgetId != null) {
            val budget = checkNotNull(budgetRequestsGateway).detail(budgetId)
            val approved = budget.proposals.firstOrNull { it.status == "accepted" }
            if (approved == null || budget.status != "accepted" || budget.photographerSlug != slug ||
                budget.photographerSpecialties.isEmpty()
            ) throw MobileAuthException(MobileAuthErrorCode.CONFLICT)
            return EngagementOffer(
                budget.photographerName, budget.photographerSpecialties, approved.priceCents, approved.id, budget
            )
        }
        val profile = publicPhotographers.get(slug).profile
        val price = profile.startingPriceCents ?: throw MobileAuthException(MobileAuthErrorCode.CONFLICT)
        return EngagementOffer(profile.name, profile.specialties, price)
    }

    suspend fun createEngagement(slug: String, reference: String, offer: EngagementOffer, draft: EngagementDraft) =
        checkNotNull(engagementGateway).create(slug, reference, offer.priceCents, offer.proposalId, draft)

    suspend fun getEngagements() = checkNotNull(engagementGateway).list()

    suspend fun getEngagement(id: Long) = checkNotNull(engagementGateway).detail(id)

    suspend fun updateEngagement(id: Long, action: EngagementAction) =
        checkNotNull(engagementGateway).update(id, action)

    suspend fun reviewEngagement(id: Long, rating: Int, comment: String) {
        checkNotNull(engagementGateway).review(id, rating, comment)
        invalidateRanking()
    }

    fun requestReviewVerification(photographerSlug: String, message: String, result: (Result<Unit>) -> Unit) {
        runRemoteAction(result) { checkNotNull(reviewsGateway).requestVerification(photographerSlug, message) }
    }

    fun loadReviewRequests(result: (Result<List<ReviewRequest>>) -> Unit) {
        runRemoteAction(result) { checkNotNull(reviewsGateway).listReviewRequests() }
    }

    fun resolveReviewRequest(id: Long, confirm: Boolean, result: (Result<Unit>) -> Unit) {
        runRemoteAction(result) { checkNotNull(reviewsGateway).resolveReviewRequest(id, confirm) }
    }

    private fun <T> runRemoteAction(result: (Result<T>) -> Unit, action: suspend () -> T) {
        viewModelScope.launch {
            val response = try {
                Result.success(action())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Result.failure(error)
            }
            result(response)
        }
    }

    suspend fun fetchAccountExport(): String = checkNotNull(accountGateway).exportData().rawJson

    fun changePassword(currentPassword: String, newPassword: String, result: (MobileAuthErrorCode?) -> Unit) {
        val gateway = accountGateway ?: run {
            result(MobileAuthErrorCode.SERVER)
            return
        }
        viewModelScope.launch {
            authenticationMutex.withLock {
                if (_authentication.value !is AuthenticationState.Authenticated) {
                    result(MobileAuthErrorCode.UNAUTHENTICATED)
                    return@withLock
                }
                val error = try {
                    gateway.changePassword(currentPassword, newPassword)
                    null
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: MobileAuthException) {
                    failure.code
                } catch (_: Exception) {
                    MobileAuthErrorCode.SERVER
                }
                result(error)
            }
        }
    }

    fun deleteAccount(password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val gateway = accountGateway ?: run { onError(localized("Serviço de conta indisponível.")); return }
        viewModelScope.launch {
            try {
                val ok = gateway.deleteAccount(password)
                if (ok) {
                    _currentUser.value = null
                    clearAccountLists()
                    _authentication.value = AuthenticationState.SignedOut
                    onSuccess()
                } else {
                    onError(localized("Não foi possível excluir a conta."))
                }
            } catch (error: MobileAuthException) {
                onError(
                    if (error.code == MobileAuthErrorCode.FORBIDDEN) localized("A senha atual está incorreta.")
                    else error.code.accountActionMessage(localized("Não foi possível excluir a conta."))
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                onError(localized("Erro de conexão ao solicitar exclusão."))
            }
        }
    }

    fun loadBudgets() {
        val gateway = budgetRequestsGateway ?: return
        val userId = _currentUser.value?.id
        viewModelScope.launch {
            try {
                val rows = gateway.list()
                if (userId == _currentUser.value?.id) _budgets.value = rows
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
            }
        }
    }

    fun loadBudgetDetail(id: Long, result: (Result<BudgetRequest>) -> Unit) {
        runRemoteAction(result) { checkNotNull(budgetRequestsGateway).detail(id) }
    }

    fun loadAgenda() {
        val gateway = budgetRequestsGateway ?: return
        val userId = _currentUser.value?.id
        viewModelScope.launch {
            try {
                val rows = gateway.agenda()
                if (userId == _currentUser.value?.id) _agenda.value = rows
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
            }
        }
    }

    fun loadFavorites() {
        val gateway = favoritesGateway ?: return
        val userId = _currentUser.value?.id
        viewModelScope.launch {
            try {
                val rows = gateway.list()
                if (userId == _currentUser.value?.id) _favorites.value = rows
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
            }
        }
    }

    fun createBudgetRequest(slug: String, draft: BudgetRequestDraft, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true
            _budgetError.value = null
            _budgetMessage.value = null
            try {
                gateway.create(slug, draft)
                loadBudgets()
                _budgetMessage.value = localized("Pedido de orçamento enviado.")
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code
                result(false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER
                result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun proposeToRequest(id: Long, priceCents: Long, conditions: String, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true
            _budgetError.value = null
            _budgetMessage.value = null
            try {
                gateway.propose(id, priceCents, conditions)
                updateBudgetStatus(id, "proposed")
                loadBudgets()
                _budgetMessage.value = localized("Proposta enviada.")
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code
                result(false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER
                result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun decideRequest(id: Long, proposalId: Long, accept: Boolean, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true
            _budgetError.value = null
            _budgetMessage.value = null
            try {
                gateway.decide(id, proposalId, accept)
                updateBudgetStatus(id, if (accept) "accepted" else "rejected")
                loadBudgets()
                _budgetMessage.value = if (accept) localized("Proposta aceita.") else localized("Proposta recusada.")
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code
                result(false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER
                result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun closeBudgetRequest(id: Long, decline: Boolean, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true
            _budgetError.value = null
            _budgetMessage.value = null
            try {
                if (decline) gateway.decline(id) else gateway.cancel(id)
                updateBudgetStatus(id, if (decline) "rejected" else "cancelled")
                loadBudgets()
                _budgetMessage.value = if (decline) localized("Pedido recusado.") else localized("Pedido cancelado.")
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code
                result(false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER
                result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    private fun updateBudgetStatus(id: Long, status: String) {
        _budgets.value = _budgets.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    fun addFavorite(slug: String, result: (Boolean) -> Unit) {
        val gateway = favoritesGateway ?: run { result(false); return }
        viewModelScope.launch {
            try {
                gateway.add(slug)
                _favorites.value = gateway.list()
                result(true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                result(false)
            }
        }
    }

    fun removeFavorite(photographerId: Long, result: (Boolean) -> Unit) {
        val gateway = favoritesGateway ?: run { result(false); return }
        viewModelScope.launch {
            try {
                gateway.remove(photographerId)
                _favorites.value = gateway.list()
                result(true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                result(false)
            }
        }
    }

    private val _professionalProfile = MutableStateFlow<ProfessionalProfileState>(ProfessionalProfileState.Loading)
    val professionalProfile: StateFlow<ProfessionalProfileState> = _professionalProfile.asStateFlow()

    fun loadProfessionalProfile() {
        val gateway = professionalProfileGateway
        if (gateway == null) {
            _professionalProfile.value = ProfessionalProfileState.Error
            return
        }
        viewModelScope.launch {
            _professionalProfile.value = ProfessionalProfileState.Loading
            _professionalProfile.value = try {
                ProfessionalProfileState.Ready(gateway.load())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                ProfessionalProfileState.Error
            }
        }
    }

    fun saveProfessionalProfile(profile: ProfessionalProfile) {
        val gateway = professionalProfileGateway ?: return
        viewModelScope.launch {
            val current = _professionalProfile.value
            if (current is ProfessionalProfileState.Ready) {
                _professionalProfile.value = current.copy(saving = true, saved = false, error = null)
            }
            _professionalProfile.value = try {
                ProfessionalProfileState.Ready(gateway.save(profile), saving = false, saved = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: MobileAuthException) {
                ProfessionalProfileState.Ready(profile, saving = false, error = error.code)
            } catch (_: Exception) {
                ProfessionalProfileState.Ready(profile, saving = false, error = MobileAuthErrorCode.SERVER)
            }
        }
    }

    private val _portfolio = MutableStateFlow<PortfolioState>(PortfolioState.Loading)
    val portfolio: StateFlow<PortfolioState> = _portfolio.asStateFlow()

    private fun portfolioReady(): PortfolioState.Ready? = _portfolio.value as? PortfolioState.Ready

    fun loadPortfolio() {
        val gateway = portfolioGateway
        if (gateway == null) {
            _portfolio.value = PortfolioState.Error
            return
        }
        viewModelScope.launch {
            _portfolio.value = PortfolioState.Loading
            _portfolio.value = try {
                PortfolioState.Ready(gateway.list())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                PortfolioState.Error
            }
        }
    }

    fun addPortfolioImage(image: ByteArray, imageContentType: String, altText: String, caption: String, category: String) {
        val gateway = portfolioGateway ?: return
        viewModelScope.launch {
            portfolioReady()?.let { _portfolio.value = it.copy(busy = true, error = null, message = null) }
            try {
                gateway.add(image, imageContentType, altText, caption, category)
                _portfolio.value = PortfolioState.Ready(gateway.list(), message = localized("Imagem adicionada."))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: MobileAuthException) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = error.code) }
            } catch (_: Exception) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = MobileAuthErrorCode.SERVER) }
            }
        }
    }

    fun updatePortfolio(items: List<PortfolioItem>) {
        val gateway = portfolioGateway ?: return
        viewModelScope.launch {
            portfolioReady()?.let { _portfolio.value = it.copy(busy = true, error = null, message = null) }
            try {
                _portfolio.value = PortfolioState.Ready(gateway.update(items), message = localized("Portfólio atualizado."))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: MobileAuthException) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = error.code) }
            } catch (_: Exception) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = MobileAuthErrorCode.SERVER) }
            }
        }
    }

    fun removePortfolioItem(id: Long) {
        val gateway = portfolioGateway ?: return
        viewModelScope.launch {
            portfolioReady()?.let { _portfolio.value = it.copy(busy = true, error = null, message = null) }
            try {
                gateway.remove(id)
                _portfolio.value = PortfolioState.Ready(gateway.list(), message = localized("Imagem removida."))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: MobileAuthException) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = error.code) }
            } catch (_: Exception) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = MobileAuthErrorCode.SERVER) }
            }
        }
    }
}
