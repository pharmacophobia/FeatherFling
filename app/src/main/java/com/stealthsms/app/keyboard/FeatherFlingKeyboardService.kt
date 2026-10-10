package com.stealthsms.app.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.stealthsms.app.R
import com.stealthsms.app.pref.FeatherFlingPreferences
import com.stealthsms.app.stego.StegoManager
import com.stealthsms.app.stego.StegoMode

class FeatherFlingKeyboardService : InputMethodService() {

    private lateinit var prefs: FeatherFlingPreferences
    private var isShifted = false
    private var isSymbols = false
    private var modeButton: TextView? = null
    private val keyViews = mutableListOf<TextView>()

    private val qwertyRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    private val symbolRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
        listOf("*", "\"", "'", ":", ";", "!", "?")
    )

    override fun onCreate() {
        super.onCreate()
        prefs = FeatherFlingPreferences(this)
    }

    override fun onCreateInputView(): View {
        val dp = resources.displayMetrics.density
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#10121F"))
            val pad = (4 * dp).toInt()
            setPadding(pad, (6 * dp).toInt(), pad, (8 * dp).toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // 1. FEATHER FLING TOOLBAR WITH 🔒 FLING & LOCK BUTTON
        val toolbar = createToolbar(dp)
        root.addView(toolbar)

        // 2. KEYBOARD KEYS CONTAINER
        val keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = "keyboard_container"
        }
        root.addView(keyboardContainer)

        renderKeyRows(keyboardContainer, dp)

        return root
    }

    private fun createToolbar(dp: Float): LinearLayout {
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = (8 * dp).toInt()
            val padV = (4 * dp).toInt()
            setPadding(padH, padV, padH, (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Stego Mode Selector Chip
        modeButton = TextView(this).apply {
            text = "🪶 ${getModeShortName(prefs.defaultMode)}"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 12f
            gravity = Gravity.CENTER
            val chipBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1D33"))
                cornerRadius = 14 * dp
                setStroke((1 * dp).toInt(), Color.parseColor("#00E5FF"))
            }
            background = chipBg
            val pH = (12 * dp).toInt()
            val pV = (6 * dp).toInt()
            setPadding(pH, pV, pH, pV)
            setOnClickListener {
                vibrateKey()
                cycleMode()
            }
        }
        toolbar.addView(modeButton)

        // Spacer
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }
        toolbar.addView(spacer)

        // 🌐 Switch IME button
        val switchImeBtn = TextView(this).apply {
            text = "🌐 Switch"
            setTextColor(Color.parseColor("#A5B4FC"))
            textSize = 12f
            gravity = Gravity.CENTER
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E2238"))
                cornerRadius = 14 * dp
            }
            background = btnBg
            val pH = (10 * dp).toInt()
            val pV = (6 * dp).toInt()
            setPadding(pH, pV, pH, pV)
            setOnClickListener {
                vibrateKey()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    switchToPreviousInputMethod()
                } else {
                    imm.showInputMethodPicker()
                }
            }
        }
        toolbar.addView(switchImeBtn)

        val spacer2 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams((8 * dp).toInt(), 1)
        }
        toolbar.addView(spacer2)

        // 🔒 THE MAIN "FLING & LOCK" ACTION BUTTON
        val flingLockBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val btnBg = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.parseColor("#7C4DFF"), Color.parseColor("#00B0FF"))
            ).apply {
                cornerRadius = 14 * dp
            }
            background = btnBg
            val pH = (14 * dp).toInt()
            val pV = (6 * dp).toInt()
            setPadding(pH, pV, pH, pV)

            val lockIcon = ImageView(context).apply {
                setImageResource(R.drawable.ic_lock_cyan)
                setColorFilter(Color.WHITE)
                val iconSize = (16 * dp).toInt()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                    rightMargin = (6 * dp).toInt()
                }
            }
            val lockLabel = TextView(context).apply {
                text = "Fling & Lock"
                setTextColor(Color.WHITE)
                textSize = 13f
                paint.isFakeBoldText = true
            }

            addView(lockIcon)
            addView(lockLabel)

            setOnClickListener {
                vibrateLockAction()
                flingAndLockCurrentText()
            }
        }
        toolbar.addView(flingLockBtn)

        return toolbar
    }

    private fun renderKeyRows(container: LinearLayout, dp: Float) {
        container.removeAllViews()
        keyViews.clear()

        val activeRows = if (isSymbols) symbolRows else qwertyRows

        // Row 1 & 2
        for (i in 0..1) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48 * dp).toInt()
                ).apply {
                    topMargin = (3 * dp).toInt()
                }
            }
            for (char in activeRows[i]) {
                val key = createKeyView(char, dp)
                rowLayout.addView(key)
            }
            container.addView(rowLayout)
        }

        // Row 3 (with Shift / Backspace)
        val row3Layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (48 * dp).toInt()
            ).apply {
                topMargin = (3 * dp).toInt()
            }
        }

        // Shift key
        val shiftKey = createSpecialKey(if (isShifted) "⇪" else "⇧", dp, 1.4f) {
            isShifted = !isShifted
            vibrateKey()
            updateKeyCaps()
        }
        row3Layout.addView(shiftKey)

        for (char in activeRows[2]) {
            val key = createKeyView(char, dp)
            row3Layout.addView(key)
        }

        // Backspace key
        val backspaceKey = createSpecialKey("⌫", dp, 1.4f) {
            vibrateKey()
            handleBackspace()
        }
        row3Layout.addView(backspaceKey)
        container.addView(row3Layout)

        // Row 4 (123 / Space / Enter)
        val row4Layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (48 * dp).toInt()
            ).apply {
                topMargin = (3 * dp).toInt()
            }
        }

        // 123 / ABC toggle
        val symToggle = createSpecialKey(if (isSymbols) "ABC" else "?123", dp, 1.5f) {
            isSymbols = !isSymbols
            vibrateKey()
            renderKeyRows(container, dp)
        }
        row4Layout.addView(symToggle)

        // Comma / Period
        val commaKey = createKeyView(",", dp, 0.9f)
        row4Layout.addView(commaKey)

        // Spacebar
        val spaceKey = createSpecialKey("Feather Fling", dp, 4.0f) {
            vibrateKey()
            currentInputConnection?.commitText(" ", 1)
        }
        row4Layout.addView(spaceKey)

        val periodKey = createKeyView(".", dp, 0.9f)
        row4Layout.addView(periodKey)

        // Enter / Done key
        val enterKey = createSpecialKey("↵", dp, 1.5f, Color.parseColor("#7C4DFF")) {
            vibrateKey()
            handleEnter()
        }
        row4Layout.addView(enterKey)
        container.addView(row4Layout)
    }

    private fun createKeyView(char: String, dp: Float, weight: Float = 1f): TextView {
        val tv = TextView(this).apply {
            text = if (isShifted) char.uppercase() else char
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
            val keyBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1D2035"))
                cornerRadius = 8 * dp
            }
            background = keyBg
            val margin = (2 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
                setMargins(margin, 0, margin, 0)
            }
            setOnClickListener {
                vibrateKey()
                val textToCommit = text.toString()
                currentInputConnection?.commitText(textToCommit, 1)
                if (isShifted) {
                    isShifted = false
                    updateKeyCaps()
                }
            }
        }
        keyViews.add(tv)
        return tv
    }

    private fun createSpecialKey(
        label: String,
        dp: Float,
        weight: Float,
        bgColor: Int = Color.parseColor("#262B45"),
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER
            val keyBg = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = 8 * dp
            }
            background = keyBg
            val margin = (2 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
                setMargins(margin, 0, margin, 0)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun updateKeyCaps() {
        for (view in keyViews) {
            val curr = view.text.toString()
            if (curr.length == 1 && curr[0].isLetter()) {
                view.text = if (isShifted) curr.uppercase() else curr.lowercase()
            }
        }
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (TextUtils.isEmpty(selectedText)) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        when (action) {
            EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
            EditorInfo.IME_ACTION_GO -> ic.performEditorAction(EditorInfo.IME_ACTION_GO)
            EditorInfo.IME_ACTION_SEND -> ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
            EditorInfo.IME_ACTION_SEARCH -> ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            else -> ic.commitText("\n", 1)
        }
    }

    private fun cycleMode() {
        val allModes = StegoMode.values()
        val currIndex = allModes.indexOf(prefs.defaultMode)
        val nextMode = allModes[(currIndex + 1) % allModes.size]
        prefs.defaultMode = nextMode
        modeButton?.text = "🪶 ${getModeShortName(nextMode)}"
        Toast.makeText(this, "Mode: ${nextMode.name}", Toast.LENGTH_SHORT).show()
    }

    private fun getModeShortName(mode: StegoMode): String {
        return when (mode) {
            StegoMode.WORDLIST_GENERATOR -> "Word-List"
            StegoMode.CONTRACTION_SYNONYM -> "Synonym"
            StegoMode.TOKEN_CHAFFING -> "Chaffed"
        }
    }

    /**
     * Reads whatever the user has typed in the messenger field,
     * encodes it via Feather Fling Stego, and replaces it with the innocent cover text.
     */
    private fun flingAndLockCurrentText() {
        val ic = currentInputConnection ?: return

        val textBefore = ic.getTextBeforeCursor(4000, 0)?.toString() ?: ""
        val textAfter = ic.getTextAfterCursor(4000, 0)?.toString() ?: ""
        val fullText = (textBefore + textAfter).trim()

        if (fullText.isBlank()) {
            Toast.makeText(this, "⚠️ Type your secret message first, then tap 🔒 Fling & Lock!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val mode = prefs.defaultMode
            val passphrase = prefs.defaultPassphrase.takeIf { it.isNotBlank() }

            val result = StegoManager.encode(
                secretText = fullText,
                mode = mode,
                passphrase = passphrase
            )

            // Clear original typed text and commit encoded cover text
            ic.deleteSurroundingText(textBefore.length, textAfter.length)
            ic.commitText(result.encodedText, 1)

            Toast.makeText(this, "🪶 Encoded with Feather Fling (${mode.name})! Ready to send.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Encoding error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun vibrateKey() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(15)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun vibrateLockAction() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 50), -1))
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
