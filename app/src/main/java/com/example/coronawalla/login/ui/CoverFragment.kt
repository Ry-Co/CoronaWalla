package com.example.coronawalla.login.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.R
import com.example.coronawalla.login.LoginActivityViewModel
import com.example.coronawalla.main.MainActivity
import com.google.firebase.auth.FirebaseAuth


class CoverFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val anonButton = view.findViewById<Button>(R.id.anonymous_button)
        val signInButton = view.findViewById<Button>(R.id.sign_in_button)


        anonButton.setOnClickListener {
            //straight to main activity as anonymous
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener{
                if(it.isSuccessful){
                    val intent = Intent(context, MainActivity::class.java).putExtra("anon", true)

                    startActivity(intent)
                }else{
                    Toast.makeText(context,it.exception.toString(),Toast.LENGTH_LONG).show()
                }
            }
            //findNavController().navigate(R.id.action_coverFragment_to_signInFragment)
        }
        signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_coverFragment_to_numberFormatFragment)
        }

    }
}
