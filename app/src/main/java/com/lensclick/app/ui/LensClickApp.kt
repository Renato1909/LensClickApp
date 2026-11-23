package com.lensclick.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lensclick.app.R
import com.lensclick.app.data.local.FairPhotographer
import com.lensclick.app.data.local.Photographer
import com.lensclick.app.data.local.User
import com.lensclick.app.data.local.readFairPhotographers
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.loadPublicPortfolioPreview
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.PortfolioItem
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.PublicPhotographer
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
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

enum class Screen {
    Onboarding, AccountType, Login, SignUp, PhotographerSignUp,
    Home, Search, Photographer, Quote, Budgets, Conversations, Chat, Account,
    ProDashboard, ProRequests, ProAgenda, ProProfile, ProPortfolio
}
private const val PhotographerRoute = "Photographer/{slug}"
private const val QuoteRoute = "Quote/{slug}"
private const val FairCatalogRoute = "Feira"
private const val FairProfileRoute = "Feira/{slug}"

@Composable
fun LensClickApp(viewModel: LensClickViewModel = viewModel(factory = LensClickViewModel.Factory)) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val fairPhotographers = remember(context) { readFairPhotographers(context) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentEntry by navController.currentBackStackEntryAsState()
    val photographers by viewModel.photographers.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val agenda by viewModel.agenda.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authentication by viewModel.authentication.collectAsStateWithLifecycle()
    val currentPhotographer = photographers.firstOrNull { it.userId == currentUser?.id }
    fun resetTo(target: Screen) {
        focusManager.clearFocus()
        keyboardController?.hide()
        navController.navigate(target.name) {
            popUpTo(navController.graph.id)
            launchSingleTop = true
        }
    }
    fun go(target: Screen) {
        if (target in setOf(Screen.Home, Screen.Search, Screen.Budgets, Screen.Conversations, Screen.Account,
                Screen.ProDashboard, Screen.ProRequests, Screen.ProAgenda, Screen.ProProfile, Screen.ProPortfolio) &&
                !(target == Screen.Search && currentUser == null)) {
            resetTo(target)
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
            navController.navigate(target.name) { launchSingleTop = true }
        }
    }

    LaunchedEffect(currentEntry?.destination?.route, authentication) {
        val route = currentEntry?.destination?.route ?: return@LaunchedEffect
        if (authentication is AuthenticationState.Authenticated &&
            route in setOf(Screen.Onboarding.name, Screen.AccountType.name, Screen.Login.name, Screen.SignUp.name, Screen.PhotographerSignUp.name)
        ) {
            val user = (authentication as AuthenticationState.Authenticated).user
            resetTo(if (user.role == "photographer") Screen.ProDashboard else Screen.Home)
        } else if (authentication is AuthenticationState.Error && route == Screen.Onboarding.name) {
            resetTo(Screen.Login)
        } else if (authentication !is AuthenticationState.Authenticated &&
            authentication !is AuthenticationState.TwoFactorRequired &&
            Screen.entries.any { it.name == route && it !in setOf(
                Screen.Onboarding, Screen.AccountType, Screen.Login, Screen.SignUp, Screen.PhotographerSignUp, Screen.Search) }) {
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
                onVerify2Fa = { token, code -> viewModel.verifyTwoFactorChallenge(token, code) },
                onSignUp = { go(Screen.AccountType) }
            ) }
            composable(Screen.SignUp.name) { SignUpScreen(
                onDone = { name, email, password, result ->
                    viewModel.register(name, email, password, result)
                },
                onLogin = { go(Screen.Login) }
            ) }
            composable(Screen.PhotographerSignUp.name) { PhotographerSignUpScreen(
                onDone = { name, email, password, specialty, city, price, bio, result ->
                    viewModel.registerPhotographer(name, email, password, specialty, city, price, bio, result)
                },
                onLogin = { go(Screen.Login) }
            ) }
            composable(Screen.Home.name) { HomeScreen(photographers, onNavigate = ::go) }
            composable(Screen.Search.name) {
                SearchScreen(
                    viewModel,
                    guest = currentUser == null,
                    onNavigate = ::go,
                    onPhotographer = { slug -> navController.navigate("Photographer/$slug") },
                    onFair = { navController.navigate(FairCatalogRoute) }
                )
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
                    ProfileStatus("Perfil de demonstração indisponível.", "Voltar") { navController.popBackStack() }
                } else {
                    FairProfileScreen(photographer, onBack = { navController.popBackStack() })
                }
            }
            composable(PhotographerRoute) { entry ->
                val slug = entry.arguments?.getString("slug").orEmpty()
                PhotographerScreen(viewModel, slug, onBack = { navController.popBackStack() }, onQuote = { navController.navigate("Quote/$slug") })
            }
            composable(QuoteRoute) { entry ->
                val slug = entry.arguments?.getString("slug").orEmpty()
                QuoteScreen(
                    onBack = { navController.popBackStack() },
                    onSent = { type, date, place, duration, description, details ->
                        viewModel.createBudgetRequest(slug, BudgetRequestDraft(type, date, place, duration, description, details)) { success -> if (success) go(Screen.Budgets) }
                    }
                )
            }
            composable(Screen.Budgets.name) { BudgetsScreen(viewModel, budgets, favorites, onNavigate = ::go) }
            composable(Screen.Conversations.name) { ConversationsScreen(viewModel, photographers, onNavigate = ::go) }
            composable(Screen.Chat.name) { ChatScreen(viewModel, onBack = { navController.popBackStack() }) }
            composable(Screen.Account.name) { AccountScreen(
                viewModel = viewModel,
                user = currentUser,
                authentication = authentication,
                canUseProfessionalMode = currentUser?.role == "photographer",
                onNavigate = ::go,
                onProfessionalMode = { resetTo(Screen.ProDashboard) },
                onLogout = { viewModel.logout() }
            ) }
            composable(Screen.ProDashboard.name) { PhotographerDashboardScreen(currentPhotographer, budgets, ::go, onClientMode = { resetTo(Screen.Home) }) }
            composable(Screen.ProRequests.name) { PhotographerRequestsScreen(viewModel, budgets, ::go) }
            composable(Screen.ProAgenda.name) { PhotographerAgendaScreen(agenda, ::go) }
            composable(Screen.ProProfile.name) { ProfessionalProfileScreen(viewModel, ::go, onClientMode = { resetTo(Screen.Home) }, onLogout = { viewModel.logout() }) }
            composable(Screen.ProPortfolio.name) { PortfolioScreen(viewModel, ::go) }
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
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
                append("Encontre o\n")
                withStyle(SpanStyle(color = Accent, fontStyle = FontStyle.Italic)) {
                    append("fotógrafo ideal.")
                }
            },
            color = Ink,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            "Conheça fotógrafos profissionais, explore portfólios e peça orçamentos.",
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
        PrimaryButton("Explorar fotógrafos", onBrowse)
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Criar conta", onStart)
        CenterLink("Já tem uma conta?", "Entrar", onLogin)
    }
}

