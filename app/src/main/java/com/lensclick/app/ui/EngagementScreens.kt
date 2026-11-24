package com.lensclick.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.Engagement
import com.lensclick.app.domain.EngagementAction
import com.lensclick.app.domain.EngagementDraft
import com.lensclick.app.domain.EngagementOffer
import com.lensclick.app.domain.PaymentMethod
import com.lensclick.app.ui.theme.LensCoffee
import com.lensclick.app.ui.theme.LensLine
import com.lensclick.app.ui.theme.LensMuted
import com.lensclick.app.ui.theme.LensSurface
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val BrazilianStates = "AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO".split(' ').toSet()
private val EventTime = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")

private fun engagementStatusLabel(status: String) = when (status) {
    "pending" -> localized("Aguardando fotógrafo")
    "accepted" -> localized("Contratação aceita")
    "declined" -> localized("Recusada")
    "delivered" -> localized("Aguardando confirmação do cliente")
    "completed" -> localized("Concluída")
    "cancelled" -> localized("Cancelada")
    else -> status
}

internal fun displayCalendarDate(value: String): String {
    val date = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }.parse(value.take(10))
    } catch (_: java.text.ParseException) {
        null
    } ?: return value
    return DateFormat.getDateInstance(DateFormat.SHORT, AppLanguage.locale).format(date)
}

private fun validEventDate(value: String): Boolean {
    if (!value.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) return false
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
    }
    return try {
        parser.parse(value) != null && value >= parser.format(System.currentTimeMillis())
    } catch (_: java.text.ParseException) {
        false
    }
}

private suspend fun <T> attempt(action: suspend () -> T): Result<T> = try {
    Result.success(action())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: Exception) {
    Result.failure(failure)
}

private fun errorMessage(error: Throwable): String = when ((error as? MobileAuthException)?.code) {
    MobileAuthErrorCode.INVALID_INPUT -> localized("Revise os dados informados e tente novamente.")
    MobileAuthErrorCode.FORBIDDEN -> localized("Esta ação está disponível apenas ao participante autorizado.")
    MobileAuthErrorCode.UNAUTHENTICATED -> localized("Sua sessão expirou. Entre novamente.")
    MobileAuthErrorCode.NOT_FOUND -> localized("Este pedido ou profissional está indisponível.")
    MobileAuthErrorCode.CONFLICT -> localized("Confira o valor, o horário e o status do pedido antes de tentar novamente.")
    MobileAuthErrorCode.NETWORK -> localized("Não foi possível conectar. Verifique sua internet.")
    MobileAuthErrorCode.RATE_LIMITED -> localized("Muitas tentativas. Aguarde antes de tentar novamente.")
    else -> localized("Não foi possível concluir a operação. Tente novamente.")
}

@Composable
internal fun HireScreen(
    viewModel: LensClickViewModel,
    slug: String,
    budgetId: Long?,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    var retry by remember { mutableIntStateOf(0) }
    val offer by produceState<Result<EngagementOffer>?>(null, slug, budgetId, retry) {
        value = null
        value = attempt { viewModel.getEngagementOffer(slug, budgetId) }
    }
    val loaded = offer
    when {
        loaded == null -> SettingsPage(localized("Contratar fotógrafo"), onBack) {
            AuthStatusBanner(localized("Carregando os dados da contratação…"), loading = true)
        }
        loaded.isFailure -> SettingsPage(localized("Contratar fotógrafo"), onBack) {
            AuthStatusBanner(errorMessage(requireNotNull(loaded.exceptionOrNull())), loading = false, error = true)
            TextButton(onClick = { retry++ }) { Text(localized("Tentar novamente")) }
        }
        else -> HireForm(viewModel, slug, loaded.getOrThrow(), onBack, onCreated)
    }
}

private enum class HireStep { Event, Payment, Review }

