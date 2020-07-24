package com.example.khazana.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.khazana.R
import com.example.khazana.adapter.FavouriteRecyclerAdapter
import com.example.khazana.database.FavouriteDatabase
import com.example.khazana.database.FavouriteEntity

class FavouriteRestaurantsFragment : Fragment() {

    private lateinit var recyclerFavourite: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: FavouriteRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private var restaurantInfoList = ArrayList<FavouriteEntity>().toList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_restaurants, container, false)

        recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
        val rlEmpty: RelativeLayout = view.findViewById(R.id.rlEmpty)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        layoutManager = LinearLayoutManager(activity)
        restaurantInfoList = RetrieveFavourites(activity as Context).execute().get()

        if (activity != null) {
            progressLayout.visibility = View.GONE
            if (restaurantInfoList.isNotEmpty()) {
                recyclerAdapter = FavouriteRecyclerAdapter(activity as Context, restaurantInfoList)
                recyclerFavourite.adapter = recyclerAdapter
                recyclerFavourite.layoutManager = layoutManager
            } else {
                rlEmpty.visibility = View.VISIBLE
            }
        }
        return view
    }

    class RetrieveFavourites(val context: Context) :
        AsyncTask<Void, Void, List<FavouriteEntity>>() {
        override fun doInBackground(vararg p0: Void?): List<FavouriteEntity> {
            val db =
                Room.databaseBuilder(context, FavouriteDatabase::class.java, "fav_restaurants-db")
                    .fallbackToDestructiveMigration().build()
            val favRes = db.favouriteDao().getAllFavRestaurants()
            db.close()
            return favRes
        }
    }
}
