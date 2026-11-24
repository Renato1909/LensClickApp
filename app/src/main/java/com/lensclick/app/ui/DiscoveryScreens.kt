package com.lensclick.app.ui

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lensclick.app.R
import com.lensclick.app.data.local.FairPhotographer
import com.lensclick.app.data.remote.loadPublicPhotographerPhoto
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.ui.theme.LensCoffee as Accent
import com.lensclick.app.ui.theme.LensInk as Ink
import com.lensclick.app.ui.theme.LensLine as Line
import com.lensclick.app.ui.theme.LensMuted as Muted
import com.lensclick.app.ui.theme.LensOnCoffee as OnCoffee
import com.lensclick.app.ui.theme.LensSand as Sand
import com.lensclick.app.ui.theme.LensSurface as SurfaceHigh
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private const val TestPhotographerSlug = "teste-9999"
private const val DemoPhotographerSlug = "demo-fotografo"
private val BrazilianLocale = Locale.forLanguageTag("pt-BR")

private data class DiscoveryCategory(val specialty: String, @get:DrawableRes val photo: Int)

private val DiscoveryCategories = listOf(
    DiscoveryCategory("wedding", R.drawable.discovery_wedding),
    DiscoveryCategory("portrait", R.drawable.discovery_portrait),
    DiscoveryCategory("family", R.drawable.discovery_family),
    DiscoveryCategory("events", R.drawable.discovery_events),
    DiscoveryCategory("corporate", R.drawable.card_corporate)
)

@Composable
internal fun HomeScreen(
    photographers: List<FairPhotographer>,
    onNavigate: (Screen) -> Unit,
    onCategory: (String) -> Unit,
    onFair: () -> Unit,
    onFairPhotographer: (String) -> Unit
) = AppPage(Screen.Home, onNavigate) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HomeCover { onNavigate(Screen.Search) } }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Explore por especialidade")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(DiscoveryCategories, key = { it.specialty }) { category ->
                        CategoryTile(category) { onCategory(category.specialty) }
                    }
                }
            }
        }
        item { FairPreview(photographers, onFair, onFairPhotographer) }
        item { BookingGuide() }
    }
}

@Composable
private fun HomeCover(onSearch: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Ink),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painterResource(R.drawable.discovery_hero),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(
                    0f to Color(0xEF211712),
                    0.58f to Color(0x9C211712),
                    0.94f to Color.Transparent
                )
            )
        )
        Column(
            Modifier.fillMaxWidth(0.74f).padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Seu momento, seu fotógrafo",
                color = OnCoffee,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.widthIn(max = 280.dp).semantics { heading() }
            )
            Text(
                "Encontre o estilo certo para o que você quer registrar.",
                color = OnCoffee,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.widthIn(max = 210.dp)
            )
            Button(
                onClick = onSearch,
                modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OnCoffee, contentColor = Ink),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Buscar fotógrafos")
            }
        }
    }
}

