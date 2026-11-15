package com.lensclick.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lensclick.app.BuildConfig
import com.lensclick.app.LensClickApplication
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.HttpPublicPhotographersGateway
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographerNotFoundException
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.data.local.User
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.domain.MobileUser
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.ProfessionalProfileGateway
import com.lensclick.app.domain.PortfolioItem
import com.lensclick.app.domain.PortfolioGateway
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.BudgetRequestsGateway
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.FavoritesGateway
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

class LensClickViewModel(
    private val repository: LensClickRepository,
    private val publicPhotographers: PublicPhotographersGateway = HttpPublicPhotographersGateway(),
    private val mobileAuthRepository: MobileAuthRepository? = null,
    private val professionalProfileGateway: ProfessionalProfileGateway? = null,
    private val portfolioGateway: PortfolioGateway? = null,
    private val budgetRequestsGateway: BudgetRequestsGateway? = null,
    private val favoritesGateway: FavoritesGateway? = null
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
                    favoritesGateway = application.favoritesGateway
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

    fun loadBudgets() {
        val gateway = budgetRequestsGateway ?: return
        viewModelScope.launch {
            try {
                _budgets.value = gateway.list()
            } catch (_: Exception) {
            }
        }
    }

    fun loadAgenda() {
        val gateway = budgetRequestsGateway ?: return
        viewModelScope.launch {
            try {
                _agenda.value = gateway.agenda()
            } catch (_: Exception) {
            }
        }
    }

    fun loadFavorites() {
        val gateway = favoritesGateway ?: return
        viewModelScope.launch {
            try {
                _favorites.value = gateway.list()
            } catch (_: Exception) {
            }
        }
    }

    fun createBudgetRequest(slug: String, draft: BudgetRequestDraft, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true; _budgetError.value = null; _budgetMessage.value = null
            try {
                gateway.create(slug, draft)
                _budgets.value = gateway.list()
                _budgetMessage.value = "Pedido de orçamento enviado."
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code; result(false)
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER; result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun proposeToRequest(id: Long, priceCents: Long, conditions: String, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true; _budgetError.value = null; _budgetMessage.value = null
            try {
                gateway.propose(id, priceCents, conditions)
                _budgets.value = gateway.list()
                _budgetMessage.value = "Proposta enviada."
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code; result(false)
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER; result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun decideRequest(id: Long, proposalId: Long, accept: Boolean, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true; _budgetError.value = null; _budgetMessage.value = null
            try {
                gateway.decide(id, proposalId, accept)
                _budgets.value = gateway.list()
                _agenda.value = gateway.agenda()
                _budgetMessage.value = if (accept) "Proposta aceita." else "Proposta recusada."
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code; result(false)
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER; result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun cancelRequest(id: Long, result: (Boolean) -> Unit) {
        val gateway = budgetRequestsGateway ?: run { result(false); return }
        viewModelScope.launch {
            _budgetBusy.value = true; _budgetError.value = null; _budgetMessage.value = null
            try {
                gateway.cancel(id)
                _budgets.value = gateway.list()
                _budgetMessage.value = "Pedido cancelado."
                result(true)
            } catch (error: MobileAuthException) {
                _budgetError.value = error.code; result(false)
            } catch (_: Exception) {
                _budgetError.value = MobileAuthErrorCode.SERVER; result(false)
            } finally {
                _budgetBusy.value = false
            }
        }
    }

    fun addFavorite(slug: String, result: (Boolean) -> Unit) {
        val gateway = favoritesGateway ?: run { result(false); return }
        viewModelScope.launch {
            try {
                gateway.add(slug)
                _favorites.value = gateway.list()
                result(true)
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
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
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
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
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
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
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
                _portfolio.value = PortfolioState.Ready(gateway.list(), message = "Imagem adicionada.")
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
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
                _portfolio.value = PortfolioState.Ready(gateway.update(items), message = "Portfólio atualizado.")
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
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
                _portfolio.value = PortfolioState.Ready(gateway.list(), message = "Imagem removida.")
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: MobileAuthException) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = error.code) }
            } catch (_: Exception) {
                portfolioReady()?.let { _portfolio.value = it.copy(busy = false, error = MobileAuthErrorCode.SERVER) }
            }
        }
    }
}
