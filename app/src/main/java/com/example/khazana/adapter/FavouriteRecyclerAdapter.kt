package com.example.khazana.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.khazana.R
import com.example.khazana.database.FavouriteDatabase
import com.example.khazana.database.FavouriteEntity
import com.example.khazana.fragments.FavouriteRestaurantsFragment
import com.example.khazana.fragments.RestaurantDetailsFragment
import com.squareup.picasso.Picasso

class FavouriteRecyclerAdapter(val context: Context, private val itemList: List<FavouriteEntity>) :
    RecyclerView.Adapter<FavouriteRecyclerAdapter.FavouriteViewHolder>() {

    class FavouriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFavButton = view.findViewById(R.id.imgFavIsFav) as ImageView
        val txtRestaurantName: TextView = view.findViewById(R.id.txtFavRestaurantName)
        val txtRestaurantPrice: TextView = view.findViewById(R.id.txtFavRestaurantPrice)
        val txtRestaurantRating: TextView = view.findViewById(R.id.txtFavRestaurantRating)
        val imgRestaurantImage: ImageView = view.findViewById(R.id.imgFavRestaurantImage)
        val cardRestaurant = view.findViewById(R.id.cardFavRes) as CardView
        val rlContent: RelativeLayout = view.findViewById(R.id.rlContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_favourite_single_row, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class DBAsyncTask(
        context: Context,
        private val favouriteEntity: FavouriteEntity,
        val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, FavouriteDatabase::class.java, "fav_restaurants-db")
            .fallbackToDestructiveMigration().build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val res: FavouriteEntity? =
                        db.favouriteDao()
                            .getFavRestaurantById(favouriteEntity.restaurantId)
                    db.close()
                    return res != null
                }
                2 -> {
                    db.favouriteDao().insertFavRestaurant(favouriteEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.favouriteDao().deleteFavRestaurant(favouriteEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onBindViewHolder(
        holder: FavouriteViewHolder,
        position: Int
    ) {
        val restaurant = itemList[position]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.imgFavButton.clipToOutline = true
        }
        holder.txtRestaurantName.text = restaurant.restaurantName
        val x = '\u20B9' + restaurant.restaurantPrice + "/person"
        holder.txtRestaurantPrice.text = x
        holder.txtRestaurantRating.text = restaurant.restaurantRating
        if (restaurant.restaurantId == "13") {
            holder.imgRestaurantImage.setImageResource(R.drawable.pizza)
        } else {
            Picasso.get().load(restaurant.restaurantImage)
                .error(R.drawable.restaurant_default_image)
                .into(holder.imgRestaurantImage)
        }

        holder.rlContent.setOnClickListener {
            val transaction =
                (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, RestaurantDetailsFragment(restaurant))
            transaction.commit()
            context.supportActionBar?.title =
                holder.txtRestaurantName.text.toString()
        }

        holder.imgFavButton.setOnClickListener {
            val favouriteEntity = FavouriteEntity(
                restaurant.restaurantId,
                restaurant.restaurantName,
                restaurant.restaurantRating,
                restaurant.restaurantPrice,
                restaurant.restaurantImage
            )

            if (!DBAsyncTask(context, favouriteEntity, 1).execute().get()) {
                val async =
                    DBAsyncTask(context, favouriteEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.imgFavButton.setImageResource(R.drawable.ic_fav_button_fill)
                }
            } else {
                val async = DBAsyncTask(context, favouriteEntity, 3).execute()
                val result = async.get()

                if (result) {
                    holder.imgFavButton.setImageResource(R.drawable.ic_fav_button)
                }
            }
        }
        holder.cardRestaurant.setOnClickListener {
            val fragment = FavouriteRestaurantsFragment()
            val args = Bundle()
            args.putString("id", restaurant.restaurantId)
            args.putString("name", restaurant.restaurantName)
            fragment.arguments = args
            val transaction =
                (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, FavouriteRestaurantsFragment())
            transaction.commit()
            (context).supportActionBar?.title =
                holder.txtRestaurantName.text.toString()
        }
    }
}