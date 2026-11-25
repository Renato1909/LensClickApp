package com.lensclick.app.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lensclick.app.domain.PaymentMethod
import com.lensclick.app.ui.theme.LensLine
import com.lensclick.app.ui.theme.LensMuted
import com.lensclick.app.ui.theme.LensSurface
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date

private enum class DemoBudgetStatus { Pending, Negotiating, Approved, Rejected, Completed }

private fun DemoBudgetStatus.label() = when (this) {
    DemoBudgetStatus.Pending -> localized("Pendente")
    DemoBudgetStatus.Negotiating -> localized("Em negociação")
    DemoBudgetStatus.Approved -> localized("Aprovado")
    DemoBudgetStatus.Rejected -> localized("Recusado")
    DemoBudgetStatus.Completed -> localized("Concluído")
}

private data class ExampleBudget(
    val id: Int,
    val photographer: String,
    val client: String,
    val specialty: String,
    val location: String,
    val date: String,
    val description: String,
    val price: Long,
    val payment: PaymentMethod,
    val initial: DemoBudgetStatus
)

private val ExampleBudgets = listOf(
    ExampleBudget(1, "Lívia Monteiro", "Marina Luz", "wedding", "Jardim das Acácias, Campinas – SP", "2027-04-10",
        "Cobertura da cerimônia e da recepção para 80 convidados.", 340_000, PaymentMethod.Pix, DemoBudgetStatus.Pending),
    ExampleBudget(2, "Rafael Sato", "Pedro Costa", "portrait", "Estúdio Central, São Paulo – SP", "2027-02-14",
        "Ensaio de retratos profissionais com fundo neutro.", 68_500, PaymentMethod.CreditCard, DemoBudgetStatus.Negotiating),
    ExampleBudget(3, "Joana Nascimento", "Camila Ramos", "family", "Parque das Nuvens, Santos – SP", "2027-03-23",
        "Ensaio de família ao ar livre com tratamento das imagens.", 92_000, PaymentMethod.DebitCard, DemoBudgetStatus.Approved),
    ExampleBudget(4, "Bruno Vidal", "Elisa Vale", "corporate", "Centro Empresarial do Norte, Sorocaba – SP", "2027-05-11",
        "Retratos da equipe e fotos do espaço de trabalho.", 145_000, PaymentMethod.Cash, DemoBudgetStatus.Rejected),
    ExampleBudget(5, "Lívia Monteiro", "Daniel Reis", "events", "Salão Horizonte, Osasco – SP", "2026-09-17",
        "Cobertura de uma comemoração familiar com galeria digital.", 126_000, PaymentMethod.Pix, DemoBudgetStatus.Completed)
)

private data class DemoBudgetEvent(val state: DemoBudgetStatus, val timestamp: Long, val professional: Boolean)

private class DemoBudgetStore(context: Context) {
    private val prefs = context.getSharedPreferences("lensclick_demo_budgets", Context.MODE_PRIVATE)

    fun history(id: Int): List<DemoBudgetEvent> {
        val events = JSONArray(prefs.getString(id.toString(), "[]"))
        return List(events.length()) {
            val row = events.getJSONObject(it)
            DemoBudgetEvent(DemoBudgetStatus.valueOf(row.getString("state")), row.getLong("timestamp"), row.getBoolean("professional"))
        }
    }

    fun save(id: Int, events: List<DemoBudgetEvent>) {
        val json = JSONArray()
        events.forEach {
            json.put(JSONObject().put("state", it.state.name).put("timestamp", it.timestamp).put("professional", it.professional))
        }
        prefs.edit().putString(id.toString(), json.toString()).apply()
    }
}

