package com.lensclick.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lensclick.app.R
import com.lensclick.app.data.local.Photographer
import com.lensclick.app.data.local.User
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.Conversation
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.Message
import com.lensclick.app.domain.PortfolioItem
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.domain.PublicPhotographerProfile
import java.io.ByteArrayOutputStream

private val Ink = Color(0xFF1E2128)
private val Surface = Color(0xFFF7F5F0)
private val SurfaceHigh = Color(0xFFFFFFFF)
private val Line = Color(0xFFE2DDD3)
private val Paper = Color(0xFFFAF8F3)
private val Muted = Color(0xFF6B7280)
private val Gold = Color(0xFFB8860B)

enum class Screen {
    Onboarding, AccountType, Login, SignUp, PhotographerSignUp,
    Home, Search, Photographer, Quote, Budgets, Conversations, Chat, Account,
    ProDashboard, ProRequests, ProAgenda, ProProfile, ProPortfolio
}
private const val PhotographerRoute = "Photographer/{slug}"
private const val QuoteRoute = "Quote/{slug}"

@Composable
fun LensClickApp(viewModel: LensClickViewModel = viewModel(factory = LensClickViewModel.Factory)) {
    val navController = rememberNavController()
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

    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
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
            composable(Screen.Search.name) { SearchScreen(viewModel, guest = currentUser == null, onNavigate = ::go, onPhotographer = { slug -> navController.navigate("Photographer/$slug") }) }
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

@Composable private fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false, light: Boolean = false, whiteText: Boolean = false) { LogoAsset(modifier = modifier, sizeDp = if (compact) 32 else 48, light = light, whiteText = whiteText) }
@Composable private fun LogoAsset(modifier: Modifier = Modifier, sizeDp: Int = 48, light: Boolean = false, whiteText: Boolean = false) {
    val context = LocalContext.current
    val resId = remember(light) {
        val name = if (light) "ic_lens_click_logo_light" else "ic_lens_click_logo"
        context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 } ?: R.drawable.card_portrait_outdoor
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(resId), contentDescription = "Logo LensClick", modifier = Modifier.size(sizeDp.dp), tint = Color.Unspecified)
        if (sizeDp >= 40) {
            Spacer(Modifier.width(8.dp))
            Column {
                Text("LensClick", color = if (whiteText) Color.White else Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Fotografia Profissional", color = if (whiteText) Color.White.copy(.8f) else Muted, fontSize = 9.sp)
            }
        }
    }
}
@Composable private fun CompactLogo(sizeDp: Int = 34) { LogoAsset(sizeDp = sizeDp) }

@Composable private fun OnboardingScreen(onStart: () -> Unit, onLogin: () -> Unit, onBrowse: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        BrandMark()
        Spacer(Modifier.weight(1f))
        Photo(Modifier.fillMaxWidth().height(280.dp), R.drawable.card_portrait_outdoor)
        Spacer(Modifier.height(24.dp))
        Text("Encontre o fotógrafo ideal para o seu momento.", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("Conectamos clientes e fotógrafos profissionais com segurança e transparência.", color = Muted, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.weight(1f))
        PrimaryButton("Começar", onStart)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBrowse, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text("Explorar sem conta", color = Ink, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(12.dp))
        CenterLink("Já tem uma conta?", "Entrar", onLogin)
    }
}

@Composable private fun AccountTypeScreen(onClient: () -> Unit, onPhotographer: () -> Unit, onLogin: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        BrandMark()
        Spacer(Modifier.weight(1f))
        Text("Como você deseja usar o LensClick?", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onClient, modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(SurfaceHigh, Ink)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Sou Cliente", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink); Text("Quero contratar serviços fotográficos", fontSize = 11.sp, color = Muted) } }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onPhotographer, modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(Ink, Color.White)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Sou Fotógrafo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White); Text("Quero divulgar meu trabalho e receber orçamentos", fontSize = 11.sp, color = Color.White.copy(.8f)) } }
        Spacer(Modifier.weight(1f))
        CenterLink("Já possui conta?", "Entrar", onLogin)
    }
}

