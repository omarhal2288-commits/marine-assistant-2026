package com.marine.assistant
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
class AccountsStatusActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=1;setPadding(22,22,22,22);setBackgroundColor(0xFF17131A.toInt())}
  root.addView(TextView(this).apply{text="متابعة حسابات معمل مارين";textSize=22f;gravity=Gravity.CENTER;setTextColor(0xFFFF6FAE.toInt())})
  root.addView(TextView(this).apply{text="واتساب أعمال: بانتظار إضافة الرقم من صاحب التحكم الكامل في Meta\nFacebook وInstagram: ظهر اسم معمل مارين في الربط داخل واتساب؛ يلزم تأكيد حالة الأصول من Meta\nTikTok وSnapchat وChatGPT: فتح التطبيقات متاح، ولا يوجد ربط إداري مؤكد من داخل هذا التطبيق.\n\nهذه شاشة متابعة يدوية فقط؛ لا تتصل بحساباتك ولا تغيّر إعداداتها ولا تدّعي أن الربط تم.";textSize=16f;setTextColor(-1);setPadding(4,24,4,24)})
  root.addView(Button(this).apply{text="رجوع";setOnClickListener{finish()}});setContentView(root)
 }
}