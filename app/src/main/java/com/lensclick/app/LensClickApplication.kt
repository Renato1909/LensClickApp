package com.lensclick.app

import android.app.Application
import com.lensclick.app.data.local.LensClickDatabase
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.ApiEnvironment
import com.lensclick.app.data.remote.HttpPortfolioGateway
import com.lensclick.app.data.remote.HttpProfessionalProfileGateway
import com.lensclick.app.data.remote.auth.AndroidKeystoreSessionStore
import com.lensclick.app.data.remote.auth.HttpMobileAuthApi
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.UrlConnectionTransport
import com.lensclick.app.domain.PortfolioGateway
import com.lensclick.app.domain.ProfessionalProfileGateway

class LensClickApplication : Application() {
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
}