@Composable
private fun AccountTypeScreen(onClient: () -> Unit, onPhotographer: () -> Unit, onLogin: () -> Unit) {
    AuthFrame("Como você deseja usar o LensClick?", "Escolha o tipo de conta para continuar.") {
        AccountTypeOption("Sou Cliente", "Quero contratar serviços fotográficos", Icons.Default.Person, onClient)
        Spacer(Modifier.height(12.dp))
        AccountTypeOption("Sou Fotógrafo", "Quero divulgar meu trabalho e receber orçamentos", Icons.Default.Add, onPhotographer)
        Spacer(Modifier.height(12.dp))
        CenterLink("Já possui conta?", "Entrar", onLogin)
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
    onVerify2Fa: (String, String) -> Unit,
    onSignUp: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var totpCode by rememberSaveable { mutableStateOf("") }
    var recoveryOpen by rememberSaveable { mutableStateOf(false) }
    val submitting = authentication is AuthenticationState.Loading
    val is2FA = authentication is AuthenticationState.TwoFactorRequired
    val errorMessage = when (authentication) {
        is AuthenticationState.Error -> authentication.code.loginMessage()
        AuthenticationState.SessionExpired -> "Sua sessão expirou ou foi revogada. Entre novamente."
        else -> null
    }
    val completedMessage = authentication == AuthenticationState.LogoutCompleted

    AuthFrame(if (is2FA) "Verificação 2FA" else "Bem-vindo de volta!", if (is2FA) "Digite o código do seu autenticador" else "Entre para continuar") {
        if (is2FA) {
            val challenge = authentication as AuthenticationState.TwoFactorRequired
            Text("Insira o código de 6 dígitos do app TOTP ou um código de recuperação.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            LensField("Código TOTP / Recuperação", totpCode, { totpCode = it }, KeyboardType.Number)
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Confirmar código", {
                onVerify2Fa(challenge.challengeToken, totpCode)
            }, enabled = totpCode.isNotBlank() && !submitting)
        } else {
            LensField("E-mail", email, { email = it }, KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            LensField("Senha", password, { password = it }, password = true)
            TextButton(onClick = { recoveryOpen = true }, contentPadding = PaddingValues(0.dp)) { Text("Esqueci minha senha", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
            Spacer(Modifier.height(26.dp))
            if (submitting) {
                AuthStatusBanner("Conectando à sua conta...", loading = true)
            } else if (errorMessage != null) {
                AuthStatusBanner(errorMessage, loading = false, error = true)
            } else if (completedMessage) {
                AuthStatusBanner("Você saiu da conta com segurança.", loading = false)
            }
            PrimaryButton(if (submitting) "Entrando..." else "Entrar", {
                onLogin(email, password)
            }, enabled = email.isNotBlank() && password.isNotBlank() && !submitting)
            Spacer(Modifier.height(12.dp))
            CenterLink("Não tem uma conta?", "Cadastre-se", onSignUp)
        }
    }

    if (recoveryOpen) ActionDialog("Recuperar senha", "Enviaremos as instruções de recuperação para $email.") { recoveryOpen = false }
}

private fun MobileAuthErrorCode.loginMessage(): String = when (this) {
    MobileAuthErrorCode.INVALID_INPUT,
    MobileAuthErrorCode.UNAUTHENTICATED -> "E-mail ou senha inválidos."
    MobileAuthErrorCode.RATE_LIMITED -> "Muitas tentativas. Aguarde antes de tentar novamente."
    MobileAuthErrorCode.NETWORK -> "Não foi possível conectar. Verifique sua internet."
    MobileAuthErrorCode.SERVER -> "O serviço está indisponível. Tente novamente."
    else -> "Não foi possível entrar. Tente novamente."
}

@Composable
private fun AuthStatusBanner(message: String, loading: Boolean, error: Boolean = false) {
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
private fun SignUpScreen(onDone: (String, String, String, (Boolean) -> Unit) -> Unit, onLogin: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by rememberSaveable { mutableStateOf(false) }
    AuthFrame("Criar conta de cliente", "Preencha seus dados para começar") {
        LensField("Nome completo", name, { name = it })
        Spacer(Modifier.height(12.dp))
        LensField("E-mail", email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        LensField("Senha (mínimo 8 caracteres)", password, { password = it }, password = true)
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Criar minha conta", {
            busy = true
            onDone(name, email, password) { busy = false }
        }, enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 8 && !busy)
        Spacer(Modifier.height(12.dp))
        CenterLink("Já tem conta?", "Entrar", onLogin)
    }
}

@Composable
private fun PhotographerSignUpScreen(
    onDone: (String, String, String, String, String, String, String, (Boolean) -> Unit) -> Unit,
    onLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("Casamento") }
    var city by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var busy by rememberSaveable { mutableStateOf(false) }
    AuthFrame("Criar conta de Fotógrafo", "Cadastre seu perfil profissional no LensClick") {
        LensField("Nome profissional", name, { name = it })
        Spacer(Modifier.height(10.dp))
        LensField("E-mail", email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        LensField("Senha (mín. 8 caracteres)", password, { password = it }, password = true)
        Spacer(Modifier.height(10.dp))
        LensField("Especialidade principal", specialty, { specialty = it })
        Spacer(Modifier.height(10.dp))
        LensField("Cidade de atuação", city, { city = it })
        Spacer(Modifier.height(10.dp))
        LensField("Preço inicial (R$)", price, { price = it }, KeyboardType.Number)
        Spacer(Modifier.height(10.dp))
        LensField("Apresentação / Bio", bio, { bio = it }, maxLines = 3)
        Spacer(Modifier.height(22.dp))
        PrimaryButton("Cadastrar como Fotógrafo", {
            busy = true
            onDone(name, email, password, specialty, city, price, bio) { busy = false }
        }, enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 8 && city.isNotBlank() && !busy)
        Spacer(Modifier.height(12.dp))
        CenterLink("Já tem conta?", "Entrar", onLogin)
    }
}

@Composable
private fun LensField(
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
private fun lensFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceHigh,
    unfocusedContainerColor = SurfaceHigh,
    focusedBorderColor = Accent,
    unfocusedBorderColor = FieldBorder
)

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
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
        Text("Sair da conta", fontWeight = FontWeight.SemiBold)
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

@Composable private fun ActionDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, color = Ink, fontWeight = FontWeight.Bold) }, text = { Text(message, color = Muted, fontSize = 13.sp) }, confirmButton = { TextButton(onClick = onDismiss) { Text("OK", color = Ink, fontWeight = FontWeight.Bold) } }, containerColor = SurfaceHigh)
}

