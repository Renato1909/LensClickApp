package com.example.lensclickapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lensclickapp.R
import com.example.lensclickapp.data.Budget
import com.example.lensclickapp.data.Photographer
import com.example.lensclickapp.data.User

private val Ink = Color(0xFF11110F)
private val Surface = Color(0xFFF6F3EB)
private val SurfaceHigh = Color(0xFFFFFEFA)
private val Line = Color(0xFFE2DDD2)
private val Paper = Color(0xFFF6F3EB)
private val Muted = Color(0xFF716D64)
private val Gold = Color(0xFF9A6A24)

private enum class Screen { Onboarding, AccountType, Login, SignUp, PhotographerSignUp, Home, Search, Photographer, Quote, Budgets, Conversations, Chat, Account, ProDashboard, ProRequests, ProAgenda, ProProfile }

@Composable
fun LensClickApp(viewModel: LensClickViewModel = viewModel(factory = LensClickViewModel.Factory)) {
    var screen by rememberSaveable { mutableStateOf(Screen.Onboarding) }
    var previous by rememberSaveable { mutableStateOf(Screen.Home) }
    val photographers by viewModel.photographers.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentPhotographer = photographers.firstOrNull { it.userId == currentUser?.id }
    fun go(target: Screen) { previous = screen; screen = target }

    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
        when (screen) {
            Screen.Onboarding -> OnboardingScreen(onStart = { go(Screen.AccountType) }, onLogin = { go(Screen.Login) })
            Screen.AccountType -> AccountTypeScreen(
                onClient = { go(Screen.SignUp) },
                onPhotographer = { go(Screen.PhotographerSignUp) },
                onLogin = { go(Screen.Login) }
            )
            Screen.Login -> LoginScreen(
                onLogin = { email, password, result ->
                    viewModel.login(email, password) { user ->
                        val success = user != null
                        result(success)
                        if (user?.role == "photographer") {
                            go(Screen.ProDashboard)
                        } else if (success) {
                            go(Screen.Home)
                        }
                    }
                },
                onSignUp = { go(Screen.AccountType) }
            )
            Screen.SignUp -> SignUpScreen(
                onDone = { name, email, password, result ->
                    viewModel.register(name, email, password) { success ->
                        result(success)
                        if (success) go(Screen.Home)
                    }
                },
                onLogin = { go(Screen.Login) }
            )
            Screen.PhotographerSignUp -> PhotographerSignUpScreen(
                onDone = { name, email, password, specialty, city, price, bio, result ->
                    viewModel.registerPhotographer(name, email, password, specialty, city, price, bio) { success ->
                        result(success)
                        if (success) {
                            go(Screen.ProDashboard)
                        }
                    }
                },
                onLogin = { go(Screen.Login) }
            )
            Screen.Home -> HomeScreen(photographers, onNavigate = ::go)
            Screen.Search -> SearchScreen(photographers, onNavigate = ::go)
            Screen.Photographer -> PhotographerScreen(onBack = { screen = previous }, onQuote = { go(Screen.Quote) })
            Screen.Quote -> QuoteScreen(
                onBack = { screen = previous },
                onSent = { type, date, place, duration, description, details ->
                    viewModel.createBudget(type, date, place, duration, description, details) { go(Screen.Budgets) }
                }
            )
            Screen.Budgets -> BudgetsScreen(budgets, onNavigate = ::go)
            Screen.Conversations -> ConversationsScreen(photographers, onNavigate = ::go)
            Screen.Chat -> ChatScreen(onBack = { go(Screen.Conversations) })
            Screen.Account -> AccountScreen(
                user = currentUser,
                canUseProfessionalMode = currentUser?.role == "photographer",
                onNavigate = ::go,
                onProfessionalMode = { go(Screen.ProDashboard) },
                onLogout = { viewModel.logout(); go(Screen.Login) }
            )
            Screen.ProDashboard -> PhotographerDashboardScreen(currentPhotographer, budgets, ::go, onClientMode = { go(Screen.Home) })
            Screen.ProRequests -> PhotographerRequestsScreen(budgets, ::go)
            Screen.ProAgenda -> PhotographerAgendaScreen(budgets, ::go)
            Screen.ProProfile -> PhotographerProfileScreen(currentUser, currentPhotographer, ::go, onClientMode = { go(Screen.Home) }, onLogout = { viewModel.logout(); go(Screen.Login) })
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier, large: Boolean = false, onDark: Boolean = false, accented: Boolean = false) {
    LogoAsset(modifier = modifier, size = if (large) 104 else 56, onDark = onDark, accented = accented)
}

@Composable
private fun LogoAsset(modifier: Modifier = Modifier, size: Int, onDark: Boolean = false, accented: Boolean = false) {
    Box(
        modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.lens_click_mark),
            contentDescription = "Monograma Lens Click",
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.12f
                    scaleY = 1.12f
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithCache {
                    val lightLogo = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFFEFA), Color(0xFFE7D3A5), Color(0xFFFFFFFF))
                    )
                    val accentLogo = Brush.linearGradient(
                        colors = listOf(Ink, Color(0xFFB47B2C), Ink)
                    )
                    onDrawWithContent {
                        drawContent()
                        when {
                            onDark -> drawRect(brush = lightLogo, blendMode = BlendMode.SrcIn)
                            accented -> drawRect(brush = accentLogo, blendMode = BlendMode.SrcIn)
                        }
                    }
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CompactLogo(size: Int = 42) = LogoAsset(size = size)

