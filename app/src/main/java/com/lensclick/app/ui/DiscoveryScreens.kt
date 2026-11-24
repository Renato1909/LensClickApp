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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lensclick.app.R
import com.lensclick.app.data.local.FairPhotographer
import com.lensclick.app.data.remote.loadPublicPhotographerPhoto
import com.lensclick.app.data.remote.loadPublicPortfolioPreview
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.ui.theme.LensCoffee as Accent
import com.lensclick.app.ui.theme.LensInk as Ink
import com.lensclick.app.ui.theme.LensLine as Line
import com.lensclick.app.ui.theme.LensMuted as Muted
import com.lensclick.app.ui.theme.LensOnCoffee as OnCoffee
import com.lensclick.app.ui.theme.LensSand as Sand
import com.lensclick.app.ui.theme.LensSurface as SurfaceHigh
import java.text.NumberFormat

internal const val TestPhotographerSlug = "teste-9999"
internal const val DemoPhotographerSlug = "demo-fotografo"

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
    viewModel: LensClickViewModel,
    photographers: List<FairPhotographer>,
    onNavigate: (Screen) -> Unit,
    onCategory: (String) -> Unit,
    onRankedProfile: (String) -> Unit,
    onRankedRequest: ((PublicPhotographer) -> Unit)?,
    onFair: () -> Unit,
    onFairPhotographer: (String) -> Unit
) = AppPage(Screen.Home, onNavigate) {
    val ranking = observeRanking(viewModel, photographers)
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("home-discovery"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HomeCover { onNavigate(Screen.Search) } }
        rankingItems(ranking, onRankedProfile, onRankedRequest)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(localized("Explore por especialidade"))
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
                localized("Seu momento, seu fotógrafo"),
                color = OnCoffee,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.widthIn(max = 280.dp).semantics { heading() }
            )
            Text(
                localized("Encontre o estilo certo para o que você quer registrar."),
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
                Text(localized("Buscar fotógrafos"))
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
        SectionTitle(localized("Inspire-se com portfólios"), onFair)
        Text(
            localized("Perfis fictícios da feira para conhecer diferentes estilos."),
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
            SectionTitle(localized("Como funciona"))
            BookingStep(Icons.Default.Search, localized("Encontre profissionais"), localized("Filtre por cidade e especialidade."))
            BookingStep(Icons.AutoMirrored.Filled.List, localized("Conheça o trabalho"), localized("Veja portfólios e avaliações no perfil."))
            BookingStep(Icons.Default.Email, localized("Peça um orçamento"), localized("Conte os detalhes do seu evento pelo app."))
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
    onRankedProfile: (String) -> Unit,
    onRankedRequest: ((PublicPhotographer) -> Unit)?,
    onFair: () -> Unit,
    onFairPhotographer: (String) -> Unit
) = AppPage(Screen.Search, onNavigate) {
    var city by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable(initialSpecialty) { mutableStateOf(initialSpecialty) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    val cityFilter = city.trim()
    val state by viewModel.publicSearch.collectAsStateWithLifecycle()
    val ranking = observeRanking(viewModel, fairPhotographers)
    val resultsPage = when (val result = state) {
        is PublicSearchState.Ready -> result.page
        is PublicSearchState.Empty -> result.page
        else -> null
    }
    val fairSlugs = remember(fairPhotographers) { fairPhotographers.map { it.summary.slug }.toSet() }
    val publishedPhotographers = resultsPage?.photographers.orEmpty().filterNot {
        it.slug == TestPhotographerSlug || it.slug in fairSlugs
    }
    val fairMatches = fairPhotographers.filter { photographer ->
        photographer.summary.city.contains(cityFilter, ignoreCase = true) &&
            (specialty == null || specialty in photographer.summary.specialties)
    }

    LaunchedEffect(cityFilter, specialty, page) {
        viewModel.searchPublicPhotographers(cityFilter, specialty, page)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().testTag("search-discovery"),
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
                            Text(localized("Entrar para usar sua conta"))
                        }
                        TextButton(onClick = onFair) { Text(localized("Perfis da feira")) }
                    }
                }
                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        if (it.trim() != cityFilter) page = 1
                        city = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized("Cidade")) },
                    placeholder = { Text(localized("Ex.: São Paulo")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (city.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                page = 1
                                city = ""
                            }) { Icon(Icons.Default.Close, contentDescription = localized("Limpar cidade")) }
                        }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = lensFieldColors()
                )
                Column {
                    Text(localized("Especialidade"), color = Ink, style = MaterialTheme.typography.labelLarge)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(localized("Todos"), specialty == null) {
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

        item { SectionTitle(localized("Fotógrafos publicados")) }
        when {
            state == PublicSearchState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
            state == PublicSearchState.Error -> item {
                SearchStatus(localized("Não foi possível carregar os perfis.")) {
                    TextButton(onClick = { viewModel.searchPublicPhotographers(cityFilter, specialty, page) }) {
                        Text(localized("Tentar novamente"))
                    }
                }
            }
            publishedPhotographers.isEmpty() -> item {
                val message = if (page == 1 && (resultsPage?.totalPages ?: 0) <= 1) {
                    localized("Nenhum fotógrafo encontrado. Experimente outra cidade ou especialidade.")
                } else {
                    localized("Esta página não tem perfis para exibir.")
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
                    TextButton(onClick = { page-- }, enabled = page > 1) { Text(localized("Anterior")) }
                    TextButton(onClick = { page++ }, enabled = page < resultsPage.totalPages) { Text(localized("Próxima")) }
                }
            }
        }

        rankingItems(ranking, onRankedProfile, onRankedRequest)
        if (fairMatches.isNotEmpty()) {
            item {
                FairPreview(fairMatches, onFair, onFairPhotographer)
            }
        }
        if (!guest && fairMatches.isEmpty()) {
            item { TextButton(onClick = onFair) { Text(localized("Perfis da feira")) } }
        }
    }
}

