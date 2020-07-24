package com.example.khazana.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavouriteEntity::class], version = 2)
abstract class FavouriteDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
}