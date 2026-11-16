package com.marine.assistant
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
class SalesActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=1;setPadding(24,24,24,24);setBackgroundColor(0xFF17131A.toInt())}
  root.addView(TextView(this).apply{text="ملخص المبيعات";textSize=24f;gravity=Gravity.CENTER;setTextColor(0xFFFF6FAE.toInt())})
  val data=try{JSONArray(getSharedPreferences("marine_orders",0).getString("orders","[]"))}catch(_:Exception){JSONArray()}
  var count=0;var total=0.0;var pending=0
  for(i in 0 until data.length()){val o=data.getJSONObject(i);count++;if(o.optString("status")=="تم التسليم")total+=o.optDouble("amount")else pending++}
  root.addView(TextView(this).apply{text="إجمالي الطلبات: "+count+"\nطلبات لم تُسلّم: "+pending+"\nقيمة الطلبات المسلّمة: "+total+"\n\nملخص محلي من الطلبات المسجلة.";textSize=18f;setTextColor(-1);setPadding(8,30,8,30)})
  root.addView(Button(this).apply{text="رجوع";setOnClickListener{finish()}});setContentView(root)
 }
}