package com.example.khazana.adapter

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.khazana.R
import com.example.khazana.model.FoodItemModel
import com.example.khazana.model.OrderDetails
import java.util.*

class OrderHistoryRecyclerAdapter(
    val context: Context,
    private val itemList: ArrayList<OrderDetails>
) :
    RecyclerView.Adapter<OrderHistoryRecyclerAdapter.OrderHistoryViewHolder>() {

    class OrderHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtResName: TextView = view.findViewById(R.id.txtOrdHisResName)
        val txtDate: TextView = view.findViewById(R.id.txtOrdHisDate)
        val recyclerResHistory: RecyclerView = view.findViewById(R.id.recyclerOrdHis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.recycler_order_history_single_row, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val order = itemList[position]
        holder.txtResName.text = order.restaurantName
        holder.txtDate.text = formatDate(order.orderDate)
        setUpRecycler(holder.recyclerResHistory, order)
    }

    private fun setUpRecycler(recyclerResHistory: RecyclerView, orderHistoryList: OrderDetails) {
        val foodItemsList = ArrayList<FoodItemModel>()
        for (i in 0 until orderHistoryList.foodItems.length()) {
            val foodJson = orderHistoryList.foodItems.getJSONObject(i)
            foodItemsList.add(
                FoodItemModel(
                    foodJson.getString("food_item_id"),
                    foodJson.getString("name"),
                    foodJson.getString("cost").toInt()
                )
            )
        }
        val mLayoutManager = LinearLayoutManager(context)
        recyclerResHistory.layoutManager = mLayoutManager
        recyclerResHistory.itemAnimator = DefaultItemAnimator()
        recyclerResHistory.adapter = CartOrderRecyclerAdapter(
            context,
            foodItemsList
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun formatDate(dateString: String): String? {
        val inputFormatter = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.ENGLISH)
        val date: Date = inputFormatter.parse(dateString) as Date

        val outputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return outputFormatter.format(date)
    }

}