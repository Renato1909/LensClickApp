package com.lensclick.app

import android.app.Application
import com.lensclick.app.ui.AppLanguage
import com.lensclick.app.data.local.LensClickDatabase
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.ApiEnvironment
import com.lensclick.app.data.remote.HttpAccountGateway
import com.lensclick.app.data.remote.HttpBudgetRequestsGateway
import com.lensclick.app.data.remote.HttpConversationsGateway
import com.lensclick.app.data.remote.HttpEngagementGateway
import com.lensclick.app.data.remote.HttpFavoritesGateway
import com.lensclick.app.data.remote.HttpPortfolioGateway
import com.lensclick.app.data.remote.HttpProfessionalProfileGateway
import com.lensclick.app.data.remote.HttpReviewsGateway
import com.lensclick.app.data.remote.HttpTwoFactorGateway
import com.lensclick.app.data.remote.auth.AndroidKeystoreSessionStore
import com.lensclick.app.data.remote.auth.HttpMobileAuthApi
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.UrlConnectionTransport
import com.lensclick.app.domain.AccountGateway
import com.lensclick.app.domain.BudgetRequestsGateway
import com.lensclick.app.domain.ConversationsGateway
import com.lensclick.app.domain.EngagementGateway
import com.lensclick.app.domain.FavoritesGateway
import com.lensclick.app.domain.PortfolioGateway
import com.lensclick.app.domain.ProfessionalProfileGateway
import com.lensclick.app.domain.ReviewsGateway
import com.lensclick.app.domain.TwoFactorGateway

class LensClickApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLanguage.initialize(this)
    }

    val repository: LensClickRepository by lazy {
        LensClickRepository(LensClickDatabase.getInstance(this).lensClickDao())
    }

    private val sessionStore: SessionStore by lazy { AndroidKeystoreSessionStore(this) }

    private val httpTransport: HttpTransport by lazy { UrlConnectionTransport(ApiEnvironment.baseUrl) }

    val mobileAuthRepository: MobileAuthRepository by lazy {
        MobileAuthRepository(api = HttpMobileAuthApi(httpTransport), sessionStore = sessionStore)
    }

    val professionalProfileGateway: ProfessionalProfileGateway by lazy {
        HttpProfessionalProfileGateway(httpTransport, sessionStore)
    }

    val portfolioGateway: PortfolioGateway by lazy {
        HttpPortfolioGateway(httpTransport, sessionStore)
    }

    val budgetRequestsGateway: BudgetRequestsGateway by lazy {
        HttpBudgetRequestsGateway(httpTransport, sessionStore)
    }

    val favoritesGateway: FavoritesGateway by lazy {
        HttpFavoritesGateway(httpTransport, sessionStore)
    }

    val twoFactorGateway: TwoFactorGateway by lazy {
        HttpTwoFactorGateway(httpTransport, sessionStore)
    }

    val conversationsGateway: ConversationsGateway by lazy {
        HttpConversationsGateway(httpTransport, sessionStore)
    }

    val reviewsGateway: ReviewsGateway by lazy {
        HttpReviewsGateway(httpTransport, sessionStore)
    }

    val engagementGateway: EngagementGateway by lazy {
        HttpEngagementGateway(httpTransport, sessionStore)
    }

    val accountGateway: AccountGateway by lazy {
        HttpAccountGateway(httpTransport, sessionStore)
    }
}
