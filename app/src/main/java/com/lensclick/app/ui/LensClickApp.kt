package com.lensclick.app.ui

import android.widget.DatePicker
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lensclick.app.BuildConfig
import com.lensclick.app.R
import com.lensclick.app.data.local.FairPhotographer
import com.lensclick.app.data.local.Photographer
import com.lensclick.app.data.local.User
import com.lensclick.app.data.local.readFairPhotographers
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.loadPublicPortfolioPreview
import com.lensclick.app.util.TotpQrCode
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.PortfolioItem
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.ui.theme.LensCoffee as Accent
import com.lensclick.app.ui.theme.LensFieldBorder as FieldBorder
import com.lensclick.app.ui.theme.LensInk as Ink
import com.lensclick.app.ui.theme.LensLine as Line
import com.lensclick.app.ui.theme.LensMuted as Muted
import com.lensclick.app.ui.theme.LensOnCoffee as OnCoffee
import com.lensclick.app.ui.theme.LensPaper as Paper
import com.lensclick.app.ui.theme.LensSand as Sand
import com.lensclick.app.ui.theme.LensSurface as SurfaceHigh
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen {
    Onboarding, AccountType, Login, SignUp, PhotographerSignUp,
    Home, Search, Budgets, Conversations, Account,
    ProDashboard, ProRequests, ProAgenda, ProProfile, ProPortfolio,
    Settings, AccountData, AccountSecurity, Notifications, Support, About, ReviewRequests,
    PhotographerRanking, Engagements, DemoBudgets
}
private const val SearchRoute = "Search?specialty={specialty}"
private const val PhotographerRoute = "Photographer/{slug}"
private const val QuoteRoute = "Quote/{slug}"
private const val QuoteForHireRoute = "QuoteForHire/{slug}"
private const val PendingClientRoute = "pendingClientRoute"
private const val PendingPortfolioAction = "pendingPortfolioAction"
private const val HireRoute = "Hire/{slug}?budget={budget}"
private const val EngagementRoute = "Engagement/{id}?created={created}"
private const val ReviewEngagementsRoute = "ReviewEngagements/{slug}"
private const val ChatRoute = "Chat/{conversationId}"
private const val BudgetDetailRoute = "BudgetDetail/{id}"
private const val FairCatalogRoute = "Feira"
private const val FairProfileRoute = "Feira/{slug}"
private val GuestBrowsingScreens = setOf(Screen.Home, Screen.Search, Screen.Budgets, Screen.Conversations, Screen.Account)
private val ResumeAfterLogin = setOf(Screen.Budgets, Screen.Conversations, Screen.Account)
private val AuthenticationScreens = setOf(Screen.Onboarding, Screen.AccountType, Screen.Login, Screen.SignUp, Screen.PhotographerSignUp)
private val PublicRoutes = (AuthenticationScreens + GuestBrowsingScreens).map { it.name }.toSet() +
    setOf(SearchRoute, PhotographerRoute, FairCatalogRoute, FairProfileRoute, Screen.PhotographerRanking.name, Screen.DemoBudgets.name)

@Composable
fun LensClickApp(viewModel: LensClickViewModel = viewModel(factory = LensClickViewModel.Factory)) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val fairPhotographers = remember(context, AppLanguage.current) { readFairPhotographers(context) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentEntry by navController.currentBackStackEntryAsState()
    val photographers by viewModel.photographers.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val agenda by viewModel.agenda.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authentication by viewModel.authentication.collectAsStateWithLifecycle()
    val finishingPhotographerRegistration by viewModel.finishingPhotographerRegistration.collectAsStateWithLifecycle()
    val currentPhotographer = photographers.firstOrNull { it.userId == currentUser?.id }
    fun resetToRoute(route: String) {
        focusManager.clearFocus()
        keyboardController?.hide()
        navController.navigate(route) {
            popUpTo(navController.graph.id)
            launchSingleTop = true
        }
    }
    fun resetTo(target: Screen) = resetToRoute(target.name)

    fun openGuestTab(route: String, restoreTabState: Boolean = true) {
        focusManager.clearFocus()
        keyboardController?.hide()
        navController.navigate(route) {
            popUpTo(Screen.Onboarding.name) { saveState = true }
            restoreState = restoreTabState
            launchSingleTop = true
        }
    }

    fun go(target: Screen) {
        when {
            currentUser == null && target in GuestBrowsingScreens -> openGuestTab(target.name)
            target in GuestBrowsingScreens || target in setOf(
                Screen.ProDashboard, Screen.ProRequests, Screen.ProAgenda, Screen.ProProfile, Screen.ProPortfolio
            ) -> resetTo(target)
            else -> {
                focusManager.clearFocus()
                keyboardController?.hide()
                navController.navigate(target.name) {
                    launchSingleTop = true
                }
            }
        }
    }

    fun openConversation(id: Long) {
        navController.navigate("Chat/$id") { launchSingleTop = true }
    }

    fun openPublicPhotographer(slug: String) {
        navController.navigate("Photographer/${Uri.encode(slug)}") { launchSingleTop = true }
    }

    fun openClientRoute(entry: NavBackStackEntry, route: String) {
        if (currentUser == null) {
            entry.savedStateHandle[PendingPortfolioAction] = null
            entry.savedStateHandle[PendingClientRoute] = route
            go(Screen.Login)
        } else {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

    fun openHire(entry: NavBackStackEntry, slug: String, hasPrice: Boolean) {
        val encodedSlug = Uri.encode(slug)
        openClientRoute(entry, if (hasPrice) "Hire/$encodedSlug" else "QuoteForHire/$encodedSlug")
    }

    val onBack: () -> Unit = { navController.popBackStack() }

    LaunchedEffect(currentEntry?.destination?.route, authentication) {
        val route = currentEntry?.destination?.route ?: return@LaunchedEffect
        if (authentication is AuthenticationState.Authenticated &&
            AuthenticationScreens.any { it.name == route }
        ) {
            val user = (authentication as AuthenticationState.Authenticated).user
            val previous = navController.previousBackStackEntry
            val pendingRoute = previous?.savedStateHandle?.remove<String>(PendingClientRoute)
            val pendingAction = previous?.savedStateHandle?.get<String>(PendingPortfolioAction)
            if (route == Screen.Login.name && user.role == "client" && (pendingRoute != null || pendingAction != null)) {
                navController.popBackStack()
                if (pendingRoute != null) navController.navigate(pendingRoute)
                return@LaunchedEffect
            }
            val returnTab = ResumeAfterLogin.firstOrNull { it.name == previous?.destination?.route }
            val destination = when {
                user.role == "photographer" -> Screen.ProDashboard
                route == Screen.Login.name && returnTab != null -> returnTab
                else -> Screen.Home
            }
            resetTo(destination)
        } else if (authentication == AuthenticationState.LogoutCompleted && route == Screen.Account.name) {
            resetTo(Screen.Login)
        } else if (authentication is AuthenticationState.Error && route == Screen.Onboarding.name) {
            resetTo(Screen.Login)
        } else if (authentication !is AuthenticationState.Authenticated &&
            authentication !is AuthenticationState.TwoFactorRequired &&
            authentication !is AuthenticationState.Loading && route !in PublicRoutes) {
            resetTo(Screen.Login)
        }
    }

    LaunchedEffect(authentication) {
        if (authentication is AuthenticationState.Authenticated) {
            viewModel.loadBudgets()
            viewModel.loadFavorites()
            viewModel.loadAgenda()
            viewModel.loadConversations()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
        NavHost(navController = navController, startDestination = Screen.Onboarding.name) {
            composable(Screen.Onboarding.name) { OnboardingScreen(onStart = { go(Screen.AccountType) }, onLogin = { go(Screen.Login) }, onBrowse = { go(Screen.Search) }) }
            composable(Screen.AccountType.name) { AccountTypeScreen(
                onClient = { go(Screen.SignUp) },
                onPhotographer = { go(Screen.PhotographerSignUp) },
                onLogin = { go(Screen.Login) }
            ) }
            composable(Screen.Login.name) { LoginScreen(
                authentication = authentication,
                onLogin = { email, password -> viewModel.login(email, password) },
                onVerify2Fa = viewModel::verifyTwoFactorChallenge,
                onCancel2Fa = viewModel::cancelTwoFactorChallenge,
                onSignUp = { go(Screen.AccountType) }
            ) }
            composable(Screen.SignUp.name) { SignUpScreen(
                error = (authentication as? AuthenticationState.Error)?.code?.registrationMessage(),
                onDone = { name, email, password, result ->
                    viewModel.register(name, email, password, result)
                },
                onLogin = { viewModel.clearAuthenticationError(); go(Screen.Login) }
            ) }
            composable(Screen.PhotographerSignUp.name) { PhotographerSignUpScreen(
                error = (authentication as? AuthenticationState.Error)?.code?.registrationMessage(),
                finishingRegistration = finishingPhotographerRegistration,
                onDone = { name, email, password, phone, profile, result ->
                    viewModel.registerPhotographer(name, email, password, phone, profile, result)
                },
                onLogin = { viewModel.clearAuthenticationError(); go(Screen.Login) }
            ) }
            composable(Screen.Home.name) { entry ->
                HomeScreen(
                    viewModel = viewModel,
                    photographers = fairPhotographers,
                    onNavigate = ::go,
                    onCategory = { specialty ->
                        val route = "Search?specialty=${Uri.encode(specialty)}"
                        if (currentUser == null) openGuestTab(route, restoreTabState = false)
                        else resetToRoute(route)
                    },
                    onRankedProfile = ::openPublicPhotographer,
                    onRankedRequest = if (currentUser?.role == "photographer") null else { photographer ->
                        if (photographer.startingPriceCents != null) openHire(entry, photographer.slug, hasPrice = true)
                        else openClientRoute(entry, "Quote/${Uri.encode(photographer.slug)}")
                    },
                    onFair = { navController.navigate(FairCatalogRoute) },
                    onFairPhotographer = { slug -> navController.navigate("Feira/$slug") }
                )
            }
            composable(
                SearchRoute,
                arguments = listOf(navArgument("specialty") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entry ->
                SearchScreen(
                    viewModel,
                    guest = currentUser == null,
                    initialSpecialty = entry.arguments?.getString("specialty"),
                    fairPhotographers = fairPhotographers,
                    onNavigate = ::go,
                    onPhotographer = ::openPublicPhotographer,
                    onRankedProfile = ::openPublicPhotographer,
                    onRankedRequest = if (currentUser?.role == "photographer") null else { photographer ->
                        if (photographer.startingPriceCents != null) openHire(entry, photographer.slug, hasPrice = true)
                        else openClientRoute(entry, "Quote/${Uri.encode(photographer.slug)}")
                    },
                    onFair = { navController.navigate(FairCatalogRoute) },
                    onFairPhotographer = { slug -> navController.navigate("Feira/$slug") }
                )
            }
            composable(Screen.PhotographerRanking.name) {
                // Resume old back stacks in discovery, where the ranking is now shown inline.
                LaunchedEffect(Unit) {
                    if (currentUser == null) openGuestTab(Screen.Search.name, restoreTabState = false)
                    else resetTo(Screen.Search)
                }
            }
            composable(FairCatalogRoute) {
                FairCatalogScreen(
                    photographers = fairPhotographers,
                    onBack = { navController.popBackStack() },
                    onPhotographer = { slug -> navController.navigate("Feira/$slug") }
                )
            }
            composable(FairProfileRoute) { entry ->
                val slug = entry.arguments?.getString("slug")
                val photographer = fairPhotographers.firstOrNull { it.summary.slug == slug }
                if (photographer == null) {
                    ProfileStatus(localized("Perfil de demonstração indisponível."), localized("Voltar")) { navController.popBackStack() }
                } else {
                    val localDemo = BuildConfig.DEBUG && currentUser?.role != "photographer"
                    FairProfileScreen(
                        photographer, onBack = onBack,
                        onHire = if (localDemo) ({ openHire(entry, photographer.summary.slug, hasPrice = true) }) else null,
                        onQuote = if (localDemo) ({
                            openClientRoute(entry, "Quote/${Uri.encode(photographer.summary.slug)}")
                        }) else null,
                        contactActions = {
                            if (localDemo) {
                                PortfolioContactActions(
                                    viewModel, entry, photographer.summary.slug, photographer.summary.name,
                                    currentUser, { go(Screen.Login) }, ::openConversation
                                )
                            }
                        }
                    )
                }
            }
            composable(PhotographerRoute) { entry ->
                val slug = entry.arguments?.getString("slug").orEmpty()
                PhotographerScreen(
                    viewModel, slug, currentUser, onBack,
                    onQuote = { openClientRoute(entry, "Quote/${Uri.encode(slug)}") },
                    onHire = { hasPrice -> openHire(entry, slug, hasPrice) },
                    onReview = { openClientRoute(entry, "ReviewEngagements/${Uri.encode(slug)}") },
                    contactActions = { name ->
                        PortfolioContactActions(viewModel, entry, slug, name, currentUser, { go(Screen.Login) }, ::openConversation)
                    }
                )
            }
            composable(HireRoute, arguments = listOf(navArgument("budget") {
                type = NavType.LongType
                defaultValue = 0L
            })) { entry ->
                val args = requireNotNull(entry.arguments)
                HireScreen(viewModel, args.getString("slug").orEmpty(), args.getLong("budget").takeIf { it > 0 }, onBack) { id ->
                    navController.navigate("Engagement/$id?created=true") { popUpTo(HireRoute) { inclusive = true } }
                }
            }
            composable(Screen.Engagements.name) {
                EngagementsScreen(viewModel, currentUser?.role == "photographer", null, onBack) { id ->
                    navController.navigate("Engagement/$id")
                }
            }
            composable(ReviewEngagementsRoute) { entry ->
                EngagementsScreen(viewModel, false, entry.arguments?.getString("slug"), onBack) { id ->
                    navController.navigate("Engagement/$id")
                }
            }
            composable(EngagementRoute, arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("created") { type = NavType.BoolType; defaultValue = false }
            )) { entry ->
                val args = requireNotNull(entry.arguments)
                EngagementDetailsScreen(viewModel, args.getLong("id"), currentUser?.role == "photographer", args.getBoolean("created"), onBack)
            }
            composable(Screen.DemoBudgets.name) { DemoBudgetsScreen(onBack) }
            listOf(QuoteRoute to false, QuoteForHireRoute to true).forEach { (route, forHire) ->
                composable(route) { entry ->
                    val slug = entry.arguments?.getString("slug").orEmpty()
                    QuoteScreen(
                        forHire = forHire,
                        onBack = onBack,
                        onSend = { draft, completed ->
                            viewModel.createBudgetRequest(slug, draft) { success ->
                                if (success) {
                                    if (navController.currentBackStackEntry == entry) go(Screen.Budgets)
                                } else {
                                    completed(viewModel.budgetError.value ?: MobileAuthErrorCode.SERVER)
                                }
                            }
                        }
                    )
                }
            }
            composable(Screen.Budgets.name) {
                BudgetsScreen(
                    viewModel = viewModel,
                    budgets = budgets,
                    favorites = favorites,
                    signedIn = authentication is AuthenticationState.Authenticated,
                    onNavigate = ::go,
                    onLogin = { go(Screen.Login) },
                    onBudget = { id -> navController.navigate("BudgetDetail/$id") { launchSingleTop = true } },
                    onEngagements = { go(Screen.Engagements) },
                    onDemo = { go(Screen.DemoBudgets) },
                    onPhotographer = ::openPublicPhotographer
                )
            }
            composable(BudgetDetailRoute, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                val id = requireNotNull(entry.arguments).getLong("id")
                BudgetDetailScreen(viewModel, id, canRespond = currentUser?.role == "client", onBack) { slug ->
                    navController.navigate("Hire/${Uri.encode(slug)}?budget=$id")
                }
            }
            composable(Screen.Conversations.name) {
                ConversationsScreen(
                    viewModel,
                    signedIn = authentication is AuthenticationState.Authenticated,
                    onConversation = ::openConversation,
                    onNavigate = ::go,
                    onLogin = { go(Screen.Login) }
                )
            }
            // Earlier versions saved the chat destination without a conversation ID.
            composable("Chat") {
                LaunchedEffect(Unit) { go(Screen.Conversations) }
            }
            composable(ChatRoute, arguments = listOf(navArgument("conversationId") { type = NavType.LongType })) { entry ->
                val id = requireNotNull(entry.arguments).getLong("conversationId")
                ChatScreen(viewModel, id, onBack)
            }
            composable(Screen.Settings.name) {
                SettingsScreen(viewModel, onBack, ::go, onProfessionalActivated = { resetTo(Screen.ProDashboard) })
            }
            composable(Screen.AccountData.name) { AccountDataScreen(viewModel, onBack) }
            composable(Screen.AccountSecurity.name) { AccountSecurityScreen(viewModel, onBack) }
            composable(Screen.Notifications.name) { NotificationsScreen(viewModel, onBack, ::openConversation) }
            composable(Screen.Support.name) { SupportScreen(onBack) }
            composable(Screen.About.name) { AboutScreen(onBack) }
            composable(Screen.ReviewRequests.name) { ReviewRequestsScreen(viewModel, onBack) }
            composable(Screen.Account.name) { AccountScreen(
                user = currentUser,
                authentication = authentication,
                canUseProfessionalMode = currentUser?.role == "photographer",
                onNavigate = ::go,
                onLogin = { go(Screen.Login) },
                onProfessionalMode = { resetTo(Screen.ProDashboard) },
                onLogout = { viewModel.logout() }
            ) }
            composable(Screen.ProDashboard.name) { PhotographerDashboardScreen(currentPhotographer, budgets, ::go, onClientMode = { resetTo(Screen.Home) }) }
            composable(Screen.ProRequests.name) { PhotographerRequestsScreen(viewModel, budgets, ::go) { id ->
                navController.navigate("BudgetDetail/$id") { launchSingleTop = true }
            } }
            composable(Screen.ProAgenda.name) { PhotographerAgendaScreen(agenda, ::go) }
            composable(Screen.ProProfile.name) { ProfessionalProfileScreen(viewModel, ::go, onClientMode = { resetTo(Screen.Home) }, onLogout = { viewModel.logout() }) }
            composable(Screen.ProPortfolio.name) { PortfolioScreen(viewModel, ::go, ::openPublicPhotographer) }
        }
    }
}

@Composable
internal fun BrandMark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "LensClick" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompactLogo()
        Text(
            "LensClick",
            color = Ink,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun CompactLogo(sizeDp: Int = 34) {
    Image(
        painter = painterResource(R.drawable.lens_click_mark),
        contentDescription = null,
        modifier = Modifier.size(sizeDp.dp),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(Ink)
    )
}

@Composable
private fun BrandPhoto(modifier: Modifier) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Sand)
            .padding(12.dp)
            .border(1.dp, Accent.copy(alpha = 0.22f), MaterialTheme.shapes.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.lens_click_mark),
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            colorFilter = ColorFilter.tint(Accent.copy(alpha = 0.50f))
        )
    }
}