@Composable
private fun CategoryTile(category: DiscoveryCategory, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(126.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = SurfaceHigh,
        border = BorderStroke(1.dp, Line)
    ) {
        Column {
            Image(
                painterResource(category.photo),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.35f),
                contentScale = ContentScale.Crop
            )
            Text(
                specialtyLabel(category.specialty),
                color = Ink,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
private fun FairPreview(
    photographers: List<FairPhotographer>,
    onFair: () -> Unit,
    onPhotographer: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Inspire-se com portfólios", onFair)
        Text(
            "Perfis fictícios da feira para conhecer diferentes estilos.",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        LazyRow(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(photographers, key = { it.summary.slug }) { photographer ->
                Surface(
                    modifier = Modifier.width(162.dp),
                    onClick = { onPhotographer(photographer.summary.slug) },
                    shape = MaterialTheme.shapes.medium,
                    color = SurfaceHigh,
                    border = BorderStroke(1.dp, Line)
                ) {
                    Column {
                        Image(
                            painterResource(photographer.portrait),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.18f),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter
                        )
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(photographer.summary.name, color = Ink, style = MaterialTheme.typography.titleMedium)
                            Text(
                                photographer.summary.specialties.joinToString(" · ", transform = ::specialtyLabel),
                                color = Muted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingGuide() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = Sand,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Como funciona")
            BookingStep(Icons.Default.Search, "Encontre profissionais", "Filtre por cidade e especialidade.")
            BookingStep(Icons.AutoMirrored.Filled.List, "Conheça o trabalho", "Veja portfólios e avaliações no perfil.")
            BookingStep(Icons.Default.Email, "Peça um orçamento", "Conte os detalhes do seu evento pelo app.")
        }
    }
}

@Composable
private fun BookingStep(icon: ImageVector, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(SurfaceHigh, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, color = Ink, style = MaterialTheme.typography.titleMedium)
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun SearchScreen(
    viewModel: LensClickViewModel,
    guest: Boolean,
    initialSpecialty: String?,
    fairPhotographers: List<FairPhotographer>,
    onNavigate: (Screen) -> Unit,
    onPhotographer: (String) -> Unit,
    onFair: () -> Unit,
    onFairPhotographer: (String) -> Unit
) = AppPage(Screen.Search, onNavigate) {
    var city by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable(initialSpecialty) { mutableStateOf(initialSpecialty) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    val cityFilter = city.trim()
    val state by viewModel.publicSearch.collectAsStateWithLifecycle()
    val resultsPage = when (val result = state) {
        is PublicSearchState.Ready -> result.page
        is PublicSearchState.Empty -> result.page
        else -> null
    }
    val publishedPhotographers = resultsPage?.photographers.orEmpty().filterNot { it.slug == TestPhotographerSlug }
    val fairMatches = fairPhotographers.filter { photographer ->
        photographer.summary.city.contains(cityFilter, ignoreCase = true) &&
            (specialty == null || specialty in photographer.summary.specialties) &&
            publishedPhotographers.none { it.slug == photographer.summary.slug }
    }

    LaunchedEffect(cityFilter, specialty, page) {
        viewModel.searchPublicPhotographers(cityFilter, specialty, page)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SearchCover()
                if (guest) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onNavigate(Screen.Login) }, modifier = Modifier.weight(1f)) {
                            Text("Entrar para usar sua conta")
                        }
                        TextButton(onClick = onFair) { Text("Perfis da feira") }
                    }
                }
                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        if (it.trim() != cityFilter) page = 1
                        city = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cidade") },
                    placeholder = { Text("Ex.: São Paulo") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (city.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                page = 1
                                city = ""
                            }) { Icon(Icons.Default.Close, contentDescription = "Limpar cidade") }
                        }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = lensFieldColors()
                )
                Column {
                    Text("Especialidade", color = Ink, style = MaterialTheme.typography.labelLarge)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip("Todos", specialty == null) {
                            if (specialty != null) {
                                page = 1
                                specialty = null
                            }
                        }
                        DiscoveryCategories.forEach { category ->
                            FilterChip(specialtyLabel(category.specialty), specialty == category.specialty) {
                                if (specialty != category.specialty) {
                                    page = 1
                                    specialty = category.specialty
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SectionTitle("Fotógrafos publicados") }
        when {
            state == PublicSearchState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
            state == PublicSearchState.Error -> item {
                SearchStatus("Não foi possível carregar os perfis.") {
                    TextButton(onClick = { viewModel.searchPublicPhotographers(cityFilter, specialty, page) }) {
                        Text("Tentar novamente")
                    }
                }
            }
            publishedPhotographers.isEmpty() -> item {
                val message = if (page == 1 && (resultsPage?.totalPages ?: 0) <= 1) {
                    "Nenhum fotógrafo encontrado. Experimente outra cidade ou especialidade."
                } else {
                    "Esta página não tem perfis para exibir."
                }
                SearchStatus(message)
            }
            else -> {
                items(publishedPhotographers, key = { it.slug }) { photographer ->
                    DiscoveryPhotographerCard(photographer) { onPhotographer(photographer.slug) }
                }
            }
        }
        if (resultsPage != null && (page > 1 || resultsPage.totalPages > page)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { page-- }, enabled = page > 1) { Text("Anterior") }
                    TextButton(onClick = { page++ }, enabled = page < resultsPage.totalPages) { Text("Próxima") }
                }
            }
        }

        if (fairMatches.isNotEmpty()) {
            item {
                FairPreview(fairMatches, onFair, onFairPhotographer)
            }
        }
        if (!guest && fairMatches.isEmpty()) {
            item { TextButton(onClick = onFair) { Text("Perfis da feira") } }
        }
    }
}

@Composable
private fun SearchCover() {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 116.dp).clip(MaterialTheme.shapes.large).background(Sand),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
            Image(
                painterResource(R.drawable.discovery_portrait),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.47f).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(
                    0.53f to Sand,
                    0.66f to Sand.copy(alpha = 0.87f),
                    0.86f to Color.Transparent
                )
            )
        )
        Column(Modifier.fillMaxWidth(0.72f).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Buscar Fotógrafos",
                color = Ink,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text("Explore perfis e portfólios", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SearchStatus(message: String, action: @Composable (() -> Unit)? = null) {
    Surface(shape = MaterialTheme.shapes.medium, color = Sand, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                message,
                color = Ink,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            action?.invoke()
        }
    }
}

@Composable
internal fun DiscoveryPhotographerCard(
    photographer: PublicPhotographer,
    @DrawableRes portrait: Int? = null,
    onClick: () -> Unit
) {
    val location = listOf(photographer.city, photographer.state).filter(String::isNotBlank).joinToString(", ")
    val specialties = photographer.specialties.joinToString(" · ", transform = ::specialtyLabel)
    val price = remember(photographer.startingPriceCents) {
        photographer.startingPriceCents?.let {
            NumberFormat.getCurrencyInstance(BrazilianLocale).format(BigDecimal.valueOf(it, 2))
        }
    }
    val rating = remember(photographer.ratingAverage) {
        photographer.ratingAverage?.let {
            NumberFormat.getNumberInstance(BrazilianLocale).format(it)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = SurfaceHigh,
        border = BorderStroke(1.dp, Line)
    ) {
        Row(
            Modifier.clickable(role = Role.Button, onClick = onClick).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val photoModifier = Modifier.size(68.dp)
            if (portrait != null) {
                Image(
                    painterResource(portrait),
                    contentDescription = null,
                    modifier = photoModifier.clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                PublicPhotographerAvatar(photographer, photoModifier)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(photographer.name, color = Ink, style = MaterialTheme.typography.titleMedium)
                if (specialties.isNotBlank()) Text(specialties, color = Muted, style = MaterialTheme.typography.bodyMedium)
                if (location.isNotBlank()) Text(location, color = Muted, style = MaterialTheme.typography.bodyMedium)
                if (photographer.slug == DemoPhotographerSlug) {
                    Text("Perfil de demonstração", color = Accent, style = MaterialTheme.typography.labelLarge)
                } else if (price != null) {
                    Text("A partir de $price", color = Accent, style = MaterialTheme.typography.labelLarge)
                }
                if (rating != null && photographer.reviewCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Avaliação", tint = Accent, modifier = Modifier.size(14.dp))
                        Text("$rating (${photographer.reviewCount})", color = Accent, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun PublicPhotographerAvatar(
    photographer: PublicPhotographer,
    modifier: Modifier,
    photoPath: String = photographer.photoPath,
    background: Color = Sand
) {
    val photo by produceState<Bitmap?>(null, photographer.slug, photoPath) {
        value = null
        if (photoPath.isNotBlank()) {
            value = loadPublicPhotographerPhoto(photographer.slug, photoPath)
        }
    }
    val bitmap = photo
    if (bitmap == null) {
        Avatar(initialsForDisplay(photographer.name), modifier, background)
    } else {
        Image(
            bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
