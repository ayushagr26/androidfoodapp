package com.example.khazana.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khazana.R
import com.example.khazana.adapter.OrderHistoryRecyclerAdapter
import com.example.khazana.model.OrderDetails
import com.example.khazana.utility.ConnectionManager
import org.json.JSONException

class OrderHistoryFragment : Fragment() {

    lateinit var orderHistoryAdapter: OrderHistoryRecyclerAdapter
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var recyclerOrderHistory: RecyclerView
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var layoutManager: RecyclerView.LayoutManager
    val orderHistoryList = arrayListOf<OrderDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.login_preference_file_name),
            Context.MODE_PRIVATE
        )
        val rlEmpty: RelativeLayout = view.findViewById(R.id.rlOrdHisEmpty)
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        val userId = sharedPreferences.getString("user_id", "user_id")
        layoutManager = LinearLayoutManager(activity as Context)
        progressLayout.visibility = View.VISIBLE
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null, Response.Listener {
                    try {
                        progressLayout.visibility = View.GONE
                        val data1 = it.getJSONObject("data")
                        val success = data1.getBoolean("success")
                        if (success) {
                            val data = data1.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val orderJsonObject = data.getJSONObject(i)
                                val foodItems = orderJsonObject.getJSONArray("food_items")
                                val orderDetails = OrderDetails(
                                    orderJsonObject.getString("order_id").toInt(),
                                    orderJsonObject.getString("restaurant_name"),
                                    orderJsonObject.getString("order_placed_at"),
                                    foodItems
                                )
                                orderHistoryList.add(orderDetails)
                                if (orderHistoryList.isEmpty()) {
                                    rlEmpty.visibility = View.VISIBLE
                                } else {
                                    rlEmpty.visibility = View.GONE
                                    if (activity != null) {
                                        orderHistoryAdapter = OrderHistoryRecyclerAdapter(
                                            activity as Context,
                                            orderHistoryList
                                        )
                                        val mLayoutManager =
                                            LinearLayoutManager(activity as Context)
                                        recyclerOrderHistory.layoutManager = mLayoutManager
                                        recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                        recyclerOrderHistory.adapter = orderHistoryAdapter
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "a959329b58247a"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
            if (orderHistoryList.isEmpty()) {
                rlEmpty.visibility = View.VISIBLE
            }
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Not found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }
}