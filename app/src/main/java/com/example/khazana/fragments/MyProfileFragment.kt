package com.example.khazana.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.khazana.R

class MyProfileFragment : Fragment() {

    private lateinit var txtProfileName: TextView
    private lateinit var txtProfilePhone: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var txtProfileDelivery: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfilePhone = view.findViewById(R.id.txtProfilePhone)
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail)
        txtProfileDelivery = view.findViewById(R.id.txtProfileDelivery)
        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.login_preference_file_name),
            Context.MODE_PRIVATE
        )
        val profileName = sharedPreferences.getString("user_name", "user_name")
        val profilePhone = sharedPreferences.getString("user_mobile_number", "user_mobile_number")
        val profileEmail = sharedPreferences.getString("user_email", "user_email")
        val profileDelivery = sharedPreferences.getString("user_address", "user_address")

        txtProfileName.text = profileName
        val profilePhoneString = "+91-$profilePhone"
        txtProfilePhone.text = profilePhoneString
        txtProfileEmail.text = profileEmail
        txtProfileDelivery.text = profileDelivery
        return view
    }
}