package com.example.khazana.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khazana.R
import com.example.khazana.activity.MainActivity
import com.example.khazana.adapter.HomeRecyclerAdapter
import com.example.khazana.database.FavouriteEntity
import com.example.khazana.utility.ConnectionManager
import com.example.khazana.utility.Sorter
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar

    val names = arrayListOf<FavouriteEntity>()
    val restaurantInfoList = arrayListOf<FavouriteEntity>()

    private fun filter(text: String) {
        val filteredNames: ArrayList<FavouriteEntity> = ArrayList()
        for (s in names) {
            if (s.restaurantName.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                filteredNames.add(s)
            }
        }
        MainActivity.key = 1
        recyclerAdapter =
            HomeRecyclerAdapter(filteredNames, activity as Context)
        recyclerHome.adapter = recyclerAdapter
        recyclerHome.layoutManager = layoutManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setHasOptionsMenu(true)
        val simpleSearchView =
            view.findViewById(R.id.search) as SearchView
        simpleSearchView.clearFocus()
        val rlHome: RelativeLayout = view.findViewById(R.id.rlHome)
        rlHome.requestFocus()
        simpleSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                filter(newText.toString())
                return false
            }
        })

        recyclerHome = view.findViewById(R.id.recyclerHome) as RecyclerView
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        layoutManager = LinearLayoutManager(activity)

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    try {
                        progressLayout.visibility = View.GONE
                        val data1 = it.getJSONObject("data")
                        val success = data1.getBoolean("success")
                        if (success) {
                            val data = data1.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = FavouriteEntity(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )
                                restaurantInfoList.add(restaurantObject)
                                names.add(restaurantObject)
                                recyclerAdapter =
                                    HomeRecyclerAdapter(restaurantInfoList, activity as Context)
                                recyclerHome.adapter = recyclerAdapter
                                recyclerHome.layoutManager = layoutManager
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var checkedItem: Int = -1
        if (id == R.id.action_sort) {
            item.setIcon(R.drawable.ic_sorter)

            val optionsList = arrayOf(
                "Cost Low to High",
                "Cost High to Low",
                "Rating",
                "Cost for One"
            )
            val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
            builder?.setTitle("Sort By?")
            builder?.setCancelable(false)
            builder?.setSingleChoiceItems(optionsList, checkedItem) { _, isChecked ->
                checkedItem = isChecked
            }
            builder?.setPositiveButton("Ok") { _, _ ->
                when (checkedItem) {
                    0 -> {
                        Collections.sort(restaurantInfoList, Sorter.costComparator)
                    }
                    1 -> {
                        Collections.sort(restaurantInfoList, Sorter.costComparator)
                        restaurantInfoList.reverse()
                    }
                    2 -> {
                        Collections.sort(restaurantInfoList, Sorter.ratingComparator)
                        restaurantInfoList.reverse()
                    }
                    3 -> {
                        Collections.sort(restaurantInfoList, Sorter.costComparator)
                    }
                }
                MainActivity.key = 0
                recyclerAdapter = HomeRecyclerAdapter(restaurantInfoList, activity as Context)
                recyclerHome.adapter = recyclerAdapter
                recyclerHome.layoutManager = layoutManager
                recyclerAdapter.notifyDataSetChanged()
            }
            builder?.setNegativeButton("Cancel", null)
            builder?.create()
            builder?.show()
        }
        return super.onOptionsItemSelected(item)
    }
}