@Composable
private fun OnboardingScreen(onStart: () -> Unit, onLogin: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.lens_mountains), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(.18f), Color.Black.copy(.55f), Ink))))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f))
            BrandMark(large = true, onDark = true)
            Spacer(Modifier.height(28.dp))
            Text("Conectando momentos\na fotógrafos incríveis.", color = Color.White, fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(4) { Dot(active = it == 0) } }
            Spacer(Modifier.height(36.dp))
            Button(onStart, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(Paper, Ink)) { Text("Começar", fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(.55f))) { Text("Entrar", color = Color.White, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable private fun Dot(active: Boolean) = Box(Modifier.size(if (active) 9.dp else 7.dp).clip(CircleShape).background(if (active) Paper else Color.Gray.copy(.55f)))

@Composable
private fun AccountTypeScreen(onClient: () -> Unit, onPhotographer: () -> Unit, onLogin: () -> Unit) =
    AuthFrame("Como você quer usar o Lens Click?", "Escolha o tipo da sua conta") {
        Text("Quero contratar um fotógrafo", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("Encontre profissionais e solicite orçamentos.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
        PrimaryButton("Cadastrar como cliente", onClient)
        Spacer(Modifier.height(22.dp))
        Text("Sou fotógrafo", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("Crie seu perfil e apareça nas buscas.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
        OutlinedButton(
            onClick = onPhotographer,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Ink)
        ) { Text("Cadastrar como fotógrafo", color = Ink, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(18.dp))
        CenterLink("Já tem uma conta?", "Entrar", onLogin)
    }

@Composable
private fun AuthFrame(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        BrandMark(large = true, accented = true)
        Spacer(Modifier.height(48.dp))
        Column(Modifier.fillMaxWidth()) {
            Text(title, color = Ink, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))
            content()
        }
    }
}

@Composable
private fun LoginScreen(onLogin: (String, String, (Boolean) -> Unit) -> Unit, onSignUp: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("seu@email.com") }
    var password by rememberSaveable { mutableStateOf("lensclick") }
    var recoveryOpen by rememberSaveable { mutableStateOf(false) }
    var loginError by rememberSaveable { mutableStateOf(false) }
    var submitting by rememberSaveable { mutableStateOf(false) }
    AuthFrame("Bem-vindo de volta!", "Entre para continuar") {
        LensField("E-mail", email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(14.dp))
        LensField("Senha", password, { password = it }, password = true)
        TextButton(onClick = { recoveryOpen = true }, contentPadding = PaddingValues(0.dp)) { Text("Esqueci minha senha", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        Spacer(Modifier.height(26.dp))
        if (loginError) Text("E-mail ou senha inválidos.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        PrimaryButton("Entrar", {
            submitting = true
            loginError = false
            onLogin(email, password) { success -> submitting = false; loginError = !success }
        }, enabled = email.isNotBlank() && password.isNotBlank() && !submitting)
        Spacer(Modifier.height(12.dp))
        CenterLink("Não tem uma conta?", "Cadastre-se", onSignUp)
    }
    if (recoveryOpen) ActionDialog("Recuperar senha", "Enviaremos as instruções de recuperação para $email.") { recoveryOpen = false }
}

@Composable
private fun SignUpScreen(onDone: (String, String, String, (Boolean) -> Unit) -> Unit, onLogin: () -> Unit) = AuthFrame("Crie sua conta", "Preencha os dados abaixo") {
    var name by rememberSaveable { mutableStateOf("") }; var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }; var confirm by rememberSaveable { mutableStateOf("") }; var accepted by rememberSaveable { mutableStateOf(false) }
    var emailExists by rememberSaveable { mutableStateOf(false) }; var submitting by rememberSaveable { mutableStateOf(false) }
    LensField("Nome completo", name, { name = it }); Spacer(Modifier.height(11.dp))
    LensField("E-mail", email, { email = it }, KeyboardType.Email); Spacer(Modifier.height(11.dp))
    LensField("Senha", pass, { pass = it }, password = true); Spacer(Modifier.height(11.dp))
    LensField("Confirmar senha", confirm, { confirm = it }, password = true)
    Row(Modifier.fillMaxWidth().clickable { accepted = !accepted }.padding(vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Checkbox(accepted, { accepted = it }, colors = CheckboxDefaults.colors(checkedColor = Ink, checkmarkColor = Paper))
        Text("Eu aceito os Termos de Uso\ne a Política de Privacidade", color = Ink, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 11.dp))
    }
    if (emailExists) Text("Este e-mail já está cadastrado.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
    PrimaryButton("Cadastrar", {
        submitting = true
        emailExists = false
        onDone(name, email, pass) { success -> submitting = false; emailExists = !success }
    }, enabled = accepted && pass == confirm && name.isNotBlank() && email.isNotBlank() && pass.isNotBlank() && !submitting)
    Spacer(Modifier.height(12.dp)); CenterLink("Já tem uma conta?", "Entrar", onLogin)
}

@Composable
private fun PhotographerSignUpScreen(
    onDone: (String, String, String, String, String, String, String, (Boolean) -> Unit) -> Unit,
    onLogin: () -> Unit
) = AuthFrame("Cadastre-se como fotógrafo", "Crie seu perfil profissional") {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var accepted by rememberSaveable { mutableStateOf(false) }
    var emailExists by rememberSaveable { mutableStateOf(false) }
    var submitting by rememberSaveable { mutableStateOf(false) }

    LensField("Nome completo", name, { name = it }); Spacer(Modifier.height(11.dp))
    LensField("E-mail", email, { email = it }, KeyboardType.Email); Spacer(Modifier.height(11.dp))
    LensField("Senha", pass, { pass = it }, password = true); Spacer(Modifier.height(11.dp))
    LensField("Confirmar senha", confirm, { confirm = it }, password = true); Spacer(Modifier.height(11.dp))
    LensField("Especialidade (ex.: Casamentos)", specialty, { specialty = it }); Spacer(Modifier.height(11.dp))
    LensField("Cidade e estado", city, { city = it }); Spacer(Modifier.height(11.dp))
    LensField("Preço inicial (ex.: R$ 800)", price, { price = it }); Spacer(Modifier.height(11.dp))
    LensField("Sobre seu trabalho", bio, { bio = it }, singleLine = false, minLines = 3)
    Row(Modifier.fillMaxWidth().clickable { accepted = !accepted }.padding(vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Checkbox(accepted, { accepted = it }, colors = CheckboxDefaults.colors(checkedColor = Ink, checkmarkColor = Paper))
        Text("Eu aceito os Termos de Uso\ne a Política de Privacidade", color = Ink, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 11.dp))
    }
    if (pass != confirm && confirm.isNotBlank()) Text("As senhas não coincidem.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
    if (emailExists) Text("Este e-mail já está cadastrado.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
    PrimaryButton("Cadastrar fotógrafo", {
        submitting = true
        emailExists = false
        onDone(name, email, pass, specialty, city, price, bio) { success ->
            submitting = false
            emailExists = !success
        }
    }, enabled = accepted && pass == confirm && pass.isNotBlank() && name.isNotBlank() && email.isNotBlank() && specialty.isNotBlank() && city.isNotBlank() && price.isNotBlank() && !submitting)
    Spacer(Modifier.height(12.dp))
    CenterLink("Já tem uma conta?", "Entrar", onLogin)
}

@Composable
private fun LensField(label: String, value: String, onChange: (String) -> Unit, keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false, singleLine: Boolean = true, minLines: Int = 1) {
    OutlinedTextField(value, onChange, modifier = Modifier.fillMaxWidth(), label = { Text(label, fontSize = 12.sp) }, singleLine = singleLine, minLines = minLines,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard), shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedBorderColor = Ink, unfocusedBorderColor = Line, focusedContainerColor = SurfaceHigh, unfocusedContainerColor = SurfaceHigh, focusedLabelColor = Ink, unfocusedLabelColor = Muted, cursorColor = Ink))
}

@Composable private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) = Button(onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(54.dp).shadow(if (enabled) 7.dp else 0.dp, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White, disabledContainerColor = Line, disabledContentColor = Muted)) { Text(text, fontWeight = FontWeight.SemiBold) }

@Composable
private fun LogoutButton(onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth().height(54.dp).shadow(4.dp, RoundedCornerShape(17.dp)),
    shape = RoundedCornerShape(17.dp),
    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
) {
    Text("Sair da conta", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
}

@Composable private fun CenterLink(prefix: String, action: String, click: () -> Unit) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Text(prefix, color = Muted, fontSize = 12.sp); TextButton(click, contentPadding = PaddingValues(5.dp)) { Text(action, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }

@Composable
private fun ActionDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Ink, fontWeight = FontWeight.SemiBold) },
        text = { Text(message, color = Muted) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendi", color = Ink, fontWeight = FontWeight.Bold) } },
        containerColor = SurfaceHigh,
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun AppPage(active: Screen, onNavigate: (Screen) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandMark()
        }
        Column(Modifier.weight(1f).fillMaxWidth()) { content() }
        BottomNav(active, onNavigate)
    }
}

@Composable
private fun BottomNav(active: Screen, onNavigate: (Screen) -> Unit) {
    val items = listOf(Triple("⌂", "Início", Screen.Home), Triple("⌕", "Buscar", Screen.Search), Triple("▣", "Orçamentos", Screen.Budgets), Triple("◉", "Conversas", Screen.Conversations), Triple("♙", "Perfil", Screen.Account))
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(29.dp))
                .clip(RoundedCornerShape(29.dp))
                .background(Ink)
                .border(1.dp, Color.White.copy(.10f), RoundedCornerShape(29.dp))
                .padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { (icon, label, destination) ->
                val selected = destination == active
                Column(
                    Modifier.weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selected) SurfaceHigh else Color.Transparent)
                        .clickable { onNavigate(destination) }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(icon, color = if (selected) Ink else Paper.copy(.82f), fontSize = 19.sp, lineHeight = 20.sp)
                    Text(label, color = if (selected) Ink else Paper.copy(.62f), fontSize = 9.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(photographers: List<Photographer>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Home, onNavigate) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(CircleShape).background(SurfaceHigh).border(1.dp, Line, CircleShape), contentAlignment = Alignment.Center) { Text("♧", color = Ink, fontSize = 20.sp) } } }
        item { Text("⌖ São Paulo, SP  ⌄", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        item { Text("Encontre o fotógrafo\nperfeito para seu momento", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 33.sp, letterSpacing = (-0.5).sp) }
        item { SearchBar(onClick = { onNavigate(Screen.Search) }) }
        item { SectionTitle("Categorias") { onNavigate(Screen.Search) } }
        item { CategoryRow() }
        item { SectionTitle("Em destaque") { onNavigate(Screen.Search) } }
        item { PhotographerCards(photographers, onOpen = { onNavigate(Screen.Photographer) }) }
        item { SectionTitle("Portfólios em alta") { onNavigate(Screen.Search) } }
        item { PortfolioStrip() }
    }
}

