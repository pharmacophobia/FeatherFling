package com.stealthsms.app.service

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import androidx.core.content.ContextCompat
import com.stealthsms.app.R
import com.stealthsms.app.pref.FeatherFlingPreferences
import com.stealthsms.app.stego.StegoManager
import com.stealthsms.app.stego.StegoMode
import com.stealthsms.app.ui.MainActivity
import kotlin.math.abs

class LockFloatingOverlay(
    private val serviceContext: Context,
    private val getNodeProvider: () -> AccessibilityNodeInfo?
) {

    private val windowManager = serviceContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = FeatherFlingPreferences(serviceContext)
    private val handler = Handler(Looper.getMainLooper())

    private var rootView: FrameLayout? = null
    private var lockButton: ImageView? = null
    private var menuCard: LinearLayout? = null
    private var isMenuVisible = false
    private var isAttached = false

    private val layoutParams = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        format = PixelFormat.TRANSLUCENT
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        gravity = Gravity.TOP or Gravity.START
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        x = prefs.buttonPosX
        y = prefs.buttonPosY
    }

    fun show() {
        if (isAttached || !prefs.isFloatingButtonEnabled) return
        createViews()
        try {
            windowManager.addView(rootView, layoutParams)
            isAttached = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        if (!isAttached || rootView == null) return
        try {
            windowManager.removeView(rootView)
            isAttached = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePosition(x: Int, y: Int) {
        if (!isAttached || rootView == null) return
        layoutParams.x = x
        layoutParams.y = y
        try {
            windowManager.updateViewLayout(rootView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createViews() {
        val dp = serviceContext.resources.displayMetrics.density
        val btnSizePx = (54 * dp).toInt()

        rootView = FrameLayout(serviceContext).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Circular Lock Button
        lockButton = ImageView(serviceContext).apply {
            layoutParams = FrameLayout.LayoutParams(btnSizePx, btnSizePx).apply {
                gravity = Gravity.CENTER
            }
            setBackgroundResource(R.drawable.floating_lock_bg)
            setImageResource(R.drawable.ic_lock_cyan)
            val pad = (13 * dp).toInt()
            setPadding(pad, pad, pad, pad)
            contentDescription = "Feather Fling Lock Button"
            elevation = 12 * dp
        }

        // Mini Menu Popup Card
        menuCard = createMenuCard(dp).apply {
            visibility = View.GONE
        }

        rootView?.addView(menuCard)
        rootView?.addView(lockButton)

        setupTouchListener(lockButton!!, dp)
    }

    private fun createMenuCard(dp: Float): LinearLayout {
        val menu = LinearLayout(serviceContext).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (12 * dp).toInt()
            setPadding(pad, pad, pad, pad)
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#121424"))
                cornerRadius = 16 * dp
                setStroke((1.5f * dp).toInt(), Color.parseColor("#7C4DFF"))
            }
            background = bg
            elevation = 16 * dp
            layoutParams = FrameLayout.LayoutParams(
                (230 * dp).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = (60 * dp).toInt()
            }
        }

        // Header Title
        val titleRow = TextView(serviceContext).apply {
            text = "🪶 Feather Fling"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 14f
            paint.isFakeBoldText = true
            setPadding(0, 0, 0, (6 * dp).toInt())
        }
        menu.addView(titleRow)

        // Stego Mode Radio Buttons
        val modeGroup = RadioGroup(serviceContext).apply {
            orientation = RadioGroup.VERTICAL
        }

        val modes = listOf(
            StegoMode.WORDLIST_GENERATOR to "Word-List Mnemonic (Default)",
            StegoMode.CONTRACTION_SYNONYM to "Synonym Substitution",
            StegoMode.TOKEN_CHAFFING to "Chaffed Token (#TRK)"
        )

        modes.forEach { (mode, label) ->
            val rb = RadioButton(serviceContext).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 11f
                isChecked = prefs.defaultMode == mode
                setOnClickListener {
                    prefs.defaultMode = mode
                    vibrateQuick()
                    Toast.makeText(serviceContext, "Mode set to: ${mode.name}", Toast.LENGTH_SHORT).show()
                }
            }
            modeGroup.addView(rb)
        }
        menu.addView(modeGroup)

        // Quick Decode Received Text Button
        val decodeBtn = Button(serviceContext).apply {
            text = "🔓 Decode Active Text"
            setTextColor(Color.WHITE)
            textSize = 11f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#311B92"))
                cornerRadius = 8 * dp
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (36 * dp).toInt()
            ).apply {
                topMargin = (8 * dp).toInt()
            }
            setOnClickListener {
                toggleMenu()
                decodeFocusedText()
            }
        }
        menu.addView(decodeBtn)

        // Open App Button
        val openAppBtn = Button(serviceContext).apply {
            text = "📱 Open Feather Fling"
            setTextColor(Color.parseColor("#B388FF"))
            textSize = 11f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1F2238"))
                cornerRadius = 8 * dp
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (36 * dp).toInt()
            ).apply {
                topMargin = (6 * dp).toInt()
            }
            setOnClickListener {
                toggleMenu()
                val intent = Intent(serviceContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                serviceContext.startActivity(intent)
            }
        }
        menu.addView(openAppBtn)

        return menu
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener(view: View, dp: Float) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false
        var downTime = 0L

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    downTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isClick = false
                    }
                    layoutParams.x = initialX + dx
                    layoutParams.y = initialY + dy
                    try {
                        windowManager.updateViewLayout(rootView, layoutParams)
                    } catch (e: Exception) {
                        // Ignore race condition on drag
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val duration = System.currentTimeMillis() - downTime
                    if (isClick && duration < 400) {
                        // Single Click: Fling & Lock active text!
                        vibrateQuick()
                        encodeFocusedText()
                    } else if (isClick && duration >= 400) {
                        // Long press: Toggle settings menu
                        vibrateLong()
                        toggleMenu()
                    } else {
                        // Save dragged position
                        prefs.buttonPosX = layoutParams.x
                        prefs.buttonPosY = layoutParams.y
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleMenu() {
        isMenuVisible = !isMenuVisible
        menuCard?.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
    }

    /**
     * Finds active editable input field, encodes its secret text via Feather Fling,
     * and sets the encoded cover text directly into the text field ready to send.
     */
    fun encodeFocusedText() {
        val node = getNodeProvider()
        if (node == null) {
            Toast.makeText(
                serviceContext,
                "⚠️ Tap inside Messenger's text box first, then tap 🔒",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val text = node.text?.toString() ?: ""
        if (text.isBlank()) {
            Toast.makeText(
                serviceContext,
                "⚠️ Type your secret message in the box first!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val mode = prefs.defaultMode
            val passphrase = prefs.defaultPassphrase.takeIf { it.isNotBlank() }

            val result = StegoManager.encode(
                secretText = text,
                mode = mode,
                passphrase = passphrase
            )

            // 1. Replace text in the active input field
            val arguments = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    result.encodedText
                )
            }
            val replaced = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            // 2. Also copy to clipboard as seamless fallback
            val clipboard = serviceContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Feather Fling", result.encodedText))

            if (!replaced) {
                // If direct ACTION_SET_TEXT failed, try paste action
                node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            }

            vibrateSuccess()
            Toast.makeText(
                serviceContext,
                "🪶 Flung & Locked (${mode.name})! Ready to send.",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                serviceContext,
                "Encoding error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Decodes the secret payload from the currently selected/focused text.
     */
    fun decodeFocusedText() {
        val node = getNodeProvider()
        val text = node?.text?.toString()?.takeIf { it.isNotBlank() } ?: run {
            val clipboard = serviceContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        }

        if (text.isBlank()) {
            Toast.makeText(
                serviceContext,
                "⚠️ No text found in active field or clipboard to decode.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val passphrase = prefs.defaultPassphrase.takeIf { it.isNotBlank() }
            val result = StegoManager.decodeAuto(text, passphrase)

            if (result.success) {
                vibrateSuccess()
                Toast.makeText(
                    serviceContext,
                    "🔓 Decoded Secret: \"${result.secretMessage}\"",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    serviceContext,
                    "❌ No Feather Fling payload: ${result.errorMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                serviceContext,
                "Decode error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun vibrateQuick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = serviceContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = serviceContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(30)
            }
        } catch (e: Exception) {
            // Ignore if vibration is not supported
        }
    }

    private fun vibrateLong() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = serviceContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = serviceContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(80)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val v = serviceContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 40, 45), -1))
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
