package com.marine.assistant

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

private class MarinDb(context: android.content.Context) : SQLiteOpenHelper(context, "marin_lab.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT NOT NULL, notes TEXT)")
        db.execSQL("CREATE TABLE orders(id INTEGER PRIMARY KEY AUTOINCREMENT, customer TEXT NOT NULL, details TEXT NOT NULL, amount REAL NOT NULL, status TEXT NOT NULL, paid_amount REAL NOT NULL DEFAULT 0, created_at TEXT DEFAULT CURRENT_TIMESTAMP)")
        db.execSQL("CREATE TABLE entries(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, kind TEXT NOT NULL, amount REAL NOT NULL, notes TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE orders ADD COLUMN paid_amount REAL NOT NULL DEFAULT 0")
    }
    fun add(table: String, values: ContentValues) = writableDatabase.insert(table, null, values)
    fun rows(sql: String): List<List<String>> {
        val out = mutableListOf<List<String>>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                val row = mutableListOf<String>()
                for (i in 0 until c.columnCount) row.add(c.getString(i) ?: "")
                out.add(row)
            }
        }
        return out
    }
}

class MainActivity : Activity() {
    private lateinit var db: MarinDb
    private lateinit var body: LinearLayout
    private val ink = 0xFF241B24.toInt()
    private val pink = 0xFFB83270.toInt()
    private val pale = 0xFFFFE5F0.toInt()
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
        db = MarinDb(this)
        showHome()
    }

    private fun base(titleText: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xFFF8F4F7.toInt())
        }
        val header = TextView(this).apply {
            text = "MARIN LAB  •  $titleText"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(pink)
            setPadding(4, 12, 4, 18)
        }
        root.addView(header, match())
        val scroll = ScrollView(this)
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("الرئيسية", "العملاء", "الطلبات", "الحسابات").forEach { label ->
            val b = Button(this).apply { text = label; textSize = 11f; setOnClickListener {
                when(label) { "الرئيسية" -> showHome(); "العملاء" -> showCustomers(); "الطلبات" -> showOrders(); else -> showAccounts() }
            } }
            nav.addView(b, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun showHome() {
        base("مساعد مارين الذكي")
        addText(body, "نظام إدارة المعمل", 20f, pink)
        addText(body, "سجّل العملاء والطلبات والحسابات هنا. تُحفظ السجلات محلياً على هذا الهاتف.", 14f, ink)
        val counts = db.rows("SELECT (SELECT COUNT(*) FROM customers),(SELECT COUNT(*) FROM orders),(SELECT COUNT(*) FROM entries)").firstOrNull()
        addText(body, "العملاء: ${counts?.get(0) ?: "0"}     الطلبات: ${counts?.get(1) ?: "0"}     القيود المالية: ${counts?.get(2) ?: "0"}", 16f, ink)
        sectionButton(body, "إدارة العملاء") { showCustomers() }
        sectionButton(body, "إدارة الطلبات") { showOrders() }
        sectionButton(body, "الحسابات والإيرادات والمصروفات") { showAccounts() }
        sectionButton(body, "تصدير نسخة احتياطية من البيانات") { exportBackup() }
        addText(body, "التطبيقات المرتبطة", 18f, pink)
        apps.forEach { (label, pkg) -> sectionButton(body, "فتح $label") { openApp(label, pkg) } }
        addText(body, "ملاحظة: أزرار المنصات تفتح تطبيقاتها الرسمية فقط. لا يوجد حالياً صندوق رسائل موحد أو نشر تلقائي؛ ذلك يتطلب موافقات وواجهات API رسمية من كل منصة.", 12f, ink)
    }

    private fun showCustomers() {
        base("العملاء")
        sectionButton(body, "+ إضافة عميل") { customerDialog() }
        val rows = db.rows("SELECT id,name,phone,notes FROM customers ORDER BY id DESC")
        if (rows.isEmpty()) addText(body, "لا يوجد عملاء مسجلون بعد.", 15f, ink)
        rows.forEach { r -> card(body, "عميل: ${r[1]}", "الهاتف: ${r[2]}\nملاحظات: ${r[3]}") }
    }

    private fun customerDialog() {
        val name = field("اسم العميل")
        val phone = field("رقم الهاتف")
        val notes = field("ملاحظات (اختياري)")
        val box = dialogBox(name, phone, notes)
        AlertDialog.Builder(this).setTitle("إضافة عميل").setView(box)
            .setNegativeButton("إلغاء", null).setPositiveButton("حفظ") { _, _ ->
                if (name.text.toString().trim().isNotEmpty() && phone.text.toString().trim().isNotEmpty()) {
                    val v = ContentValues().apply { put("name", name.text.toString().trim()); put("phone", phone.text.toString().trim()); put("notes", notes.text.toString()) }
                    db.add("customers", v); showCustomers()
                } else toast("الاسم ورقم الهاتف مطلوبان")
            }.show()
    }

    private fun showOrders() {
        base("الطلبات")
        sectionButton(body, "+ تسجيل طلب") { orderDialog() }
        val rows = db.rows("SELECT id,customer,details,amount,status FROM orders ORDER BY id DESC")
        if (rows.isEmpty()) addText(body, "لا توجد طلبات مسجلة بعد.", 15f, ink)
        rows.forEach { r -> card(body, "طلب #${r[0]} — ${r[1]}", "التفاصيل: ${r[2]}\nالمبلغ: ${r[3]}\nالحالة: ${r[4]}") }
    }

    private fun orderDialog() {
        val customer = field("اسم العميل")
        val details = field("تفاصيل الطلب")
        val amount = field("المبلغ")
        val status = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("جديد", "قيد التنفيذ", "جاهز", "تم التسليم", "ملغي")) }
        val box = dialogBox(customer, details, amount, status)
        AlertDialog.Builder(this).setTitle("تسجيل طلب").setView(box)
            .setNegativeButton("إلغاء", null).setPositiveButton("حفظ") { _, _ ->
                val n = amount.text.toString().toDoubleOrNull()
                if (customer.text.toString().trim().isNotEmpty() && details.text.toString().trim().isNotEmpty() && n != null && n >= 0) {
                    val v = ContentValues().apply { put("customer", customer.text.toString()); put("details", details.text.toString()); put("amount", n); put("status", status.selectedItem.toString()) }
                    db.add("orders", v); showOrders()
                } else toast("أدخل العميل والتفاصيل ومبلغاً صحيحاً")
            }.show()
    }

    private fun showAccounts() {
        base("الحسابات")
        val totals = db.rows("SELECT COALESCE(SUM(CASE WHEN kind='إيراد' THEN amount ELSE 0 END),0),COALESCE(SUM(CASE WHEN kind='مصروف' THEN amount ELSE 0 END),0) FROM entries").first()
        val income = totals[0].toDoubleOrNull() ?: 0.0
        val expense = totals[1].toDoubleOrNull() ?: 0.0
        addText(body, "إجمالي الإيرادات: %.2f".format(income), 17f, pink)
        addText(body, "إجمالي المصروفات: %.2f".format(expense), 17f, ink)
        addText(body, "الرصيد: %.2f".format(income-expense), 19f, pink)
        sectionButton(body, "+ إضافة إيراد أو مصروف") { entryDialog() }
        val rows = db.rows("SELECT id,title,kind,amount,notes FROM entries ORDER BY id DESC")
        if (rows.isEmpty()) addText(body, "لا توجد قيود مالية بعد.", 15f, ink)
        rows.forEach { r -> card(body, "${r[2]}: ${r[1]}", "المبلغ: ${r[3]}\nملاحظات: ${r[4]}") }
        addText(body, "هذه سجلات محلية وليست مرتبطة بالبنك أو بمدفوعات واتساب.", 12f, ink)
    }

    private fun entryDialog() {
        val title = field("البيان")
        val amount = field("المبلغ")
        val notes = field("ملاحظات (اختياري)")
        val kind = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("إيراد", "مصروف")) }
        val box = dialogBox(title, amount, kind, notes)
        AlertDialog.Builder(this).setTitle("قيد مالي جديد").setView(box)
            .setNegativeButton("إلغاء", null).setPositiveButton("حفظ") { _, _ ->
                val n = amount.text.toString().toDoubleOrNull()
                if (title.text.toString().trim().isNotEmpty() && n != null && n >= 0) {
                    val v = ContentValues().apply { put("title", title.text.toString()); put("kind", kind.selectedItem.toString()); put("amount", n); put("notes", notes.text.toString()) }
                    db.add("entries", v); showAccounts()
                } else toast("أدخل البيان ومبلغاً صحيحاً")
            }.show()
    }

    private fun field(hintText: String) = EditText(this).apply { hint = hintText; textSize = 15f; singleLine = hintText != "ملاحظات (اختياري)" }
    private fun dialogBox(vararg views: View) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(22, 8, 22, 4)
        views.forEach { addView(it, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)) }
    }
    private fun sectionButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val b = Button(this).apply { text = label; setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(pink); setOnClickListener { action() } }
        val p = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(0, 5, 0, 5); parent.addView(b, p)
    }
    private fun actionCard(parent: LinearLayout, heading: String, detail: String): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(14, 12, 14, 12); setBackgroundColor(pale) }
        addText(box, heading, 16f, pink)
        addText(box, detail + "\\n(اضغط مطولاً للحذف)", 13f, ink)
        val p = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
        p.setMargins(0, 6, 0, 6)
        parent.addView(box, p)
        return box
    }
    private fun confirmDelete(title: String, table: String, id: String) {
        AlertDialog.Builder(this).setTitle(title)
            .setMessage("لا يمكن التراجع عن الحذف. هل تريد المتابعة؟")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف") { _, _ ->
                val allowed = setOf("customers", "orders", "entries")
                if (table in allowed) db.writableDatabase.delete(table, "id=?", arrayOf(id))
                when(table) { "customers" -> showCustomers(); "orders" -> showOrders(); else -> showAccounts() }
            }.show()
    }
    private fun card(parent: LinearLayout, heading: String, detail: String) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(14, 12, 14, 12); setBackgroundColor(pale) }
        addText(box, heading, 16f, pink); addText(box, detail, 13f, ink)
        val p = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(0, 6, 0, 6); parent.addView(box, p)
    }
    private fun addText(parent: LinearLayout, value: String, size: Float, color: Int) {
        val t = TextView(this).apply { text = value; textSize = size; setTextColor(color); setPadding(6, 8, 6, 8) }
        parent.addView(t, match())
    }
    private fun exportBackup() {
        val text = buildString {
            append("نسخة احتياطية — مساعد مارين الذكي\n\n")
            append("العملاء\n")
            db.rows("SELECT id,name,phone,notes FROM customers ORDER BY id").forEach { append(it.joinToString(" | ")).append("\n") }
            append("\nالطلبات\n")
            db.rows("SELECT id,customer,details,amount,status,created_at FROM orders ORDER BY id").forEach { append(it.joinToString(" | ")).append("\n") }
            append("\nالحسابات\n")
            db.rows("SELECT id,title,kind,amount,notes,created_at FROM entries ORDER BY id").forEach { append(it.joinToString(" | ")).append("\n") }
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية — معمل مارين")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, "حفظ أو إرسال النسخة الاحتياطية"))
    }

    private fun openApp(label: String, packageName: String) {
        val launch: Intent? = packageManager.getLaunchIntentForPackage(packageName)
        if (launch == null) toast("التطبيق غير مثبت أو لا يمكن فتحه: $label") else startActivity(launch)
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    private fun match() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
}
