package com.example.khazana.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.khazana.R
import com.example.khazana.model.CartDetails

class RestaurantDetailsRecyclerAdapter(
    val context: Context,
    private val itemList: ArrayList<CartDetails>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RestaurantDetailsRecyclerAdapter.RestaurantDetailsViewHolder>() {
    companion object {
        var isCartEmpty = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_restaurant_details_single_row, parent, false)
        return RestaurantDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemClickListener {
        fun onAddItemClick(foodItem: CartDetails)
        fun onRemoveItemClick(foodItem: CartDetails)
    }

    override fun onBindViewHolder(holder: RestaurantDetailsViewHolder, position: Int) {
        val menuObject = itemList[position]
        holder.foodItemName.text = menuObject.itemName
        val cost = "Rs. ${menuObject.itemCost}"
        holder.foodItemCost.text = cost
        holder.sno.text = (position + 1).toString()
        holder.addToCart.setOnClickListener {
            holder.addToCart.visibility = View.GONE
            holder.removeFromCart.visibility = View.VISIBLE
            listener.onAddItemClick(menuObject)
        }

        holder.removeFromCart.setOnClickListener {
            holder.removeFromCart.visibility = View.GONE
            holder.addToCart.visibility = View.VISIBLE
            listener.onRemoveItemClick(menuObject)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class RestaurantDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodItemName: TextView = view.findViewById(R.id.txtItemName)
        val foodItemCost: TextView = view.findViewById(R.id.txtResDetailPrice)
        val sno: TextView = view.findViewById(R.id.txtResDetailsItemNumber)
        val addToCart: Button = view.findViewById(R.id.btnAddToFav)
        val removeFromCart: Button = view.findViewById(R.id.btnRemoveFromFav)
    }
}
