package br.com.lensclick.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import br.com.lensclick.app.data.model.PhotographerDetail
import br.com.lensclick.app.ui.components.ErrorMessage
import br.com.lensclick.app.ui.theme.Primary
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.Surface
import br.com.lensclick.app.ui.theme.SurfaceHigh
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
    "limited" to "Disponibilidade limitada",
    "unavailable" to "Indisponível no momento"
)

@Composable
fun PhotographerProfileScreen(
    slug: String,
    onBack: () -> Unit,
    onRequestQuote: () -> Unit = {},
    onStartChat: (Long, String) -> Unit = { _, _ -> },
    viewModel: PhotographerProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(slug) { viewModel.load(slug) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        androidx.compose.material3.TextButton(onClick = onBack) {
            Text("← Voltar", color = PrimaryLight)
        }

        when {
            state.loading -> Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryLight)
            }
            state.error != null -> ErrorMessage(text = state.error!!, modifier = Modifier.padding(top = 24.dp))
            state.profile != null -> PhotographerProfileContent(
                profile = state.profile!!,
                coverUrl = viewModel.profilePictureUrl(),
                portfolioUrlOf = viewModel::portfolioImageUrl,
                onRequestQuote = onRequestQuote,
                onStartChat = onStartChat
            )
        }
    }
}

@Composable
private fun PhotographerProfileContent(
    profile: PhotographerDetail,
    coverUrl: String?,
    portfolioUrlOf: (br.com.lensclick.app.data.model.PortfolioItem) -> String,
    onRequestQuote: () -> Unit = {},
    onStartChat: (Long, String) -> Unit = { _, _ -> }
) {
    Column {
        // Cabeçalho: avatar + nome + localização
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Foto de ${profile.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(SurfaceHigh)
                )
            } else {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(SurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.take(1).uppercase(),
                        color = PrimaryLight,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = listOfNotNull(
                        profile.city.takeIf { it.isNotBlank() },
                        profile.state.takeIf { it.isNotBlank() }
                    ).joinToString(" - ").ifBlank { "Brasil" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim
                )
                Text(
                    text = AVAILABILITY_LABELS[profile.availability] ?: profile.availability,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (profile.availability) {
                        "available" -> br.com.lensclick.app.ui.theme.Success
                        "limited" -> androidx.compose.ui.graphics.Color(0xFFFBBF24)
                        else -> TextDim
                    }
                )
            }
        }

        // Especialidades
        if (profile.specialties.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                profile.specialties.take(4).forEach { spec ->
                    AssistChip(
                        onClick = {},
                        label = { Text(SPECIALTY_LABELS[spec] ?: spec, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            if (profile.specialties.size > 4) {
                Text(
                    text = "+${profile.specialties.size - 4} especialidades",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Preço inicial e raio de atendimento
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("A partir de", style = MaterialTheme.typography.labelSmall, color = TextDim)
                    Text(
                        text = formatPrice(profile.startingPriceCents),
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryLight
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Atende até", style = MaterialTheme.typography.labelSmall, color = TextDim)
                    Text(
                        text = profile.serviceRadiusKm?.let { "${it.toInt()} km" } ?: "A combinar",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bio
        if (profile.bio.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text("Sobre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(profile.bio, style = MaterialTheme.typography.bodyMedium, color = TextDim)
        }

        // Contatos públicos
        val contacts = listOfNotNull(
            profile.contacts.email.takeIf { it.isNotBlank() }?.let { "E-mail: $it" },
            profile.contacts.phone.takeIf { it.isNotBlank() }?.let { "Telefone: $it" },
            profile.instagram.takeIf { it.isNotBlank() }?.let { "Instagram: @$it" },
            profile.website.takeIf { it.isNotBlank() }?.let { "Site: $it" }
        )
        if (contacts.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Contato", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            contacts.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodyMedium, color = TextDim, modifier = Modifier.padding(top = 2.dp))
            }
        }

        // Portfólio
        if (profile.portfolio.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Portfólio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height((profile.portfolio.size / 2 * 190 + 190).dp)
            ) {
                items(profile.portfolio.sortedBy { it.position }) { item ->
                    AsyncImage(
                        model = portfolioUrlOf(item),
                        contentDescription = item.altText.ifBlank { "Imagem do portfólio de ${profile.name}" },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceHigh)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onRequestQuote,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Solicitar orçamento")
            }
        }
        if (profile.userId > 0) {
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.OutlinedButton(
                onClick = { onStartChat(profile.userId, profile.name) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Conversar") }
        }
        Spacer(Modifier.height(32.dp))
    }
}

private fun formatPrice(cents: Long?): String =
    cents?.takeIf { it > 0 }?.let {
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR")).apply {
            maximumFractionDigits = if (it % 100 == 0L) 0 else 2
        }.format(it / 100.0)
    } ?: "A combinar"