@Composable private fun SearchBar(onClick: () -> Unit) = Row(Modifier.fillMaxWidth().height(52.dp).shadow(4.dp, RoundedCornerShape(17.dp)).clip(RoundedCornerShape(17.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(17.dp)).clickable(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Text("⌕", color = Ink, fontSize = 20.sp); Spacer(Modifier.width(10.dp)); Text("Buscar fotógrafos, estilos...", color = Muted, fontSize = 12.sp); Spacer(Modifier.weight(1f)); Text("☷", color = Ink) }

@Composable private fun SectionTitle(title: String, onSeeAll: (() -> Unit)? = null) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); if (onSeeAll != null) Text("Ver todas  ›", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onSeeAll).padding(6.dp)) }

@Composable private fun CategoryRow() {
    val cats = listOf("♧\nCasamento", "♙\nEnsaio", "♧\nEventos", "♡\nInfantil", "▣\nCorporativo")
    var selected by rememberSaveable { mutableStateOf(0) }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        cats.forEachIndexed { index, category ->
            val active = index == selected
            Text(category, color = if (active) Color.White else Ink, fontSize = 10.sp, lineHeight = 24.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.width(72.dp).clip(RoundedCornerShape(18.dp)).background(if (active) Ink else SurfaceHigh).border(1.dp, if (active) Ink else Line, RoundedCornerShape(18.dp)).clickable { selected = index }.padding(vertical = 12.dp, horizontal = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable private fun PhotographerCards(photographers: List<Photographer>, onOpen: () -> Unit) { LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(photographers.take(3), key = { it.id }) { p -> Column(Modifier.width(148.dp).shadow(5.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(18.dp)).clickable(onClick = onOpen).padding(bottom = 12.dp)) { Photo(Modifier.fillMaxWidth().height(126.dp), photographerImage(p)); Text(p.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(11.dp, 9.dp, 11.dp, 0.dp)); Text(p.specialty, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 11.dp)); Text("★ 5,0", color = Gold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)) } } } }

private fun photographerImage(photographer: Photographer): Int = when {
    photographer.name == "Mariana Lopes" -> R.drawable.card_wedding
    photographer.name == "Lucas Almeida" -> R.drawable.card_portrait_outdoor
    photographer.name == "Carlos Nobre" -> R.drawable.card_event
    photographer.name == "Juliana Reis" -> R.drawable.card_portrait_studio
    photographer.specialty.contains("casamento", ignoreCase = true) -> R.drawable.card_wedding
    photographer.specialty.contains("corporativo", ignoreCase = true) -> R.drawable.card_corporate
    photographer.specialty.contains("evento", ignoreCase = true) -> R.drawable.card_event
    else -> R.drawable.card_portrait_outdoor
}

@Composable private fun Photo(modifier: Modifier = Modifier, imageRes: Int = R.drawable.lens_mountains) = Image(painterResource(imageRes), null, modifier.clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
@Composable private fun PortfolioStrip() = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    listOf(R.drawable.card_wedding, R.drawable.card_portrait_outdoor, R.drawable.card_event).forEach { image -> Photo(Modifier.weight(1f).height(78.dp), image) }
}

@Composable
private fun SearchScreen(photographers: List<Photographer>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Search, onNavigate) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Text("Descobrir", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(20.dp, 8.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp), placeholder = { Text("Nome, estilo ou cidade", color = Muted, fontSize = 12.sp) }, leadingIcon = { Text("⌕", color = Ink, fontSize = 20.sp) }, singleLine = true, shape = RoundedCornerShape(17.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = SurfaceHigh, unfocusedContainerColor = SurfaceHigh, focusedBorderColor = Ink, unfocusedBorderColor = Line, cursorColor = Ink))
        val filters = listOf("Todos", "Casamento", "Ensaios", "Eventos", "Corporativo")
        Row(Modifier.padding(20.dp, 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { filters.forEachIndexed { index, label -> FilterChip(label, selectedFilter == index) { selectedFilter = index } } }
        val selectedCategory = filters[selectedFilter]
        val results = photographers.filter { photographer ->
            val matchesQuery = query.isBlank() || photographer.name.contains(query, true) || photographer.specialty.contains(query, true)
            val matchesCategory = selectedFilter == 0 || photographer.specialty.contains(selectedCategory.removeSuffix("s"), true)
            matchesQuery && matchesCategory
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${results.size} ${if (results.size == 1) "profissional" else "profissionais"}", color = Muted, fontSize = 11.sp)
            if (query.isNotBlank() || selectedFilter != 0) TextButton(onClick = { query = ""; selectedFilter = 0 }, contentPadding = PaddingValues(6.dp)) { Text("Limpar filtros", color = Ink, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (results.isEmpty()) item { Column(Modifier.fillParentMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Nenhum resultado", color = Ink, fontWeight = FontWeight.SemiBold); Text("Tente outro nome ou remova os filtros.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) } }
            items(results) { p -> PhotographerRow(p, { onNavigate(Screen.Photographer) }) }
        }
    }
}

@Composable private fun FilterChip(text: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    val interaction = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(Modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) Ink else SurfaceHigh).border(1.dp, if (selected) Ink else Line, RoundedCornerShape(14.dp)).then(interaction).padding(horizontal = 14.dp, vertical = 10.dp)) { Text(text, color = if (selected) Color.White else Ink, fontSize = 10.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) }
}

