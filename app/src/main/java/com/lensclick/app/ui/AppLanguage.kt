package com.lensclick.app.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import org.json.JSONObject

internal enum class Language(val tag: String, val label: String) {
    Portuguese("pt-BR", "Português"),
    English("en", "English"),
    Spanish("es", "Español")
}

internal object AppLanguage {
    private const val PreferenceKey = "language"
    private val slot = Regex("""\{([0-9]+)\}""")
    private lateinit var preferences: SharedPreferences
    private lateinit var catalogs: Map<Language, Map<String, String>>

    var current: Language by mutableStateOf(Language.Portuguese)
        private set

    val locale: Locale
        get() = Locale.forLanguageTag(current.tag)

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("lensclick_preferences", Context.MODE_PRIVATE)
        catalogs = Language.entries.filter { it != Language.Portuguese }.associateWith { language ->
            val entries = context.assets.open("i18n/${language.tag}.json").bufferedReader().use {
                JSONObject(it.readText())
            }
            entries.keys().asSequence().associateWith(entries::getString)
        }
        current = Language.entries.firstOrNull { it.tag == preferences.getString(PreferenceKey, null) }
            ?: Language.Portuguese
    }

    fun select(language: Language) {
        preferences.edit().putString(PreferenceKey, language.tag).apply()
        current = language
    }

    fun text(source: String, args: Array<out Any?>): String {
        val language = current
        val pattern = if (language == Language.Portuguese) source else catalogs[language]?.get(source) ?: source
        return if (args.isEmpty()) pattern else slot.replace(pattern) { match ->
            args[match.groupValues[1].toInt()].toString()
        }
    }

    fun pickerContext(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }
}

internal fun localized(source: String, vararg args: Any?): String = AppLanguage.text(source, args)
