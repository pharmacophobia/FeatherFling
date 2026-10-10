package com.stealthsms.app.pref

import android.content.Context
import android.content.SharedPreferences
import com.stealthsms.app.stego.StegoMode

class FeatherFlingPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("feather_fling_prefs", Context.MODE_PRIVATE)

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

    var isFloatingButtonEnabled: Boolean
        get() = prefs.getBoolean("floating_button_enabled", true)
        set(value) = prefs.edit().putBoolean("floating_button_enabled", value).apply()

    var showOnlyWhenTyping: Boolean
        get() = prefs.getBoolean("show_only_when_typing", false)
        set(value) = prefs.edit().putBoolean("show_only_when_typing", value).apply()

    var buttonPosX: Int
        get() = prefs.getInt("button_pos_x", 40)
        set(value) = prefs.edit().putInt("button_pos_x", value).apply()

    var buttonPosY: Int
        get() = prefs.getInt("button_pos_y", 600)
        set(value) = prefs.edit().putInt("button_pos_y", value).apply()
}
