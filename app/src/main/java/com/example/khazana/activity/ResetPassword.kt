package com.example.khazana.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khazana.R
import com.example.khazana.utility.ConnectionManager
import org.json.JSONObject

class ResetPassword : AppCompatActivity() {

    private lateinit var etOtp: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConPass: EditText
    private lateinit var btnSubmit: Button
    private var mobile: String? = "404"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etOtp = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConPass = findViewById(R.id.etConfirmPassword)
        btnSubmit = findViewById(R.id.btnResetSubmit)

        if (intent != null) {
            mobile = intent.getStringExtra("mobileNumber")
        }
        try {
            btnSubmit.setOnClickListener {
                if (etConPass.text.toString() == etNewPassword.text.toString()) {
                    val queue = Volley.newRequestQueue(this@ResetPassword)
                    val url = "http://13.235.250.119/v2/reset_password/fetch_result"
                    val jsonParams = JSONObject()
                    jsonParams.put("otp", etOtp.text.toString())
                    jsonParams.put("password", etNewPassword.text.toString())
                    jsonParams.put("mobile_number", mobile.toString())
                    if (ConnectionManager().checkConnectivity(this@ResetPassword)) {
                        val jsonRequest = object : JsonObjectRequest(
                            Method.POST,
                            url,
                            jsonParams,
                            Response.Listener {
                                val jsonData = it.getJSONObject("data")
                                val success = jsonData.getBoolean("success")
                                if (success) {
                                    Toast.makeText(
                                        this@ResetPassword,
                                        "Password has successfully been changed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(
                                        this@ResetPassword,
                                        LogIn::class.java
                                    )
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@ResetPassword,
                                        "Password length should be at least 6 characters!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            Response.ErrorListener {
                                Toast.makeText(
                                    this@ResetPassword,
                                    "Some unexpected error occurred!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-Type"] = "application/json"
                                headers["token"] = "a959329b58247a"
                                return headers
                            }
                        }
                        queue.add(jsonRequest)
                    } else {
                        val dialog = AlertDialog.Builder(this@ResetPassword)
                        dialog.setTitle("Error")
                        dialog.setMessage("Internet Connection not found ")
                        dialog.setPositiveButton("Open Settings") { _, _ ->
                            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit") { _, _ ->
                            ActivityCompat.finishAffinity(this@ResetPassword)
                        }
                        dialog.create()
                        dialog.show()
                    }
                } else {
                    Toast.makeText(
                        this@ResetPassword,
                        "New password and confirm password don't match!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@ResetPassword,
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onBackPressed() {
        startActivity(
            Intent(
                this@ResetPassword,
                ForgotPasswordActivity::class.java
            )
        )
        super.onBackPressed()
    }
}