@Composable
private fun HireForm(
    viewModel: LensClickViewModel,
    slug: String,
    offer: EngagementOffer,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val reference = rememberSaveable { UUID.randomUUID().toString() }
    var category by rememberSaveable { mutableStateOf(offer.categories.firstOrNull().orEmpty()) }
    var description by rememberSaveable { mutableStateOf(offer.budget?.description.orEmpty()) }
    var eventType by rememberSaveable { mutableStateOf(offer.budget?.eventType.orEmpty()) }
    var date by rememberSaveable { mutableStateOf(offer.budget?.eventDate.orEmpty()) }
    var start by rememberSaveable { mutableStateOf("") }
    var end by rememberSaveable { mutableStateOf("") }
    var venue by rememberSaveable { mutableStateOf(offer.budget?.place.orEmpty()) }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var venueDetails by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf(offer.budget?.additionalInfo.orEmpty()) }
    var payment by rememberSaveable { mutableStateOf(PaymentMethod.Cash.name) }
    var step by rememberSaveable { mutableStateOf(HireStep.Event) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val draft = EngagementDraft(
        category, description.trim(), eventType.trim(), date.trim(), start.trim(), end.trim(), venue.trim(),
        address.trim(), city.trim(), state.trim().uppercase(Locale.ROOT), venueDetails.trim(), notes.trim(),
        PaymentMethod.valueOf(payment)
    )
    val dateValid = validEventDate(draft.eventDate)
    val timesValid = EventTime.matches(draft.start) && EventTime.matches(draft.end) && draft.start < draft.end
    val valid = category.isNotBlank() && draft.description.length in 10..1000 &&
        draft.eventType.length in 2..80 && dateValid && timesValid && draft.venue.length in 2..160 &&
        draft.address.length in 5..220 && draft.city.length in 2..80 && draft.state in BrazilianStates &&
        draft.venueDetails.length <= 1000 && draft.notes.length <= 1000

    fun back() {
        if (busy) return
        error = null
        when (step) {
            HireStep.Event -> onBack()
            HireStep.Payment -> step = HireStep.Event
            HireStep.Review -> step = HireStep.Payment
        }
    }
    BackHandler(step != HireStep.Event || busy) { back() }
    val title = when (step) {
        HireStep.Event -> localized("Contratar fotógrafo")
        HireStep.Payment -> localized("Pagamento")
        HireStep.Review -> localized("Revisar contratação")
    }
    key(step) {
        SettingsPage(title, ::back) {
            if (step != HireStep.Payment) {
                Text(offer.photographerName, style = MaterialTheme.typography.titleMedium)
            }
            if (step == HireStep.Review) {
                EventSummary(draft)
                SectionTitle(localized("Pagamento"))
                PaymentSummary(draft.paymentMethod, offer.priceCents, 0)
                error?.let { AuthStatusBanner(it, loading = false, error = true) }
                if (busy) AuthStatusBanner(localized("Enviando contratação…"), loading = true)
                PrimaryButton(localized("Confirmar contratação"), enabled = !busy, onClick = {
                    busy = true
                    error = null
                    scope.launch {
                        val result = attempt { viewModel.createEngagement(slug, reference, offer, draft) }
                        busy = false
                        result.onSuccess { onCreated(it.id) }.onFailure { error = errorMessage(it) }
                    }
                })
                TextButton(onClick = { step = HireStep.Event; error = null }, enabled = !busy) {
                    Text(localized("Editar informações"))
                }
                TextButton(onClick = ::back, enabled = !busy) { Text(localized("Alterar forma de pagamento")) }
            } else if (step == HireStep.Event) {
                Text(localized("Dados do evento → Pagamento → Revisão"), color = LensMuted)
                SectionTitle(localized("Serviço"))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    offer.categories.forEach { option ->
                        FilterChip(specialtyLabel(option), category == option) { category = option }
                    }
                }
                LensField(localized("Tipo de evento"), eventType, { eventType = it })
                LensField(localized("Descrição do serviço (mín. 10 caracteres)"), description, { description = it }, singleLine = false, maxLines = 4)
                SectionTitle(localized("Data, horário e local"))
                if (offer.budget == null) {
                    LensField(localized("Data (AAAA-MM-DD)"), date, { date = it })
                } else {
                    Text(displayCalendarDate(date), color = LensMuted)
                }
                if (date.isNotBlank() && !dateValid) Text(localized("Informe uma data válida a partir de hoje."), color = MaterialTheme.colorScheme.error)
                Text(localized("Horário de Brasília (UTC−3)"), color = LensMuted)
                LensField(localized("Início (HH:MM)"), start, { start = it })
                LensField(localized("Término (HH:MM)"), end, { end = it })
                if (end.isNotBlank() && !timesValid) Text(localized("O término precisa ser depois do início, no mesmo dia."), color = MaterialTheme.colorScheme.error)
                LensField(localized("Nome do local"), venue, { venue = it })
                LensField(localized("Endereço completo"), address, { address = it })
                LensField(localized("Cidade"), city, { city = it })
                LensField(localized("Estado (UF)"), state, { state = it })
                if (state.isNotBlank() && draft.state !in BrazilianStates) Text(localized("Informe uma UF brasileira válida."), color = MaterialTheme.colorScheme.error)
                LensField(localized("Informações sobre o local"), venueDetails, { venueDetails = it }, singleLine = false, maxLines = 3)
                LensField(localized("Observações e solicitações especiais"), notes, { notes = it }, singleLine = false, maxLines = 3)
                PrimaryButton(localized("Continuar para pagamento"), enabled = valid, onClick = { step = HireStep.Payment })
            } else {
                PaymentOptions(offer.photographerName, offer.priceCents, draft.paymentMethod) { payment = it.name }
                PrimaryButton(localized("Revisar contratação"), onClick = { step = HireStep.Review })
            }
        }
    }
}

