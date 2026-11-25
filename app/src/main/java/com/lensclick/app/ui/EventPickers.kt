package com.lensclick.app.ui

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.lensclick.app.ui.theme.LensInk
import com.lensclick.app.ui.theme.LensSurface
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

internal val BrasiliaTimeZone: TimeZone = TimeZone.getTimeZone("America/Sao_Paulo")

@Composable
internal fun EventDateField(
    date: String,
    label: String,
    selectedLabel: (String) -> String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
    fromToday: Boolean = false
) {
    var open by rememberSaveable { mutableStateOf(false) }
    PickerButton(if (date.isBlank()) label else selectedLabel(displayCalendarDate(date)), enabled) { open = true }
    if (open) {
        val initial = remember(date, fromToday) {
            val today = Calendar.getInstance(BrasiliaTimeZone)
            val firstDay = Calendar.getInstance().apply {
                clear()
                set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
            }
            val selectedDay = firstDay.clone() as Calendar
            if (date.isNotBlank()) {
                val (year, month, day) = date.split('-').map(String::toInt)
                selectedDay.set(year, month - 1, day)
            }
            val minimum = firstDay.timeInMillis.takeIf { fromToday }
            if (minimum != null && selectedDay.timeInMillis < minimum) selectedDay.timeInMillis = minimum
            selectedDay to minimum
        }
        val (selectedDay, minimum) = initial
        var year by rememberSaveable { mutableIntStateOf(selectedDay.get(Calendar.YEAR)) }
        var month by rememberSaveable { mutableIntStateOf(selectedDay.get(Calendar.MONTH)) }
        var day by rememberSaveable { mutableIntStateOf(selectedDay.get(Calendar.DAY_OF_MONTH)) }
        PickerDialog(
            label,
            onDismiss = { open = false },
            onConfirm = {
                onSelected(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day))
                open = false
            }
        ) {
            AndroidView(
                factory = { context ->
                    DatePicker(AppLanguage.pickerContext(context)).apply {
                        minimum?.let { minDate = it }
                        init(year, month, day) { _, selectedYear, selectedMonth, selectedDate ->
                            year = selectedYear
                            month = selectedMonth
                            day = selectedDate
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

internal fun eventTimeMinutes(time: String): Int? {
    val hour = time.substringBefore(':').toIntOrNull() ?: return null
    val minute = time.substringAfter(':', "").toIntOrNull() ?: return null
    return hour * 60 + minute
}

@Composable
internal fun EventTimeField(
    time: String,
    label: String,
    selectedLabel: (String) -> String,
    defaultMinutes: Int,
    onSelected: (String) -> Unit
) {
    var open by rememberSaveable { mutableStateOf(false) }
    PickerButton(if (time.isBlank()) label else selectedLabel(time), enabled = true) { open = true }
    if (open) {
        var minutes by rememberSaveable { mutableIntStateOf(eventTimeMinutes(time) ?: defaultMinutes) }
        PickerDialog(
            label,
            onDismiss = { open = false },
            onConfirm = {
                onSelected(String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60))
                open = false
            }
        ) {
            AndroidView(
                factory = { context ->
                    TimePicker(AppLanguage.pickerContext(context)).apply {
                        setIs24HourView(true)
                        hour = minutes / 60
                        minute = minutes % 60
                        setOnTimeChangedListener { _, selectedHour, selectedMinute ->
                            minutes = selectedHour * 60 + selectedMinute
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PickerButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedButton(
        onClick = {
            focus.clearFocus()
            keyboard?.hide()
            onClick()
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) { Text(label) }
}

@Composable
private fun PickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    picker: @Composable () -> Unit
) {
    // Compose owns the window so native picker views work on Android 7.
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = LensSurface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 12.dp)) {
                Text(
                    title, modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium, color = LensInk
                )
                picker()
                Row(Modifier.align(Alignment.End).padding(horizontal = 8.dp)) {
                    TextButton(onClick = onDismiss) { Text(localized("Cancelar")) }
                    TextButton(onClick = onConfirm) { Text(localized("Confirmar")) }
                }
            }
        }
    }
}
