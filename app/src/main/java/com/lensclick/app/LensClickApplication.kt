package com.lensclick.app

import android.app.Application
import com.lensclick.app.data.local.LensClickDatabase
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.ApiEnvironment
import com.lensclick.app.data.remote.auth.AndroidKeystoreSessionStore
import com.lensclick.app.data.remote.auth.HttpMobileAuthApi
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.UrlConnectionTransport

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
