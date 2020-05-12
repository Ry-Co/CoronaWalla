package com.example.coronawalla.login.direlect

import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.login.LoginActivityViewModel
import com.example.coronawalla.R
import java.util.*

class SignUpFragment : Fragment() {

    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(LoginActivityViewModel::class.java) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputTextET = view.findViewById<EditText>(R.id.email_or_mobile_ET)
        val nextButton = view.findViewById<Button>(R.id.next_button3)
        nextButton.setOnClickListener {
            val txt = inputTextET.text.toString()
            when {
                txt == "" -> { Toast.makeText(context, "Enter your email or phone number!", Toast.LENGTH_SHORT).show() }
                txt.contains("@") -> { email(txt) }
                else -> { phoneNumber(txt) }
            }
        }
    }

    fun email(email:String){
        if (email.contains("@") and email.contains(".")) {
            //update view model value
            viewModel?.email = email
            viewModel?.isSignIn = false
            //navigate to password fragment
            findNavController().navigate(R.id.action_signUpFragment_to_passwordFragment)
        } else {
            Toast.makeText(context, "Please input a valid email address or phone number", Toast.LENGTH_SHORT).show()
        }
    }

    fun phoneNumber(phoneNumber:String){
        if (phoneNumber === "" || !Patterns.PHONE.matcher(phoneNumber).matches()) {
            Toast.makeText(context, "Please input a valid phone number or email", Toast.LENGTH_SHORT).show()
        } else {
            // format the phone number

            val tempNum = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)
            } else {
                PhoneNumberUtils.formatNumber(phoneNumber) //Deprecated method
            }
            // update the view model
            viewModel?.phoneNumber = tempNum
            viewModel?.isSignIn = false
            // navigate to confirmation fragment
            findNavController().navigate(R.id.action_signUpFragment_to_numberFormatFragment)
        }

    }
}
