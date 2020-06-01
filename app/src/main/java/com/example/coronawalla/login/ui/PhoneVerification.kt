package com.example.coronawalla.login.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.coronawalla.LauncherActivity
import com.example.coronawalla.login.LoginActivityViewModel
import com.example.coronawalla.R
import com.example.coronawalla.login.direlect.PasswordFragment
import com.example.coronawalla.main.ui.profile.UserClass
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit


class PhoneVerification : Fragment() {
    private val TAG: String? = PasswordFragment::class.simpleName
    private lateinit var viewModel:LoginActivityViewModel
    private var verificationID: String? = null
    private lateinit var mAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =  ViewModelProvider(this.requireActivity()).get(LoginActivityViewModel::class.java)
        mAuth = viewModel.mAuth
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val verifyButton = view.findViewById<Button>(R.id.anonymous_button)
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
        val number = viewModel.phoneNumber.toString().trim()
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
        if(viewModel.mAuth.currentUser==null){
            viewModel.mAuth.signInWithCredential(p0).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
                    //update user info
                    viewModel.mAuth.currentUser?.updatePhoneNumber(p0)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                //Toast.makeText(context, "Phone added!", Toast.LENGTH_SHORT).show()
                                val mAuth = viewModel.mAuth
                                val user = UserClass()
                                user.user_id = mAuth.currentUser!!.uid

                                viewModel!!.db.collection("users").document(mAuth.currentUser!!.uid).set(user).addOnCompleteListener{ it ->
                                    if(it.isSuccessful){
                                        Log.d(TAG, "User added")
                                        val intent = Intent(activity, LauncherActivity::class.java).putExtra("anon", false)
                                        startActivity(intent)
                                    }else{
                                        Log.e(TAG, "Error:: "+it.exception.toString())
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
                } else {
                    Toast.makeText(
                        context,
                        "There was an error:: " + it.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }else if(viewModel.mAuth.currentUser!!.isAnonymous){
            viewModel.mAuth.currentUser!!.linkWithCredential(p0).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show()
                    //update user info
                    viewModel.mAuth.currentUser?.updatePhoneNumber(p0)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                //Toast.makeText(context, "Phone added!", Toast.LENGTH_SHORT).show()
                                val mAuth = viewModel.mAuth
                                val user = UserClass()
                                user.user_id = mAuth.currentUser!!.uid

                                viewModel.db.collection("users").document(mAuth.currentUser!!.uid).set(user).addOnCompleteListener{ it ->
                                    if(it.isSuccessful){
                                        Log.d(TAG, "User added")
                                        val intent = Intent(activity, LauncherActivity::class.java).putExtra("anon", false)
                                        startActivity(intent)
                                    }else{
                                        Log.e(TAG, "Error:: "+it.exception.toString())
                                    }
                                }

                            } else {
                                Toast.makeText(
                                    context,
                                    "There was an error:: " + it.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(TAG, it.exception!!.message.toString())
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        "There was an error:: " + it.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, it.exception!!.message.toString())
                }
            }
        }

    }


}
