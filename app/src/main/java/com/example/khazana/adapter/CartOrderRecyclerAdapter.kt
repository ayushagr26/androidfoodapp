package com.example.khazana.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.khazana.R
import com.example.khazana.model.FoodItemModel

class CartOrderRecyclerAdapter(val context: Context, private val itemList: List<FoodItemModel>) :
    RecyclerView.Adapter<CartOrderRecyclerAdapter.CartOrderViewHolder>() {

    class CartOrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.txtItemName)
        val itemCost: TextView = view.findViewById(R.id.txtItemCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_cart_order_single_row, parent, false)
        return CartOrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CartOrderViewHolder, position: Int) {
        val itemOrder = itemList[position]
        holder.itemName.text = itemOrder.itemName
        val x = "Rs." + itemOrder.itemCost
        holder.itemCost.text = x
    }
}