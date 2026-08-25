package br.com.lensclick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.data.remote.ApiClient
import br.com.lensclick.app.data.remote.AuthRepository
import br.com.lensclick.app.ui.navigation.LensClickNavHost
import br.com.lensclick.app.ui.theme.Bg
import br.com.lensclick.app.ui.theme.LensClickTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            LensClickTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot(authRepo: AuthRepository = AuthRepository()) {
    val user by SessionManager.user.collectAsState()
    val loading by SessionManager.loading.collectAsState()

    // Restaura a sessão via cookie persistido na inicialização.
    LaunchedEffect(Unit) {
        if (user == null) {
            val restored = authRepo.me()
            if (restored != null) SessionManager.setLoggedIn(restored)
            else SessionManager.setLoggedOut()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> CircularProgressIndicator(color = br.com.lensclick.app.ui.theme.PrimaryLight)
            else -> LensClickNavHost(
                isLoggedIn = user != null,
                onLogout = {
                    SessionManager.setLoggedOut()
                    kotlinx.coroutines.runBlocking { ApiClient.cookieStorage.clearSession() }
                }
            )
        }
    }
}
