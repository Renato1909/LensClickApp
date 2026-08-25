package br.com.lensclick.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.lensclick.app.data.model.Photographer
import br.com.lensclick.app.ui.components.ErrorMessage
import br.com.lensclick.app.ui.components.LcButton
import br.com.lensclick.app.ui.components.LcTextField
import br.com.lensclick.app.ui.navigation.Routes
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.Surface
import br.com.lensclick.app.ui.theme.TextDim

private val SPECIALTY_LABELS = mapOf(
    "wedding" to "Casamento",
    "events" to "Eventos",
    "portrait" to "Retrato",
    "family" to "Família",
    "corporate" to "Corporativo",
    "product" to "Produto",
    "fashion" to "Moda",
    "newborn" to "Newborn",
    "nature" to "Natureza",
    "real-estate" to "Imobiliário"
)

private val AVAILABILITY_LABELS = mapOf(
    "available" to "Disponível",
    "limited" to "Limitado",
    "unavailable" to "Indisponível"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onOpenProfile: (String) -> Unit = { slug ->
        navController.navigate("${Routes.PHOTOGRAPHER}/$slug")
    },
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Paginação infinita: carrega mais quando o usuário chega perto do fim.
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.results.isNotEmpty() && last >= state.results.size - 3 && state.page < state.totalPages
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Encontrar fotógrafos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryLight,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { navController.navigate(Routes.ACCOUNT) }) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Minha conta",
                    tint = PrimaryLight
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(2f)) {
                LcTextField(
                    value = state.city,
                    onValueChange = viewModel::onCityChange,
                    label = "Cidade"
                )
            }
            Box(Modifier.weight(1f)) {
                LcTextField(
                    value = state.state,
                    onValueChange = viewModel::onStateChange,
                    label = "UF"
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Chips de especialidade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf<String?>(null).plus(SPECIALTY_LABELS.keys.toList()).take(5).forEach { spec ->
                FilterChip(
                    selected = state.specialty == spec,
                    onClick = { viewModel.onSpecialtyChange(spec) },
                    label = {
                        Text(spec?.let { SPECIALTY_LABELS[it] ?: it } ?: "Todas", style = MaterialTheme.typography.labelSmall)
                    }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SPECIALTY_LABELS.keys.toList().drop(5).forEach { spec ->
                FilterChip(
                    selected = state.specialty == spec,
                    onClick = { viewModel.onSpecialtyChange(spec) },
                    label = { Text(SPECIALTY_LABELS[spec] ?: spec, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Chips de disponibilidade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf<String?>(null).plus(AVAILABILITY_LABELS.keys.toList()).forEach { av ->
                FilterChip(
                    selected = state.availability == av,
                    onClick = { viewModel.onAvailabilityChange(av) },
                    label = {
                        Text(av?.let { AVAILABILITY_LABELS[it] ?: it } ?: "Qualquer", style = MaterialTheme.typography.labelSmall)
                    }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        LcButton(text = "Buscar", onClick = { viewModel.search(reset = true) }, loading = state.loading)

        state.error?.let { error ->
            ErrorMessage(text = error, modifier = Modifier.padding(top = 12.dp))
        }

        if (state.searched && !state.loading) {
            Text(
                text = "${state.total} fotógrafo(s) encontrado(s)",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            items(state.results, key = { it.slug }) { photographer ->
                PhotographerCard(photographer = photographer, onClick = { onOpenProfile(photographer.slug) })
            }
            if (state.loadingMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLight, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    }
                }
            }
            if (state.searched && state.results.isEmpty() && !state.loading && state.error == null) {
                item {
                    Text(
                        text = "Nenhum fotógrafo encontrado com esses filtros.",
                        color = TextDim,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotographerCard(photographer: Photographer, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Inicial do nome como avatar provisório
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(br.com.lensclick.app.ui.theme.SurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = photographer.name.take(1).uppercase(),
                        color = PrimaryLight,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        text = photographer.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = listOfNotNull(
                            photographer.city.takeIf { it.isNotBlank() },
                            photographer.state.takeIf { it.isNotBlank() }
                        ).joinToString(" - ").ifBlank { "Brasil" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
            if (photographer.specialties.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = photographer.specialties.take(3)
                        .joinToString(" · ") { SPECIALTY_LABELS[it] ?: it },
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryLight
                )
            }
        }
    }
}