@Composable private fun AuthFrame(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        BrandMark()
        Spacer(Modifier.height(32.dp))
        Text(title, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(28.dp))
        content()
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
            Text("Insira o código de 6 dígitos do app TOTP ou um código de recuperação.", color = Muted, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        BrandMark()
        Spacer(Modifier.height(20.dp))
        Text("Criar conta de Fotógrafo", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Cadastre seu perfil profissional no LensClick", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(20.dp))
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
private fun LensField(label: String, value: String, onValue: (String) -> Unit, type: KeyboardType = KeyboardType.Text, password: Boolean = false, singleLine: Boolean = true, maxLines: Int = 1) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 5.dp))
        OutlinedTextField(value = value, onValueChange = onValue, modifier = Modifier.fillMaxWidth(), singleLine = singleLine, maxLines = maxLines, keyboardOptions = KeyboardOptions(keyboardType = type), visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = SurfaceHigh, unfocusedContainerColor = SurfaceHigh, focusedBorderColor = Ink, unfocusedBorderColor = Line, cursorColor = Ink))
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Ink, Color.White)) { Text(text, color = Color.White, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onLogout,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Text("Sair da conta", fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun CenterLink(prefix: String, action: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("$prefix ", color = Muted, fontSize = 12.sp); Text(action, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onClick)) } }

@Composable private fun ActionDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, color = Ink, fontWeight = FontWeight.Bold) }, text = { Text(message, color = Muted, fontSize = 13.sp) }, confirmButton = { TextButton(onClick = onDismiss) { Text("OK", color = Ink, fontWeight = FontWeight.Bold) } }, containerColor = SurfaceHigh)
}

@Composable private fun AppPage(current: Screen, onNavigate: (Screen) -> Unit, isPhotographerMode: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().background(Paper)) {
        Box(Modifier.weight(1f).fillMaxWidth()) { Column(Modifier.fillMaxSize()) { content() } }
        if (isPhotographerMode) PhotographerBottomNav(current, onNavigate) else BottomNav(current, onNavigate)
    }
}

@Composable private fun BottomNav(current: Screen, onNavigate: (Screen) -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).background(SurfaceHigh).border(1.dp, Line), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        val items = listOf(Screen.Home to "Início", Screen.Search to "Buscar", Screen.Budgets to "Orçamentos", Screen.Conversations to "Mensagens", Screen.Account to "Perfil")
        items.forEach { (screen, label) ->
            val sel = current == screen
            Column(
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable { onNavigate(screen) }
                    .semantics {
                        contentDescription = label
                        role = Role.Button
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(label, color = if (sel) Ink else Muted, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable private fun HomeScreen(photographers: List<Photographer>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Home, onNavigate) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { BrandMark(); Text("Campinas, SP", color = Muted, fontSize = 11.sp) }
        Spacer(Modifier.height(20.dp))
        Text("Encontre o fotógrafo perfeito", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        SearchBar { onNavigate(Screen.Search) }
        Spacer(Modifier.height(22.dp))
        SectionTitle("Categorias") { onNavigate(Screen.Search) }
        Spacer(Modifier.height(10.dp))
        CategoryRow()
        Spacer(Modifier.height(22.dp))
        SectionTitle("Destaques da semana")
        Spacer(Modifier.height(10.dp))
        PhotographerCards(photographers) { onNavigate(Screen.Search) }
    }
}

@Composable private fun SearchBar(onClick: () -> Unit) { Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Text("⌕", color = Ink, fontSize = 20.sp); Spacer(Modifier.width(10.dp)); Text("Buscar por cidade ou especialidade...", color = Muted, fontSize = 12.sp) } }

@Composable private fun SectionTitle(title: String, onMore: (() -> Unit)? = null) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold); if (onMore != null) Text("Ver todos ›", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable(onClick = onMore)) } }

@Composable private fun CategoryRow() {
    val cats = listOf("Casamento", "Retrato", "Corporativo", "Eventos", "Ensaio")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { cats.forEach { FilterChip(it) } }
}