@Composable private fun PhotographerRow(p: Photographer, open: () -> Unit) {
    var favorite by rememberSaveable { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = open).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(photographerImage(p)), p.name, Modifier.size(62.dp).clip(CircleShape), contentScale = ContentScale.Crop); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(p.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(p.specialty, color = Muted, fontSize = 10.sp); Text(p.city, color = Muted, fontSize = 10.sp); Text("★ 5,0 (128)", color = Gold, fontSize = 10.sp) }
        Column(horizontalAlignment = Alignment.End) { Text(if (favorite) "♥" else "♡", color = Ink, fontSize = 22.sp, modifier = Modifier.clip(CircleShape).clickable { favorite = !favorite }.padding(6.dp)); Spacer(Modifier.height(9.dp)); Text("A partir de ${p.price}", color = Ink, fontSize = 9.sp, fontWeight = FontWeight.Medium) }
    }
}

@Composable private fun Avatar(initials: String, modifier: Modifier = Modifier) = Box(modifier.clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF6E5948), SurfaceHigh))).border(1.dp, Ink.copy(.5f), CircleShape), contentAlignment = Alignment.Center) { Text(initials, color = Paper, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

@Composable
private fun PhotographerScreen(onBack: () -> Unit, onQuote: () -> Unit) {
    var favorite by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Box(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column {
                Box {
                    Photo(Modifier.fillMaxWidth().height(240.dp), R.drawable.card_portrait_outdoor)
                    Box(Modifier.padding(16.dp).size(42.dp).clip(CircleShape).background(Color.Black.copy(.58f)).clickable(onClick = onBack), contentAlignment = Alignment.Center) { Text("‹", color = Color.White, fontSize = 34.sp) }
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.TopEnd) { CompactLogo(42) }
                }
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Avatar("LA", Modifier.size(92.dp)); Spacer(Modifier.width(14.dp)); Column { Text("Lucas Almeida", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold); Text("Fotógrafo de Ensaios", color = Muted, fontSize = 12.sp) } }
                    Spacer(Modifier.height(14.dp)); Text("⌖ São Paulo, SP     ◉ Atende todo o Brasil", color = Muted, fontSize = 11.sp); Text("★ 4,9 (88 avaliações)", color = Gold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 10.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Ensaios", "Casual", "Feminino", "Masculino").forEach { FilterChip(it) } }
                    Spacer(Modifier.height(22.dp)); Text("Sobre mim", color = Ink, fontWeight = FontWeight.SemiBold); Text("Fotógrafo especializado em ensaios externos, com foco em luz natural e fotografia autêntica e leve.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) { Stat("8+", "Anos de experiência"); Stat("320+", "Ensaios realizados"); Stat("98%", "Clientes satisfeitos") }
                    SectionTitle("Portfólio"); Spacer(Modifier.height(10.dp)); PortfolioStrip()
                }
            }
        }
        Row(Modifier.background(SurfaceHigh).padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onQuote, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(Ink, Color.White)) { Text("Solicitar orçamento", fontWeight = FontWeight.SemiBold) }; OutlinedButton({ favorite = !favorite }, Modifier.size(52.dp), contentPadding = PaddingValues(0.dp), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text(if (favorite) "♥" else "♡", color = Ink, fontSize = 23.sp) } }
    }
}

