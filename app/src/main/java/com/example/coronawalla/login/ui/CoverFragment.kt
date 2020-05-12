package com.example.coronawalla.login.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.LauncherActivity
import com.example.coronawalla.R
import com.google.firebase.auth.FirebaseAuth


class CoverFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signInButton = view.findViewById<Button>(R.id.verifyOTPButton)
        val signUpButton = view.findViewById<Button>(R.id.sign_up_button)
        val anonbutton = view.findViewById<Button>(R.id.anonButton)
        signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_coverFragment_to_signInFragment)
        }
        signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_coverFragment_to_signUpFragment)
        }
        anonbutton.setOnClickListener {
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener{
                if(it.isSuccessful){
                    val intent = Intent(activity, LauncherActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.e("TAG", it.exception.toString())
                }
            }
        }
    }




}
