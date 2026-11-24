package com.lensclick.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lensclick.app.domain.PaymentMethod
import com.lensclick.app.ui.theme.LensCoffee
import com.lensclick.app.ui.theme.LensFieldBorder
import com.lensclick.app.ui.theme.LensInk
import com.lensclick.app.ui.theme.LensLine
import com.lensclick.app.ui.theme.LensMuted
import com.lensclick.app.ui.theme.LensOnCoffee
import com.lensclick.app.ui.theme.LensPaper
import com.lensclick.app.ui.theme.LensSand
import com.lensclick.app.ui.theme.LensSurface
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

internal fun moneyInReais(cents: Long): String =
    NumberFormat.getCurrencyInstance(AppLanguage.locale).apply {
        currency = Currency.getInstance("BRL")
    }.format(BigDecimal.valueOf(cents, 2))

internal fun PaymentMethod.label() = when (this) {
    PaymentMethod.Cash -> localized("Dinheiro")
    PaymentMethod.Pix -> "PIX"
    PaymentMethod.CreditCard -> localized("Cartão de crédito")
    PaymentMethod.DebitCard -> localized("Cartão de débito")
}

internal fun paymentStatusLabel(status: String) = when (status) {
    "awaiting_cash" -> localized("Aguardando pagamento em dinheiro")
    "cash_received" -> localized("Dinheiro recebido pelo fotógrafo")
    "gateway_required" -> localized("Aguardando integração de pagamento")
    else -> status
}

@Composable
internal fun PaymentOptions(
    photographerName: String,
    price: Long,
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit
) {
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        Modifier.fillMaxWidth().clip(shape)
            .background(Brush.horizontalGradient(listOf(LensInk, LensCoffee)))
    ) {
        Canvas(Modifier.align(Alignment.CenterEnd).size(118.dp)) {
            val center = Offset(size.width * 0.98f, size.height * 0.22f)
            for (scale in listOf(0.43f, 0.61f)) {
                drawCircle(LensSand.copy(alpha = 0.16f), size.width * scale, center, style = Stroke(1.dp.toPx()))
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(localized("Total da contratação"), color = LensSand, style = MaterialTheme.typography.labelLarge)
            Text(moneyInReais(price), color = LensOnCoffee, style = MaterialTheme.typography.headlineLarge)
            Text(photographerName, color = LensSand, style = MaterialTheme.typography.bodyMedium)
        }
    }
    SectionTitle(localized("Forma de pagamento"))
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PaymentMethod.entries.forEach { method ->
            val active = method == selected
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = if (active) LensSand else LensSurface,
                border = BorderStroke(1.dp, if (active) LensFieldBorder else LensLine)
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 60.dp)
                        .selectable(selected = active, role = Role.RadioButton, onClick = { onSelect(method) })
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PaymentIcon(method)
                    Column(Modifier.weight(1f)) {
                        Text(method.label(), color = LensInk, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (method == PaymentMethod.Cash) localized("Direto com o fotógrafo") else localized("Requer integração"),
                            color = LensMuted, style = MaterialTheme.typography.bodySmall
                        )
                    }
                    RadioButton(selected = active, onClick = null)
                }
            }
        }
    }
    PaymentNotice(selected)
}

@Composable
internal fun PaymentSummary(method: PaymentMethod, price: Long, fees: Long, status: String? = null) {
    Surface(
        color = LensSurface, shape = MaterialTheme.shapes.large, border = BorderStroke(1.dp, LensLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                PaymentIcon(method)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(method.label(), color = LensInk, style = MaterialTheme.typography.titleMedium)
                    status?.let {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (it == "cash_received") {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp), tint = LensCoffee)
                            }
                            Text(paymentStatusLabel(it), color = LensCoffee, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            HorizontalDivider(color = LensLine)
            PaymentAmount(localized("Serviço"), price)
            PaymentAmount(localized("Taxas"), fees)
            HorizontalDivider(color = LensLine)
            PaymentAmount(localized("Total"), price + fees, total = true)
            PaymentNotice(method, received = status == "cash_received")
        }
    }
}

@Composable
private fun PaymentAmount(label: String, cents: Long, total: Boolean = false) {
    val style = if (total) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, Modifier.weight(1f), color = if (total) LensInk else LensMuted, style = style)
        Text(
            moneyInReais(cents), color = if (total) LensCoffee else LensInk, style = style,
            fontWeight = if (total) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PaymentNotice(method: PaymentMethod, received: Boolean = false) {
    if (received) return
    val explanation = when (method) {
        PaymentMethod.Cash -> localized("Combine a entrega do dinheiro com o fotógrafo. Ele registra o recebimento no pedido.")
        PaymentMethod.Pix -> localized("O pedido registra a escolha de PIX. A emissão do código e a confirmação dependem de um gateway configurado.")
        PaymentMethod.CreditCard, PaymentMethod.DebitCard ->
            localized("A cobrança por cartão depende de um gateway configurado. Nenhum dado de cartão é solicitado.")
    }
    Surface(color = LensPaper, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Info, null, Modifier.size(17.dp), tint = LensCoffee)
            Text(explanation, color = LensMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PaymentIcon(method: PaymentMethod) {
    Box(
        Modifier.size(36.dp).background(LensPaper, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(21.dp)) {
            val unit = size.width / 24f
            val stroke = 1.7f * unit
            fun point(x: Float, y: Float) = Offset(x * unit, y * unit)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
                drawLine(LensCoffee, point(x1, y1), point(x2, y2), stroke, StrokeCap.Round)
            }
            when (method) {
                PaymentMethod.Cash -> {
                    drawRoundRect(
                        LensCoffee, point(2f, 5f), Size(20 * unit, 14 * unit),
                        CornerRadius(2 * unit), style = Stroke(stroke)
                    )
                    drawCircle(LensCoffee, 2.6f * unit, point(12f, 12f), style = Stroke(stroke))
                    line(5f, 12f, 6f, 12f)
                    line(18f, 12f, 19f, 12f)
                }
                PaymentMethod.Pix -> {
                    val diamond = Path().apply {
                        moveTo(12 * unit, 2.5f * unit)
                        lineTo(21.5f * unit, 12 * unit)
                        lineTo(12 * unit, 21.5f * unit)
                        lineTo(2.5f * unit, 12 * unit)
                        close()
                    }
                    drawPath(diamond, LensCoffee, style = Stroke(stroke))
                    line(8f, 12f, 16f, 12f)
                    line(13f, 9f, 16f, 12f)
                    line(13f, 15f, 16f, 12f)
                }
                PaymentMethod.CreditCard, PaymentMethod.DebitCard -> {
                    drawRoundRect(
                        LensCoffee, point(2f, 5f), Size(20 * unit, 14 * unit),
                        CornerRadius(2 * unit), style = Stroke(stroke)
                    )
                    line(2.5f, 9.5f, 21.5f, 9.5f)
                    line(6f, 15f, if (method == PaymentMethod.CreditCard) 10f else 8f, 15f)
                }
            }
        }
    }
}