@Composable
private fun GuestPage(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            content = content
        )
    }
}

@Composable
private fun OnboardingScreen(onStart: () -> Unit, onLogin: () -> Unit, onBrowse: () -> Unit) {
    GuestPage {
        BrandMark()
        HorizontalDivider(Modifier.padding(top = 14.dp, bottom = 22.dp), color = Line)
        Text(
            buildAnnotatedString {
                append(localized("Encontre o\n"))
                withStyle(SpanStyle(color = Accent, fontStyle = FontStyle.Italic)) {
                    append(localized("fotógrafo ideal."))
                }
            },
            color = Ink,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            localized("Conheça fotógrafos profissionais, explore portfólios e peça orçamentos."),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
        )
        Image(
            painter = painterResource(R.drawable.editorial_camera),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0.65f, 0f),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.58f)
                .clip(MaterialTheme.shapes.medium)
                .background(Ink)
        )
        Spacer(Modifier.height(22.dp))
        PrimaryButton(localized("Explorar fotógrafos"), onBrowse)
        Spacer(Modifier.height(8.dp))
        SecondaryButton(localized("Criar conta"), onStart)
        CenterLink(localized("Já tem uma conta?"), localized("Entrar"), onLogin)
    }
}

@Composable
private fun AccountTypeScreen(onClient: () -> Unit, onPhotographer: () -> Unit, onLogin: () -> Unit) {
    AuthFrame(localized("Como você deseja usar o LensClick?"), localized("Escolha o tipo de conta para continuar.")) {
        AccountTypeOption(localized("Sou Cliente"), localized("Quero contratar serviços fotográficos"), Icons.Default.Person, onClient)
        Spacer(Modifier.height(12.dp))
        AccountTypeOption(localized("Sou Fotógrafo"), localized("Quero divulgar meu trabalho e receber orçamentos"), Icons.Default.Add, onPhotographer)
        Spacer(Modifier.height(12.dp))
        CenterLink(localized("Já possui conta?"), localized("Entrar"), onLogin)
    }
}