@Composable private fun Stat(value: String, label: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = Muted, fontSize = 8.sp); Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

@Composable
private fun QuoteScreen(onBack: () -> Unit, onSent: (String, String, String, String, String, String) -> Unit) {
    var type by rememberSaveable { mutableStateOf("Casamento") }; var date by rememberSaveable { mutableStateOf("24/06/2025") }; var place by rememberSaveable { mutableStateOf("São Paulo, SP") }; var duration by rememberSaveable { mutableStateOf("8 horas") }; var description by rememberSaveable { mutableStateOf("") }; var details by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("‹", color = Ink, fontSize = 36.sp, modifier = Modifier.clickable(onClick = onBack)); Spacer(Modifier.width(8.dp)); Text("Solicitar orçamento", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); CompactLogo(40) }
        Text("Preencha os dados do seu evento\ne receba propostas personalizadas.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 14.dp))
        LensField("Tipo de evento", type, { type = it }); Spacer(Modifier.height(10.dp)); LensField("Data do evento", date, { date = it }); Spacer(Modifier.height(10.dp)); LensField("Local do evento", place, { place = it }); Spacer(Modifier.height(10.dp)); LensField("Duração", duration, { duration = it }); Spacer(Modifier.height(10.dp)); LensField("Descrição do evento", description, { description = it }, singleLine = false, minLines = 3); Spacer(Modifier.height(10.dp)); LensField("Informações adicionais (opcional)", details, { details = it }, singleLine = false, minLines = 3); Spacer(Modifier.height(22.dp)); PrimaryButton("Enviar solicitação", { onSent(type, date, place, duration, description, details) }, enabled = type.isNotBlank() && date.isNotBlank() && place.isNotBlank())
    }
}

