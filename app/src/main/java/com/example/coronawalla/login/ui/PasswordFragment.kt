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
import androidx.lifecycle.ViewModelProviders
import com.example.coronawalla.LauncherActivity
import com.example.coronawalla.login.LoginActivityViewModel
import com.example.coronawalla.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class PasswordFragment : Fragment() {
    private val TAG: String? = PasswordFragment::class.simpleName

    private val viewModel by lazy {
        activity?.let { ViewModelProviders.of(it).get(LoginActivityViewModel::class.java) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val signInButton = view.findViewById<Button>(R.id.signInButton)
        signInButton.setOnClickListener {
            val password = passwordEditText.text
            val email = viewModel?.email

            if (viewModel?.isSignIn!!) {
                //sign in
                if (password.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                } else {
                    signInUserWithEmail(email.toString(), password.toString())
                }
            } else {
                //sign up
                if (password.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                } else {
                    makeUserWithEmail(email.toString(),password.toString())
                }
            }

        }
    }


    private fun makeUserWithEmail(e:String, p:String){
        val mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(e.toString(), p.toString())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(context, "User Created!", Toast.LENGTH_SHORT).show()
                    val user = HashMap<String, Any>()
                    val activePostMap = HashMap<String, String>()
                    user["mActivePosts"] = activePostMap
                    user["mUserID"] = mAuth.currentUser!!.uid
                    user["mAuthUser"] = mAuth.currentUser!!
                    user["mHandle"] = "@NoHandle"
                    user["mUserName"] = "Anonymous"
                    user["mKarmaCount"] = 0
                    user["mFollowersCount"] = 0
                    user["mFollowingCount"] = 0
                    user["mNamedPostCount"] = 0
                    user["mAnonPostCount"] = 0
                    user["mRatio"] = 0.0
                    user["mProfileImageURL"] = ""

                    FirebaseFirestore.getInstance().collection("users").document(mAuth.currentUser!!.uid).set(user).addOnCompleteListener{ it ->
                        if(it.isSuccessful){
                            Log.d(TAG, "User created")
                            val intent = Intent(activity, LauncherActivity::class.java)
                            startActivity(intent)
                        }else{
                            Log.e(TAG,"Error:: "+ it.exception.toString())
                        }
                    }



                    //val intent = Intent(context, MainActivity::class.java)
                    //startActivity(intent)

                }else{
                    Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun signInUserWithEmail(e:String, p:String){
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Sign in success!", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, LauncherActivity::class.java)
                startActivity(intent)

            }else{
                Toast.makeText(context, "Sign in failed", Toast.LENGTH_SHORT).show()

            }
        }

    }
}
