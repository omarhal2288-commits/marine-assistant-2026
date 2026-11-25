package com.marine.assistant

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import java.text.NumberFormat
import java.util.Locale

class SalesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            setBackgroundColor(0xFF17131A.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(TextView(this).apply {
            text = "لوحة المبيعات"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(0xFFFF6FAE.toInt())
            setPadding(0, 10, 0, 20)
        }, match())

        val orders = readOrders()
        var allOrders = 0
        var delivered = 0
        var active = 0
        var cancelled = 0
        var deliveredTotal = 0.0
        var activeTotal = 0.0

        for (i in 0 until orders.length()) {
            val order = orders.optJSONObject(i) ?: continue
            allOrders++
            val amount = order.optDouble("amount", 0.0).coerceAtLeast(0.0)
            when (order.optString("status", "جديد")) {
                "تم التسليم" -> {
                    delivered++
                    deliveredTotal += amount
                }
                "ملغي" -> cancelled++
                else -> {
                    active++
                    activeTotal += amount
                }
            }
        }

        addMetric(content, "إجمالي الطلبات المسجلة", allOrders.toString())
        addMetric(content, "طلبات تم تسليمها", delivered.toString())
        addMetric(content, "طلبات قيد المتابعة", active.toString())
        addMetric(content, "طلبات ملغاة", cancelled.toString())
        addMetric(content, "قيمة الطلبات المسلّمة", formatAmount(deliveredTotal))
        addMetric(content, "قيمة الطلبات غير المسلّمة", formatAmount(activeTotal))

        content.addView(TextView(this).apply {
            text = "يُحتسب إجمالي المبيعات من الطلبات التي حالتها «تم التسليم» فقط. الطلبات الملغاة لا تدخل في قيمة المبيعات. هذه أرقام محلية محفوظة داخل التطبيق وليست مرتبطة بحساب بنكي أو منصة دفع."
            textSize = 14f
            setTextColor(0xFFFFD6E7.toInt())
            setPadding(4, 18, 4, 20)
        }, match())

        content.addView(Button(this).apply {
            text = "تحديث الملخص"
            setOnClickListener { recreate() }
        }, match())
        content.addView(Button(this).apply {
            text = "رجوع"
            setOnClickListener { finish() }
        }, match())

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun readOrders(): JSONArray {
        val raw = getSharedPreferences("marine_orders", MODE_PRIVATE)
            .getString("orders", "[]") ?: "[]"
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun addMetric(parent: LinearLayout, label: String, value: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 12)
            setBackgroundColor(0xFF29222D.toInt())
        }
        card.addView(TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(0xFFFFB8D4.toInt())
        }, match())
        card.addView(TextView(this).apply {
            text = value
            textSize = 21f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 4, 0, 0)
        }, match())
        val params = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, 10)
        parent.addView(card, params)
    }

    private fun formatAmount(amount: Double): String =
        NumberFormat.getNumberInstance(Locale("ar")).format(amount)

    private fun match() = LinearLayout.LayoutParams(
        -1,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
