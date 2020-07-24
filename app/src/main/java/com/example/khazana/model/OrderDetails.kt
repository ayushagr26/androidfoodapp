package com.example.khazana.model

import org.json.JSONArray

data class OrderDetails(
    val orderId: Int,
    val restaurantName: String,
    val orderDate: String,
    val foodItems: JSONArray
)