@Composable
internal fun DemoBudgetsScreen(fairQuotes: List<FairDemoQuote>, onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { DemoBudgetStore(context) }
    var histories by remember(store) { mutableStateOf(ExampleBudgets.associate { it.id to store.history(it.id) }) }
    var professional by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    val budget = ExampleBudgets.firstOrNull { it.id == selected }

    fun back() {
        if (budget == null) onBack() else selected = null
    }
    BackHandler(budget != null) { back() }
    SettingsPage(localized("Orçamentos de demonstração"), ::back) {
        Text(localized("Os dados são fictícios. As alterações ficam neste aparelho."), color = LensMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
            FilterChip(localized("Cliente"), !professional) { professional = false }
            FilterChip(localized("Fotógrafo"), professional) { professional = true }
        }
        if (budget == null) {
            if (!professional && fairQuotes.isNotEmpty()) {
                SectionTitle(localized("Solicitações salvas neste aparelho"))
                fairQuotes.forEach { quote ->
                    val draft = quote.draft
                    Surface(
                        modifier = Modifier.fillMaxWidth(), color = LensSurface,
                        shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, LensLine)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(quote.photographer, style = MaterialTheme.typography.titleMedium)
                            Text(draft.eventType)
                            Text(localized("Data: {0} • Local: {1}", displayCalendarDate(draft.eventDate), draft.place), color = LensMuted)
                            Text(draft.description)
                            if (draft.duration.isNotBlank()) Text(draft.duration, color = LensMuted)
                            if (draft.additionalInfo.isNotBlank()) Text(draft.additionalInfo, color = LensMuted)
                        }
                    }
                }
            }
            ExampleBudgets.forEach { item ->
                val state = histories[item.id]?.lastOrNull()?.state ?: item.initial
                Surface(
                    onClick = { selected = item.id }, modifier = Modifier.fillMaxWidth(), color = LensSurface,
                    shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, LensLine)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("#${item.id} · ${if (professional) item.client else item.photographer}", style = MaterialTheme.typography.titleMedium)
                        Text("${specialtyLabel(item.specialty)} · ${moneyInReais(item.price)}")
                        Text(state.label(), color = LensMuted)
                    }
                }
            }
        } else {
            val events = histories[budget.id].orEmpty()
            val state = events.lastOrNull()?.state ?: budget.initial
            Text("#${budget.id} · ${budget.photographer}", style = MaterialTheme.typography.titleLarge)
            Text(localized("Cliente · {0}", budget.client))
            Text(specialtyLabel(budget.specialty))
            Text(localized(budget.description))
            Text("${displayCalendarDate(budget.date)} · ${budget.location}")
            Text("${budget.payment.label()} · ${moneyInReais(budget.price)}")
            Text(state.label(), style = MaterialTheme.typography.titleMedium)
            val choices = when {
                professional && state == DemoBudgetStatus.Pending ->
                    listOf(DemoBudgetStatus.Negotiating to localized("Enviar proposta"), DemoBudgetStatus.Rejected to localized("Recusar orçamento"))
                professional && state == DemoBudgetStatus.Approved ->
                    listOf(DemoBudgetStatus.Completed to localized("Marcar como concluído"))
                !professional && state == DemoBudgetStatus.Negotiating ->
                    listOf(DemoBudgetStatus.Approved to localized("Aprovar orçamento"), DemoBudgetStatus.Rejected to localized("Recusar orçamento"))
                else -> emptyList()
            }
            choices.forEach { (next, title) ->
                TextButton(onClick = {
                    val updated = events + DemoBudgetEvent(next, System.currentTimeMillis(), professional)
                    store.save(budget.id, updated)
                    histories = histories + (budget.id to updated)
                }) { Text(title) }
            }
            SectionTitle(localized("Histórico"))
            Text(localized("Status inicial · {0}", budget.initial.label()), color = LensMuted)
            events.forEach { event ->
                val actor = if (event.professional) localized("Fotógrafo") else localized("Cliente")
                val recorded = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, AppLanguage.locale).format(Date(event.timestamp))
                Text("${event.state.label()} · $actor · $recorded", color = LensMuted)
            }
        }
    }
}

internal fun budgetEventLabel(action: String) = when (action) {
    "created" -> localized("Pedido enviado")
    "proposed" -> localized("Proposta enviada")
    "accepted" -> localized("Proposta aceita")
    "rejected" -> localized("Pedido recusado")
    "cancelled" -> localized("Pedido cancelado")
    "withdrawn" -> localized("Proposta retirada")
    else -> action
}
