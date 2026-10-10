package com.stealthsms.app.pref

import android.content.Context
import android.content.SharedPreferences
import com.stealthsms.app.stego.StegoMode

class FeatherFlingPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("feather_fling_prefs", Context.MODE_PRIVATE)

    var isGameMenuEnabled: Boolean
        get() = prefs.getBoolean("game_menu_enabled", true)
        set(value) = prefs.edit().putBoolean("game_menu_enabled", value).apply()

    var isBiometricsEnabled: Boolean
        get() = prefs.getBoolean("biometrics_enabled", false)
        set(value) = prefs.edit().putBoolean("biometrics_enabled", value).apply()

    var defaultMode: StegoMode
        get() {
            val name = prefs.getString("default_stego_mode", StegoMode.WORDLIST_GENERATOR.name)
            return try {
                StegoMode.valueOf(name ?: StegoMode.WORDLIST_GENERATOR.name)
            } catch (e: Exception) {
                StegoMode.WORDLIST_GENERATOR
            }
        }
        set(value) = prefs.edit().putString("default_stego_mode", value.name).apply()

    var defaultPassphrase: String
        get() = prefs.getString("default_passphrase", "") ?: ""
        set(value) = prefs.edit().putString("default_passphrase", value).apply()
}