@Composable
private fun BudgetsScreen(budgets: List<Budget>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Budgets, onNavigate) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    Column(Modifier.padding(20.dp)) {
        Text("Meus orçamentos", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
        Text("Acompanhe cada momento, do pedido à entrega.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, bottom = 18.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Line).padding(4.dp)) {
            listOf("Solicitações", "Propostas", "Favoritos").forEachIndexed { index, label ->
                val selected = index == selectedTab
                Box(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if (selected) Ink else Color.Transparent).clickable { selectedTab = index }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) { Text(label, color = if (selected) Color.White else Muted, fontSize = 10.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) }
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(if (selectedTab == 2) budgets.filter { it.status == "Aceito" } else budgets) { BudgetCard(it) } }
    }
}

@Composable private fun BudgetCard(b: Budget) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable { expanded = !expanded }.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(b.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(b.status, color = Color.White, fontSize = 9.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Ink).padding(8.dp, 5.dp)) }
        Text(b.date, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(b.info, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium); Text(if (expanded) "⌃" else "⌄", color = Muted) }
        if (expanded) { HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Line); Text("Toque em uma proposta para conversar com o fotógrafo ou acompanhar os próximos passos.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp) }
    }
}

@Composable
private fun ConversationsScreen(photographers: List<Photographer>, onNavigate: (Screen) -> Unit) = AppPage(Screen.Conversations, onNavigate) {
    var query by rememberSaveable { mutableStateOf("") }
    val conversations = photographers + Photographer(name = "Rafael Costa", specialty = "Corporativo", initials = "RC", price = "")
    val results = conversations.filter { it.name.contains(query, true) || it.specialty.contains(query, true) }
    Column(Modifier.padding(top = 12.dp)) {
        Text("Conversas", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(20.dp, 8.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp), placeholder = { Text("Buscar conversa", color = Muted, fontSize = 12.sp) }, leadingIcon = { Text("⌕", color = Ink, fontSize = 20.sp) }, trailingIcon = { if (query.isNotBlank()) Text("×", color = Ink, fontSize = 20.sp, modifier = Modifier.clickable { query = "" }.padding(8.dp)) }, singleLine = true, shape = RoundedCornerShape(17.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = SurfaceHigh, unfocusedContainerColor = SurfaceHigh, focusedBorderColor = Ink, unfocusedBorderColor = Line, cursorColor = Ink))
        Spacer(Modifier.height(12.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (results.isEmpty()) item { Text("Nenhuma conversa encontrada.", color = Muted, fontSize = 12.sp, modifier = Modifier.fillParentMaxWidth().padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            items(results) { p -> Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).clickable { onNavigate(Screen.Chat) }.padding(horizontal = 10.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(p.initials, Modifier.size(52.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(p.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(if (p.name == "Lucas Almeida") "Perfeito! Vou te enviar algumas..." else "Obrigado! Ficou incrível 🙏", color = Muted, fontSize = 11.sp, maxLines = 1) }; Column(horizontalAlignment = Alignment.End) { Text("Ontem", color = Muted, fontSize = 9.sp); if (p.name == "Lucas Almeida") Box(Modifier.padding(top = 7.dp).size(8.dp).clip(CircleShape).background(Ink)) } } }
        }
    }
}

@Composable
private fun ChatScreen(onBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }; var sent by rememberSaveable { mutableStateOf(listOf<String>()) }
    var optionsOpen by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text("‹", color = Ink, fontSize = 36.sp, modifier = Modifier.clickable(onClick = onBack)); Avatar("LA", Modifier.size(42.dp)); Spacer(Modifier.width(10.dp)); Column { Text("Lucas Almeida", color = Ink, fontWeight = FontWeight.SemiBold); Text("● Online", color = Muted, fontSize = 10.sp) }; Spacer(Modifier.weight(1f)); CompactLogo(34); Text("⋮", color = Ink, fontSize = 24.sp, modifier = Modifier.clip(CircleShape).clickable { optionsOpen = true }.padding(8.dp)) }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Bubble("Olá! Recebi sua solicitação\nde orçamento.", false) }; item { Bubble("Oi Lucas! Tudo bem?", true) }; item { Bubble("Tudo ótimo! Podemos sim\nconversar sobre seu evento.", false) }; item { Bubble("Perfeito! Vou te enviar\nalgumas referências.", true) }; item { PortfolioStrip() }; item { Bubble("Ficaram incríveis! ❤️", true) }; items(sent) { Bubble(it, true) } }
        Row(Modifier.background(SurfaceHigh).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Digite sua mensagem...", color = Muted, fontSize = 12.sp) }, shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedBorderColor = Line, unfocusedBorderColor = Line, cursorColor = Ink)); Spacer(Modifier.width(9.dp)); Button(onClick = { if (input.isNotBlank()) { sent = sent + input; input = "" } }, Modifier.size(54.dp), contentPadding = PaddingValues(0.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(Ink, Color.White)) { Text("➤", fontSize = 20.sp) } }
    }
    if (optionsOpen) ActionDialog("Opções da conversa", "Notificações, arquivos compartilhados e bloqueio estarão disponíveis aqui.") { optionsOpen = false }
}

