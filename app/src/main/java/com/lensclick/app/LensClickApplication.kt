package com.lensclick.app

import android.app.Application
import com.lensclick.app.data.LensClickDatabase
import com.lensclick.app.data.LensClickRepository
import com.lensclick.app.data.ApiEnvironment
import com.lensclick.app.data.auth.AndroidKeystoreSessionStore
import com.lensclick.app.data.auth.HttpMobileAuthApi
import com.lensclick.app.data.auth.MobileAuthRepository
import com.lensclick.app.data.auth.UrlConnectionTransport

class LensClickApplication : Application() {
    val repository: LensClickRepository by lazy {
        LensClickRepository(LensClickDatabase.getInstance(this).lensClickDao())
    }

    val mobileAuthRepository: MobileAuthRepository by lazy {
        MobileAuthRepository(
            api = HttpMobileAuthApi(UrlConnectionTransport(ApiEnvironment.baseUrl)),
            sessionStore = AndroidKeystoreSessionStore(this)
        )
    }
}