@Composable private fun PhotographerCards(photographers: List<Photographer>, onClick: () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(photographers) { p ->
            Column(Modifier.width(200.dp).clip(RoundedCornerShape(18.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(12.dp)) {
                Photo(Modifier.fillMaxWidth().height(130.dp), photographerImage(p))
                Spacer(Modifier.height(10.dp))
                Text(p.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(p.specialty, color = Muted, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.price, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("★ 4.9", color = Gold, fontSize = 12.sp) }
            }
        }
    }
}

private fun photographerImage(p: Photographer): Int = when (p.initials) {
    "LA" -> R.drawable.card_portrait_outdoor
    "MF" -> R.drawable.card_portrait_studio
    "RC" -> R.drawable.card_portrait_studio
    else -> R.drawable.card_portrait_outdoor
}

@Composable private fun Photo(modifier: Modifier, resId: Int = R.drawable.card_portrait_outdoor) {
    Icon(painter = painterResource(resId), contentDescription = null, modifier = modifier.clip(RoundedCornerShape(12.dp)), tint = Color.Unspecified)
}

@Composable private fun PortfolioStrip() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { Photo(Modifier.weight(1f).height(80.dp), R.drawable.card_portrait_outdoor) }
    }
}

@Composable
private fun SearchScreen(viewModel: LensClickViewModel, guest: Boolean, onNavigate: (Screen) -> Unit, onPhotographer: (String) -> Unit) = AppPage(Screen.Search, onNavigate) {
    var query by rememberSaveable { mutableStateOf("São Paulo") }
    var selectedSpecialty by rememberSaveable { mutableStateOf<String?>(null) }
    val searchState by viewModel.publicSearch.collectAsStateWithLifecycle()
    val specialties = listOf("Todos", "Casamento", "Retrato", "Corporativo", "Eventos")

    LaunchedEffect(query, selectedSpecialty) {
        viewModel.searchPublicPhotographers(query, selectedSpecialty.takeIf { it != "Todos" }, 1)
    }

    Column(Modifier.padding(top = 12.dp)) {
        Text("Buscar Fotógrafos", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp, 8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Digite a cidade (ex: São Paulo)", color = Muted, fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = SurfaceHigh, unfocusedContainerColor = SurfaceHigh, focusedBorderColor = Ink, unfocusedBorderColor = Line)
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            specialties.forEach { s ->
                val selected = (selectedSpecialty ?: "Todos") == s
                FilterChip(s, selected) { selectedSpecialty = if (s == "Todos") null else s }
            }
        }
        Spacer(Modifier.height(14.dp))
        when (val state = searchState) {
            PublicSearchState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
            PublicSearchState.Error -> ProfileStatus("Erro ao conectar à busca remota.", "Tentar novamente") { viewModel.searchPublicPhotographers(query, selectedSpecialty, 1) }
            is PublicSearchState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum fotógrafo encontrado.", color = Muted, fontSize = 13.sp) }
            is PublicSearchState.Ready -> {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.page.photographers, key = { it.slug }) { p ->
                        PublicPhotographerRow(p) { onPhotographer(p.slug) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicPhotographerRow(p: PublicPhotographer, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceHigh)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(initialsForDisplay(p.name), Modifier.size(54.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(p.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(p.specialties.joinToString(" · ") { specialtyLabel(it) }, color = Muted, fontSize = 11.sp)
            Text("⌖ ${listOf(p.city, p.state).filter { it.isNotBlank() }.joinToString(", ")}", color = Muted, fontSize = 10.sp)
        }
        p.startingPriceCents?.let { cents ->
            Text("R$ ${cents / 100}", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
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
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Ink else SurfaceHigh)
            .border(1.dp, if (selected) Ink else Line, RoundedCornerShape(20.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable private fun Avatar(initials: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Box {
                            Photo(Modifier.fillMaxWidth().height(240.dp), R.drawable.card_portrait_outdoor)
                            Box(Modifier.padding(16.dp).size(42.dp).clip(CircleShape).background(Color.Black.copy(.58f)).clickable(onClick = onBack), contentAlignment = Alignment.Center) { Text("‹", color = Color.White, fontSize = 34.sp) }
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.TopEnd) { CompactLogo(42) }
                        }
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Avatar(initialsForDisplay(profile.profile.name), Modifier.size(92.dp)); Spacer(Modifier.width(14.dp)); Column { Text(profile.profile.name, color = Ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold); Text(profile.profile.specialties.joinToString(" · ") { specialtyLabel(it) }, color = Muted, fontSize = 12.sp) } }
                            Spacer(Modifier.height(14.dp)); Text("⌖ ${listOf(profile.profile.city, profile.profile.state).filter { it.isNotBlank() }.joinToString(", ")}", color = Muted, fontSize = 11.sp); profile.profile.ratingAverage?.let { Text("★ $it (${profile.profile.reviewCount} avaliações)", color = Gold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 10.dp)) }
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { profile.profile.specialties.forEach { FilterChip(specialtyLabel(it)) } }
                            Spacer(Modifier.height(22.dp)); Text("Sobre mim", color = Ink, fontWeight = FontWeight.SemiBold); Text(profile.bio.ifBlank { "Este fotógrafo ainda não adicionou uma apresentação." }, color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 8.dp))
                            SectionTitle("Portfólio"); Spacer(Modifier.height(10.dp))
                            if (profile.portfolio.isEmpty()) Text("Este fotógrafo ainda não publicou imagens.", color = Muted, fontSize = 12.sp)
                            else LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(profile.portfolio, key = { it.id }) { item -> Column(Modifier.width(150.dp)) { Photo(Modifier.fillMaxWidth().height(100.dp)); Text(item.caption.ifBlank { item.altText }, color = Muted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp)) } } }

                            Spacer(Modifier.height(24.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Avaliações e Opiniões", color = Ink, fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { reviewDialogOpen = true }) { Text("Avaliar ★", color = Ink, fontWeight = FontWeight.Bold) }
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
                                                Text("★ ${r.rating}", color = Gold, fontSize = 11.sp)
                                            }
                                            if (r.comment.isNotBlank()) {
                                                Text(r.comment, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(Modifier.background(SurfaceHigh).padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onQuote, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(Ink, Color.White)) { Text("Solicitar orçamento", fontWeight = FontWeight.SemiBold) }; OutlinedButton({ if (favorite) { favorites.firstOrNull { it.slug == slug }?.let { viewModel.removeFavorite(it.photographerId) {} } } else { viewModel.addFavorite(slug) {} } }, Modifier.size(52.dp), contentPadding = PaddingValues(0.dp), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text(if (favorite) "♥" else "♡", color = Ink, fontSize = 23.sp) } }
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
                                        color = Gold,
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

@Composable private fun Stat(value: String, label: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = Muted, fontSize = 8.sp); Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

@Composable
private fun QuoteScreen(onBack: () -> Unit, onSent: (String, String, String, String, String, String) -> Unit) {
    var type by rememberSaveable { mutableStateOf("Casamento") }
    var date by rememberSaveable { mutableStateOf("") }
    var place by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("‹", color = Ink, fontSize = 34.sp, modifier = Modifier.clickable(onClick = onBack)); Spacer(Modifier.width(10.dp)); Text("Solicitar Orçamento", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
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
        Text("Meus Pedidos & Favoritos", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp, 8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip("Orçamentos (${budgets.size})", tab == 0) { tab = 0 }
            FilterChip("Favoritos (${favorites.size})", tab == 1) { tab = 1 }
        }
        Spacer(Modifier.height(14.dp))
        if (tab == 0) {
            if (budgets.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum pedido de orçamento encontrado.", color = Muted, fontSize = 12.sp) }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgets, key = { it.id }) { b ->
                        BudgetCard(b)
                    }
                }
            }
        } else {
            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Você ainda não salvou fotógrafos favoritos.", color = Muted, fontSize = 12.sp) }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favorites, key = { it.photographerId }) { f ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(14.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(f.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(f.slug, color = Muted, fontSize = 11.sp)
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
        Text("Data: ${b.eventDate} • Local: ${b.place}", color = Muted, fontSize = 11.sp)
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
        Text("Conversas", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(20.dp, 8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Buscar conversa", color = Muted, fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(17.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = SurfaceHigh, unfocusedContainerColor = SurfaceHigh, focusedBorderColor = Ink, unfocusedBorderColor = Line, cursorColor = Ink)
        )
        Spacer(Modifier.height(12.dp))
        val filtered = conversations.filter { it.counterpartName.contains(query, true) || it.lastMessage.contains(query, true) }
        LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (filtered.isEmpty()) {
                item { Text("Nenhuma conversa encontrada.", color = Muted, fontSize = 12.sp, modifier = Modifier.fillParentMaxWidth().padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            } else {
                items(filtered, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
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
                            Text(c.lastMessage.ifBlank { "Nenhuma mensagem ainda." }, color = Muted, fontSize = 11.sp, maxLines = 1)
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
        Text(message, color = Ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
            Text("‹", color = Ink, fontSize = 36.sp, modifier = Modifier.clickable(onClick = onBack))
            Avatar(initialsForDisplay(currentConv?.counterpartName ?: "Chat"), Modifier.size(42.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(currentConv?.counterpartName ?: "Mensagens", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("● Online", color = Muted, fontSize = 10.sp)
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
        Row(Modifier.background(SurfaceHigh).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite sua mensagem...", color = Muted, fontSize = 12.sp) },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedBorderColor = Line, unfocusedBorderColor = Line, cursorColor = Ink)
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
                colors = ButtonDefaults.buttonColors(Ink, Color.White)
            ) {
                Text("➤", fontSize = 20.sp)
            }
        }
    }
}

@Composable private fun Bubble(text: String, mine: Boolean) = Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) { Text(text, color = if (mine) Color.White else Ink, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.widthIn(max = 260.dp).clip(RoundedCornerShape(16.dp)).background(if (mine) Ink else SurfaceHigh).border(1.dp, if (mine) Ink else Line, RoundedCornerShape(16.dp)).padding(13.dp)) }

@Composable private fun PhotographerPage(current: Screen, onNavigate: (Screen) -> Unit, content: @Composable ColumnScope.() -> Unit) = AppPage(current, onNavigate, isPhotographerMode = true, content = content)

@Composable private fun PhotographerBottomNav(current: Screen, onNavigate: (Screen) -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).background(SurfaceHigh).border(1.dp, Line), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        val items = listOf(Screen.ProDashboard to "Painel", Screen.ProRequests to "Pedidos", Screen.ProAgenda to "Agenda", Screen.ProProfile to "Perfil", Screen.ProPortfolio to "Portfólio")
        items.forEach { (screen, label) ->
            val sel = current == screen
            Column(
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable { onNavigate(screen) }
                    .semantics {
                        contentDescription = label
                        role = Role.Button
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(label, color = if (sel) Ink else Muted, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun PhotographerDashboardScreen(p: Photographer?, budgets: List<BudgetRequest>, onNavigate: (Screen) -> Unit, onClientMode: () -> Unit) = PhotographerPage(Screen.ProDashboard, onNavigate) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { BrandMark(light = false); Text("Modo Fotógrafo", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(p?.initials ?: "FT", Modifier.size(56.dp)); Spacer(Modifier.width(14.dp)); Column { Text(p?.name ?: "Perfil Profissional", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(p?.specialty ?: "Fotografia", color = Muted, fontSize = 12.sp) } }
        Spacer(Modifier.height(20.dp))
        SectionTitle("Métricas")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProMetric("Pedidos", "${budgets.size}", Modifier.weight(1f))
            ProMetric("Agenda", "${budgets.count { it.status == "accepted" }}", Modifier.weight(1f))
            ProMetric("Avaliação", "★ 4.9", Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("Ações Rápidas")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProAction("Editar Perfil", "Infomações e preços", Modifier.weight(1f)) { onNavigate(Screen.ProProfile) }
            ProAction("Portfólio", "Gerenciar imagens", Modifier.weight(1f)) { onNavigate(Screen.ProPortfolio) }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text("Alternar para modo cliente", color = Ink, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable private fun ProMetric(label: String, value: String, modifier: Modifier) { Column(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 11.sp) } }
@Composable private fun ProAction(title: String, desc: String, modifier: Modifier, onClick: () -> Unit) { Column(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(14.dp)) { Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(desc, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp)) } }

@Composable
private fun PhotographerRequestsScreen(viewModel: LensClickViewModel, budgets: List<BudgetRequest>, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProRequests, onNavigate) {
    var proposeTarget by remember { mutableStateOf<BudgetRequest?>(null) }
    Column(Modifier.padding(top = 12.dp)) {
        Text("Pedidos de Orçamento", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp, 8.dp))
        if (budgets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum pedido recebido no momento.", color = Muted, fontSize = 12.sp) }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(budgets, key = { it.id }) { b ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(b.eventType, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            FilterChip(budgetStatusLabel(b.status), selected = true)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Cliente: ${b.counterpartName} • Data: ${b.eventDate}", color = Muted, fontSize = 11.sp)
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
        Text("Minha Agenda", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp, 8.dp))
        if (agenda.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum evento agendado ou aceito.", color = Muted, fontSize = 12.sp) }
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
        Text("Perfil Profissional", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
        OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text("Modo Cliente", color = Ink, fontWeight = FontWeight.SemiBold) }
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
            Text("Portfólio", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("+ Adicionar") }
        }
        when (val result = state) {
            PortfolioState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
            PortfolioState.Error -> ProfileStatus("Erro ao carregar portfólio.", "Tentar novamente") { viewModel.loadPortfolio() }
            is PortfolioState.Ready -> {
                val itemsList = result.items
                if (itemsList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhuma imagem no seu portfólio.", color = Muted, fontSize = 12.sp) }
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
            Photo(Modifier.size(70.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.caption.ifBlank { item.altText.ifBlank { "Imagem do portfólio" } }, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Categoria: ${item.category}", color = Muted, fontSize = 11.sp)
                if (item.isCover) Text("★ Capa", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

    val actions = listOf("♙  Meus dados", "⚙  Configurações", "♢  Segurança (2FA)", "♧  Notificações", "?  Ajuda e suporte", "ⓘ  Sobre o Lens Click")
    val loggingOut = authentication is AuthenticationState.Loading

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                "⚙",
                color = Ink,
                fontSize = 24.sp,
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable { selectedAction = "Configurações" }
                    .semantics {
                        contentDescription = "Configurações"
                        role = Role.Button
                    }
                    .padding(8.dp)
            )
        }
        if (user == null) {
            AccountEmptyState()
        } else {
            Avatar(initialsForDisplay(user.name), Modifier.size(86.dp))
            Spacer(Modifier.height(10.dp))
            Text(user.name, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(user.email, color = Muted, fontSize = 11.sp)
            Text(if (canUseProfessionalMode) "Modo cliente • conta de fotógrafo" else "Conta de cliente", color = Gold, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(22.dp))
        }
        if (canUseProfessionalMode) {
            OutlinedButton(onClick = onProfessionalMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text("Voltar ao modo fotógrafo", color = Ink, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(14.dp))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(12.dp))) {
            actions.forEach { action ->
                val label = action.substringAfter("  ")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedAction = label }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(action, color = Ink, fontSize = 13.sp)
                    Text("›", color = Muted, fontSize = 19.sp)
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
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
                                        viewModel.disableTwoFactor("senha123") {
                                            selectedAction = null
                                        }
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
                    Text(exportDataJson!!, color = Ink, fontSize = 11.sp)
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
                    Text("1. Adicione a chave abaixo no seu aplicativo autenticador (Google Authenticator, Authy, etc.):", color = Muted, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(twoFactorSetup?.secret ?: "CHAVE_SECRETA_TOTP", color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("2. Guarde seus códigos de recuperação em um local seguro:", color = Muted, fontSize = 11.sp)
                    twoFactorSetup?.recoveryCodes?.forEach { code ->
                        Text("• $code", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
