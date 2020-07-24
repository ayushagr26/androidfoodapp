package com.example.khazana.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khazana.R
import com.example.khazana.activity.CartActivity
import com.example.khazana.adapter.HomeRecyclerAdapter
import com.example.khazana.adapter.RestaurantDetailsRecyclerAdapter
import com.example.khazana.database.CartDatabase
import com.example.khazana.database.CartEntity
import com.example.khazana.database.FavouriteEntity
import com.example.khazana.model.CartDetails
import com.example.khazana.utility.ConnectionManager
import com.google.gson.Gson

class RestaurantDetailsFragment(private val resObject: FavouriteEntity) : Fragment() {

    private lateinit var recyclerMenu: RecyclerView
    private lateinit var restaurantMenuAdapter: RestaurantDetailsRecyclerAdapter
    private var menuList = arrayListOf<CartDetails>()
    private lateinit var rlLoading: RelativeLayout
    private var orderList = arrayListOf<CartDetails>()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var goToCart: Button
        var resId: String? = "0"
        var resName: String? = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant_details, container, false)
        sharedPreferences =
            activity?.getSharedPreferences("FoodApp", Context.MODE_PRIVATE) as SharedPreferences
        rlLoading = view?.findViewById(R.id.progressLayout) as RelativeLayout
        rlLoading.visibility = View.VISIBLE
        val favImage: ImageView = view.findViewById(R.id.imgResDetFavButton)
        resId = resObject.restaurantId
        resName = resObject.restaurantName
        setHasOptionsMenu(true)
        goToCart = view.findViewById(R.id.btnProceedToCart)
        goToCart.visibility = View.GONE
        goToCart.setOnClickListener {
            proceedToCart()
        }

        setUpRestaurantMenu(view)
        val listOfFavourites =
            context?.let { HomeRecyclerAdapter.GetAllFavAsyncTask(it).execute().get() }
        if (listOfFavourites != null) {
            if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(resObject.restaurantId)) {
                favImage.setImageResource(R.drawable.ic_fav_button_fill)
            } else {
                favImage.setImageResource(R.drawable.ic_fav_button)
            }
        }

        favImage.setOnClickListener {
            val favouriteEntity = FavouriteEntity(
                resObject.restaurantId,
                resObject.restaurantName,
                resObject.restaurantRating,
                resObject.restaurantPrice,
                resObject.restaurantImage
            )
            if (!context?.let { it1 ->
                    HomeRecyclerAdapter.DBAsyncTask(it1, favouriteEntity, 1).execute().get()
                }!!) {
                val async =
                    context?.let { it1 ->
                        HomeRecyclerAdapter.DBAsyncTask(it1, favouriteEntity, 2).execute()
                    }
                val result = async?.get()
                if (result!!) {
                    favImage.setImageResource(R.drawable.ic_fav_button_fill)
                }
            } else {
                val async = context?.let { it2 ->
                    HomeRecyclerAdapter.DBAsyncTask(
                        it2,
                        favouriteEntity,
                        3
                    ).execute()
                }
                val result = async?.get()

                if (result!!) {
                    favImage.setImageResource(R.drawable.ic_fav_button)
                }
            }
        }
        return view
    }

    private fun setUpRestaurantMenu(view: View) {

        recyclerMenu = view.findViewById(R.id.recyclerResDetails)
        if (ConnectionManager().checkConnectivity(activity as Context)) {

            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"
            val jsonObjectRequest = object :
                JsonObjectRequest(Method.GET, url, null, Response.Listener {
                    rlLoading.visibility = View.GONE
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val menuObject = resArray.getJSONObject(i)
                                val foodItem = CartDetails(
                                    menuObject.getString("id"),
                                    menuObject.getString("name"),
                                    menuObject.getString("cost_for_one").toInt(),
                                    menuObject.getString("restaurant_id")
                                )
                                menuList.add(foodItem)
                                restaurantMenuAdapter = RestaurantDetailsRecyclerAdapter(
                                    activity as Context,
                                    menuList,
                                    object : RestaurantDetailsRecyclerAdapter.OnItemClickListener {
                                        override fun onAddItemClick(foodItem: CartDetails) {
                                            orderList.add(foodItem)
                                            if (orderList.size > 0) {
                                                goToCart.visibility = View.VISIBLE
                                                RestaurantDetailsRecyclerAdapter.isCartEmpty = false
                                            }
                                        }

                                        override fun onRemoveItemClick(foodItem: CartDetails) {
                                            orderList.remove(foodItem)
                                            if (orderList.isEmpty()) {
                                                goToCart.visibility = View.GONE
                                                RestaurantDetailsRecyclerAdapter.isCartEmpty = true
                                            }
                                        }
                                    })
                                val mLayoutManager = LinearLayoutManager(activity)
                                recyclerMenu.layoutManager = mLayoutManager
                                recyclerMenu.itemAnimator = DefaultItemAnimator()
                                recyclerMenu.adapter = restaurantMenuAdapter
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error occurred!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(
                        activity as Context,
                        "Some unexpected error occurred!",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "a959329b58247a"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun proceedToCart() {
        val gson = Gson()
        val foodItems = gson.toJson(orderList)

        val async = ItemsOfCart(activity as Context, resId?.toInt(), foodItems, 1).execute()
        val result = async.get()
        if (result) {
            val data = Bundle()
            data.putString("restaurant_id", resId)
            data.putString("restaurant_name", resName)
            val intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("data", data)
            startActivity(intent)
        } else {
            Toast.makeText(
                (activity as Context),
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    class ItemsOfCart(
        context: Context,
        val restaurantId: Int?,
        private val foodItems: String,
        private val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, CartDatabase::class.java, "cartOrder-db")
            .fallbackToDestructiveMigration().build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.cartDao().insertOrder(CartEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.cartDao().deleteOrder(CartEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}
