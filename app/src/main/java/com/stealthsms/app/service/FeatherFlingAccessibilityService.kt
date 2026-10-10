package com.stealthsms.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.stealthsms.app.pref.FeatherFlingPreferences

class FeatherFlingAccessibilityService : AccessibilityService() {

    private var overlay: LockFloatingOverlay? = null
    private var lastFocusedNode: AccessibilityNodeInfo? = null
    private lateinit var prefs: FeatherFlingPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = FeatherFlingPreferences(this)
        overlay = LockFloatingOverlay(this) {
            findActiveInputNode()
        }
        overlay?.show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val source = event.source
                if (source != null && source.isEditable) {
                    lastFocusedNode = source
                    if (prefs.showOnlyWhenTyping) {
                        overlay?.show()
                    }
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Check if current active window has an editable field
                val inputNode = findActiveInputNode()
                if (inputNode != null) {
                    lastFocusedNode = inputNode
                    if (prefs.showOnlyWhenTyping) {
                        overlay?.show()
                    }
                } else if (prefs.showOnlyWhenTyping) {
                    overlay?.hide()
                }
            }
        }
    }

    private fun findActiveInputNode(): AccessibilityNodeInfo? {
        // 1. Try currently focused input node from root in active window
        val root = rootInActiveWindow
        if (root != null) {
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (focused != null && focused.isEditable) {
                lastFocusedNode = focused
                return focused
            }
            // Search tree for editable node
            val editable = findFirstEditable(root)
            if (editable != null) {
                lastFocusedNode = editable
                return editable
            }
        }

        // 2. Fallback to cached last focused node if still valid
        if (lastFocusedNode != null && lastFocusedNode!!.isEditable) {
            return lastFocusedNode
        }

        return null
    }

    private fun findFirstEditable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (root.isEditable) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val res = findFirstEditable(child)
            if (res != null) return res
        }
        return null
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        overlay?.hide()
        overlay = null
    }
}
