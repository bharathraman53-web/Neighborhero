package com.neighborhero.neighborhero.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String, val storageKey: String) {
    NORMAL("Normal", "normal"),
    DARK("Dark", "dark");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return when (key?.lowercase()) {
                "dark" -> DARK
                "normal", "light" -> NORMAL
                else -> NORMAL
            }
        }
    }
}

object ThemeManager {
    private const val PREFS_NAME = "neighborhero_theme_prefs"
    private const val KEY_THEME_MODE = "app_theme_mode"

    private var prefs: SharedPreferences? = null
    private val _themeMode = MutableStateFlow(AppThemeMode.NORMAL)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p
            val savedKey = p.getString(KEY_THEME_MODE, AppThemeMode.NORMAL.storageKey)
            _themeMode.value = AppThemeMode.fromKey(savedKey)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.storageKey)?.apply()
    }
}
