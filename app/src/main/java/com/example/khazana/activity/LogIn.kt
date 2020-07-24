package com.example.khazana.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khazana.R
import com.example.khazana.utility.ConnectionManager
import com.example.khazana.utility.SessionManager
import com.example.khazana.utility.Validations
import org.json.JSONException
import org.json.JSONObject

class LogIn : Activity() {

    private lateinit var etMobileNumber: EditText
    private lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    lateinit var txtRegister: TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        setContentView(R.layout.activity_login)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.prefName, Context.MODE_PRIVATE)
        sharedPreferences =
            getSharedPreferences(
                getString(R.string.login_preference_file_name),
                Context.MODE_PRIVATE
            )
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val intent = Intent(this@LogIn, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)

        txtForgotPassword.setOnClickListener {
            startActivity(Intent(this@LogIn, ForgotPasswordActivity::class.java))
        }

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        btnLogin.setOnClickListener {

            btnLogin.visibility = View.INVISIBLE
            if (Validations.validateMobile(etMobileNumber.text.toString()) && Validations.validatePasswordLength(
                    etPassword.text.toString()
                )
            ) {
                if (ConnectionManager().checkConnectivity(this@LogIn)) {

                    val queue = Volley.newRequestQueue(this@LogIn)
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", etMobileNumber.text.toString())
                    jsonParams.put("password", etPassword.text.toString())
                    val url = "http://13.235.250.119/v2/login/fetch_result/"

                    val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonParams,
                        Response.Listener {
                            try {
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    savePreferences()
                                    val response = data.getJSONObject("data")
                                    sharedPreferences.edit()
                                        .putString("user_id", response.getString("user_id")).apply()
                                    sharedPreferences.edit()
                                        .putString("user_name", response.getString("name")).apply()
                                    sharedPreferences.edit()
                                        .putString(
                                            "user_mobile_number",
                                            response.getString("mobile_number")
                                        ).apply()
                                    sharedPreferences.edit()
                                        .putString("user_address", response.getString("address"))
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("user_email", response.getString("email"))
                                        .apply()
                                    sessionManager.setLogin(true)
                                    startActivity(
                                        Intent(
                                            this@LogIn,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    btnLogin.visibility = View.VISIBLE
                                    txtForgotPassword.visibility = View.VISIBLE
                                    btnLogin.visibility = View.VISIBLE
                                    val errorMessage = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@LogIn,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                btnLogin.visibility = View.VISIBLE
                                txtForgotPassword.visibility = View.VISIBLE
                                txtRegister.visibility = View.VISIBLE
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener {
                            btnLogin.visibility = View.VISIBLE
                            txtForgotPassword.visibility = View.VISIBLE
                            txtRegister.visibility = View.VISIBLE
                            Log.e("Error::::", "/post request fail! Error: ${it.message}")
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
                    btnLogin.visibility = View.VISIBLE
                    txtForgotPassword.visibility = View.VISIBLE
                    txtRegister.visibility = View.VISIBLE
                    Toast.makeText(this@LogIn, "No internet Connection", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                btnLogin.visibility = View.VISIBLE
                txtForgotPassword.visibility = View.VISIBLE
                txtRegister.visibility = View.VISIBLE
                Toast.makeText(this@LogIn, "Invalid Phone or Password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
    }
}