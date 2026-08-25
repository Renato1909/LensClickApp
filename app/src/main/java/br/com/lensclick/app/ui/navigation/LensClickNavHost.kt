package br.com.lensclick.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.lensclick.app.ui.screens.auth.LoginScreen
import br.com.lensclick.app.ui.screens.auth.RegisterScreen
import br.com.lensclick.app.ui.screens.search.PhotographerProfileScreen
import br.com.lensclick.app.ui.screens.search.SearchScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home" // busca de fotógrafos
    const val PHOTOGRAPHER = "photographer/{slug}"
    const val ACCOUNT = "account"
    const val EDIT_PROFILE = "edit-profile"
    const val CHANGE_PASSWORD = "change-password"
    const val PROFESSIONAL_PROFILE = "professional-profile"
    const val DELETE_ACCOUNT = "delete-account"
    const val QUOTES = "quotes"
    const val REQUEST_QUOTE = "request-quote/{slug}"
    const val CONVERSATIONS = "conversations"
    const val THREAD = "thread/{userId}/{userName}"

    fun photographer(slug: String) = "photographer/$slug"
    fun requestQuote(slug: String) = "request-quote/$slug"
    fun thread(userId: Long, userName: String) =
        "thread/$userId/${android.net.Uri.encode(userName)}"
}

@Composable
fun LensClickNavHost(isLoggedIn: Boolean, onLogout: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.HOME else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(onGoToRegister = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            SearchScreen(navController = navController)
        }
        composable(
            route = Routes.PHOTOGRAPHER,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug").orEmpty()
            PhotographerProfileScreen(
                slug = slug,
                onBack = { navController.popBackStack() },
                onRequestQuote = { navController.navigate(Routes.requestQuote(slug)) },
                onStartChat = { userId, userName ->
                    navController.navigate(Routes.thread(userId, userName))
                }
            )
        }

        // ----- Orçamentos -----
        composable(
            route = Routes.REQUEST_QUOTE,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug").orEmpty()
            br.com.lensclick.app.ui.screens.quotes.RequestQuoteScreen(
                slug = slug,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.QUOTES) {
            br.com.lensclick.app.ui.screens.quotes.QuotesScreen(onBack = { navController.popBackStack() })
        }

        // ----- Mensagens -----
        composable(Routes.CONVERSATIONS) {
            br.com.lensclick.app.ui.screens.messages.ConversationsScreen(
                onBack = { navController.popBackStack() },
                onOpenThread = { userId, userName ->
                    navController.navigate(Routes.thread(userId, userName))
                }
            )
        }
        composable(
            route = Routes.THREAD,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val userName = backStackEntry.arguments?.getString("userName").orEmpty()
            br.com.lensclick.app.ui.screens.messages.ThreadScreen(
                withUserId = userId,
                otherUserName = android.net.Uri.decode(userName),
                onBack = { navController.popBackStack() }
            )
        }

        // ----- Minha conta -----
        composable(Routes.ACCOUNT) {
            br.com.lensclick.app.ui.screens.account.AccountScreen(
                onLogout = {
                    onLogout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        composable(Routes.EDIT_PROFILE) {
            br.com.lensclick.app.ui.screens.account.EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CHANGE_PASSWORD) {
            br.com.lensclick.app.ui.screens.account.ChangePasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFESSIONAL_PROFILE) {
            br.com.lensclick.app.ui.screens.account.ProfessionalProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DELETE_ACCOUNT) {
            br.com.lensclick.app.ui.screens.account.DeleteAccountScreen(
                onBack = { navController.popBackStack() },
                onDeleted = {
                    onLogout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
