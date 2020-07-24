package com.example.khazana.utility

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var privateMode = 0
    val prefName = "FoodApp"

    private val keyIsLoggedIn = "isLoggedIn"
    private var pref: SharedPreferences = context.getSharedPreferences(prefName, privateMode)
    private var editor: SharedPreferences.Editor = pref.edit()

    fun setLogin(isLoggedIn: Boolean) {
        editor.putBoolean(keyIsLoggedIn, isLoggedIn)
        editor.apply()
    }
}
