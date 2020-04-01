package com.example.coronawalla.login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.login.LoginActivityViewModel
import com.example.coronawalla.R
import com.hbb20.CountryCodePicker


class NumberFormatFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(LoginActivityViewModel::class.java) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_number_format, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val verifyButton = view.findViewById<Button>(R.id.signInButton)
        val phoneNumberET = view.findViewById<EditText>(R.id.passwordEditText)
        val ccp = view.findViewById<CountryCodePicker>(R.id.ccp)
        ccp.registerCarrierNumberEditText(phoneNumberET)
        phoneNumberET.setText(viewModel?.phoneNumber)
        verifyButton.setOnClickListener {
            // GO TO PHONE VERIFICATION PAGE WITH CODE

            var fullNumber = ccp.fullNumberWithPlus
            if(ccp.isValidFullNumber){
                viewModel?.phoneNumber = fullNumber
                findNavController().navigate(R.id.action_numberFormatFragment_to_phoneVerification)
            }else{
                Toast.makeText(context, fullNumber+" is not a valid number", Toast.LENGTH_SHORT).show()
            }
        }

    }



}
