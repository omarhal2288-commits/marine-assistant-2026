package com.marine.assistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Bundle

class MarineAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: MarineAccessibilityService? = null
        @Volatile var latestScreenText: String = ""
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        val lines = mutableListOf<String>()
        collectText(root, lines, 0)
        latestScreenText = lines.distinct().joinToString("\n").take(6000)
        root.recycle()
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<String>, depth: Int) {
        if (depth > 18) return
        val text = node.text?.toString()?.trim().orEmpty()
        val desc = node.contentDescription?.toString()?.trim().orEmpty()
        if (text.isNotBlank()) out.add(text)
        if (desc.isNotBlank()) out.add(desc)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, out, depth + 1)
            child.recycle()
        }
    }

    fun goBack() { performGlobalAction(GLOBAL_ACTION_BACK) }
    fun goHome() { performGlobalAction(GLOBAL_ACTION_HOME) }

    fun typeIntoFocusedField(value: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findFocusedEditable(root)
        val result = if (node == null) false else {
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }
        node?.recycle()
        root.recycle()
        return result
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && node.isFocused) return AccessibilityNodeInfo.obtain(node)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFocusedEditable(child)
            child.recycle()
            if (found != null) return found
        }
        return null
    }

    fun clickByText(value: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val matches = root.findAccessibilityNodeInfosByText(value)
        var success = false
        for (node in matches) {
            var target: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(node)
            while (target != null && !target.isClickable) {
                val parent = target.parent
                target.recycle()
                target = parent
            }
            if (target != null) {
                success = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                target.recycle()
            }
            node.recycle()
            if (success) break
        }
        root.recycle()
        return success
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }
}