@Composable
private fun EventSummary(draft: EngagementDraft) {
    Text(specialtyLabel(draft.category), style = MaterialTheme.typography.titleMedium)
    Text(draft.eventType)
    Text(draft.description)
    Text("${displayCalendarDate(draft.eventDate)} · ${draft.start}–${draft.end} (UTC−3)")
    Text("${draft.venue} · ${draft.address}")
    Text("${draft.city} – ${draft.state}")
    if (draft.venueDetails.isNotBlank()) Text(draft.venueDetails)
    if (draft.notes.isNotBlank()) {
        Text(localized("Observações"), style = MaterialTheme.typography.titleSmall)
        Text(draft.notes)
    }
}

@Composable
internal fun EngagementsScreen(viewModel: LensClickViewModel, professional: Boolean, reviewSlug: String?, onBack: () -> Unit, onOpen: (Long) -> Unit) {
    var retry by remember { mutableIntStateOf(0) }
    val response by produceState<Result<List<Engagement>>?>(null, retry) {
        value = null
        value = attempt { viewModel.getEngagements() }
    }
    SettingsPage(if (reviewSlug != null) localized("Contratações para avaliar") else localized("Contratações"), onBack) {
        when {
            response == null -> AuthStatusBanner(localized("Carregando contratações…"), loading = true)
            response?.isFailure == true -> {
                AuthStatusBanner(errorMessage(requireNotNull(response?.exceptionOrNull())), loading = false, error = true)
                TextButton(onClick = { retry++ }) { Text(localized("Tentar novamente")) }
            }
            else -> {
                val rows = response?.getOrNull().orEmpty().filter {
                    reviewSlug == null || (it.slug == reviewSlug && it.status == "completed" && !it.reviewPublished)
                }
                if (rows.isEmpty()) Text(
                    if (reviewSlug == null) localized("Você ainda não tem contratações.")
                    else localized("Assim que uma contratação for concluída, você poderá avaliar o fotógrafo.")
                )
                rows.forEach { row ->
                    Surface(
                        onClick = { onOpen(row.id) },
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, LensLine), color = LensSurface
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("#${row.id} · ${if (professional) row.clientName else row.photographerName}",
                                style = MaterialTheme.typography.titleMedium)
                            Text("${row.service.eventType} · ${displayCalendarDate(row.service.eventDate)}")
                            Text(engagementStatusLabel(row.status), color = LensCoffee)
                            Text(paymentStatusLabel(row.paymentStatus), color = LensMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun EngagementDetailsScreen(
    viewModel: LensClickViewModel,
    id: Long,
    professional: Boolean,
    justCreated: Boolean,
    onBack: () -> Unit
) {
    var retry by remember { mutableIntStateOf(0) }
    val response by produceState<Result<com.lensclick.app.domain.EngagementDetail>?>(null, id, retry) {
        value = null
        value = attempt { viewModel.getEngagement(id) }
    }
    val scope = rememberCoroutineScope()
    val detail = response?.getOrNull()
    var busy by remember { mutableStateOf(false) }
    var reviewOpen by rememberSaveable { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    fun update(action: EngagementAction) {
        busy = true
        actionError = null
        scope.launch {
            val result = attempt { viewModel.updateEngagement(id, action) }
            busy = false
            result.onSuccess { retry++ }.onFailure { actionError = errorMessage(it) }
        }
    }
    BackHandler(busy) {}
    SettingsPage(localized("Contratação #{0}", id), onBack = { if (!busy) onBack() }) {
        if (justCreated) AuthStatusBanner(localized("Pedido enviado ao fotógrafo."), loading = false)
        if (response == null || busy) AuthStatusBanner(localized("Atualizando contratação…"), loading = true)
        response?.exceptionOrNull()?.let {
            AuthStatusBanner(errorMessage(it), loading = false, error = true)
            TextButton(onClick = { retry++ }) { Text(localized("Tentar novamente")) }
        }
        actionError?.let { AuthStatusBanner(it, loading = false, error = true) }
        if (detail != null) {
            val engagement = detail.engagement
            Text(engagementStatusLabel(engagement.status), color = LensCoffee, style = MaterialTheme.typography.titleMedium)
            Text(if (professional) engagement.clientName else engagement.photographerName,
                style = MaterialTheme.typography.titleMedium)
            EventSummary(engagement.service)
            SectionTitle(localized("Pagamento"))
            PaymentSummary(
                engagement.service.paymentMethod, engagement.priceCents, engagement.feeCents, engagement.paymentStatus
            )
            if (professional) {
                if (engagement.status == "pending") {
                    PrimaryButton(localized("Aceitar contratação"), { update(EngagementAction.Accept) }, enabled = !busy)
                    TextButton(onClick = { update(EngagementAction.Decline) }, enabled = !busy) { Text(localized("Recusar contratação")) }
                }
                if (engagement.status == "accepted") {
                    PrimaryButton(localized("Informar serviço realizado"), { update(EngagementAction.Deliver) }, enabled = !busy)
                }
                if (engagement.status in setOf("accepted", "delivered", "completed") &&
                    engagement.paymentStatus == "awaiting_cash"
                ) TextButton(onClick = { update(EngagementAction.ReceiveCash) }, enabled = !busy) {
                    Text(localized("Confirmar dinheiro recebido"))
                }
            } else {
                if (engagement.status == "delivered") {
                    PrimaryButton(localized("Confirmar conclusão"), { update(EngagementAction.Complete) }, enabled = !busy)
                }
                if (engagement.status in setOf("pending", "accepted") && engagement.paymentStatus != "cash_received") {
                    TextButton(onClick = { update(EngagementAction.Cancel) }, enabled = !busy) { Text(localized("Cancelar contratação")) }
                }
                if (engagement.status == "completed") {
                    if (engagement.reviewPublished) Text(localized("Avaliação enviada."), color = LensMuted)
                    else PrimaryButton(localized("Avaliar fotógrafo"), { reviewOpen = true }, enabled = !busy)
                }
            }
            SectionTitle(localized("Histórico"))
            detail.history.forEach { event ->
                Text("${displayCalendarDate(event.createdAt)} · ${engagementStatusLabel(event.status)}")
                Text(paymentStatusLabel(event.paymentStatus), color = LensMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (reviewOpen) EngagementReviewDialog(busy, actionError, { reviewOpen = false; actionError = null }) { rating, comment ->
        busy = true
        actionError = null
        scope.launch {
            val result = attempt { viewModel.reviewEngagement(id, rating, comment) }
            busy = false
            result.onSuccess { reviewOpen = false; retry++ }.onFailure { actionError = errorMessage(it) }
        }
    }
}

@Composable
private fun EngagementReviewDialog(busy: Boolean, error: String?, onDismiss: () -> Unit, onSend: (Int, String) -> Unit) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(localized("Avaliar fotógrafo")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized("Escolha uma nota de 1 a 5 estrelas."))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { star -> FilterChip(star.toString(), rating == star) { rating = star } }
                }
                LensField(localized("Comentário (mín. 10 caracteres)"), comment, { comment = it }, singleLine = false, maxLines = 3)
                error?.let { AuthStatusBanner(it, loading = false, error = true) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(rating, comment.trim()) }, enabled = !busy && rating in 1..5 && comment.trim().length in 10..1000) {
                Text(localized("Enviar avaliação"))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(localized("Cancelar")) } }
    )
}