@Composable
private fun AccountTypeOption(title: String, detail: String, icon: ImageVector, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceHigh)
            .border(1.dp, Line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).background(Sand, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AuthFrame(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    GuestPage {
        BrandMark()
        HorizontalDivider(Modifier.padding(top = 14.dp, bottom = 24.dp), color = Line)
        PageTitle(title)
        Text(
            subtitle,
            color = Muted,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )
        Column(content = content)
    }
}

@Composable
private fun LoginScreen(
    authentication: AuthenticationState,
    onLogin: (String, String) -> Unit,
    onVerify2Fa: (String) -> Unit,
    onCancel2Fa: () -> Unit,
    onSignUp: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val challenge = authentication as? AuthenticationState.TwoFactorRequired
    var totpCode by remember(challenge?.challengeToken) { mutableStateOf("") }
    val context = LocalContext.current
    var recoveryFailed by remember { mutableStateOf(false) }
    val submitting = authentication is AuthenticationState.Loading
    val is2FA = challenge != null
    val errorMessage = when (authentication) {
        is AuthenticationState.Error -> authentication.code.loginMessage()
        AuthenticationState.SessionExpired -> localized("Sua sessão expirou ou foi revogada. Entre novamente.")
        else -> null
    }
    val completedMessage = authentication == AuthenticationState.LogoutCompleted

    BackHandler(enabled = is2FA, onBack = onCancel2Fa)
    AuthFrame(if (is2FA) localized("Verificação 2FA") else localized("Bem-vindo de volta!"), if (is2FA) localized("Digite o código do seu autenticador") else localized("Entre para continuar")) {
        if (challenge != null) {
            Text(localized("Insira o código de 6 dígitos do app TOTP ou um código de recuperação."), color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            LensField(localized("Código TOTP / Recuperação"), totpCode, { totpCode = it }, KeyboardType.Ascii)
            Spacer(Modifier.height(20.dp))
            if (challenge.verifying) {
                AuthStatusBanner(localized("Verificando o código..."), loading = true)
            } else if (challenge.error != null) {
                AuthStatusBanner(challenge.error.twoFactorMessage(), loading = false, error = true)
            }
            PrimaryButton(localized("Confirmar código"), {
                onVerify2Fa(totpCode)
                totpCode = ""
            }, enabled = totpCode.isNotBlank() && !challenge.verifying)
            TextButton(onClick = onCancel2Fa, enabled = !challenge.verifying) { Text(localized("Voltar ao login")) }
        } else {
            LensField(localized("E-mail"), email, { email = it }, KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            LensField(localized("Senha"), password, { password = it }, password = true)
            TextButton(
                onClick = { recoveryFailed = !context.openLensClickPage("/recuperar-senha") },
                enabled = !submitting,
                contentPadding = PaddingValues(0.dp)
            ) { Text(localized("Esqueci minha senha"), color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
            if (recoveryFailed) AuthStatusBanner(localized("Não foi possível abrir a página de recuperação."), loading = false, error = true)
            Spacer(Modifier.height(26.dp))
            if (submitting) {
                AuthStatusBanner(localized("Conectando à sua conta..."), loading = true)
            } else if (errorMessage != null) {
                AuthStatusBanner(errorMessage, loading = false, error = true)
            } else if (completedMessage) {
                AuthStatusBanner(localized("Você saiu da conta com segurança."), loading = false)
            }
            PrimaryButton(if (submitting) localized("Entrando...") else localized("Entrar"), {
                onLogin(email, password)
                password = ""
            }, enabled = email.isNotBlank() && password.isNotBlank() && !submitting)
            Spacer(Modifier.height(12.dp))
            CenterLink(localized("Não tem uma conta?"), localized("Cadastre-se"), onSignUp)
        }
    }

}

private fun MobileAuthErrorCode.loginMessage(): String = when (this) {
    MobileAuthErrorCode.INVALID_INPUT,
    MobileAuthErrorCode.UNAUTHENTICATED -> localized("E-mail ou senha inválidos.")
    MobileAuthErrorCode.INVALID_TWO_FACTOR_CHALLENGE -> localized("O desafio de verificação expirou. Entre novamente.")
    MobileAuthErrorCode.RATE_LIMITED -> localized("Muitas tentativas. Aguarde antes de tentar novamente.")
    MobileAuthErrorCode.NETWORK -> localized("Não foi possível conectar. Verifique sua internet.")
    MobileAuthErrorCode.SERVER -> localized("O serviço está indisponível. Tente novamente.")
    else -> localized("Não foi possível entrar. Tente novamente.")
}

private fun MobileAuthErrorCode.registrationMessage(): String = when (this) {
    MobileAuthErrorCode.CONFLICT -> localized("Este e-mail já está cadastrado. Entre na sua conta ou use outro e-mail.")
    MobileAuthErrorCode.INVALID_INPUT -> localized("Confira os dados informados e tente novamente.")
    MobileAuthErrorCode.RATE_LIMITED -> localized("Muitas tentativas. Aguarde alguns minutos antes de tentar novamente.")
    MobileAuthErrorCode.NETWORK -> localized("Não foi possível conectar. Verifique sua internet e tente novamente.")
    MobileAuthErrorCode.SERVER -> localized("O serviço está indisponível. Tente novamente em instantes.")
    else -> localized("Não foi possível criar a conta. Tente novamente.")
}

private fun MobileAuthErrorCode.twoFactorMessage(): String = when (this) {
    MobileAuthErrorCode.INVALID_INPUT -> localized("Código inválido. Confira o código e tente novamente.")
    MobileAuthErrorCode.FORBIDDEN -> localized("A senha atual está incorreta.")
    MobileAuthErrorCode.UNAUTHENTICATED -> localized("Sua sessão expirou. Entre novamente.")
    MobileAuthErrorCode.NETWORK -> localized("Não foi possível conectar. Verifique sua internet.")
    MobileAuthErrorCode.RATE_LIMITED -> localized("Muitas tentativas. Aguarde antes de tentar novamente.")
    else -> localized("Não foi possível concluir a verificação. Tente novamente.")
}

@Composable
internal fun AuthStatusBanner(message: String, loading: Boolean, error: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (error) MaterialTheme.colorScheme.errorContainer else SurfaceHigh)
            .border(1.dp, if (error) MaterialTheme.colorScheme.error else Line, RoundedCornerShape(12.dp))
            .semantics {
                liveRegion = if (error) LiveRegionMode.Assertive else LiveRegionMode.Polite
                stateDescription = message
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Ink
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            message,
            color = if (error) MaterialTheme.colorScheme.onErrorContainer else Muted,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SignUpScreen(
    error: String? = null,
    onDone: (String, String, String, (Boolean) -> Unit) -> Unit,
    onLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by rememberSaveable { mutableStateOf(false) }
    AuthFrame(localized("Criar conta de cliente"), localized("Preencha seus dados para começar")) {
        LensField(localized("Nome completo"), name, { name = it })
        Spacer(Modifier.height(12.dp))
        LensField(localized("E-mail"), email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        LensField(localized("Senha (mínimo 8 caracteres)"), password, { password = it }, password = true)
        Spacer(Modifier.height(16.dp))
        if (error != null) {
            AuthStatusBanner(error, loading = false, error = true)
            Spacer(Modifier.height(10.dp))
        }
        PrimaryButton(localized("Criar minha conta"), {
            busy = true
            onDone(name, email, password) { busy = false }
        }, enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 8 && !busy)
        Spacer(Modifier.height(12.dp))
        CenterLink(localized("Já tem conta?"), localized("Entrar"), onLogin)
    }
}

@Composable
private fun PhotographerSignUpScreen(
    error: String? = null,
    finishingRegistration: Boolean,
    onDone: (String, String, String, String, ProfessionalProfile, (Boolean) -> Unit) -> Unit,
    onLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("wedding") }
    var city by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val priceCents = parsePriceCents(price)
    AuthFrame(localized("Criar conta de Fotógrafo"), localized("Cadastre seu perfil profissional no LensClick")) {
        LensField(localized("Nome profissional"), name, { name = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("E-mail"), email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        LensField(localized("Telefone"), phone, { phone = it }, KeyboardType.Phone)
        Spacer(Modifier.height(10.dp))
        LensField(localized("Senha (mín. 8 caracteres)"), password, { password = it }, password = true)
        Spacer(Modifier.height(10.dp))
        Text(localized("Especialidade principal"), color = Ink, style = MaterialTheme.typography.labelLarge)
        Row(
            Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("wedding", "portrait", "events", "family", "corporate", "product", "fashion", "newborn", "nature", "real-estate").forEach { option ->
                FilterChip(specialtyLabel(option), selected = option == specialty) { specialty = option }
            }
        }
        Spacer(Modifier.height(10.dp))
        LensField(localized("Cidade de atuação"), city, { city = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("Preço inicial (R$)"), price, { price = it }, KeyboardType.Decimal)
        if (price.isNotBlank() && priceCents == null) Text(localized("Informe um valor válido, como 1200,50."), color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(10.dp))
        LensField(localized("Apresentação / Bio"), bio, { bio = it }, singleLine = false, maxLines = 3)
        Spacer(Modifier.height(16.dp))
        if (busy) AuthStatusBanner(localized("Configurando conta profissional…"), loading = true)
        if (error != null && !busy) {
            AuthStatusBanner(if (finishingRegistration) localized("Sua conta foi criada. Confira os dados e tente concluir o perfil profissional.") else error, loading = false, error = true)
            Spacer(Modifier.height(10.dp))
        }
        PrimaryButton(if (finishingRegistration) localized("Concluir cadastro profissional") else localized("Cadastrar como Fotógrafo"), {
            busy = true
            onDone(name, email, password, phone, ProfessionalProfile(
                bio = bio.trim(),
                specialties = listOf(specialty),
                city = city.trim(),
                startingPriceCents = priceCents
            )) { busy = false }
        }, enabled = name.trim().length >= 2 && email.isNotBlank() &&
            (password.length >= 8 || finishingRegistration) && phone.trim().length in 8..30 &&
            city.trim().length >= 2 && (price.isBlank() || priceCents != null) && !busy)
        Spacer(Modifier.height(12.dp))
        CenterLink(localized("Já tem conta?"), localized("Entrar"), onLogin)
    }
}

@Composable
internal fun LensField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    type: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            color = Ink,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValue,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = type),
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            shape = MaterialTheme.shapes.medium,
            colors = lensFieldColors()
        )
    }
}

@Composable
internal fun lensFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceHigh,
    unfocusedContainerColor = SurfaceHigh,
    focusedBorderColor = Accent,
    unfocusedBorderColor = FieldBorder
)

@Composable
internal fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, FieldBorder)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onLogout,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Text(localized("Sair da conta"), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CenterLink(prefix: String, action: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prefix, color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f, fill = false))
        TextButton(onClick = onClick) { Text(action) }
    }
}



@Composable
internal fun AppPage(
    current: Screen,
    onNavigate: (Screen) -> Unit,
    isPhotographerMode: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Paper).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.widthIn(max = 680.dp).fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 22.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandMark()
        }
        HorizontalDivider(color = Line)
        Column(Modifier.weight(1f).widthIn(max = 680.dp).fillMaxWidth(), content = content)
        AppNavigation(current, onNavigate, if (isPhotographerMode) PhotographerTabs else ClientTabs)
    }
}

private data class NavigationTab(val screen: Screen, val label: String, val icon: ImageVector)

private val ClientTabs = listOf(
    NavigationTab(Screen.Home, "Início", Icons.Default.Home),
    NavigationTab(Screen.Search, "Buscar", Icons.Default.Search),
    NavigationTab(Screen.Budgets, "Orçamentos", Icons.AutoMirrored.Filled.List),
    NavigationTab(Screen.Conversations, "Mensagens", Icons.Default.Email),
    NavigationTab(Screen.Account, "Perfil", Icons.Default.Person)
)

private val PhotographerTabs = listOf(
    NavigationTab(Screen.ProDashboard, "Painel", Icons.Default.Home),
    NavigationTab(Screen.ProRequests, "Pedidos", Icons.AutoMirrored.Filled.List),
    NavigationTab(Screen.ProAgenda, "Agenda", Icons.Default.DateRange),
    NavigationTab(Screen.ProProfile, "Perfil", Icons.Default.Person),
    NavigationTab(Screen.ProPortfolio, "Portfólio", Icons.Default.Add)
)

