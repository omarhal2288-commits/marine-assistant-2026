package com.marine.assistant

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AccountsStatusActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            setBackgroundColor(0xFF17131A.toInt())
        }
        root.addView(TextView(this).apply {
            text = "متابعة حسابات معمل مارين"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(0xFFFF6FAE.toInt())
        })

        root.addView(TextView(this).apply {
            text = "واتساب أعمال وFacebook وInstagram وTikTok وSnapchat وChatGPT\n\nهذه الشاشة تذكير توضيحي فقط، وليست فحصاً مباشراً للحسابات. فتح التطبيق من هنا لا يعني ربطه إدارياً.\n\nلتأكيد أي ربط، افتح إعدادات الحساب أو لوحة الأعمال الرسمية وتحقق من ظهور الحساب والصلاحيات هناك. لا تشارك كلمة المرور أو رمز التحقق مع أي شخص.\n\nحالة الربط الفعلية غير مؤكدة من داخل مساعد مارين حالياً؛ لن يعرض التطبيق أي حساب على أنه مرتبط دون تحقق رسمي."
            textSize = 16f
            setTextColor(-1)
            setPadding(4, 24, 4, 24)
        })

        root.addView(Button(this).apply {
            text = "رجوع"
            setOnClickListener { finish() }
        })
        setContentView(root)
    }
}
