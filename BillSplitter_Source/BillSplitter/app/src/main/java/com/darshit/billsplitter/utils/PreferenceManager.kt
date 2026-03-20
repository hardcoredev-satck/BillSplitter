package com.darshit.billsplitter.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bill_splitter_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getCurrency(): String = prefs.getString(KEY_CURRENCY, "USD") ?: "USD"

    fun getCurrencySymbol(): String = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"

    fun setCurrency(code: String, symbol: String) {
        prefs.edit()
            .putString(KEY_CURRENCY, code)
            .putString(KEY_CURRENCY_SYMBOL, symbol)
            .apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }
}
