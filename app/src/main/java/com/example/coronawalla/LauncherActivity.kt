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
        //null anon then normal
        if(mAuth.currentUser == null){
            Log.e(TAG, "User is null, going to login")
            val intent = Intent(this,
                LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else if(mAuth.currentUser!!.isAnonymous ){
            Log.e(TAG, "User is null, going to login")
            val intent = Intent(this,
                LoginActivity::class.java)
            startActivity(intent)
            finish()
            //go to main activity

        }else{
            //go to login
            Log.i(TAG, "User is non null and non anon, going to main")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}
