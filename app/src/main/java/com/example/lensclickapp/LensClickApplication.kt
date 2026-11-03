package com.example.lensclickapp

import android.app.Application
import com.example.lensclickapp.data.LensClickDatabase
import com.example.lensclickapp.data.LensClickRepository
import com.example.lensclickapp.data.ApiEnvironment
import com.example.lensclickapp.data.auth.AndroidKeystoreSessionStore
import com.example.lensclickapp.data.auth.HttpMobileAuthApi
import com.example.lensclickapp.data.auth.MobileAuthRepository
import com.example.lensclickapp.data.auth.UrlConnectionTransport

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