private data class InlineRanking(val state: PhotographerRankingState, val load: () -> Unit)

@Composable
private fun observeRanking(viewModel: LensClickViewModel, fair: List<FairPhotographer>): InlineRanking {
    val state by viewModel.photographerRanking.collectAsStateWithLifecycle()
    val excluded = remember(fair) { fair.map { it.summary.slug }.toSet() }
    LaunchedEffect(excluded, state.nextPage) {
        if (state.nextPage == 1 && !state.failed) viewModel.loadPhotographerRanking(excluded)
    }
    return InlineRanking(state) { viewModel.loadPhotographerRanking(excluded) }
}

private fun LazyListScope.rankingItems(
    ranking: InlineRanking,
    onPhotographer: (String) -> Unit,
    onRequest: ((PublicPhotographer) -> Unit)?
) {
    val state = ranking.state
    item(key = "ranking-title") { SectionTitle(localized("Fotógrafos mais bem avaliados")) }
    itemsIndexed(state.photographers, key = { _, photographer -> "ranking-${photographer.slug}" }) { index, photographer ->
        RankedPhotographer(
            photographer, index + 1, { onPhotographer(photographer.slug) },
            onRequest?.let { request -> { request(photographer) } }
        )
    }
    when {
        state.loading || (state.nextPage == 1 && !state.failed) -> item {
            SearchStatus(localized("Carregando ranking…")) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Accent)
            }
        }
        state.failed -> item {
            SearchStatus(localized("Não foi possível carregar as avaliações do ranking.")) {
                TextButton(onClick = { ranking.load() }) { Text(localized("Tentar novamente")) }
            }
        }
        state.nextPage != null -> item {
            TextButton(onClick = { ranking.load() }) { Text(localized("Ver mais fotógrafos")) }
        }
        state.photographers.isEmpty() -> item {
            Text(
                localized("Ainda não há fotógrafos com avaliações publicadas."),
                color = Muted, style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RankedPhotographer(
    photographer: PublicPhotographer,
    rank: Int,
    onProfile: () -> Unit,
    onRequest: (() -> Unit)?
) {
    val previewUrl = photographer.portfolioPreviewUrl
    val preview by produceState<Bitmap?>(null, previewUrl) {
        value = if (previewUrl.isBlank()) null else loadPublicPortfolioPreview(previewUrl)
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = SurfaceHigh,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            preview?.let { photo ->
                Image(
                    photo.asImageBitmap(), contentDescription = localized("Prévia do portfólio"),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).aspectRatio(2.5f),
                    contentScale = ContentScale.Crop
                )
            }
            DiscoveryPhotographerCard(photographer, rank = rank, onClick = onProfile)
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
                TextButton(onClick = onProfile) { Text(localized("Ver perfil")) }
                onRequest?.let {
                    TextButton(onClick = it) {
                        Text(if (photographer.startingPriceCents == null) localized("Solicitar orçamento") else localized("Contratar"))
                    }
                }
            }
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
                localized("Buscar Fotógrafos"),
                color = Ink,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text(localized("Explore perfis e portfólios"), color = Muted, style = MaterialTheme.typography.bodyMedium)
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
    rank: Int? = null,
    onClick: () -> Unit
) {
    val location = listOf(photographer.city, photographer.state).filter(String::isNotBlank).joinToString(", ")
    val specialties = photographer.specialties.joinToString(" · ", transform = ::specialtyLabel)
    val price = photographer.startingPriceCents?.let(::moneyInReais)
    val rating = photographerRatingLabel(photographer.ratingAverage, photographer.reviewCount)

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
            if (rank != null) {
                val placement = if (AppLanguage.current == Language.English) "#$rank" else "${rank}º"
                Text(
                    placement,
                    modifier = Modifier.widthIn(min = 26.dp).semantics { contentDescription = localized("{0}º lugar", rank) },
                    color = Accent,
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
                    Text(localized("Perfil de demonstração"), color = Accent, style = MaterialTheme.typography.labelLarge)
                } else if (price != null) {
                    Text(localized("A partir de {0}", price), color = Accent, style = MaterialTheme.typography.labelLarge)
                }
                if (rating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
                        Text(rating, color = Accent, style = MaterialTheme.typography.labelLarge)
                    }
                } else if (portrait == null && photographer.slug != DemoPhotographerSlug) {
                    Text(localized("Sem avaliações"), color = Muted, style = MaterialTheme.typography.bodySmall)
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

internal fun photographerRatingLabel(average: Double?, count: Int): String? {
    if (average == null || average !in 1.0..5.0 || count < 1) return null
    val score = NumberFormat.getNumberInstance(AppLanguage.locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }.format(average)
    val reviews = if (count == 1) localized("1 avaliação") else localized("{0} avaliações", count)
    return localized("{0} de 5 · {1}", score, reviews)
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


@Composable
internal fun ReviewDistribution(counts: Map<Int, Int>, total: Int) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(localized("Distribuição das notas"), color = Ink, style = MaterialTheme.typography.labelLarge)
        for (stars in 5 downTo 1) {
            val count = counts[stars] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("★ $stars", color = Accent, style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(
                    progress = { (count.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f),
                    color = Accent,
                    trackColor = Sand
                )
                Text(count.toString(), color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
