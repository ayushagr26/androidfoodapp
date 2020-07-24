package com.example.khazana.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.khazana.R
import com.example.khazana.database.FavouriteDatabase
import com.example.khazana.database.FavouriteEntity
import com.example.khazana.fragments.RestaurantDetailsFragment
import com.squareup.picasso.Picasso

class HomeRecyclerAdapter(
    private var restaurants: ArrayList<FavouriteEntity>,
    val context: Context
) : RecyclerView.Adapter<HomeRecyclerAdapter.AllRestaurantsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewGroup: Int): AllRestaurantsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_home_single_row, parent, false)

        return AllRestaurantsViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return restaurants.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: AllRestaurantsViewHolder, position: Int) {
        val resObject = restaurants[position]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.resThumbnail.clipToOutline = true
        }
        holder.restaurantName.text = resObject.restaurantName
        holder.rating.text = resObject.restaurantRating
        val costForTwo = "${resObject.restaurantPrice}/person"
        holder.cost.text = costForTwo
        if (resObject.restaurantId == "13") {
            holder.resThumbnail.setImageResource(R.drawable.pizza)
        } else {
            Picasso.get().load(resObject.restaurantImage).error(R.drawable.restaurant_default_image)
                .into(holder.resThumbnail)
        }

        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()
        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(resObject.restaurantId)) {
            holder.favImage.setImageResource(R.drawable.ic_fav_button_fill)
        } else {
            holder.favImage.setImageResource(R.drawable.ic_fav_button)
        }

        holder.favImage.setOnClickListener {
            val favouriteEntity = FavouriteEntity(
                resObject.restaurantId,
                resObject.restaurantName,
                resObject.restaurantRating,
                resObject.restaurantPrice,
                resObject.restaurantImage
            )
            if (!DBAsyncTask(context, favouriteEntity, 1).execute().get()) {
                val async =
                    DBAsyncTask(context, favouriteEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.favImage.setImageResource(R.drawable.ic_fav_button_fill)
                }
            } else {
                val async = DBAsyncTask(context, favouriteEntity, 3).execute()
                val result = async.get()
                if (result) {
                    holder.favImage.setImageResource(R.drawable.ic_fav_button)
                }
            }
        }

        holder.cardRestaurant.setOnClickListener {
            val transaction =
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, RestaurantDetailsFragment(resObject))
            transaction.commit()
            (context as AppCompatActivity).supportActionBar?.title =
                holder.restaurantName.text.toString()
            (context as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        }
    }

    class AllRestaurantsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resThumbnail = view.findViewById(R.id.imgRestaurantImage) as ImageView
        val restaurantName = view.findViewById(R.id.txtRestaurantName) as TextView
        val rating = view.findViewById(R.id.txtRestaurantRating) as TextView
        val cost = view.findViewById(R.id.txtRestaurantPrice) as TextView
        val cardRestaurant = view.findViewById(R.id.cardRestaurant) as CardView
        val favImage = view.findViewById(R.id.imgFavButton) as ImageView
    }

    class DBAsyncTask(
        context: Context,
        private val favouriteEntity: FavouriteEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, FavouriteDatabase::class.java, "fav_restaurants-db")
            .build()

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

    class GetAllFavAsyncTask(
        context: Context
    ) :
        AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, FavouriteDatabase::class.java, "fav_restaurants-db")
            .build()

        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.favouriteDao().getAllFavRestaurants()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.restaurantId)
            }
            return listOfIds
        }
    }
}