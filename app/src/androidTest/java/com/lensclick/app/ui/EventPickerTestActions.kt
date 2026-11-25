package com.lensclick.app.ui

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom

internal fun chooseEventDate(year: Int, month: Int, day: Int) =
    updatePicker(DatePicker::class.java, "select event date") { it.updateDate(year, month - 1, day) }

internal fun chooseEventTime(hour: Int, minute: Int) =
    updatePicker(TimePicker::class.java, "select event time") {
        it.hour = hour
        it.minute = minute
    }

private fun <T : View> updatePicker(type: Class<T>, description: String, change: (T) -> Unit) {
    onView(isAssignableFrom(type)).perform(object : ViewAction {
        override fun getConstraints() = isAssignableFrom(type)
        override fun getDescription() = description
        override fun perform(uiController: UiController, view: View) {
            change(type.cast(view)!!)
            uiController.loopMainThreadUntilIdle()
        }
    })
}