@Composable private fun Bubble(text: String, mine: Boolean) = Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) { Text(text, color = if (mine) Color.White else Ink, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.widthIn(max = 260.dp).clip(RoundedCornerShape(16.dp)).background(if (mine) Ink else SurfaceHigh).border(1.dp, if (mine) Ink else Line, RoundedCornerShape(16.dp)).padding(13.dp)) }

@Composable
private fun PhotographerPage(active: Screen, onNavigate: (Screen) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandMark()
        }
        Column(Modifier.weight(1f).fillMaxWidth()) { content() }
        PhotographerBottomNav(active, onNavigate)
    }
}

@Composable
private fun PhotographerBottomNav(active: Screen, onNavigate: (Screen) -> Unit) {
    val items = listOf(
        Triple("⌂", "Painel", Screen.ProDashboard),
        Triple("▣", "Pedidos", Screen.ProRequests),
        Triple("□", "Agenda", Screen.ProAgenda),
        Triple("♙", "Perfil", Screen.ProProfile)
    )
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(29.dp))
                .clip(RoundedCornerShape(29.dp))
                .background(Ink)
                .border(1.dp, Color.White.copy(.10f), RoundedCornerShape(29.dp))
                .padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { (icon, label, destination) ->
                val selected = active == destination
                Column(
                    Modifier.weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selected) SurfaceHigh else Color.Transparent)
                        .clickable { onNavigate(destination) }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(icon, color = if (selected) Ink else Paper.copy(.82f), fontSize = 19.sp, lineHeight = 20.sp)
                    Text(label, color = if (selected) Ink else Paper.copy(.62f), fontSize = 9.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PhotographerDashboardScreen(
    photographer: Photographer?,
    budgets: List<Budget>,
    onNavigate: (Screen) -> Unit,
    onClientMode: () -> Unit
) = PhotographerPage(Screen.ProDashboard, onNavigate) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Painel profissional", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                    Text("Olá, ${photographer?.name?.substringBefore(' ') ?: "fotógrafo"}", color = Muted, fontSize = 12.sp)
                }
                Avatar(photographer?.initials ?: "FT", Modifier.size(48.dp))
            }
        }
        item {
            OutlinedButton(onClick = onClientMode, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) {
                Text("Trocar para modo cliente", color = Ink, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProMetric("${budgets.count { it.status == "Aguardando" }}", "Novos pedidos", Modifier.weight(1f))
                ProMetric("${budgets.count { it.status == "Aceito" }}", "Confirmados", Modifier.weight(1f))
                ProMetric("${budgets.size}", "Total", Modifier.weight(1f))
            }
        }
        item { SectionTitle("Ações rápidas") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProAction("Ver pedidos", "Revise novas oportunidades", Modifier.weight(1f)) { onNavigate(Screen.ProRequests) }
                ProAction("Abrir agenda", "Organize seus trabalhos", Modifier.weight(1f)) { onNavigate(Screen.ProAgenda) }
            }
        }
        item { SectionTitle("Pedidos recentes") { onNavigate(Screen.ProRequests) } }
        items(budgets.take(3), key = { it.id }) { BudgetCard(it) }
    }
}

