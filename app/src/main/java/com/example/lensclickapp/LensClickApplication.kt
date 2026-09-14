package com.example.lensclickapp

import android.app.Application
import com.example.lensclickapp.data.LensClickDatabase
import com.example.lensclickapp.data.LensClickRepository

class LensClickApplication : Application() {
    val repository: LensClickRepository by lazy {
        LensClickRepository(LensClickDatabase.getInstance(this).lensClickDao())
    }
}
