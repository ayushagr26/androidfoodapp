package com.example.khazana.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khazana.R
import com.example.khazana.adapter.CartOrderRecyclerAdapter
import com.example.khazana.database.CartDatabase
import com.example.khazana.database.CartEntity
import com.example.khazana.model.FoodItemModel
import com.example.khazana.utility.ConnectionManager
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerCart: RecyclerView
    private lateinit var cartRecyclerAdapter: CartOrderRecyclerAdapter
    private lateinit var txtNameOfRestaurant: TextView
    lateinit var progressLayout: RelativeLayout
    private lateinit var btnPlaceOrder: Button
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val orderList = arrayListOf<FoodItemModel>()
    private lateinit var sharedPreferences: SharedPreferences
    private var restaurantName: String? = null
    var restaurantId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_order)

        recyclerCart = findViewById(R.id.recyclerCartOrder)
        txtNameOfRestaurant = findViewById(R.id.txtOrderResName)
        progressLayout = findViewById(R.id.progressLayout)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        layoutManager = LinearLayoutManager(this@CartActivity)
        toolbar = findViewById(R.id.cartOrderToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"

        sharedPreferences =
            getSharedPreferences(
                getString(R.string.login_preference_file_name),
                Context.MODE_PRIVATE
            )

        progressLayout.visibility = View.GONE

        if (intent != null) {
            val bundle = intent.getBundleExtra("data")
            restaurantName = bundle?.getString("restaurant_name")
            restaurantId = bundle?.getString("restaurant_id")
        } else {
            finish()
            Toast.makeText(
                this@CartActivity,
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (restaurantId == null || restaurantName == null) {
            finish()
            Toast.makeText(
                this@CartActivity,
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        txtNameOfRestaurant.text = restaurantName
        val dbList = GetOrders(this@CartActivity, restaurantId.toString()).execute().get()
        for (data in dbList) {
            orderList.addAll(
                Gson().fromJson(data.foodItems, Array<FoodItemModel>::class.java).asList()
            )
        }
        cartRecyclerAdapter = CartOrderRecyclerAdapter(this@CartActivity, orderList)
        recyclerCart.adapter = cartRecyclerAdapter
        recyclerCart.layoutManager = layoutManager
        var totalCost = 0
        for (i in 0 until orderList.size) {
            totalCost += orderList[i].itemCost
        }
        val totalCostString = "Place Order(Total: Rs. $totalCost )"
        btnPlaceOrder.text = totalCostString
        btnPlaceOrder.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            sendRequest(totalCost)
        }
    }

    private fun sendRequest(totalCost: Int) {
        val queue = Volley.newRequestQueue(this@CartActivity)
        val url = " http://13.235.250.119/v2/place_order/fetch_result/"
        val jsonObject = JSONObject()
        jsonObject.put("user_id", sharedPreferences.getString("user_id", null))
        jsonObject.put("restaurant_id", restaurantId)
        jsonObject.put("total_cost", totalCost.toString())
        val food = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].itemId)
            food.put(i, foodId)
        }
        jsonObject.put("food", food)
        if (ConnectionManager().checkConnectivity(this@CartActivity)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, url, jsonObject, Response.Listener {
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            ClearCart(this@CartActivity, restaurantId.toString()).execute().get()
                            val dialog = Dialog(
                                this@CartActivity,
                                android.R.style.Theme_Black_NoTitleBar_Fullscreen
                            )
                            dialog.setContentView(R.layout.order_placed_dialog)
                            dialog.show()
                            dialog.setCancelable(false)
                            val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                            btnOk.setOnClickListener {
                                dialog.dismiss()
                                startActivity(Intent(this@CartActivity, MainActivity::class.java))
                                finishAffinity(this@CartActivity)
                            }
                        } else {
                            Toast.makeText(
                                this@CartActivity,
                                "Some unexpected error occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@CartActivity,
                            "Some unexpected error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(
                        this@CartActivity,
                        "Some unexpected error occurred!",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "a959329b58247a"
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(this@CartActivity, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    class GetOrders(val context: Context, val restaurantId: String) :
        AsyncTask<Void, Void, List<CartEntity>>() {
        val db = Room.databaseBuilder(context, CartDatabase::class.java, "cartOrder-db")
            .fallbackToDestructiveMigration().build()

        override fun doInBackground(vararg params: Void?): List<CartEntity> {
            val x = db.cartDao().getAllOrders()
            db.close()
            return x
        }
    }

    class ClearCart(val context: Context, val restaurantId: String) :
        AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, CartDatabase::class.java, "cartOrder-db")
            .fallbackToDestructiveMigration().build()

        override fun doInBackground(vararg params: Void?): Boolean {
            db.cartDao().clearCart()
            db.close()
            return true
        }
    }

    override fun onBackPressed() {
        ClearCart(this@CartActivity, restaurantId.toString()).execute().get()
        super.onBackPressed()
    }
}