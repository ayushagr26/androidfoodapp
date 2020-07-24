package com.example.khazana.utility

import com.example.khazana.database.FavouriteEntity

class Sorter {
    companion object {
        var costComparator = Comparator<FavouriteEntity> { res1, res2 ->
            val costOne = res1.restaurantPrice
            val costTwo = res2.restaurantPrice
            if (costOne.compareTo(costTwo) == 0) {
                ratingComparator.compare(res1, res2)
            } else {
                costOne.compareTo(costTwo)
            }
        }

        var ratingComparator = Comparator<FavouriteEntity> { res1, res2 ->
            val ratingOne = res1.restaurantRating
            val ratingTwo = res2.restaurantRating
            if (ratingOne.compareTo(ratingTwo) == 0) {
                val costOne = res1.restaurantPrice
                val costTwo = res2.restaurantPrice
                costOne.compareTo(costTwo)
            } else {
                ratingOne.compareTo(ratingTwo)
            }
        }
    }
}