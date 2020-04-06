package com.example.coronawalla.main

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.coronawalla.R
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation

class MainActivity : AppCompatActivity() {
    private val GEO_QUERY_RADIUS_IN_KM = 5 * 1.60934 //5 miles


    private val viewModel by lazy{
        this.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val permOptions = QuickPermissionsOptions(
        handleRationale = true,
        rationaleMessage = "Location permissions are required for core functionality!",
        handlePermanentlyDenied = true,
        permanentlyDeniedMessage = "Location permissions are needed for the core functionality of this app. Please enable these permissions to continue")

    override fun onStart() {
        super.onStart()
        getUsersCurrentLocation()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val navController = findNavController(R.id.main_nav_host_fragment)
        bottomNavigation.setupWithNavController(navController)

        viewModel.toolbarMode.observe(this, Observer {
            when(viewModel.toolbarMode.value){
                 1 -> roamToolbar() //roam
                 0 -> localToolbar() //local
                -1 -> profileToolbar() //profile
                 2 -> postToolbar() //post creation toolbar
                 3 -> postPreviewToolbar()
            }
        })

        viewModel.currentLocation.observe(this, Observer {
            if(it != null){
                //go get local posts
                getLocalDocuments()
            }
        })
    }


    private fun getLocalDocuments(){
        val usersGP = GeoPoint(viewModel!!.currentLocation.value!!.latitude, viewModel!!.currentLocation.value!!.longitude)
        val geoFirestore = GeoFirestore(FirebaseFirestore.getInstance().collection("posts"))
        geoFirestore.getAtLocation(usersGP,GEO_QUERY_RADIUS_IN_KM){docs, ex ->
            if(ex != null){
                Log.e("TAGG::", ex.message)
                return@getAtLocation
            }else{
                viewModel.localDocList.value = docs!!
            }

        }
    }
    private fun getUsersCurrentLocation() = runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, options = permOptions){
        val flp = LocationServices.getFusedLocationProviderClient(this)
        flp.lastLocation.addOnCompleteListener{
            if (it.isSuccessful){
                viewModel.currentLocation.value = it.result

            }else{
                Log.e("TAG", it.exception.toString())
            }
        }
    }
    private fun profileToolbar(){
        toolbar_title_tv.text = "Profile"
        post_IV.visibility = View.INVISIBLE
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
    }
    private fun localToolbar(){
        toolbar_title_tv.text = "Local"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        post_IV.visibility = View.VISIBLE
        post_IV.setOnClickListener{
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_local_to_postFragment)
            Toast.makeText(this, "POST", Toast.LENGTH_SHORT).show()
        }
    }
    private fun roamToolbar(){
        toolbar_title_tv.text = "Roam"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        post_IV.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
    }
    private fun postToolbar(){
        toolbar_title_tv.text = "Post"
        post_IV.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.INVISIBLE
        toolbar_send_tv.visibility = View.VISIBLE
        toolbar_cancel_tv.visibility = View.VISIBLE
        toolbar_cancel_tv.setOnClickListener {
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_postFragment_to_local)
        }
    }
    private fun postPreviewToolbar(){
        toolbar_title_tv.text = "Post"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        post_IV.visibility = View.INVISIBLE

    }
}