@Composable
private fun ProMetric(value: String, label: String, modifier: Modifier = Modifier) = Column(
    modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(14.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(value, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Text(label, color = Muted, fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
}

@Composable
private fun ProAction(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) = Column(
    modifier.clip(RoundedCornerShape(16.dp)).background(Ink).clickable(onClick = onClick).padding(16.dp)
) {
    Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Text(subtitle, color = Color.White.copy(.65f), fontSize = 9.sp, lineHeight = 13.sp, modifier = Modifier.padding(top = 5.dp))
}

@Composable
private fun PhotographerRequestsScreen(budgets: List<Budget>, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProRequests, onNavigate) {
    Column(Modifier.fillMaxSize().padding(top = 10.dp)) {
        Text("Pedidos de orçamento", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(20.dp, 10.dp))
        Text("Solicitações de clientes disponíveis para você.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 20.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (budgets.isEmpty()) item { Text("Nenhum pedido novo.", color = Muted, modifier = Modifier.padding(vertical = 40.dp)) }
            items(budgets, key = { it.id }) { BudgetCard(it) }
        }
    }
}

@Composable
private fun PhotographerAgendaScreen(budgets: List<Budget>, onNavigate: (Screen) -> Unit) = PhotographerPage(Screen.ProAgenda, onNavigate) {
    val confirmed = budgets.filter { it.status == "Aceito" }
    Column(Modifier.fillMaxSize().padding(top = 10.dp)) {
        Text("Agenda", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(20.dp, 10.dp))
        Text("Trabalhos confirmados", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 20.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (confirmed.isEmpty()) item { Text("Você ainda não tem trabalhos confirmados.", color = Muted, modifier = Modifier.padding(vertical = 40.dp)) }
            items(confirmed, key = { it.id }) { BudgetCard(it) }
        }
    }
}

@Composable
private fun PhotographerProfileScreen(
    user: User?,
    photographer: Photographer?,
    onNavigate: (Screen) -> Unit,
    onClientMode: () -> Unit,
    onLogout: () -> Unit
) = PhotographerPage(Screen.ProProfile, onNavigate) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Avatar(photographer?.initials ?: initialsForDisplay(user?.name), Modifier.size(90.dp))
        Text(photographer?.name ?: user?.name.orEmpty(), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
        Text(photographer?.specialty.orEmpty(), color = Muted, fontSize = 12.sp)
        Text(photographer?.city.orEmpty(), color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Text("Sobre", color = Ink, fontWeight = FontWeight.SemiBold)
            Text(photographer?.bio?.ifBlank { "Adicione uma apresentação ao seu perfil." }.orEmpty(), color = Muted, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 7.dp))
            HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Line)
            Text("Preço inicial: ${photographer?.price.orEmpty()}", color = Ink, fontSize = 12.sp)
            Text(user?.email.orEmpty(), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton("Usar o app como cliente", onClientMode)
        Spacer(Modifier.height(10.dp))
        LogoutButton(onLogout)
        Spacer(Modifier.height(8.dp))
    }
}

private fun initialsForDisplay(name: String?): String = name.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "LC" }

@Composable
private fun AccountScreen(user: User?, canUseProfessionalMode: Boolean, onNavigate: (Screen) -> Unit, onProfessionalMode: () -> Unit, onLogout: () -> Unit) = AppPage(Screen.Account, onNavigate) {
    var selectedAction by rememberSaveable { mutableStateOf<String?>(null) }
    val actions = listOf("♙  Meus dados", "⚙  Configurações", "♢  Segurança", "♧  Notificações", "?  Ajuda e suporte", "ⓘ  Sobre o Lens Click")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Text("⚙", color = Ink, fontSize = 24.sp, modifier = Modifier.clip(CircleShape).clickable { selectedAction = "Configurações" }.padding(8.dp)) }
        Avatar(initialsForDisplay(user?.name), Modifier.size(86.dp)); Spacer(Modifier.height(10.dp)); Text(user?.name ?: "Minha conta", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold); Text(user?.email.orEmpty(), color = Muted, fontSize = 11.sp); Text(if (canUseProfessionalMode) "Modo cliente • conta de fotógrafo" else "Conta de cliente", color = Gold, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp)); Spacer(Modifier.height(22.dp))
        if (canUseProfessionalMode) {
            OutlinedButton(onClick = onProfessionalMode, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Text("Voltar ao modo fotógrafo", color = Ink, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(14.dp))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceHigh).border(1.dp, Line, RoundedCornerShape(12.dp))) { actions.forEach { action -> Row(Modifier.fillMaxWidth().clickable { selectedAction = action.substringAfter("  ") }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(action, color = Ink, fontSize = 13.sp); Text("›", color = Muted, fontSize = 19.sp) }; HorizontalDivider(color = Line) } }
        Spacer(Modifier.height(24.dp)); LogoutButton(onLogout); Spacer(Modifier.height(8.dp))
    }
    selectedAction?.let { action -> ActionDialog(action, "Esta área foi acionada corretamente. A próxima etapa é conectá-la aos dados reais da conta.") { selectedAction = null } }
}
