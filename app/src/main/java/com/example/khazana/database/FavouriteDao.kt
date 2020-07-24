package com.example.khazana.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavouriteDao {

    @Insert
    fun insertFavRestaurant(favouriteEntity: FavouriteEntity)

    @Delete
    fun deleteFavRestaurant(favouriteEntity: FavouriteEntity)

    @Query("SELECT* FROM fav_restaurants")
    fun getAllFavRestaurants(): List<FavouriteEntity>

    @Query("SELECT* FROM fav_restaurants WHERE restaurant_id=:favRestaurantId")
    fun getFavRestaurantById(favRestaurantId: String): FavouriteEntity
}