@Composable
private fun AppNavigation(current: Screen, onNavigate: (Screen) -> Unit, tabs: List<NavigationTab>) {
    val density = LocalDensity.current
    val largeLabels = density.fontScale > 1f
    val labelStyle = MaterialTheme.typography.labelSmall
    val textMeasurer = rememberTextMeasurer()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = if (largeLabels) 6.dp else 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = SurfaceHigh,
            border = BorderStroke(1.dp, Line),
            shadowElevation = 5.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(4.dp)
                    .height(IntrinsicSize.Min)
            ) {
                tabs.forEach { (screen, sourceLabel, icon) ->
                    val label = localized(sourceLabel)
                    val selected = current == screen
                    val tint = if (selected) Accent else Muted
                    val weight = if (largeLabels) {
                        val labelWidth = textMeasurer.measure(
                            label,
                            style = labelStyle.copy(fontWeight = FontWeight.Bold),
                            softWrap = false,
                            maxLines = 1
                        ).size.width
                        with(density) { maxOf(48.dp.toPx(), labelWidth + 4.dp.toPx()) }
                    } else 1f
                    Column(
                        modifier = Modifier
                            .weight(weight)
                            .heightIn(min = 56.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(if (selected) Sand else Color.Transparent)
                            .selectable(
                                selected = selected,
                                role = Role.Tab,
                                onClick = { onNavigate(screen) }
                            )
                            .semantics { contentDescription = label }
                            .padding(horizontal = 1.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(3.dp))
                        Text(
                            label,
                            modifier = Modifier.clearAndSetSemantics {},
                            color = tint,
                            style = labelStyle,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SectionTitle(title: String, onMore: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (onMore != null) {
            TextButton(onClick = onMore) { Text(localized("Ver todos")) }
        }
    }
}

@Composable
internal fun PageTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        color = Ink,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier.semantics { heading() }
    )
}

internal fun specialtyLabel(code: String): String = when (code.lowercase()) {
    "wedding", "casamento" -> localized("Casamento")
    "portrait", "retrato" -> localized("Retrato")
    "corporate", "corporativo" -> localized("Corporativo")
    "family", "família", "familia" -> localized("Família")
    "events", "eventos" -> localized("Eventos")
    "product" -> localized("Produtos")
    "fashion" -> localized("Moda")
    "newborn" -> localized("Recém-nascidos")
    "nature" -> localized("Natureza")
    "real-estate" -> localized("Imóveis")
    else -> code.replaceFirstChar { it.uppercase() }
}

@Composable
internal fun FilterChip(label: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    val shape = MaterialTheme.shapes.small
    val action = if (onClick == null) {
        Modifier
    } else {
        Modifier
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }
    Box(action, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .background(if (selected) Sand else SurfaceHigh, shape)
                .border(1.dp, if (selected) Accent else Line, shape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                label,
                color = if (selected) Accent else Ink,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
internal fun Avatar(initials: String, modifier: Modifier = Modifier, background: Color = Sand) {
    Box(modifier.background(background, CircleShape), contentAlignment = Alignment.Center) {
        Text(initials, color = Accent, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun EmptyMessage(message: String) {
    Text(
        message,
        color = Muted,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 28.dp)
    )
}

@Composable
private fun FairFrame(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    GuestPage {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onBack)
            CompactLogo()
        }
        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Line)
        PageTitle(title)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun FairCatalogScreen(
    photographers: List<FairPhotographer>,
    onBack: () -> Unit,
    onPhotographer: (String) -> Unit
) {
    FairFrame(localized("Perfis da feira"), onBack) {
        Text(localized("Fotógrafos fictícios com portfólios de exemplo."), color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(14.dp))
        photographers.forEach { photographer ->
            DiscoveryPhotographerCard(photographer.summary, portrait = photographer.portrait) {
                onPhotographer(photographer.summary.slug)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FairProfileScreen(
    photographer: FairPhotographer,
    onBack: () -> Unit,
    onHire: (() -> Unit)?,
    onQuote: (() -> Unit)?,
    contactActions: @Composable () -> Unit
) {
    FairFrame(photographer.summary.name, onBack) {
        Image(
            painter = painterResource(photographer.portrait),
            contentDescription = null,
            modifier = Modifier.size(76.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(10.dp))
        Text(
            photographer.summary.specialties.joinToString(" · ") { specialtyLabel(it) } +
                " — ${photographer.summary.city}, ${photographer.summary.state}",
            color = Accent,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(localized("Perfil fictício para demonstração."), color = Accent, style = MaterialTheme.typography.labelLarge)
        if (onHire != null && onQuote != null) {
            Text(localized("Pedidos de demonstração ficam no ambiente local."), color = Muted)
            Spacer(Modifier.height(10.dp))
            PortfolioActions(onHire, onQuote)
            Spacer(Modifier.height(8.dp))
            contactActions()
            Spacer(Modifier.height(12.dp))
        }
        Text(photographer.bio, color = Muted, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        SectionTitle(localized("Portfólio"))
        Spacer(Modifier.height(8.dp))
        photographer.photos.forEach { photo ->
            Image(
                painter = painterResource(photo.drawable),
                contentDescription = photo.altText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.5f).clip(MaterialTheme.shapes.medium)
            )
            Text(
                photo.caption,
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            )
        }
        SectionTitle(localized("Avaliações"))
        Text(localized("Este perfil de demonstração não tem avaliações de clientes."), color = Muted)
    }
}

@Composable
private fun PortfolioActions(onHire: () -> Unit, onQuote: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PrimaryButton(localized("Contratar"), onHire)
        OutlinedButton(
            onClick = onQuote,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(localized("Solicitar orçamento"), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PublicPortfolioPhoto(item: PortfolioItem, modifier: Modifier) {
    val preview by produceState<Bitmap?>(initialValue = null, key1 = item.imageUrl) {
        value = null
        value = loadPublicPortfolioPreview(item.imageUrl)
    }
    val bitmap = preview
    if (bitmap == null) {
        BrandPhoto(modifier)
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = item.altText.ifBlank { null },
            modifier = modifier.clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun PhotographerScreen(
    viewModel: LensClickViewModel,
    slug: String,
    user: User?,
    onBack: () -> Unit,
    onQuote: () -> Unit,
    onHire: (Boolean) -> Unit,
    onReview: () -> Unit,
    contactActions: @Composable (String) -> Unit
) {
    val state by viewModel.publicProfile.collectAsStateWithLifecycle()
    val publicReviews by viewModel.publicReviews.collectAsStateWithLifecycle()
    val reviewState = publicReviews?.takeIf { it.slug == slug }
    val reviews = reviewState?.data
    LaunchedEffect(slug) {
        viewModel.loadPublicPhotographer(slug)
        viewModel.loadReviews(slug)
    }

    when (val result = state) {
        PublicProfileState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
        PublicProfileState.Error -> ProfileStatus(localized("Não foi possível carregar este perfil."), localized("Tentar novamente")) { viewModel.loadPublicPhotographer(slug) }
        PublicProfileState.NotFoundOrUnpublished -> ProfileStatus(localized("Este perfil não está disponível ou ainda não foi publicado."), localized("Voltar"), onBack)
        is PublicProfileState.Ready -> {
            val profile = result.profile
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Box(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BackButton(onBack)
                            CompactLogo()
                        }
                        Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp)) {
                            Column(
                                Modifier.fillMaxWidth().background(Sand, MaterialTheme.shapes.large).padding(20.dp)
                            ) {
                                PublicPhotographerAvatar(
                                    profile.profile,
                                    Modifier.size(64.dp),
                                    photoPath = profile.profilePic,
                                    background = SurfaceHigh
                                )
                                Spacer(Modifier.height(12.dp))
                                PageTitle(profile.profile.name)
                                Text(
                                    profile.profile.specialties.joinToString(" · ") { specialtyLabel(it) },
                                    color = Muted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val location = listOf(profile.profile.city, profile.profile.state)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                if (location.isNotBlank()) {
                                    Text(
                                        location,
                                        color = Muted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 5.dp)
                                    )
                                }
                                val average = if (reviews == null) profile.profile.ratingAverage else reviews.summary.average
                                val count = reviews?.summary?.total ?: profile.profile.reviewCount
                                val rating = photographerRatingLabel(average, count)
                                Text(
                                    if (rating == null) localized("Sem avaliações") else "★ $rating",
                                    color = if (rating == null) Muted else Accent,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            if (user?.role != "photographer") {
                                PortfolioActions(
                                    onHire = { onHire(profile.profile.startingPriceCents != null) },
                                    onQuote = onQuote
                                )
                            }
                            if (user?.role != "photographer") {
                                Spacer(Modifier.height(8.dp))
                                contactActions(profile.profile.name)
                            }
                            Spacer(Modifier.height(22.dp))
                            HorizontalDivider(color = Line)
                            Spacer(Modifier.height(17.dp))
                            SectionTitle(localized("Portfólio"))
                            Spacer(Modifier.height(10.dp))
                            if (profile.portfolio.isEmpty()) {
                                Text(localized("Este fotógrafo ainda não publicou imagens."), color = Muted, fontSize = 12.sp)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(profile.portfolio, key = { it.id }) { item ->
                                        Column(Modifier.width(176.dp)) {
                                            PublicPortfolioPhoto(item, Modifier.fillMaxWidth().height(118.dp))
                                            Text(
                                                item.caption.ifBlank { item.altText },
                                                color = Muted,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                            PortfolioReviews(
                                reviewState,
                                onRetry = { viewModel.loadReviews(slug) },
                                onReview = onReview.takeIf { user?.role != "photographer" }
                            )
                            Spacer(Modifier.height(20.dp))
                            if (profile.profile.specialties.isNotEmpty()) {
                                SectionTitle(localized("Serviços e preços"))
                                val startingPrice = profile.profile.startingPriceCents
                                profile.profile.specialties.forEach { code ->
                                    val price = startingPrice?.let { localized("A partir de {0}", moneyInReais(it)) }
                                        ?: localized("Sob orçamento")
                                    Text("${specialtyLabel(code)} · $price", color = Muted,
                                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
                                }
                                Spacer(Modifier.height(22.dp))
                            }
                            Spacer(Modifier.height(20.dp))
                            Spacer(Modifier.height(22.dp))
                            SectionTitle(localized("Sobre mim"))
                            Text(
                                profile.bio.ifBlank { localized("Este fotógrafo ainda não adicionou uma apresentação.") },
                                color = Muted,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }


        }
    }
}


private enum class PortfolioAction { Favorite, Chat }

@Composable
private fun PortfolioContactActions(
    viewModel: LensClickViewModel,
    entry: NavBackStackEntry,
    slug: String,
    name: String,
    user: User?,
    onLogin: () -> Unit,
    onConversation: (Long) -> Unit
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val currentFavorite = favorites.firstOrNull { it.slug == slug }
    val pendingAction by entry.savedStateHandle.getStateFlow<String?>(PendingPortfolioAction, null).collectAsStateWithLifecycle()
    var favoriteBusy by remember { mutableStateOf(false) }
    var chatLoading by remember { mutableStateOf(false) }
    var composing by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun execute(action: PortfolioAction) {
        if (user == null) {
            entry.savedStateHandle.remove<String>(PendingClientRoute)
            entry.savedStateHandle[PendingPortfolioAction] = action.name
            onLogin()
            return
        }
        error = null
        when (action) {
            PortfolioAction.Favorite -> {
                favoriteBusy = true
                val onResult: (Boolean) -> Unit = { success ->
                    favoriteBusy = false
                    if (!success) error = localized("Não foi possível atualizar o favorito. Tente novamente.")
                }
                if (currentFavorite == null) viewModel.addFavorite(slug, onResult)
                else viewModel.removeFavorite(currentFavorite.photographerId, onResult)
            }
            PortfolioAction.Chat -> {
                chatLoading = true
                scope.launch {
                    try {
                        val id = viewModel.findPortfolioConversation(slug)
                        if (id == null) composing = true else onConversation(id)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        error = failure.remoteErrorCode().accountActionMessage(localized("Não foi possível carregar as conversas."))
                    } finally {
                        chatLoading = false
                    }
                }
            }
        }
    }

    LaunchedEffect(user?.id, pendingAction) {
        val action = PortfolioAction.entries.firstOrNull { it.name == pendingAction }
        if (user?.role == "client" && action != null) {
            entry.savedStateHandle[PendingPortfolioAction] = null
            execute(action)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { execute(PortfolioAction.Favorite) },
                enabled = !favoriteBusy,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics {
                    if (currentFavorite != null) contentDescription = localized("Remover dos favoritos")
                },
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(
                    if (currentFavorite != null) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null, modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(if (currentFavorite != null) localized("Favoritado") else localized("Favoritar"))
            }
            OutlinedButton(
                onClick = { execute(PortfolioAction.Chat) },
                enabled = !chatLoading,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (chatLoading) CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(localized("Conversar"))
            }
        }
        error?.let { AuthStatusBanner(it, loading = false, error = true) }
    }

    if (composing) {
        ContactMessageDialog(
            title = localized("Conversar com {0}", name),
            field = localized("Mensagem"), length = 1..2_000,
            onClose = { composing = false }
        ) { message, completed ->
            viewModel.startConversation(slug, message) { response ->
                response.onSuccess { id ->
                    composing = false
                    onConversation(id)
                }.onFailure {
                    val code = it.remoteErrorCode()
                    completed(if (code == MobileAuthErrorCode.CONFLICT) localized("Esta conversa foi encerrada.")
                        else code.accountActionMessage(localized("Não foi possível iniciar a conversa.")))
                }
            }
        }
    }
}

@Composable
private fun PortfolioReviews(
    reviewState: PhotographerReviewsState?,
    onRetry: () -> Unit,
    onReview: (() -> Unit)? = null,
    showRating: Boolean = false
) {
    val reviews = reviewState?.data
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            localized("Avaliações"),
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (onReview != null) {
            TextButton(onClick = onReview) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(localized("Avaliar"), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showRating && reviews != null) {
        val rating = photographerRatingLabel(reviews.summary.average, reviews.summary.total)
        rating?.let { Text("★ $it", color = Accent, style = MaterialTheme.typography.labelLarge) }
    }
    Spacer(Modifier.height(8.dp))
    if (reviewState?.failed == true) {
        Text(localized("Não foi possível carregar as avaliações."), color = Muted, fontSize = 12.sp)
        TextButton(onClick = onRetry) { Text(localized("Tentar novamente")) }
    } else if (reviews == null) {
        Text(localized("Carregando avaliações…"), color = Muted, fontSize = 12.sp)
    } else if (reviews.reviews.isEmpty()) {
        Text(localized("Ainda não há avaliações enviadas para este fotógrafo."), color = Muted, fontSize = 12.sp)
    } else {
        reviews.summary.distribution?.let { counts ->
            ReviewDistribution(counts, reviews.summary.total)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reviews.reviews.forEach { r ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(r.authorName, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(localized("★ {0} de 5", r.rating), color = Accent, fontSize = 12.sp)
                    }
                    if (r.comment.isNotBlank()) {
                        Text(r.comment, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Text(displayCalendarDate(r.createdAt), color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

private fun Throwable.remoteErrorCode() =
    (this as? MobileAuthException)?.code ?: MobileAuthErrorCode.SERVER

@Composable
private fun ContactMessageDialog(
    title: String,
    field: String,
    length: IntRange,
    onClose: () -> Unit,
    onSend: (String, (String) -> Unit) -> Unit
) {
    var message by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!busy) onClose() },
        title = { Text(title, color = Ink) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LensField(field, message, { message = it }, singleLine = false, maxLines = 4)
                if (busy) AuthStatusBanner(localized("Enviando…"), loading = true)
                error?.let { AuthStatusBanner(it, loading = false, error = true) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                busy = true
                error = null
                onSend(message.trim()) { failure ->
                    busy = false
                    error = failure
                }
            }, enabled = !busy && message.trim().length in length) { Text(localized("Enviar")) }
        },
        dismissButton = { TextButton(onClick = onClose, enabled = !busy) { Text(localized("Cancelar")) } },
        containerColor = SurfaceHigh
    )
}

@Composable
internal fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localized("Voltar"), tint = Accent)
    }
}

@Composable
private fun QuoteScreen(
    forHire: Boolean,
    onBack: () -> Unit,
    onSend: (BudgetRequestDraft, (MobileAuthErrorCode) -> Unit) -> Unit
) {
    var type by rememberSaveable { mutableStateOf(localized("Casamento")) }
    var date by rememberSaveable { mutableStateOf("") }
    var place by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var failure by rememberSaveable { mutableStateOf<String?>(null) }
    var calendarOpen by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Spacer(Modifier.width(10.dp))
            PageTitle(if (forHire) localized("Contratar com orçamento") else localized("Solicitar orçamento"), Modifier.weight(1f))
        }
        if (forHire) {
            Text(localized("Peça um orçamento para definir o valor. Depois de aceitar a proposta, você poderá escolher como pagar."), color = Muted)
        }
        Spacer(Modifier.height(16.dp))
        LensField(localized("Tipo de evento"), type, { type = it })
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { calendarOpen = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            enabled = !submitting,
            shape = MaterialTheme.shapes.medium
        ) {
            val formatted = displayCalendarDate(date)
            Text(if (date.isBlank()) localized("Selecionar data prevista") else localized("Data prevista · {0}", formatted))
        }
        Spacer(Modifier.height(10.dp))
        LensField(localized("Local / Cidade"), place, { place = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("Duração estimada"), duration, { duration = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("Descrição do evento (mín. 10 caracteres)"), description, { description = it }, singleLine = false, maxLines = 3)
        Spacer(Modifier.height(10.dp))
        LensField(localized("Informações adicionais"), details, { details = it }, singleLine = false, maxLines = 2)
        Spacer(Modifier.height(24.dp))
        if (submitting) AuthStatusBanner(localized("Enviando solicitação…"), loading = true)
        failure?.let { AuthStatusBanner(it, loading = false, error = true) }
        PrimaryButton(localized("Enviar solicitação"), {
            submitting = true
            failure = null
            onSend(BudgetRequestDraft(type, date, place, duration, description, details)) { error ->
                submitting = false
                failure = when (error) {
                    MobileAuthErrorCode.CONFLICT -> localized("Já existe um pedido em andamento com este fotógrafo.")
                    MobileAuthErrorCode.INVALID_INPUT -> localized("Revise o tipo, a data, o local e a descrição do evento.")
                    MobileAuthErrorCode.FORBIDDEN -> localized("A plataforma aceita pedidos de orçamento de contas de cliente.")
                    else -> error.accountActionMessage(localized("Não foi possível enviar a solicitação."))
                }
            }
        }, enabled = !submitting && type.trim().length >= 2 && place.trim().length >= 2 &&
            date.isNotBlank() && description.trim().length >= 10)
    }
    if (calendarOpen) {
        QuoteDatePicker(date, onDismiss = { calendarOpen = false }) {
            date = it
            calendarOpen = false
        }
    }
}

@Composable
private fun QuoteDatePicker(date: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val initial = remember(date) {
        Calendar.getInstance().apply {
            if (date.isNotBlank()) {
                val (year, month, day) = date.split('-').map(String::toInt)
                set(year, month - 1, day)
            }
        }
    }
    var chosenYear by rememberSaveable { mutableIntStateOf(initial.get(Calendar.YEAR)) }
    var chosenMonth by rememberSaveable { mutableIntStateOf(initial.get(Calendar.MONTH)) }
    var chosenDay by rememberSaveable { mutableIntStateOf(initial.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = SurfaceHigh) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 12.dp)) {
                Text(
                    localized("Selecionar data prevista"),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium, color = Ink
                )
                // The calendar is a child view. The Compose dialog owns the window and its activity token.
                AndroidView(
                    factory = { context ->
                        DatePicker(AppLanguage.pickerContext(context)).apply {
                            init(chosenYear, chosenMonth, chosenDay) { _, selectedYear, selectedMonth, selectedDay ->
                                chosenYear = selectedYear
                                chosenMonth = selectedMonth
                                chosenDay = selectedDay
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.align(Alignment.End).padding(horizontal = 8.dp)) {
                    TextButton(onClick = onDismiss) { Text(localized("Cancelar")) }
                    TextButton(onClick = {
                        onConfirm(String.format(Locale.ROOT, "%04d-%02d-%02d", chosenYear, chosenMonth + 1, chosenDay))
                    }) { Text(localized("Confirmar")) }
                }
            }
        }
    }
}

@Composable
private fun BudgetsScreen(
    viewModel: LensClickViewModel,
    budgets: List<BudgetRequest>,
    favorites: List<Favorite>,
    signedIn: Boolean,
    onNavigate: (Screen) -> Unit,
    onLogin: () -> Unit,
    onBudget: (Long) -> Unit,
    onEngagements: () -> Unit,
    onDemo: () -> Unit,
    onPhotographer: (String) -> Unit
) = AppPage(Screen.Budgets, onNavigate) {
    if (!signedIn) {
        GuestTabSignIn(
            localized("Orçamentos e favoritos"),
            localized("Entre na sua conta para acompanhar seus pedidos e ver os fotógrafos que você salvou."),
            onLogin,
            onDemo
        )
        return@AppPage
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.padding(top = 12.dp)) {
        PageTitle(localized("Meus Pedidos & Favoritos"), Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).horizontalScroll(rememberScrollState()).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(localized("Orçamentos ({0})", budgets.size), tab == 0) { tab = 0 }
            FilterChip(localized("Favoritos ({0})", favorites.size), tab == 1) { tab = 1 }
        }
        Row(Modifier.padding(horizontal = 16.dp).horizontalScroll(rememberScrollState())) {
            TextButton(onClick = onEngagements) { Text(localized("Contratações")) }
            TextButton(onClick = onDemo) { Text(localized("Orçamentos de demonstração")) }
        }
        if (tab == 0) {
            if (budgets.isEmpty()) {
                EmptyMessage(localized("Nenhum pedido de orçamento encontrado."))
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgets, key = { it.id }) { b ->
                        BudgetCard(b, onOpen = { onBudget(b.id) })
                    }
                }
            }
        } else {
            if (favorites.isEmpty()) {
                EmptyMessage(localized("Você ainda não salvou fotógrafos favoritos."))
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favorites, key = { it.photographerId }) { f ->
                        var removing by remember { mutableStateOf(false) }
                        var failed by remember { mutableStateOf(false) }
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(14.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                Modifier.weight(1f).clickable(role = Role.Button) { onPhotographer(f.slug) }
                            ) {
                                Text(f.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(f.slug, color = Muted, fontSize = 12.sp)
                            }
                            OutlinedButton(onClick = {
                                removing = true
                                failed = false
                                viewModel.removeFavorite(f.photographerId) { success ->
                                    removing = false
                                    failed = !success
                                }
                            }, enabled = !removing) { Text(localized("Remover")) }
                        }
                        if (failed) AuthStatusBanner(localized("Não foi possível remover o favorito."), loading = false, error = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(b: BudgetRequest, onOpen: (() -> Unit)? = null) {
    val action = if (onOpen == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onOpen)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).then(action).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(b.eventType, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            FilterChip(budgetStatusLabel(b.status), selected = true)
        }
        if (onOpen != null) Text(localized("Ver detalhes"), color = Accent, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Text(localized("Data: {0} • Local: {1}", displayCalendarDate(b.eventDate), b.place), color = Muted, fontSize = 12.sp)
        if (b.description.isNotBlank()) Text(b.description, color = Ink, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun BudgetDetailScreen(
    viewModel: LensClickViewModel,
    id: Long,
    canRespond: Boolean,
    onBack: () -> Unit,
    onHire: (String) -> Unit
) {
    var budget by remember(id) { mutableStateOf<BudgetRequest?>(null) }
    var loading by remember(id) { mutableStateOf(true) }
    var busy by remember(id) { mutableStateOf(false) }
    var error by remember(id) { mutableStateOf<String?>(null) }
    var confirmingCancel by rememberSaveable(id) { mutableStateOf(false) }

    fun load() {
        loading = true
        error = null
        viewModel.loadBudgetDetail(id) { result ->
            loading = false
            result.onSuccess { budget = it }
                .onFailure { error = it.remoteErrorCode().accountActionMessage(localized("Não foi possível carregar este orçamento.")) }
        }
    }
    LaunchedEffect(id) { load() }

    fun complete(success: Boolean) {
        busy = false
        if (success) {
            load()
        } else {
            error = viewModel.budgetError.value?.accountActionMessage(localized("Não foi possível atualizar o pedido."))
                ?: localized("Não foi possível atualizar o pedido.")
        }
    }
    SettingsPage(localized("Detalhes do orçamento"), onBack) {
        if (loading) AuthStatusBanner(localized("Carregando orçamento…"), loading = true)
        if (busy) AuthStatusBanner(localized("Atualizando pedido…"), loading = true)
        error?.let {
            AuthStatusBanner(it, loading = false, error = true)
            TextButton(onClick = { load() }, enabled = !loading && !busy) { Text(localized("Atualizar")) }
        }
        if (!loading) budget?.let { request ->
            BudgetCard(request)
            if (request.duration.isNotBlank()) Text(localized("Duração · {0}", request.duration), color = Muted)
            if (request.additionalInfo.isNotBlank()) Text(request.additionalInfo, color = Muted)
            request.proposals.forEach { proposal ->
                Column(
                    Modifier.fillMaxWidth().background(SurfaceHigh, MaterialTheme.shapes.medium).padding(14.dp)
                ) {
                    val value = moneyInReais(proposal.priceCents)
                    Text(value, color = Ink, style = MaterialTheme.typography.titleMedium)
                    if (proposal.conditions.isNotBlank()) Text(proposal.conditions, color = Muted)
                    if (canRespond && request.status == "proposed" && proposal.status == "pending") {
                        Row {
                            TextButton(onClick = {
                                busy = true
                                error = null
                                viewModel.decideRequest(id, proposal.id, true, ::complete)
                            }, enabled = !busy) { Text(localized("Aceitar proposta")) }
                            TextButton(onClick = {
                                busy = true
                                error = null
                                viewModel.decideRequest(id, proposal.id, false, ::complete)
                            }, enabled = !busy) { Text(localized("Recusar proposta")) }
                        }
                    }
                }
            }
            if (canRespond && request.status == "accepted" && request.photographerSlug.isNotBlank() &&
                request.proposals.any { it.status == "accepted" }
            ) {
                PrimaryButton(localized("Contratar com esta proposta"), { onHire(request.photographerSlug) })
            }
            if (request.history.isNotEmpty()) {
                SectionTitle(localized("Histórico"))
                request.history.forEach { event ->
                    Text("${displayCalendarDate(event.createdAt)} · ${budgetEventLabel(event.action)}", color = Muted)
                }
            }
            if (request.status in setOf("pending", "proposed")) {
                TextButton(onClick = { confirmingCancel = true }, enabled = !busy) {
                    Text(if (canRespond) localized("Cancelar pedido") else localized("Recusar orçamento"))
                }
            }
        }
    }
    if (confirmingCancel) {
        AlertDialog(
            onDismissRequest = { confirmingCancel = false },
            title = { Text(if (canRespond) localized("Cancelar pedido?") else localized("Recusar orçamento?")) },
            text = { Text(localized("O pedido será encerrado para você e para o fotógrafo.")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmingCancel = false
                    busy = true
                    error = null
                    viewModel.closeBudgetRequest(id, decline = !canRespond, ::complete)
                }) { Text(if (canRespond) localized("Confirmar cancelamento") else localized("Confirmar recusa")) }
            },
            dismissButton = { TextButton(onClick = { confirmingCancel = false }) { Text(localized("Voltar")) } }
        )
    }
}

private fun budgetStatusLabel(status: String): String = when (status.lowercase()) {
    "pending" -> localized("Aguardando")
    "proposed" -> localized("Em negociação")
    "accepted" -> localized("Aceito")
    "rejected" -> localized("Recusado")
    "cancelled" -> localized("Cancelado")
    else -> status
}

@Composable
private fun ConversationsScreen(
    viewModel: LensClickViewModel,
    signedIn: Boolean,
    onConversation: (Long) -> Unit,
    onNavigate: (Screen) -> Unit,
    onLogin: () -> Unit
) = AppPage(Screen.Conversations, onNavigate) {
    if (!signedIn) {
        GuestTabSignIn(localized("Mensagens"), localized("Entre na sua conta para ver suas conversas com fotógrafos."), onLogin)
        return@AppPage
    }

    var query by rememberSaveable { mutableStateOf("") }
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val loading by viewModel.conversationsLoading.collectAsStateWithLifecycle()
    val failed by viewModel.conversationsFailed.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadConversations() }

    Column(Modifier.padding(top = 12.dp)) {
        PageTitle(localized("Conversas"), Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text(localized("Buscar conversa"), color = Muted, fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(17.dp),
            colors = lensFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        val filtered = conversations.filter { it.counterpartName.contains(query, true) || it.lastMessage.contains(query, true) }
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (loading && conversations.isEmpty()) {
                item { AuthStatusBanner(localized("Carregando conversas…"), loading = true) }
            } else if (failed) {
                item {
                    AuthStatusBanner(localized("Não foi possível carregar as conversas."), loading = false, error = true)
                    TextButton(onClick = viewModel::loadConversations) { Text(localized("Tentar novamente")) }
                }
            } else if (filtered.isEmpty()) {
                item { EmptyMessage(localized("Nenhuma conversa encontrada.")) }
            }
            if (filtered.isNotEmpty()) {
                items(filtered, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(SurfaceHigh)
                            .border(1.dp, Line, MaterialTheme.shapes.medium)
                            .clickable(role = Role.Button) { onConversation(c.id) }
                            .padding(horizontal = 10.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(initialsForDisplay(c.counterpartName), Modifier.size(52.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.counterpartName, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(c.lastMessage.ifBlank { localized("Nenhuma mensagem ainda.") }, color = Muted, fontSize = 12.sp, maxLines = 1)
                        }
                        if (c.unreadCount > 0) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(Ink))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatus(message: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, color = Ink, textAlign = TextAlign.Center)
        TextButton(onClick = onAction) { Text(action, color = Ink) }
    }
}

@Composable
private fun ChatScreen(viewModel: LensClickViewModel, conversationId: Long, onBack: () -> Unit) {
    var input by rememberSaveable(conversationId) { mutableStateOf("") }
    var sending by remember(conversationId) { mutableStateOf(false) }
    var sendError by remember(conversationId) { mutableStateOf<String?>(null) }
    val chat by viewModel.chat.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val conversation = conversations.firstOrNull { it.id == conversationId }
    val currentChat = chat?.takeIf { it.conversationId == conversationId }
    val messages = currentChat?.messages
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
        if (conversation == null) viewModel.loadConversations()
    }
    LaunchedEffect(messages?.lastOrNull()?.id) {
        if (!messages.isNullOrEmpty()) listState.scrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Avatar(initialsForDisplay(conversation?.counterpartName ?: "Chat"), Modifier.size(42.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(conversation?.counterpartName ?: localized("Mensagens"), color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(localized("Conversa via LensClick"), color = Muted, fontSize = 12.sp)
            }
            CompactLogo(34)
        }
        LazyColumn(
            Modifier.weight(1f).padding(horizontal = 18.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                currentChat?.loadFailed == true -> item {
                    AuthStatusBanner(localized("Não foi possível carregar as mensagens."), loading = false, error = true)
                    TextButton(onClick = { viewModel.loadMessages(conversationId) }) { Text(localized("Tentar novamente")) }
                }
                messages == null -> item { AuthStatusBanner(localized("Carregando mensagens…"), loading = true) }
                messages.isEmpty() -> item { EmptyMessage(localized("Nenhuma mensagem nesta conversa.")) }
                else -> items(messages, key = { it.id }) { message -> Bubble(message.text, message.isMine) }
            }
        }
        HorizontalDivider(color = Line)
        sendError?.let { AuthStatusBanner(it, loading = false, error = true) }
        Row(Modifier.fillMaxWidth().background(SurfaceHigh).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                readOnly = sending,
                modifier = Modifier.weight(1f),
                placeholder = { Text(localized("Digite sua mensagem..."), color = Muted, fontSize = 12.sp) },
                shape = RoundedCornerShape(18.dp),
                colors = lensFieldColors()
            )
            Spacer(Modifier.width(9.dp))
            Button(
                onClick = {
                    sending = true
                    sendError = null
                    viewModel.sendMessage(conversationId, input.trim()) { error ->
                        sending = false
                        if (error == null) {
                            input = ""
                        } else {
                            sendError = if (error == MobileAuthErrorCode.CONFLICT) localized("Esta conversa foi encerrada.")
                            else error.accountActionMessage(localized("Não foi possível enviar a mensagem. Tente novamente."))
                        }
                    }
                },
                modifier = Modifier.size(54.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                enabled = input.isNotBlank() && input.trim().length <= 2_000 && messages != null && !sending
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = localized("Enviar mensagem"), modifier = Modifier.size(22.dp))
            }
        }
        if (input.trim().length > 2_000) {
            Text(localized("A mensagem deve ter até 2.000 caracteres."), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 14.dp))
        }
    }
}

@Composable private fun Bubble(text: String, mine: Boolean) = Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
    Text(
        text,
        color = if (mine) OnCoffee else Ink,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(if (mine) RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp)
                  else RoundedCornerShape(14.dp, 14.dp, 14.dp, 4.dp))
            .background(if (mine) Accent else SurfaceHigh)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

@Composable private fun PhotographerPage(current: Screen, onNavigate: (Screen) -> Unit, content: @Composable ColumnScope.() -> Unit) = AppPage(current, onNavigate, isPhotographerMode = true, content = content)

@Composable
private fun PhotographerDashboardScreen(p: Photographer?, budgets: List<BudgetRequest>, onNavigate: (Screen) -> Unit, onClientMode: () -> Unit) = PhotographerPage(Screen.ProDashboard, onNavigate) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        PageTitle(localized("Painel do fotógrafo"))
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(p?.initials ?: "FT", Modifier.size(56.dp)); Spacer(Modifier.width(14.dp)); Column { Text(p?.name ?: localized("Perfil Profissional"), color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(p?.specialty ?: localized("Fotografia"), color = Muted, fontSize = 12.sp) } }
        Spacer(Modifier.height(20.dp))
        SectionTitle(localized("Métricas"))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProMetric(localized("Pedidos"), "${budgets.size}", Modifier.weight(1f))
            ProMetric(localized("Agenda"), "${budgets.count { it.status == "accepted" }}", Modifier.weight(1f))
            ProMetric(localized("Avaliação"), "—", Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle(localized("Ações Rápidas"))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProAction(localized("Editar Perfil"), localized("Informações e preços"), Modifier.weight(1f)) { onNavigate(Screen.ProProfile) }
            ProAction(localized("Portfólio"), localized("Gerenciar imagens"), Modifier.weight(1f)) { onNavigate(Screen.ProPortfolio) }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FieldBorder)) { Text(localized("Alternar para modo cliente"), color = Ink, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable private fun ProMetric(label: String, value: String, modifier: Modifier) { Column(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 12.sp) } }
@Composable private fun ProAction(title: String, desc: String, modifier: Modifier, onClick: () -> Unit) { Column(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(14.dp)) { Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(desc, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) } }

@Composable
private fun PhotographerRequestsScreen(
    viewModel: LensClickViewModel, budgets: List<BudgetRequest>, onNavigate: (Screen) -> Unit, onBudget: (Long) -> Unit
) = PhotographerPage(Screen.ProRequests, onNavigate) {
    var proposeTarget by remember { mutableStateOf<BudgetRequest?>(null) }
    Column(Modifier.padding(top = 12.dp)) {
        Row(Modifier.padding(horizontal = 16.dp).horizontalScroll(rememberScrollState())) {
            TextButton(onClick = { onNavigate(Screen.Engagements) }) { Text(localized("Contratações recebidas")) }
            TextButton(onClick = { onNavigate(Screen.DemoBudgets) }) { Text(localized("Orçamentos de demonstração")) }
        }
        PageTitle(localized("Pedidos de Orçamento"), Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        if (budgets.isEmpty()) {
            EmptyMessage(localized("Nenhum pedido recebido no momento."))
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(budgets, key = { it.id }) { b ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(b.eventType, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            FilterChip(budgetStatusLabel(b.status), selected = true)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(localized("Cliente: {0} • Data: {1}", b.counterpartName, displayCalendarDate(b.eventDate)), color = Muted, fontSize = 12.sp)
                        if (b.description.isNotBlank()) Text(b.description, color = Ink, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { onBudget(b.id) }) { Text(localized("Ver detalhes")) }
                        if (b.status == "pending") PrimaryButton(localized("Enviar proposta"), { proposeTarget = b })
                    }
                }
            }
        }
    }

    proposeTarget?.let { b ->
        ProposeDialog(
            onDismiss = { proposeTarget = null },
            onSend = { priceCents, conditions, completed ->
                viewModel.proposeToRequest(b.id, priceCents, conditions) { success ->
                    if (success) proposeTarget = null
                    else completed(viewModel.budgetError.value)
                }
            }
        )
    }
}

@Composable
private fun ProposeDialog(onDismiss: () -> Unit, onSend: (Long, String, (MobileAuthErrorCode?) -> Unit) -> Unit) {
    var priceStr by rememberSaveable { mutableStateOf("") }
    var conditions by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val cents = parsePriceCents(priceStr)
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(localized("Enviar Proposta"), color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LensField(localized("Valor total (R$)"), priceStr, { priceStr = it }, KeyboardType.Decimal)
                Spacer(Modifier.height(10.dp))
                LensField(localized("Condições / Detalhes"), conditions, { conditions = it }, singleLine = false, maxLines = 3)
                if (priceStr.isNotBlank() && cents == null) Text(localized("Informe um valor válido, como 1200,50."), color = MaterialTheme.colorScheme.error)
                if (busy) AuthStatusBanner(localized("Enviando proposta…"), loading = true)
                error?.let { AuthStatusBanner(it, loading = false, error = true) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    busy = true
                    error = null
                    onSend(requireNotNull(cents), conditions) { code ->
                        busy = false
                        error = if (code == MobileAuthErrorCode.CONFLICT) localized("Já existe uma proposta pendente ou o pedido foi encerrado.")
                        else code?.accountActionMessage(localized("Não foi possível enviar a proposta.")) ?: localized("Não foi possível enviar a proposta.")
                    }
                },
                enabled = cents != null && !busy
            ) { Text(localized("Enviar Proposta")) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(localized("Cancelar")) } },
        containerColor = SurfaceHigh
    )
}

private fun parsePriceCents(text: String): Long? = text.trim()
    .takeIf { it.matches(Regex("[0-9]+([,.][0-9]{1,2})?")) }
    ?.replace(',', '.')?.toBigDecimalOrNull()?.movePointRight(2)
    ?.takeIf { it <= BigDecimal.valueOf(100_000_000) }?.toLong()

@Composable
private fun PhotographerAgendaScreen(agenda: List<BudgetRequest>, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProAgenda, onNavigate) {
    Column(Modifier.padding(top = 12.dp)) {
        PageTitle(localized("Minha Agenda"), Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        if (agenda.isEmpty()) {
            EmptyMessage(localized("Nenhum evento agendado ou aceito."))
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(agenda, key = { it.id }) { e ->
                    BudgetCard(e)
                }
            }
        }
    }
}

@Composable
private fun ProfessionalProfileScreen(
    viewModel: LensClickViewModel,
    onNavigate: (Screen) -> Unit,
    onClientMode: () -> Unit,
    onLogout: () -> Unit
) = PhotographerPage(Screen.ProProfile, onNavigate) {
    val state by viewModel.professionalProfile.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadProfessionalProfile() }
    when (val result = state) {
        ProfessionalProfileState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
        ProfessionalProfileState.Error -> ProfileStatus(localized("Não foi possível carregar seu perfil profissional."), localized("Tentar novamente")) { viewModel.loadProfessionalProfile() }
        is ProfessionalProfileState.Ready -> ProfessionalProfileForm(
            result, viewModel, onClientMode, onLogout,
            onSettings = { onNavigate(Screen.Settings) }
        )
    }
}

@Composable
private fun ProfessionalProfileForm(
    state: ProfessionalProfileState.Ready,
    viewModel: LensClickViewModel,
    onClientMode: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit
) {
    var bio by rememberSaveable(state.profile) { mutableStateOf(state.profile.bio) }
    var city by rememberSaveable(state.profile) { mutableStateOf(state.profile.city) }
    var stateCode by rememberSaveable(state.profile) { mutableStateOf(state.profile.state) }
    var priceStr by rememberSaveable(state.profile) {
        mutableStateOf(state.profile.startingPriceCents?.let { BigDecimal.valueOf(it, 2).toPlainString() }.orEmpty())
    }
    var radiusStr by rememberSaveable(state.profile) { mutableStateOf(state.profile.serviceRadiusKm?.toString().orEmpty()) }
    var publicEmail by rememberSaveable(state.profile) { mutableStateOf(state.profile.publicEmail) }
    var publicPhone by rememberSaveable(state.profile) { mutableStateOf(state.profile.publicPhone) }
    var instagram by rememberSaveable(state.profile) { mutableStateOf(state.profile.instagram) }
    var website by rememberSaveable(state.profile) { mutableStateOf(state.profile.website) }
    var isPublished by rememberSaveable(state.profile) { mutableStateOf(state.profile.isPublished) }
    val priceCents = parsePriceCents(priceStr)
    val radius = radiusStr.trim().toIntOrNull()
    val validRadius = radiusStr.isBlank() || (radius != null && radius in 0..1_000)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        PageTitle(localized("Perfil Profissional"))
        Text(localized("Configure como seu perfil aparece para os clientes"), color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        LensField(localized("Bio / Apresentação"), bio, { bio = it }, singleLine = false, maxLines = 4)
        Spacer(Modifier.height(10.dp))
        LensField(localized("Cidade"), city, { city = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("Estado (UF)"), stateCode, { stateCode = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("Preço Inicial (R$)"), priceStr, { priceStr = it }, KeyboardType.Decimal)
        if (priceStr.isNotBlank() && priceCents == null) {
            Text(localized("Informe um preço válido, como 1200,50."), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(10.dp))
        LensField(localized("Raio de Atendimento (km)"), radiusStr, { radiusStr = it }, KeyboardType.Number)
        if (!validRadius) {
            Text(localized("Informe um raio entre 0 e 1.000 km."), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(10.dp))
        LensField(localized("E-mail Público"), publicEmail, { publicEmail = it }, KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        LensField(localized("Telefone Público"), publicPhone, { publicPhone = it }, KeyboardType.Phone)
        Spacer(Modifier.height(10.dp))
        LensField("Instagram", instagram, { instagram = it })
        Spacer(Modifier.height(10.dp))
        LensField(localized("Website"), website, { website = it }, KeyboardType.Uri)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(localized("Publicar perfil na busca"), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            FilterChip(if (isPublished) localized("Publicado") else localized("Rascunho"), selected = isPublished) { isPublished = !isPublished }
        }
        if (isPublished && state.error == MobileAuthErrorCode.INVALID_INPUT) {
            Text(localized("Para publicar, preencha a apresentação, especialidade, cidade, UF e um contato público, e adicione sua foto de perfil."), color = Muted)
            WebSettingsLinks(localized("Gerenciar foto e especialidades no site") to "/perfil")
        }
        Spacer(Modifier.height(24.dp))
        if (state.saving) {
            AuthStatusBanner(localized("Salvando perfil..."), loading = true)
        } else if (state.saved) {
            AuthStatusBanner(localized("Perfil salvo com sucesso!"), loading = false)
        } else if (state.error != null) {
            AuthStatusBanner(profileErrorMessage(state.error), loading = false, error = true)
        }
        PrimaryButton(localized("Salvar Perfil"), {
            viewModel.saveProfessionalProfile(
                state.profile.copy(
                    bio = bio,
                    city = city,
                    state = stateCode,
                    startingPriceCents = priceCents,
                    serviceRadiusKm = radius,
                    publicEmail = publicEmail,
                    publicPhone = publicPhone,
                    instagram = instagram,
                    website = website,
                    isPublished = isPublished
                )
            )
        }, enabled = !state.saving && (priceStr.isBlank() || priceCents != null) && validRadius)
        Spacer(Modifier.height(12.dp))
        SecondaryButton(localized("Configurações"), onSettings)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FieldBorder)) { Text(localized("Modo Cliente"), color = Ink, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(12.dp))
        LogoutButton(onLogout, enabled = !state.saving)
    }
}

private fun profileErrorMessage(code: MobileAuthErrorCode): String = when (code) {
    MobileAuthErrorCode.INVALID_INPUT -> localized("Dados do perfil inválidos. Verifique os campos.")
    MobileAuthErrorCode.UNAUTHENTICATED -> localized("Sua sessão expirou. Faça login novamente.")
    MobileAuthErrorCode.RATE_LIMITED -> localized("Muitas alterações. Aguarde um instante.")
    MobileAuthErrorCode.NETWORK -> localized("Não foi possível conectar. Verifique sua rede.")
    else -> localized("Erro ao salvar perfil. Tente novamente.")
}

@Composable
private fun PortfolioScreen(
    viewModel: LensClickViewModel, onNavigate: (Screen) -> Unit, onPublicProfile: (String) -> Unit
) = PhotographerPage(Screen.ProPortfolio, onNavigate) {
    val state by viewModel.portfolio.collectAsStateWithLifecycle()
    val professional by viewModel.professionalProfile.collectAsStateWithLifecycle()
    val publicReviews by viewModel.publicReviews.collectAsStateWithLifecycle()
    val publishedSlug = (professional as? ProfessionalProfileState.Ready)?.profile
        ?.takeIf { it.isPublished }?.slug?.takeIf(String::isNotBlank)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var upload by remember { mutableStateOf<ByteArray?>(null) }
    var preparing by remember { mutableStateOf(false) }
    var fileError by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            preparing = true
            scope.launch {
                upload = withContext(Dispatchers.IO) { readPortfolioUpload(context, uri) }
                preparing = false
                if (upload == null) fileError = localized("Não foi possível ler a imagem. Escolha outra foto.")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadPortfolio()
        viewModel.loadProfessionalProfile()
    }
    LaunchedEffect(publishedSlug) {
        publishedSlug?.let(viewModel::loadReviews)
    }

    Column(Modifier.padding(top = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            PageTitle(localized("Portfólio"))
            Button(onClick = {
                fileError = null
                try {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (_: android.content.ActivityNotFoundException) {
                    fileError = localized("Não foi possível abrir o seletor de fotos.")
                }
            }, enabled = (state as? PortfolioState.Ready)?.busy == false && !preparing) { Text(localized("+ Adicionar")) }
        }
        if (publishedSlug != null) {
            TextButton(onClick = { onPublicProfile(publishedSlug) }, modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(localized("Ver portfólio público"))
            }
        }
        if (preparing) AuthStatusBanner(localized("Preparando imagem…"), loading = true)
        fileError?.let { AuthStatusBanner(it, loading = false, error = true) }
        when (val result = state) {
            PortfolioState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
            PortfolioState.Error -> ProfileStatus(localized("Erro ao carregar portfólio."), localized("Tentar novamente")) { viewModel.loadPortfolio() }
            is PortfolioState.Ready -> {
                if (result.busy) AuthStatusBanner(localized("Atualizando portfólio…"), loading = true)
                result.error?.let {
                    val message = when (it) {
                        MobileAuthErrorCode.INVALID_INPUT -> localized("Revise a categoria, a descrição e o tamanho da imagem.")
                        MobileAuthErrorCode.CONFLICT -> localized("O portfólio chegou ao limite ou foi alterado. Atualize a lista.")
                        else -> it.accountActionMessage(localized("Não foi possível atualizar o portfólio."))
                    }
                    AuthStatusBanner(message, loading = false, error = true)
                }
                result.message?.let { AuthStatusBanner(it, loading = false) }
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (result.items.isEmpty()) item { EmptyMessage(localized("Nenhuma imagem no seu portfólio.")) }
                    items(result.items, key = { it.id }) { item ->
                        PortfolioItemCard(item, enabled = !result.busy) { viewModel.removePortfolioItem(item.id) }
                    }
                    if (publishedSlug != null) item {
                        Column(Modifier.fillMaxWidth()) {
                            PortfolioReviews(
                                publicReviews?.takeIf { it.slug == publishedSlug },
                                onRetry = { viewModel.loadReviews(publishedSlug) },
                                showRating = true
                            )
                        }
                    }
                }
            }
        }
    }

    upload?.let { bytes ->
        PortfolioMetadataDialog(
            caption = "",
            altText = "",
            category = "portrait",
            title = localized("Adicionar Imagem"),
            onDismiss = { upload = null },
            onConfirm = { altText, caption, category ->
                viewModel.addPortfolioImage(bytes, "image/jpeg", altText, caption, category)
                upload = null
            }
        )
    }
}

@Composable
private fun PortfolioItemCard(item: PortfolioItem, enabled: Boolean, onRemove: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandPhoto(Modifier.size(70.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.caption.ifBlank { item.altText.ifBlank { localized("Imagem do portfólio") } }, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(localized("Categoria: {0}", item.category), color = Muted, fontSize = 12.sp)
                if (item.isCover) Text(localized("★ Capa"), color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onRemove, enabled = enabled) { Text(localized("Excluir"), color = MaterialTheme.colorScheme.error) }
        }
    }
}

private fun readPortfolioUpload(context: Context, uri: Uri): ByteArray? {
    return try {
        val resolver = context.contentResolver
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val scaled = scalePortfolioBitmap(bitmap, 1200)
        val out = ByteArrayOutputStream()
        if (scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)) out.toByteArray() else null
    } catch (_: Exception) {
        null
    }
}

private fun scalePortfolioBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= maxDim && h <= maxDim) return bitmap
    val ratio = w.toFloat() / h.toFloat()
    val newW = if (w > h) maxDim else (maxDim * ratio).toInt()
    val newH = if (w > h) (maxDim / ratio).toInt() else maxDim
    return Bitmap.createScaledBitmap(bitmap, newW.coerceAtLeast(1), newH.coerceAtLeast(1), true)
}

@Composable
private fun PortfolioMetadataDialog(
    caption: String,
    altText: String,
    category: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var c by rememberSaveable { mutableStateOf(caption) }
    var a by rememberSaveable { mutableStateOf(altText) }
    var cat by rememberSaveable { mutableStateOf(category) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                LensField(localized("Legenda"), c, { c = it })
                Spacer(Modifier.height(10.dp))
                LensField(localized("Texto Alternativo (mín. 3 caracteres)"), a, { a = it })
                Spacer(Modifier.height(10.dp))
                LensField(localized("Categoria"), cat, { cat = it })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(a.trim(), c.trim(), cat.trim()) }, enabled = a.trim().length >= 3) { Text(localized("Confirmar")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(localized("Cancelar")) } },
        containerColor = SurfaceHigh
    )
}

internal fun initialsForDisplay(name: String?): String = name.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "LC" }

@Composable
private fun QrCodeImage(content: String) {
    val bitmap = remember(content) {
        runCatching { TotpQrCode.bitmap(content) }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = localized("QR Code para configurar o aplicativo autenticador"),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(10.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Text(localized("Não foi possível gerar o QR Code. Use a chave manual abaixo."), color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun TwoFactorEnrollmentDialog(
    state: TwoFactorEnrollmentState,
    onRetry: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val ready = state as? TwoFactorEnrollmentState.Ready
    val loading = state == TwoFactorEnrollmentState.Loading
    val busy = loading || ready?.verifying == true
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localized("Ativar verificação em duas etapas"), color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when {
                    loading -> AuthStatusBanner(localized("Preparando o autenticador..."), loading = true)
                    ready == null -> AuthStatusBanner(localized("Não foi possível iniciar a configuração."), loading = false, error = true)
                    else -> {
                        Text(localized("Escaneie o QR Code com seu aplicativo autenticador."), color = Muted, fontSize = 12.sp)
                        QrCodeImage(ready.setup.otpauthUrl)
                        Text(localized("Chave para configuração manual"), color = Muted, fontSize = 12.sp)
                        Text(ready.setup.secret, color = Ink, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Text(localized("Guarde os códigos de recuperação em um local seguro."), color = Muted, fontSize = 12.sp)
                        ready.setup.recoveryCodes.forEach { recoveryCode ->
                            Text(recoveryCode, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        LensField(localized("Código de 6 dígitos"), code, { entered ->
                            code = entered.filter { it in '0'..'9' }.take(6)
                        }, KeyboardType.Number)
                        if (ready.verifying) {
                            AuthStatusBanner(localized("Verificando o código..."), loading = true)
                        } else if (ready.error != null) {
                            AuthStatusBanner(ready.error.twoFactorMessage(), loading = false, error = true)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (state == TwoFactorEnrollmentState.Error) {
                Button(onClick = onRetry) { Text(localized("Tentar novamente")) }
            } else if (ready != null) {
                Button(
                    onClick = {
                        onConfirm(code)
                        code = ""
                    },
                    enabled = code.length == 6 && !busy
                ) { Text(localized("Ativar 2FA")) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(localized("Cancelar")) } },
        containerColor = SurfaceHigh
    )
}

@Composable
private fun AccountScreen(
    user: User?,
    authentication: AuthenticationState,
    canUseProfessionalMode: Boolean,
    onNavigate: (Screen) -> Unit,
    onLogin: () -> Unit,
    onProfessionalMode: () -> Unit,
    onLogout: () -> Unit
) = AppPage(Screen.Account, onNavigate) {
    if (user == null) {
        GuestTabSignIn(localized("Seu perfil"), localized("Entre na sua conta para acessar seus dados e configurações."), onLogin)
        return@AppPage
    }

    val actions = listOf(
        Triple(localized("Meus dados"), Icons.Default.Person, Screen.AccountData),
        Triple(localized("Configurações"), Icons.Default.Settings, Screen.Settings),
        Triple(localized("Segurança (2FA)"), Icons.Default.Lock, Screen.AccountSecurity),
        Triple(localized("Notificações"), Icons.Default.Email, Screen.Notifications),
        Triple(localized("Ajuda e suporte"), Icons.AutoMirrored.Filled.List, Screen.Support),
        Triple(localized("Sobre o Lens Click"), Icons.Default.Info, Screen.About)
    )
    val loggingOut = authentication is AuthenticationState.Loading

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon(
                Icons.Default.Settings,
                contentDescription = localized("Configurações"),
                tint = Ink,
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable { onNavigate(Screen.Settings) }
                    .semantics { role = Role.Button }
                    .padding(12.dp)
            )
        }
        Avatar(initialsForDisplay(user.name), Modifier.size(86.dp))
        Spacer(Modifier.height(10.dp))
        Text(user.name, color = Ink, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(user.email, color = Muted, fontSize = 13.sp)
        Text(if (canUseProfessionalMode) localized("Modo cliente • conta de fotógrafo") else localized("Conta de cliente"), color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(22.dp))
        if (canUseProfessionalMode) {
            OutlinedButton(onClick = onProfessionalMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FieldBorder)) { Text(localized("Voltar ao modo fotógrafo"), color = Ink, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(14.dp))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(12.dp))) {
            actions.forEach { (label, icon, destination) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 56.dp)
                        .clickable(role = Role.Button) { onNavigate(destination) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
                    Text(label, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("›", color = Muted, fontSize = 20.sp)
                }
                HorizontalDivider(color = Line)
            }
        }
        Spacer(Modifier.height(24.dp))
        LogoutButton(onLogout, enabled = !loggingOut)
        Spacer(Modifier.height(8.dp))
    }

}

@Composable
private fun AccountDataScreen(viewModel: LensClickViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var choosingFile by rememberSaveable { mutableStateOf(false) }
    var exportBusy by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf<String?>(null) }
    var exportSaved by rememberSaveable { mutableStateOf(false) }
    var deleteDialogOpen by rememberSaveable { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        choosingFile = false
        val json = pendingJson
        pendingJson = null
        if (uri != null) {
            exportBusy = true
            scope.launch {
                try {
                    val data = json ?: viewModel.fetchAccountExport()
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: throw IOException("Output stream unavailable")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(data) }
                    }
                    exportSaved = true
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    exportError = failure.remoteErrorCode().accountActionMessage(localized("Não foi possível salvar o arquivo."))
                } finally {
                    exportBusy = false
                }
            }
        }
    }

    SettingsPage(localized("Meus Dados (LGPD)"), onBack) {
        Text(localized("Exporte uma cópia dos seus dados ou gerencie a exclusão da conta."), color = Muted)
        WebSettingsLinks(localized("Editar dados e foto no site") to "/perfil")
        PrimaryButton(localized("Exportar meus dados"), {
            exportBusy = true
            exportSaved = false
            exportError = null
            scope.launch {
                try {
                    pendingJson = viewModel.fetchAccountExport()
                    choosingFile = true
                    filePicker.launch("lensclick-dados.json")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    choosingFile = false
                    pendingJson = null
                    exportError = failure.remoteErrorCode().accountActionMessage(localized("Não foi possível exportar os dados."))
                } finally {
                    exportBusy = false
                }
            }
        }, enabled = !exportBusy && !choosingFile)
        OutlinedButton(
            onClick = { deleteDialogOpen = true },
            enabled = !exportBusy && !choosingFile,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) { Text(localized("Excluir minha conta"), color = MaterialTheme.colorScheme.error) }
        if (exportBusy) AuthStatusBanner(localized("Exportando seus dados…"), loading = true)
        if (exportSaved) AuthStatusBanner(localized("Cópia dos seus dados salva em JSON."), loading = false)
        exportError?.let { AuthStatusBanner(it, loading = false, error = true) }
    }

    if (deleteDialogOpen) {
        var password by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!busy) deleteDialogOpen = false },
            title = { Text(localized("Excluir Conta Permanentemente"), color = Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(localized("Esta ação é irreversível. Digite sua senha atual para confirmar a exclusão da conta."), color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    LensField(localized("Senha atual"), password, { password = it }, password = true)
                    if (busy) AuthStatusBanner(localized("Excluindo conta…"), loading = true)
                    error?.let { AuthStatusBanner(it, loading = false, error = true) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    busy = true
                    error = null
                    viewModel.deleteAccount(
                        password,
                        onSuccess = { deleteDialogOpen = false },
                        onError = { error = it; busy = false }
                    )
                }, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, Color.White),
                    enabled = password.isNotBlank() && !busy
                ) { Text(localized("Excluir Permanentemente")) }
            },
            dismissButton = { TextButton(onClick = { deleteDialogOpen = false }, enabled = !busy) { Text(localized("Cancelar")) } },
            containerColor = SurfaceHigh
        )
    }
}

@Composable
private fun AccountSecurityScreen(viewModel: LensClickViewModel, onBack: () -> Unit) {
    val status by viewModel.twoFactorStatus.collectAsStateWithLifecycle()
    val loading by viewModel.twoFactorStatusLoading.collectAsStateWithLifecycle()
    val twoFactorEnrollment by viewModel.twoFactorEnrollment.collectAsStateWithLifecycle()
    var disable2FaOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.loadTwoFactorStatus() }

    SettingsPage(localized("Segurança & 2FA"), onBack) {
        Text(localized("Verificação em duas etapas"), color = Muted, style = MaterialTheme.typography.titleMedium)
        when {
            loading -> AuthStatusBanner(localized("Consultando a verificação…"), loading = true)
            status == null -> {
                AuthStatusBanner(localized("Não foi possível consultar o status da verificação."), loading = false, error = true)
                TextButton(onClick = viewModel::loadTwoFactorStatus) { Text(localized("Consultar status")) }
            }
            status?.enabled == true -> {
                FilterChip(localized("2FA Ativado"), selected = true)
                OutlinedButton(
                    onClick = { disable2FaOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(localized("Desativar 2FA"), color = MaterialTheme.colorScheme.error) }
            }
            else -> {
                FilterChip(localized("2FA Desativado"))
                PrimaryButton(localized("Configurar 2FA (TOTP)"), viewModel::setupTwoFactor)
            }
        }
    }

    if (twoFactorEnrollment != TwoFactorEnrollmentState.Idle) {
        TwoFactorEnrollmentDialog(
            state = twoFactorEnrollment,
            onRetry = viewModel::setupTwoFactor,
            onConfirm = viewModel::enableTwoFactor,
            onDismiss = viewModel::closeTwoFactorSetup
        )
    }

    if (disable2FaOpen) {
        var password by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var submitting by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!submitting) disable2FaOpen = false },
            title = { Text(localized("Desativar 2FA"), color = Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(localized("Informe sua senha atual e um código do autenticador ou de recuperação. Ao desativar o 2FA, todas as sessões serão encerradas."), color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    LensField(localized("Senha atual"), password, { password = it }, password = true)
                    Spacer(Modifier.height(12.dp))
                    LensField(localized("Código TOTP / Recuperação"), code, { code = it }, KeyboardType.Ascii)
                    if (submitting) {
                        AuthStatusBanner(localized("Desativando a verificação..."), loading = true)
                    } else {
                        errorMessage?.let { AuthStatusBanner(it, loading = false, error = true) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        submitting = true
                        errorMessage = null
                        viewModel.disableTwoFactor(password, code) { error ->
                            submitting = false
                            if (error == null) disable2FaOpen = false
                            errorMessage = error?.twoFactorMessage()
                        }
                        password = ""
                        code = ""
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, Color.White),
                    enabled = password.isNotBlank() && code.isNotBlank() && !submitting
                ) { Text(localized("Confirmar Desativação")) }
            },
            dismissButton = { TextButton(onClick = { disable2FaOpen = false }, enabled = !submitting) { Text(localized("Cancelar")) } },
            containerColor = SurfaceHigh
        )
    }

}

@Composable
private fun GuestTabSignIn(title: String, description: String, onLogin: () -> Unit, onDemo: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageTitle(title)
        Text(description, color = Muted, style = MaterialTheme.typography.bodyLarge)
        PrimaryButton(localized("Entrar na conta"), onLogin)
        onDemo?.let { TextButton(onClick = it) { Text(localized("Orçamentos de demonstração")) } }
    }
}
