package com.example.coronawalla.login.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.coronawalla.login.LoginActivityViewModel
import com.example.coronawalla.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class PhoneVerification : Fragment() {
    private val viewModel by lazy {
        activity?.let { ViewModelProviders.of(it).get(LoginActivityViewModel::class.java) }
    }
    private var verificationID: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val verifyButton = view.findViewById<Button>(R.id.verifyOTPButton)
        val codeET = view.findViewById<EditText>(R.id.SMScode_ET)
        sendCode()
        verifyButton.setOnClickListener {
            val code = codeET.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(context, "Code required", Toast.LENGTH_SHORT).show()
                codeET.requestFocus()
            } else {
                verificationID.let {
                    val credential = PhoneAuthProvider.getCredential(it.toString(), code)
                    addPhoneNumber(credential)
                }

            }
        }
    }

    private fun sendCode() {
        var number = viewModel?.phoneNumber.toString().trim()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            phoneAuthCallbacks
        )
    }

    private val phoneAuthCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                addPhoneNumber(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(context, p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationID = p0
            }

        }

    private fun addPhoneNumber(p0: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(p0).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
                //TODO: Go to main activity
                //update user info
                FirebaseAuth.getInstance().currentUser?.updatePhoneNumber(p0)
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            //Toast.makeText(context, "Phone added!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "There was an error:: " + it.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    context,
                    "There was an error:: " + it.exception?.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}