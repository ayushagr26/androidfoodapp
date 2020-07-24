package com.example.khazana.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cartOrder")
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "restaurant_id") val restaurantId: Int?,
    @ColumnInfo(name = "food_items") val foodItems: String
)