@Composable
private fun AppPage(
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
                tabs.forEach { (screen, label, icon) ->
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
private fun HomeScreen(photographers: List<Photographer>, onNavigate: (Screen) -> Unit) =
    AppPage(Screen.Home, onNavigate) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Sand, MaterialTheme.shapes.large)
                    .padding(20.dp)
            ) {
                Text("Campinas, SP", color = Accent, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(9.dp))
                Text(
                    "Encontre o fotógrafo perfeito",
                    color = Ink,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(18.dp))
                SearchBar { onNavigate(Screen.Search) }
            }
            Spacer(Modifier.height(24.dp))
            SectionTitle("Categorias") { onNavigate(Screen.Search) }
            Spacer(Modifier.height(8.dp))
            CategoryRow()
            if (photographers.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Destaques da semana")
                Spacer(Modifier.height(12.dp))
                PhotographerCards(photographers) { onNavigate(Screen.Search) }
            }
        }
    }

@Composable
private fun SearchBar(onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(SurfaceHigh)
            .border(1.dp, FieldBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
        Text(
            "Buscar por cidade ou especialidade",
            color = Ink,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Accent, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun SectionTitle(title: String, onMore: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (onMore != null) {
            TextButton(onClick = onMore) { Text("Ver todos") }
        }
    }
}

@Composable
private fun PageTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        color = Ink,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
private fun CategoryRow() {
    val categories = listOf("Casamento", "Retrato", "Corporativo", "Eventos", "Ensaio")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { FilterChip(it) }
    }
}

@Composable
private fun PhotographerCards(photographers: List<Photographer>, onClick: () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(photographers) { photographer ->
            val shape = MaterialTheme.shapes.medium
            Column(
                Modifier
                    .width(184.dp)
                    .clip(shape)
                    .background(SurfaceHigh)
                    .border(1.dp, Line, shape)
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(10.dp)
            ) {
                BrandPhoto(Modifier.fillMaxWidth().height(100.dp))
                Spacer(Modifier.height(9.dp))
                Text(photographer.name, color = Ink, style = MaterialTheme.typography.titleMedium)
                Text(photographer.specialty, color = Muted, style = MaterialTheme.typography.bodyMedium)
                Text(
                    photographer.price,
                    color = Accent,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchScreen(
    viewModel: LensClickViewModel,
    guest: Boolean,
    onNavigate: (Screen) -> Unit,
    onPhotographer: (String) -> Unit,
    onFair: () -> Unit
) = AppPage(Screen.Search, onNavigate) {
    var query by rememberSaveable { mutableStateOf("São Paulo") }
    var selectedSpecialty by rememberSaveable { mutableStateOf<String?>(null) }
    val searchState by viewModel.publicSearch.collectAsStateWithLifecycle()
    val specialties = listOf(
        null to "Todos",
        "wedding" to "Casamento",
        "portrait" to "Retrato",
        "corporate" to "Corporativo",
        "events" to "Eventos"
    )

    LaunchedEffect(query, selectedSpecialty) {
        viewModel.searchPublicPhotographers(query, selectedSpecialty, 1)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageTitle("Buscar Fotógrafos")
                if (guest) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Sand, MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Entrar para usar sua conta",
                            color = Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Modo visitante", color = Accent, style = MaterialTheme.typography.labelSmall)
                    }
                }
                SecondaryButton("Perfis da feira", onFair)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cidade") },
                    placeholder = { Text("Ex.: São Paulo") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = lensFieldColors()
                )
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    specialties.forEach { (code, label) ->
                        FilterChip(label, selectedSpecialty == code) {
                            selectedSpecialty = code
                        }
                    }
                }
                HorizontalDivider(color = Line)
            }
        }

        when (val state = searchState) {
            PublicSearchState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            PublicSearchState.Error -> item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Erro ao conectar à busca remota.", color = Ink, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { viewModel.searchPublicPhotographers(query, selectedSpecialty, 1) }) {
                        Text("Tentar novamente")
                    }
                }
            }
            is PublicSearchState.Empty -> item {
                EmptyMessage("Nenhum fotógrafo encontrado.")
            }
            is PublicSearchState.Ready -> {
                items(state.page.photographers, key = { it.slug }) { photographer ->
                    PublicPhotographerRow(photographer) { onPhotographer(photographer.slug) }
                }
            }
        }
    }
}

