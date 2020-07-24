package com.example.khazana.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CartDao {

    @Insert
    fun insertOrder(cartEntity: CartEntity)

    @Delete
    fun deleteOrder(cartEntity: CartEntity)

    @Query("SELECT* FROM cartOrder")
    fun getAllOrders(): List<CartEntity>

    @Query("DELETE FROM cartOrder")
    fun clearCart()
}