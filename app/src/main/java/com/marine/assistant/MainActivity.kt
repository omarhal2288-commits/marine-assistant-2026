package com.marine.assistant

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val apps = listOf(
        "واتساب أعمال" to "com.whatsapp.w4b",
        "إنستغرام" to "com.instagram.android",
        "سناب شات" to "com.snapchat.android",
        "تيك توك" to "com.zhiliaoapp.musically",
        "فيسبوك" to "com.facebook.katana",
        "ChatGPT" to "com.openai.chatgpt"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFF17131A.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val title = TextView(this).apply {
            text = "مساعد مارين الذكي"
            textSize = 25f
            setTextColor(0xFFFF6FAE.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
        }
        content.addView(title, match())
        val note = TextView(this).apply {
            text = "نسخة تشغيل آمنة: تفتح التطبيقات المثبتة فقط، ولا تقرأ الشاشة أو تنقر أو تكتب داخل التطبيقات."
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 18)
        }
        content.addView(note, match())
        apps.forEach { (label, packageName) ->
            val button = Button(this).apply {
                text = label
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFFB83270.toInt())
                setOnClickListener { openApp(label, packageName) }
            }
            val params = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 6, 0, 6)
            content.addView(button, params)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun openApp(label: String, packageName: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch == null) {
            Toast.makeText(this, "التطبيق غير مثبت أو لا يمكن فتحه: $label", Toast.LENGTH_LONG).show()
        } else {
            startActivity(launch)
        }
    }

    private fun match() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
}
