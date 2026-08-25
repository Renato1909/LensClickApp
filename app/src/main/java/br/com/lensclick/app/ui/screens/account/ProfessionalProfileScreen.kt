package br.com.lensclick.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.HorizontalDivider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.lensclick.app.data.remote.ProfessionalProfile
import br.com.lensclick.app.ui.components.LcButton
import br.com.lensclick.app.ui.components.LcTextField
import br.com.lensclick.app.ui.theme.Primary
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.TextDim

private val SPECIALTY_OPTIONS = listOf(
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

/**
 * Perfil profissional do fotógrafo: bio, especialidades, localização,
 * preço inicial, contatos públicos e publicação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(onBack: () -> Unit) {
    val viewModel: ProfessionalProfileViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Voltar", color = PrimaryLight) }
        Text(
            text = "Perfil profissional",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = state.publishHint,
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Column(
                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator(color = PrimaryLight) }

            else -> {
                LcTextField(
                    value = state.bio,
                    onValueChange = viewModel::onBioChange,
                    label = "Apresentação (bio)",
                    supportingText = "${state.bio.trim().length}/1200 — mínimo de 60 para publicar",
                    minLines = 4,
                    isError = state.bio.trim().length > 1200
                )
                Spacer(Modifier.height(14.dp))

                Text("Especialidades (até 6)", style = MaterialTheme.typography.titleSmall)
                SPECIALTY_OPTIONS.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { (value, label) ->
                            FilterChip(
                                selected = value in state.specialties,
                                onClick = { viewModel.toggleSpecialty(value) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f).padding(top = 6.dp)
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1.2f)) {
                        LcTextField(value = state.city, onValueChange = viewModel::onCityChange, label = "Cidade")
                    }
                    Box(Modifier.weight(0.8f)) {
                        OutlinedTextField(
                            value = state.state,
                            onValueChange = { viewModel.onStateChange(it.uppercase().take(2)) },
                            label = { Text("UF") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        LcTextField(
                            value = state.serviceRadiusKm,
                            onValueChange = viewModel::onRadiusChange,
                            label = "Raio de atendimento (km)"
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        LcTextField(
                            value = state.startingPriceReais,
                            onValueChange = viewModel::onPriceChange,
                            label = "Preço inicial (R$)"
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                Text("Disponibilidade", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    listOf(
                        "available" to "Disponível",
                        "limited" to "Limitado",
                        "unavailable" to "Indisponível"
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = state.availability == value,
                            onClick = { viewModel.onAvailabilityChange(value) },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                LcTextField(value = state.publicEmail, onValueChange = viewModel::onPublicEmailChange, label = "E-mail profissional")
                Spacer(Modifier.height(12.dp))
                LcTextField(
                    value = state.publicPhone,
                    onValueChange = viewModel::onPublicPhoneChange,
                    label = "Telefone profissional"
                )
                Spacer(Modifier.height(12.dp))
                LcTextField(
                    value = state.instagram,
                    onValueChange = viewModel::onInstagramChange,
                    label = "Instagram (@ ou usuário)"
                )
                Spacer(Modifier.height(12.dp))
                LcTextField(value = state.website, onValueChange = viewModel::onWebsiteChange, label = "Site / portfólio externo")

                Spacer(Modifier.height(16.dp))
                ToggleRow("Mostrar e-mail no perfil público", state.showEmail, viewModel::onShowEmailChange)
                ToggleRow("Mostrar telefone no perfil público", state.showPhone, viewModel::onShowPhoneChange)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = TextDim.copy(alpha = 0.2f))

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                state.savedMessage?.let { Text(it, color = br.com.lensclick.app.ui.theme.Success, modifier = Modifier.padding(bottom = 8.dp)) }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = state.isPublished, onCheckedChange = viewModel::onPublishedChange)
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = if (state.isPublished) "Perfil publicado" else "Perfil oculto (rascunho)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.height(20.dp))
                LcButton(text = "Salvar perfil", onClick = viewModel::submit, loading = state.saving)

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::togglePublish,
                    enabled = !state.saving,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isPublished) "Despublicar perfil" else "Publicar perfil")
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
