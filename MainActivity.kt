package com.marine.assistant

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
    private lateinit var status: TextView
    private lateinit var command: EditText

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
        status = TextView(this).apply {
            text = "اختر تطبيقاً لفتحه، أو فعّل خدمة الوصول لاستخدام أدوات المساعدة."
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }
        content.addView(status, match())

        apps.forEach { (label, pkg) ->
            addButton(content, "فتح $label") { openApp(label, pkg) }
        }
        addButton(content, "تفعيل خدمة المساعدة") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        addButton(content, "قراءة النص الظاهر") {
            val text = MarineAccessibilityService.latestScreenText
            status.text = if (text.isBlank()) "لا يوجد نص مقروء بعد. افتح تطبيقاً ثم جرّب مرة أخرى."
                         else text.take(3500)
        }
        addButton(content, "رجوع") { confirmAndRun("الرجوع إلى الشاشة السابقة؟") { MarineAccessibilityService.instance?.goBack() } }
        addButton(content, "الرئيسية") { confirmAndRun("العودة إلى الشاشة الرئيسية؟") { MarineAccessibilityService.instance?.goHome() } }

        command = EditText(this).apply {
            hint = "اكتب نصاً للكتابة داخل الحقل المحدد"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFFBBAFBA.toInt())
        }
        content.addView(command, match())
        addButton(content, "كتابة النص بعد التأكيد") {
            val value = command.text.toString()
            if (value.isBlank()) {
                Toast.makeText(this, "اكتب النص أولاً", Toast.LENGTH_SHORT).show()
            } else {
                confirmAndRun("هل تسمح بكتابة النص التالي في الحقل المحدد؟\n\n$value") {
                    val ok = MarineAccessibilityService.instance?.typeIntoFocusedField(value) ?: false
                    Toast.makeText(this, if (ok) "تم تنفيذ الكتابة" else "لم أجد حقلاً قابلاً للكتابة", Toast.LENGTH_LONG).show()
                }
            }
        }
        addButton(content, "النقر على عنصر باسمه بعد التأكيد") {
            val value = command.text.toString()
            if (value.isBlank()) {
                Toast.makeText(this, "اكتب اسم العنصر أولاً", Toast.LENGTH_SHORT).show()
            } else {
                confirmAndRun("هل تريد محاولة النقر على العنصر الذي يحمل النص التالي؟\n\n$value") {
                    val ok = MarineAccessibilityService.instance?.clickByText(value) ?: false
                    Toast.makeText(this, if (ok) "تم تنفيذ النقر" else "لم يتم العثور على عنصر قابل للنقر", Toast.LENGTH_LONG).show()
                }
            }
        }
        addButton(content, "تحديث حالة خدمة المساعدة") {
            status.text = if (MarineAccessibilityService.instance == null)
                "خدمة المساعدة غير مفعّلة. فعّلها من زر الإعدادات."
            else "خدمة المساعدة مفعّلة."
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

    private fun confirmAndRun(message: String, action: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("تأكيد")
            .setMessage(message)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("موافق") { _, _ -> action() }
            .show()
    }

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFB83270.toInt())
            setOnClickListener { action() }
        }
        val lp = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 5, 0, 5)
        parent.addView(button, lp)
    }

    private fun match() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
}
