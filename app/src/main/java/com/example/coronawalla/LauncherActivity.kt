package com.example.coronawalla

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.coronawalla.login.LoginActivity
import com.example.coronawalla.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {
    private val TAG: String? = LauncherActivity::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val mAuth = FirebaseAuth.getInstance();
        if(mAuth.currentUser != null){
            //go to main activity
            Log.i(TAG, "User is non null, going to main")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            //go to login
            Log.e(TAG, "User is null, going to login")
            val intent = Intent(this,
                LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
