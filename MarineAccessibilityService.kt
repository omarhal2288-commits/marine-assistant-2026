package com.marine.assistant

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MarineAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: MarineAccessibilityService? = null
        @Volatile var latestScreenText: String = ""

        private val allowedPackages = setOf(
            "com.whatsapp.w4b",
            "com.instagram.android",
            "com.snapchat.android",
            "com.zhiliaoapp.musically",
            "com.facebook.katana",
            "com.openai.chatgpt"
        )

        fun clearLatestScreenText() {
            latestScreenText = ""
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName !in allowedPackages) return

        val root = rootInActiveWindow
        if (root == null) {
            latestScreenText = ""
            return
        }

        try {
            val lines = mutableListOf<String>()
            collectText(root, lines, 0)
            latestScreenText = lines.distinct().joinToString("\n").take(6000)
        } finally {
            root.recycle()
        }
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<String>, depth: Int) {
        if (depth > 18) return
        val text = node.text?.toString()?.trim().orEmpty()
        val description = node.contentDescription?.toString()?.trim().orEmpty()
        if (text.isNotBlank()) out.add(text)
        if (description.isNotBlank()) out.add(description)

        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            try {
                collectText(child, out, depth + 1)
            } finally {
                child.recycle()
            }
        }
    }

    fun goBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun typeIntoFocusedField(value: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findFocusedEditable(root)
        val result = if (node == null) {
            false
        } else {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        node?.recycle()
        root.recycle()
        return result
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && node.isFocused) return AccessibilityNodeInfo.obtain(node)

        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            val found = try {
                findFocusedEditable(child)
            } finally {
                child.recycle()
            }
            if (found != null) return found
        }
        return null
    }

    fun clickByText(value: String): Boolean {
        val query = value.trim()
        if (query.isBlank()) return false

        val root = rootInActiveWindow ?: return false
        val matches = root.findAccessibilityNodeInfosByText(query)
        val exactMatches = matches.filter { node ->
            node.text?.toString()?.trim()?.equals(query, ignoreCase = true) == true ||
                node.contentDescription?.toString()?.trim()?.equals(query, ignoreCase = true) == true
        }

        if (exactMatches.size != 1) {
            matches.forEach { it.recycle() }
            root.recycle()
            return false
        }

        var target: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(exactMatches.single())
        var success = false
        while (target != null) {
            if (target.isClickable) {
                success = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                target.recycle()
                target = null
            } else {
                val parent = target.parent
                target.recycle()
                target = parent
            }
        }

        matches.forEach { it.recycle() }
        root.recycle()
        return success
    }

    override fun onInterrupt() {
        // No queued actions are kept between accessibility interruptions.
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        latestScreenText = ""
        super.onDestroy()
    }
}
