package com.marine.assistant

import android.app.Activity
import android.content.Intent
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
            setPadding(0, 12, 0, 20)
        }
        content.addView(title, match())

        val privacy = TextView(this).apply {
            text = "النسخة الآمنة: تفتح التطبيقات التي تختارها فقط. لا تقرأ الشاشة، ولا تنقر أو تكتب داخل التطبيقات، ولا تطلب صلاحية إمكانية الوصول أو الإنترنت."
            textSize = 14f
            setTextColor(0xFFFFD6E7.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 20)
        }
        content.addView(privacy, match())

        apps.forEach { (label, packageName) ->
            addButton(content, label) { openApp(label, packageName) }
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

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFB83270.toInt())
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 6, 0, 6)
        parent.addView(button, params)
    }

    private fun match() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
}
