package com.marine.assistant
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class OrdersActivity : Activity() {
 data class Order(val id:Long,val customer:String,val phone:String,val item:String,val amount:Double,val status:String)
 private val orders=mutableListOf<Order>()
 private lateinit var list:LinearLayout
 private lateinit var summary:TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);load()
  val root=LinearLayout(this).apply{orientation=1;setPadding(20,20,20,20);setBackgroundColor(0xFF17131A.toInt())}
  root.addView(TextView(this).apply{text="طلبات معمل مارين";textSize=23f;gravity=Gravity.CENTER;setTextColor(0xFFFF6FAE.toInt())},lp())
  summary=TextView(this).apply{setTextColor(-1);setPadding(0,12,0,12)};root.addView(summary,lp())
  button(root,"إضافة طلب جديد"){edit(null)};button(root,"رجوع"){finish()}
  val scroll=ScrollView(this);list=LinearLayout(this).apply{orientation=1};scroll.addView(list);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));setContentView(root);render()
 }
 private fun render(){list.removeAllViews();summary.text="عدد الطلبات: "+orders.size
  if(orders.isEmpty()){list.addView(TextView(this).apply{text="لا توجد طلبات مسجلة بعد";setTextColor(-1);gravity=Gravity.CENTER;setPadding(24,24,24,24)},lp());return}
  orders.sortedByDescending{it.id}.forEach{o->
   val card=LinearLayout(this).apply{orientation=1;setPadding(12,10,12,10);setBackgroundColor(0xFF29222D.toInt())}
   card.addView(TextView(this).apply{text=o.item+"\nالعميل: "+o.customer+"\nالهاتف: "+o.phone+"\nالقيمة: "+o.amount+"\nالحالة: "+o.status;setTextColor(-1);textSize=15f},lp())
   val row=LinearLayout(this).apply{orientation=0}
   row.addView(Button(this).apply{text="تعديل";setOnClickListener{edit(o)}},LinearLayout.LayoutParams(0,-2,1f))
   row.addView(Button(this).apply{text="تم التسليم";setOnClickListener{replace(o.copy(status="تم التسليم"))}},LinearLayout.LayoutParams(0,-2,1f))
   row.addView(Button(this).apply{text="حذف";setOnClickListener{AlertDialog.Builder(this@OrdersActivity).setMessage("حذف الطلب؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف"){_,_->orders.removeAll{it.id==o.id};save();render()}.show()}},LinearLayout.LayoutParams(0,-2,1f))
   card.addView(row,lp());val p=LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,10);list.addView(card,p)
  }
 }
 private fun edit(old:Order?){
  val box=LinearLayout(this).apply{orientation=1;setPadding(18,4,18,0)}
  fun field(h:String,v:String="")=EditText(this).apply{hint=h;setText(v);box.addView(this,lp())}
  val customer=field("اسم العميل",old?.customer.orEmpty());val phone=field("رقم الهاتف",old?.phone.orEmpty());val item=field("تفاصيل الطلب",old?.item.orEmpty());val amount=field("المبلغ بالأرقام",old?.amount?.toString().orEmpty())
  val choices=listOf("جديد","قيد التنفيذ","جاهز","تم التسليم","ملغي")
  val status=Spinner(this).apply{adapter=ArrayAdapter(this@OrdersActivity,android.R.layout.simple_spinner_dropdown_item,choices);setSelection(choices.indexOf(old?.status?:"جديد").coerceAtLeast(0))};box.addView(status,lp())
  AlertDialog.Builder(this).setTitle(if(old==null)"طلب جديد" else "تعديل الطلب").setView(box).setNegativeButton("إلغاء",null).setPositiveButton("حفظ"){_,_->
   val name=customer.text.toString().trim();val detail=item.text.toString().trim();val value=amount.text.toString().toDoubleOrNull()
   if(name.isBlank()||detail.isBlank()||value==null||value<0){Toast.makeText(this,"أدخل الاسم والتفاصيل ومبلغاً صحيحاً",Toast.LENGTH_LONG).show();return@setPositiveButton}
   val o=Order(old?.id?:System.currentTimeMillis(),name,phone.text.toString().trim(),detail,value,status.selectedItem.toString())
   if(old==null)orders.add(o)else{val i=orders.indexOfFirst{it.id==old.id};if(i>=0)orders[i]=o};save();render()
  }.show()
 }
 private fun replace(o:Order){val i=orders.indexOfFirst{it.id==o.id};if(i>=0)orders[i]=o;save();render()}
 private fun load(){try{val a=JSONArray(getSharedPreferences("marine_orders",0).getString("orders","[]"));for(i in 0 until a.length()){val x=a.getJSONObject(i);orders.add(Order(x.optLong("id"),x.optString("customer"),x.optString("phone"),x.optString("item"),x.optDouble("amount"),x.optString("status","جديد")))}}catch(_:Exception){}}
 private fun save(){val a=JSONArray();orders.forEach{o->a.put(JSONObject().put("id",o.id).put("customer",o.customer).put("phone",o.phone).put("item",o.item).put("amount",o.amount).put("status",o.status))};getSharedPreferences("marine_orders",0).edit().putString("orders",a.toString()).apply()}
 private fun button(root:LinearLayout,label:String,act:()->Unit){root.addView(Button(this).apply{text=label;setOnClickListener{act()}},lp())}
 private fun lp()=LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT)
}