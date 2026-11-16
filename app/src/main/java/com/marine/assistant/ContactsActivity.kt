package com.marine.assistant

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local customer register. Data stays in this app's private SharedPreferences;
 * it is not uploaded or synchronized with any service.
 */
class ContactsActivity : Activity() {
    private data class Customer(
        val id: Long,
        val name: String,
        val phone: String,
        val email: String,
        val notes: String
    )

    private val customers = mutableListOf<Customer>()
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private lateinit var count: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadCustomers()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 20)
            setBackgroundColor(0xFF17131A.toInt())
        }
        val heading = TextView(this).apply {
            text = "عملاء معمل مارين"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(0xFFFF6FAE.toInt())
            setPadding(0, 8, 0, 16)
        }
        root.addView(heading, match())

        count = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 4, 0, 10)
        }
        root.addView(count, match())

        search = EditText(this).apply {
            hint = "ابحث بالاسم أو رقم الهاتف"
            textSize = 15f
            setSingleLine(true)
        }
        root.addView(search, match())
        search.addTextChangedListener(SimpleTextWatcher { renderList() })

        addButton(root, "إضافة عميل جديد") { showCustomerDialog(null) }
        addButton(root, "العودة إلى المساعد") { finish() }

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        renderList()
    }

    private fun renderList() {
        list.removeAllViews()
        val term = search.text.toString().trim()
        val filtered = customers.filter {
            term.isBlank() || it.name.contains(term, true) || it.phone.contains(term, true)
        }
        count.text = "إجمالي العملاء: ${customers.size}  •  النتائج: ${filtered.size}"

        if (filtered.isEmpty()) {
            val empty = TextView(this).apply {
                text = if (customers.isEmpty()) "لا يوجد عملاء بعد. اضغط «إضافة عميل جديد» للبدء."
                else "لا توجد نتائج مطابقة."
                textSize = 15f
                setTextColor(0xFFFFD6E7.toInt())
                gravity = Gravity.CENTER
                setPadding(8, 28, 8, 28)
            }
            list.addView(empty, match())
            return
        }

        filtered.forEach { customer ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
                setBackgroundColor(0xFF29222D.toInt())
            }
            val details = buildString {
                append(customer.name)
                if (customer.phone.isNotBlank()) append("\nالهاتف: ${customer.phone}")
                if (customer.email.isNotBlank()) append("\nالبريد: ${customer.email}")
                if (customer.notes.isNotBlank()) append("\nملاحظات: ${customer.notes}")
            }
            val text = TextView(this).apply {
                this.text = details
                textSize = 15f
                setTextColor(0xFFFFFFFF.toInt())
            }
            card.addView(text, match())
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val edit = Button(this).apply {
                this.text = "تعديل"
                setOnClickListener { showCustomerDialog(customer) }
            }
            val delete = Button(this).apply {
                this.text = "حذف"
                setOnClickListener { confirmDelete(customer) }
            }
            actions.addView(edit, LinearLayout.LayoutParams(0, -2, 1f))
            actions.addView(delete, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(actions, match())
            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 12)
            list.addView(card, params)
        }
    }

    private fun showCustomerDialog(existing: Customer?) {
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 0)
        }
        fun field(hintText: String, value: String = ""): EditText {
            return EditText(this).apply {
                hint = hintText
                setText(value)
                textSize = 15f
            }.also { fields.addView(it, match()) }
        }
        val name = field("اسم العميل *", existing?.name.orEmpty())
        val phone = field("رقم الهاتف", existing?.phone.orEmpty())
        val email = field("البريد الإلكتروني (اختياري)", existing?.email.orEmpty())
        val notes = field("ملاحظات أو تفاصيل الطلب", existing?.notes.orEmpty())

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "إضافة عميل" else "تعديل بيانات العميل")
            .setView(fields)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ") { _, _ ->
                val cleanName = name.text.toString().trim()
                if (cleanName.isBlank()) {
                    Toast.makeText(this, "اكتب اسم العميل أولاً", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val item = Customer(
                    existing?.id ?: System.currentTimeMillis(),
                    cleanName,
                    phone.text.toString().trim(),
                    email.text.toString().trim(),
                    notes.text.toString().trim()
                )
                if (existing == null) customers.add(item)
                else {
                    val index = customers.indexOfFirst { it.id == existing.id }
                    if (index >= 0) customers[index] = item
                }
                saveCustomers()
                renderList()
                Toast.makeText(this, "تم حفظ بيانات العميل على هذا الهاتف", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun confirmDelete(customer: Customer) {
        AlertDialog.Builder(this)
            .setTitle("حذف العميل")
            .setMessage("هل تريد حذف بيانات ${customer.name} من هذا التطبيق؟")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف") { _, _ ->
                customers.removeAll { it.id == customer.id }
                saveCustomers()
                renderList()
            }
            .show()
    }

    private fun loadCustomers() {
        val raw = getSharedPreferences("marine_customers", MODE_PRIVATE)
            .getString("customers", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                customers.add(
                    Customer(
                        item.optLong("id"),
                        item.optString("name"),
                        item.optString("phone"),
                        item.optString("email"),
                        item.optString("notes")
                    )
                )
            }
        } catch (_: Exception) {
            customers.clear()
        }
    }

    private fun saveCustomers() {
        val array = JSONArray()
        customers.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("phone", it.phone)
                put("email", it.email)
                put("notes", it.notes)
            })
        }
        getSharedPreferences("marine_customers", MODE_PRIVATE)
            .edit().putString("customers", array.toString()).apply()
    }

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFB83270.toInt())
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 5, 0, 5)
        parent.addView(button, params)
    }

    private fun match() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)

    private class SimpleTextWatcher(private val changed: () -> Unit) :
        android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = changed()
        override fun afterTextChanged(s: android.text.Editable?) = Unit
    }
}