@Composable
private fun PublicPhotographerRow(photographer: PublicPhotographer, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    val location = listOf(photographer.city, photographer.state)
        .filter { it.isNotBlank() }
        .joinToString(", ")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceHigh)
            .border(1.dp, Line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(initialsForDisplay(photographer.name), Modifier.size(50.dp))
        Column(Modifier.weight(1f)) {
            Text(photographer.name, color = Ink, style = MaterialTheme.typography.titleMedium)
            Text(
                photographer.specialties.joinToString(" · ") { specialtyLabel(it) },
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
            if (location.isNotBlank()) {
                Text(location, color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            photographer.startingPriceCents?.let { cents ->
                Text(
                    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                        .format(BigDecimal.valueOf(cents, 2)),
                    color = Accent,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Accent, modifier = Modifier.size(17.dp))
    }
}

private fun specialtyLabel(code: String): String = when (code.lowercase()) {
    "wedding", "casamento" -> "Casamento"
    "portrait", "retrato" -> "Retrato"
    "corporate", "corporativo" -> "Corporativo"
    "events", "eventos" -> "Eventos"
    else -> code.replaceFirstChar { it.uppercase() }
}

@Composable
private fun FilterChip(label: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
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
private fun Avatar(initials: String, modifier: Modifier = Modifier, background: Color = Sand) {
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
    FairFrame("Perfis da feira", onBack) {
        Text("Fotógrafos fictícios com portfólios de exemplo.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(14.dp))
        photographers.forEach { photographer ->
            PublicPhotographerRow(photographer.summary) { onPhotographer(photographer.summary.slug) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FairProfileScreen(photographer: FairPhotographer, onBack: () -> Unit) {
    FairFrame(photographer.summary.name, onBack) {
        Text(
            photographer.summary.specialties.joinToString(" · ") { specialtyLabel(it) } +
                " — ${photographer.summary.city}, ${photographer.summary.state}",
            color = Accent,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(12.dp))
        Text("Perfil fictício para demonstração.", color = Accent, style = MaterialTheme.typography.labelLarge)
        Text(photographer.bio, color = Muted, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        SectionTitle("Portfólio")
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
private fun PhotographerScreen(viewModel: LensClickViewModel, slug: String, onBack: () -> Unit, onQuote: () -> Unit) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favorite = favorites.any { it.slug == slug }
    val state by viewModel.publicProfile.collectAsStateWithLifecycle()
    val publicReviews by viewModel.publicReviews.collectAsStateWithLifecycle()
    var reviewDialogOpen by rememberSaveable { mutableStateOf(false) }
    var reviewRating by rememberSaveable { mutableIntStateOf(5) }
    var reviewComment by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(slug) {
        viewModel.loadPublicPhotographer(slug)
        viewModel.loadReviews(slug)
    }

    when (val result = state) {
        PublicProfileState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
        PublicProfileState.Error -> ProfileStatus("Não foi possível carregar este perfil.", "Tentar novamente") { viewModel.loadPublicPhotographer(slug) }
        PublicProfileState.NotFoundOrUnpublished -> ProfileStatus("Este perfil não está disponível ou ainda não foi publicado.", "Voltar", onBack)
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
                                Avatar(initialsForDisplay(profile.profile.name), Modifier.size(50.dp), SurfaceHigh)
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
                                profile.profile.ratingAverage?.let {
                                    Text(
                                        "★ $it (${profile.profile.reviewCount} avaliações)",
                                        color = Accent,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(22.dp))
                            SectionTitle("Sobre mim")
                            Text(
                                profile.bio.ifBlank { "Este fotógrafo ainda não adicionou uma apresentação." },
                                color = Muted,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Spacer(Modifier.height(22.dp))
                            HorizontalDivider(color = Line)
                            Spacer(Modifier.height(17.dp))
                            SectionTitle("Portfólio")
                            Spacer(Modifier.height(10.dp))
                            if (profile.portfolio.isEmpty()) {
                                Text("Este fotógrafo ainda não publicou imagens.", color = Muted, fontSize = 12.sp)
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
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Avaliações",
                                    color = Ink,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f).semantics { heading() }
                                )
                                TextButton(onClick = { reviewDialogOpen = true }) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Avaliar", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            val reviewsList = publicReviews?.reviews ?: emptyList()
                            if (reviewsList.isEmpty()) {
                                Text("Ainda não há avaliações enviadas para este fotógrafo.", color = Muted, fontSize = 12.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    reviewsList.forEach { r ->
                                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(12.dp)).padding(12.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(r.authorName, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("★ ${r.rating}", color = Accent, fontSize = 12.sp)
                                            }
                                            if (r.comment.isNotBlank()) {
                                                Text(r.comment, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = Line)
                Row(
                    Modifier.fillMaxWidth().background(SurfaceHigh).padding(horizontal = 22.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onQuote, Modifier.weight(1f).heightIn(min = 52.dp), shape = MaterialTheme.shapes.medium) {
                        Text("Solicitar orçamento", textAlign = TextAlign.Center)
                    }
                    OutlinedButton(
                        { if (favorite) { favorites.firstOrNull { it.slug == slug }?.let { viewModel.removeFavorite(it.photographerId) {} } } else { viewModel.addFavorite(slug) {} } },
                        Modifier.size(54.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, if (favorite) Accent else Ink)
                    ) {
                        Icon(
                            if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (favorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                            tint = if (favorite) Accent else Ink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (reviewDialogOpen) {
                AlertDialog(
                    onDismissRequest = { reviewDialogOpen = false },
                    title = { Text("Avaliar Fotógrafo", color = Ink, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Selecione sua nota de 1 a 5 estrelas:", color = Muted, fontSize = 12.sp)
                            Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..5).forEach { star ->
                                    Text(
                                        if (star <= reviewRating) "★" else "☆",
                                        fontSize = 24.sp,
                                        color = Accent,
                                        modifier = Modifier.clickable { reviewRating = star }
                                    )
                                }
                            }
                            LensField("Comentário", reviewComment, { reviewComment = it }, maxLines = 3)
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.submitReview(slug, reviewRating, reviewComment) {
                                reviewDialogOpen = false
                                reviewComment = ""
                            }
                        }) { Text("Enviar Avaliação") }
                    },
                    dismissButton = {
                        TextButton(onClick = { reviewDialogOpen = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Accent)
    }
}

@Composable
private fun QuoteScreen(onBack: () -> Unit, onSent: (String, String, String, String, String, String) -> Unit) {
    var type by rememberSaveable { mutableStateOf("Casamento") }
    var date by rememberSaveable { mutableStateOf("") }
    var place by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Spacer(Modifier.width(10.dp))
            PageTitle("Solicitar orçamento", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        LensField("Tipo de evento", type, { type = it })
        Spacer(Modifier.height(10.dp))
        LensField("Data prevista", date, { date = it }, KeyboardType.Number)
        Spacer(Modifier.height(10.dp))
        LensField("Local / Cidade", place, { place = it })
        Spacer(Modifier.height(10.dp))
        LensField("Duração estimada", duration, { duration = it })
        Spacer(Modifier.height(10.dp))
        LensField("Descrição do evento", description, { description = it }, maxLines = 3)
        Spacer(Modifier.height(10.dp))
        LensField("Informações adicionais", details, { details = it }, maxLines = 2)
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Enviar solicitação", { onSent(type, date, place, duration, description, details) }, enabled = date.isNotBlank() && place.isNotBlank())
    }
}

@Composable
private fun BudgetsScreen(viewModel: LensClickViewModel, budgets: List<BudgetRequest>, favorites: List<Favorite>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Budgets, onNavigate) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var proposeItem by remember { mutableStateOf<BudgetRequest?>(null) }
    Column(Modifier.padding(top = 12.dp)) {
        PageTitle("Meus Pedidos & Favoritos", Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).horizontalScroll(rememberScrollState()).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip("Orçamentos (${budgets.size})", tab == 0) { tab = 0 }
            FilterChip("Favoritos (${favorites.size})", tab == 1) { tab = 1 }
        }
        Spacer(Modifier.height(14.dp))
        if (tab == 0) {
            if (budgets.isEmpty()) {
                EmptyMessage("Nenhum pedido de orçamento encontrado.")
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgets, key = { it.id }) { b ->
                        BudgetCard(b)
                    }
                }
            }
        } else {
            if (favorites.isEmpty()) {
                EmptyMessage("Você ainda não salvou fotógrafos favoritos.")
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favorites, key = { it.photographerId }) { f ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(14.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(f.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(f.slug, color = Muted, fontSize = 12.sp)
                            }
                            OutlinedButton(onClick = { viewModel.removeFavorite(f.photographerId) {} }) { Text("Remover") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(b: BudgetRequest) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(b.eventType, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            FilterChip(budgetStatusLabel(b.status), selected = true)
        }
        Spacer(Modifier.height(6.dp))
        Text("Data: ${b.eventDate} • Local: ${b.place}", color = Muted, fontSize = 12.sp)
        if (b.description.isNotBlank()) Text(b.description, color = Ink, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

private fun budgetStatusLabel(status: String): String = when (status.lowercase()) {
    "pending" -> "Aguardando"
    "proposed" -> "Proposta recebida"
    "accepted" -> "Aceito"
    "rejected" -> "Recusado"
    "cancelled" -> "Cancelado"
    else -> status
}

@Composable
private fun ConversationsScreen(viewModel: LensClickViewModel, photographers: List<Photographer>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Conversations, onNavigate) {
    var query by rememberSaveable { mutableStateOf("") }
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Column(Modifier.padding(top = 12.dp)) {
        PageTitle("Conversas", Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Buscar conversa", color = Muted, fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(17.dp),
            colors = lensFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        val filtered = conversations.filter { it.counterpartName.contains(query, true) || it.lastMessage.contains(query, true) }
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (filtered.isEmpty()) {
                item { EmptyMessage("Nenhuma conversa encontrada.") }
            } else {
                items(filtered, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(SurfaceHigh)
                            .border(1.dp, Line, MaterialTheme.shapes.medium)
                            .clickable {
                                viewModel.loadMessages(c.id)
                                onNavigate(Screen.Chat)
                            }
                            .padding(horizontal = 10.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(initialsForDisplay(c.counterpartName), Modifier.size(52.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.counterpartName, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(c.lastMessage.ifBlank { "Nenhuma mensagem ainda." }, color = Muted, fontSize = 12.sp, maxLines = 1)
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
private fun ChatScreen(viewModel: LensClickViewModel, onBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentConv = conversations.firstOrNull()

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Avatar(initialsForDisplay(currentConv?.counterpartName ?: "Chat"), Modifier.size(42.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(currentConv?.counterpartName ?: "Mensagens", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Conversa via LensClick", color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            CompactLogo(34)
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (messages.isEmpty()) {
                item { Bubble("Inicie a conversa digitando uma mensagem abaixo.", false) }
            } else {
                items(messages) { m ->
                    Bubble(m.text, m.isMine)
                }
            }
        }
        HorizontalDivider(color = Line)
        Row(Modifier.fillMaxWidth().background(SurfaceHigh).imePadding().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite sua mensagem...", color = Muted, fontSize = 12.sp) },
                shape = RoundedCornerShape(18.dp),
                colors = lensFieldColors()
            )
            Spacer(Modifier.width(9.dp))
            Button(
                onClick = {
                    if (input.isNotBlank() && currentConv != null) {
                        val textToSend = input
                        input = ""
                        viewModel.sendMessage(currentConv.id, textToSend) {}
                    }
                },
                modifier = Modifier.size(54.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                enabled = input.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar mensagem", modifier = Modifier.size(22.dp))
            }
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
        PageTitle("Painel do fotógrafo")
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(p?.initials ?: "FT", Modifier.size(56.dp)); Spacer(Modifier.width(14.dp)); Column { Text(p?.name ?: "Perfil Profissional", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(p?.specialty ?: "Fotografia", color = Muted, fontSize = 12.sp) } }
        Spacer(Modifier.height(20.dp))
        SectionTitle("Métricas")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProMetric("Pedidos", "${budgets.size}", Modifier.weight(1f))
            ProMetric("Agenda", "${budgets.count { it.status == "accepted" }}", Modifier.weight(1f))
            ProMetric("Avaliação", "—", Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("Ações Rápidas")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProAction("Editar Perfil", "Informações e preços", Modifier.weight(1f)) { onNavigate(Screen.ProProfile) }
            ProAction("Portfólio", "Gerenciar imagens", Modifier.weight(1f)) { onNavigate(Screen.ProPortfolio) }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FieldBorder)) { Text("Alternar para modo cliente", color = Ink, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable private fun ProMetric(label: String, value: String, modifier: Modifier) { Column(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 12.sp) } }
@Composable private fun ProAction(title: String, desc: String, modifier: Modifier, onClick: () -> Unit) { Column(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(14.dp)) { Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(desc, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) } }

@Composable
private fun PhotographerRequestsScreen(viewModel: LensClickViewModel, budgets: List<BudgetRequest>, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProRequests, onNavigate) {
    var proposeTarget by remember { mutableStateOf<BudgetRequest?>(null) }
    Column(Modifier.padding(top = 12.dp)) {
        PageTitle("Pedidos de Orçamento", Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        if (budgets.isEmpty()) {
            EmptyMessage("Nenhum pedido recebido no momento.")
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(budgets, key = { it.id }) { b ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(b.eventType, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            FilterChip(budgetStatusLabel(b.status), selected = true)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Cliente: ${b.counterpartName} • Data: ${b.eventDate}", color = Muted, fontSize = 12.sp)
                        if (b.description.isNotBlank()) Text(b.description, color = Ink, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton("Enviar proposta", { proposeTarget = b }, enabled = b.status == "pending")
                    }
                }
            }
        }
    }

    proposeTarget?.let { b ->
        ProposeDialog(
            onDismiss = { proposeTarget = null },
            onSend = { priceCents, conditions ->
                viewModel.proposeToRequest(b.id, priceCents, conditions) {
                    proposeTarget = null
                }
            }
        )
    }
}

@Composable
private fun ProposeDialog(onDismiss: () -> Unit, onSend: (Long, String) -> Unit) {
    var priceStr by rememberSaveable { mutableStateOf("") }
    var conditions by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enviar Proposta", color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                LensField("Valor total (R$)", priceStr, { priceStr = it }, KeyboardType.Number)
                Spacer(Modifier.height(10.dp))
                LensField("Condições / Detalhes", conditions, { conditions = it }, maxLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cents = (priceStr.toDoubleOrNull() ?: 0.0) * 100
                    onSend(cents.toLong(), conditions)
                },
                enabled = priceStr.isNotBlank()
            ) { Text("Enviar Proposta") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = SurfaceHigh
    )
}

@Composable
private fun PhotographerAgendaScreen(agenda: List<BudgetRequest>, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProAgenda, onNavigate) {
    Column(Modifier.padding(top = 12.dp)) {
        PageTitle("Minha Agenda", Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp))
        if (agenda.isEmpty()) {
            EmptyMessage("Nenhum evento agendado ou aceito.")
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
        ProfessionalProfileState.Error -> ProfileStatus("Não foi possível carregar seu perfil profissional.", "Tentar novamente") { viewModel.loadProfessionalProfile() }
        is ProfessionalProfileState.Ready -> ProfessionalProfileForm(result, viewModel, onClientMode, onLogout)
    }
}

@Composable
private fun ProfessionalProfileForm(
    state: ProfessionalProfileState.Ready,
    viewModel: LensClickViewModel,
    onClientMode: () -> Unit,
    onLogout: () -> Unit
) {
    var bio by rememberSaveable(state.profile) { mutableStateOf(state.profile.bio) }
    var city by rememberSaveable(state.profile) { mutableStateOf(state.profile.city) }
    var stateCode by rememberSaveable(state.profile) { mutableStateOf(state.profile.state) }
    var priceStr by rememberSaveable(state.profile) { mutableStateOf(state.profile.startingPriceCents?.let { "${it / 100}" }.orEmpty()) }
    var radiusStr by rememberSaveable(state.profile) { mutableStateOf(state.profile.serviceRadiusKm?.toString().orEmpty()) }
    var publicEmail by rememberSaveable(state.profile) { mutableStateOf(state.profile.publicEmail) }
    var publicPhone by rememberSaveable(state.profile) { mutableStateOf(state.profile.publicPhone) }
    var instagram by rememberSaveable(state.profile) { mutableStateOf(state.profile.instagram) }
    var website by rememberSaveable(state.profile) { mutableStateOf(state.profile.website) }
    var isPublished by rememberSaveable(state.profile) { mutableStateOf(state.profile.isPublished) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        PageTitle("Perfil Profissional")
        Text("Configure como seu perfil aparece para os clientes", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        LensField("Bio / Apresentação", bio, { bio = it }, maxLines = 4)
        Spacer(Modifier.height(10.dp))
        LensField("Cidade", city, { city = it })
        Spacer(Modifier.height(10.dp))
        LensField("Estado (UF)", stateCode, { stateCode = it })
        Spacer(Modifier.height(10.dp))
        LensField("Preço Inicial (R$)", priceStr, { priceStr = it }, KeyboardType.Number)
        Spacer(Modifier.height(10.dp))
        LensField("Raio de Atendimento (km)", radiusStr, { radiusStr = it }, KeyboardType.Number)
        Spacer(Modifier.height(10.dp))
        LensField("E-mail Público", publicEmail, { publicEmail = it }, KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        LensField("Telefone Público", publicPhone, { publicPhone = it }, KeyboardType.Phone)
        Spacer(Modifier.height(10.dp))
        LensField("Instagram", instagram, { instagram = it })
        Spacer(Modifier.height(10.dp))
        LensField("Website", website, { website = it }, KeyboardType.Uri)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Publicar perfil na busca", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            FilterChip(if (isPublished) "Publicado" else "Rascunho", selected = isPublished) { isPublished = !isPublished }
        }
        Spacer(Modifier.height(24.dp))
        if (state.saving) {
            AuthStatusBanner("Salvando perfil...", loading = true)
        } else if (state.saved) {
            AuthStatusBanner("Perfil salvo com sucesso!", loading = false)
        } else if (state.error != null) {
            AuthStatusBanner(profileErrorMessage(state.error), loading = false, error = true)
        }
        PrimaryButton("Salvar Perfil", {
            val priceCents = priceStr.toDoubleOrNull()?.let { (it * 100).toLong() }
            val radius = radiusStr.toIntOrNull()
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
        }, enabled = !state.saving)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FieldBorder)) { Text("Modo Cliente", color = Ink, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(12.dp))
        LogoutButton(onLogout, enabled = !state.saving)
    }
}

private fun profileErrorMessage(code: MobileAuthErrorCode): String = when (code) {
    MobileAuthErrorCode.INVALID_INPUT -> "Dados do perfil inválidos. Verifique os campos."
    MobileAuthErrorCode.UNAUTHENTICATED -> "Sua sessão expirou. Faça login novamente."
    MobileAuthErrorCode.RATE_LIMITED -> "Muitas alterações. Aguarde um instante."
    MobileAuthErrorCode.NETWORK -> "Não foi possível conectar. Verifique sua rede."
    else -> "Erro ao salvar perfil. Tente novamente."
}

@Composable
private fun PortfolioScreen(viewModel: LensClickViewModel, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProPortfolio, onNavigate) {
    val state by viewModel.portfolio.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var uploadDialogUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) uploadDialogUri = uri
    }

    LaunchedEffect(Unit) { viewModel.loadPortfolio() }

    Column(Modifier.padding(top = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            PageTitle("Portfólio")
            Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("+ Adicionar") }
        }
        when (val result = state) {
            PortfolioState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
            PortfolioState.Error -> ProfileStatus("Erro ao carregar portfólio.", "Tentar novamente") { viewModel.loadPortfolio() }
            is PortfolioState.Ready -> {
                val itemsList = result.items
                if (itemsList.isEmpty()) {
                    EmptyMessage("Nenhuma imagem no seu portfólio.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(itemsList, key = { _, item -> item.id }) { index, item ->
                            PortfolioItemCard(item, itemsList, index == 0, viewModel)
                        }
                    }
                }
            }
        }
    }

    uploadDialogUri?.let { uri ->
        val upload = readPortfolioUpload(context, uri)
        if (upload == null) {
            uploadDialogUri = null
        } else {
            PortfolioMetadataDialog(
                caption = "",
                altText = "",
                category = "portrait",
                title = "Adicionar Imagem",
                onDismiss = { uploadDialogUri = null },
                onConfirm = { altText, caption, category ->
                    viewModel.addPortfolioImage(upload.bytes, upload.contentType, altText, caption, category)
                    uploadDialogUri = null
                }
            )
        }
    }
}

@Composable
private fun PortfolioItemCard(item: PortfolioItem, allItems: List<PortfolioItem>, isFirst: Boolean, viewModel: LensClickViewModel) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandPhoto(Modifier.size(70.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.caption.ifBlank { item.altText.ifBlank { "Imagem do portfólio" } }, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Categoria: ${item.category}", color = Muted, fontSize = 12.sp)
                if (item.isCover) Text("★ Capa", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { viewModel.removePortfolioItem(item.id) }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
        }
    }
}

private class PortfolioUpload(val bytes: ByteArray, val contentType: String)

private fun readPortfolioUpload(context: Context, uri: Uri): PortfolioUpload? {
    return try {
        val resolver = context.contentResolver
        val type = resolver.getType(uri) ?: "image/jpeg"
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val scaled = scalePortfolioBitmap(bitmap, 1200)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        PortfolioUpload(out.toByteArray(), type)
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
    return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
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
                LensField("Legenda", c, { c = it })
                Spacer(Modifier.height(10.dp))
                LensField("Texto Alternativo (Alt)", a, { a = it })
                Spacer(Modifier.height(10.dp))
                LensField("Categoria", cat, { cat = it })
            }
        },
        confirmButton = { Button(onClick = { onConfirm(a, c, cat) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = SurfaceHigh
    )
}

private fun initialsForDisplay(name: String?): String = name.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "LC" }

@Composable
private fun AccountScreen(
    viewModel: LensClickViewModel,
    user: User?,
    authentication: AuthenticationState,
    canUseProfessionalMode: Boolean,
    onNavigate: (Screen) -> Unit,
    onProfessionalMode: () -> Unit,
    onLogout: () -> Unit
) = AppPage(Screen.Account, onNavigate) {
    var selectedAction by rememberSaveable { mutableStateOf<String?>(null) }
    val twoFactorStatus by viewModel.twoFactorStatus.collectAsStateWithLifecycle()
    val twoFactorSetup by viewModel.twoFactorSetup.collectAsStateWithLifecycle()

    var exportDialogOpen by rememberSaveable { mutableStateOf(false) }
    var exportDataJson by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteDialogOpen by rememberSaveable { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var accountErrorMsg by rememberSaveable { mutableStateOf<String?>(null) }

    var twoFactorSetupOpen by rememberSaveable { mutableStateOf(false) }
    var twoFactorVerifyCode by rememberSaveable { mutableStateOf("") }
    var disable2FaOpen by rememberSaveable { mutableStateOf(false) }
    var disable2FaPassword by remember { mutableStateOf("") }

    val actions = listOf(
        "Meus dados" to Icons.Default.Person,
        "Configurações" to Icons.Default.Settings,
        "Segurança (2FA)" to Icons.Default.Lock,
        "Notificações" to Icons.Default.Email,
        "Ajuda e suporte" to Icons.AutoMirrored.Filled.List,
        "Sobre o Lens Click" to Icons.Default.Info
    )
    val loggingOut = authentication is AuthenticationState.Loading

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = Ink,
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable { selectedAction = "Configurações" }
                    .semantics { role = Role.Button }
                    .padding(12.dp)
            )
        }
        if (user == null) {
            AccountEmptyState()
        } else {
            Avatar(initialsForDisplay(user.name), Modifier.size(86.dp))
            Spacer(Modifier.height(10.dp))
            Text(user.name, color = Ink, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(user.email, color = Muted, fontSize = 13.sp)
            Text(if (canUseProfessionalMode) "Modo cliente • conta de fotógrafo" else "Conta de cliente", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(22.dp))
        }
        if (canUseProfessionalMode) {
            OutlinedButton(onClick = onProfessionalMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FieldBorder)) { Text("Voltar ao modo fotógrafo", color = Ink, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(14.dp))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(12.dp))) {
            actions.forEach { (label, icon) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 56.dp)
                        .clickable { selectedAction = label }
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

    selectedAction?.let { action ->
        when (action) {
            "Meus dados" -> {
                AlertDialog(
                    onDismissRequest = { selectedAction = null },
                    title = { Text("Meus Dados (LGPD)", color = Ink, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Gerencie seus dados e privacidade de acordo com a LGPD.", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(14.dp))
                            PrimaryButton("Exportar meus dados", {
                                viewModel.exportAccountData(
                                    onSuccess = { rawJson ->
                                        exportDataJson = rawJson
                                        exportDialogOpen = true
                                    },
                                    onError = { err -> accountErrorMsg = err }
                                )
                            })
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { deleteDialogOpen = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) { Text("Excluir minha conta", color = MaterialTheme.colorScheme.error) }
                        }
                    },
                    confirmButton = { TextButton(onClick = { selectedAction = null }) { Text("Fechar") } },
                    containerColor = SurfaceHigh
                )
            }
            "Segurança (2FA)", "Segurança" -> {
                val is2FaEnabled = twoFactorStatus?.enabled == true
                AlertDialog(
                    onDismissRequest = { selectedAction = null },
                    title = { Text("Segurança & 2FA", color = Ink, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Status da verificação em duas etapas (TOTP):", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            FilterChip(if (is2FaEnabled) "2FA Ativado" else "2FA Desativado", selected = is2FaEnabled)
                            Spacer(Modifier.height(16.dp))
                            if (is2FaEnabled) {
                                OutlinedButton(
                                    onClick = {
                                        disable2FaOpen = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Desativar 2FA", color = MaterialTheme.colorScheme.error) }
                            } else {
                                PrimaryButton("Configurar 2FA (TOTP)", {
                                    viewModel.setupTwoFactor()
                                    twoFactorSetupOpen = true
                                })
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { selectedAction = null }) { Text("Fechar") } },
                    containerColor = SurfaceHigh
                )
            }
            else -> {
                ActionDialog(action, "Esta área foi conectada aos serviços e configurações da sua conta.") { selectedAction = null }
            }
        }
    }

    if (exportDialogOpen && exportDataJson != null) {
        AlertDialog(
            onDismissRequest = { exportDialogOpen = false },
            title = { Text("Dados Exportados (JSON)", color = Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(exportDataJson!!, color = Ink, fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = { exportDialogOpen = false }) { Text("OK") } },
            containerColor = SurfaceHigh
        )
    }

    if (deleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { deleteDialogOpen = false },
            title = { Text("Excluir Conta Permanentemente", color = Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Esta ação é irreversível. Digite sua senha atual para confirmar a exclusão da conta:", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    LensField("Senha atual", deletePassword, { deletePassword = it }, password = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(
                            password = deletePassword,
                            onSuccess = { deleteDialogOpen = false },
                            onError = { err -> accountErrorMsg = err }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, Color.White),
                    enabled = deletePassword.isNotBlank()
                ) { Text("Excluir Permanentemente") }
            },
            dismissButton = { TextButton(onClick = { deleteDialogOpen = false }) { Text("Cancelar") } },
            containerColor = SurfaceHigh
        )
    }

    if (twoFactorSetupOpen) {
        AlertDialog(
            onDismissRequest = { twoFactorSetupOpen = false },
            title = { Text("Ativar Autenticação TOTP", color = Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("1. Adicione a chave abaixo no seu aplicativo autenticador (Google Authenticator, Authy, etc.):", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(twoFactorSetup?.secret ?: "CHAVE_SECRETA_TOTP", color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("2. Guarde seus códigos de recuperação em um local seguro:", color = Muted, fontSize = 12.sp)
                    twoFactorSetup?.recoveryCodes?.forEach { code ->
                        Text("• $code", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    LensField("Código de 6 dígitos para confirmar", twoFactorVerifyCode, { twoFactorVerifyCode = it }, KeyboardType.Number)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.enableTwoFactor(twoFactorVerifyCode) {
                        twoFactorSetupOpen = false
                        twoFactorVerifyCode = ""
                    }
                }) { Text("Ativar 2FA") }
            },
            dismissButton = { TextButton(onClick = { twoFactorSetupOpen = false }) { Text("Cancelar") } },
            containerColor = SurfaceHigh
        )
    }

    if (disable2FaOpen) {
        AlertDialog(
            onDismissRequest = { disable2FaOpen = false },
            title = { Text("Desativar 2FA", color = Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Digite sua senha atual para confirmar a desativação da verificação em duas etapas:", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    LensField("Senha atual", disable2FaPassword, { disable2FaPassword = it }, password = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disableTwoFactor(disable2FaPassword) { ok ->
                            if (ok) {
                                disable2FaOpen = false
                                disable2FaPassword = ""
                                selectedAction = null
                            } else {
                                accountErrorMsg = "Falha ao desativar 2FA. Verifique sua senha."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, Color.White),
                    enabled = disable2FaPassword.isNotBlank()
                ) { Text("Confirmar Desativação") }
            },
            dismissButton = { TextButton(onClick = { disable2FaOpen = false }) { Text("Cancelar") } },
            containerColor = SurfaceHigh
        )
    }

    accountErrorMsg?.let { msg ->
        ActionDialog("Aviso da Conta", msg) { accountErrorMsg = null }
    }
}

@Composable
private fun AccountEmptyState() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceHigh)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(24.dp)
            .semantics {
                stateDescription = "Conta sem dados carregados"
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sua conta", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Não foi possível carregar seus dados agora. Tente novamente entrando na conta.",
            color = Muted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
