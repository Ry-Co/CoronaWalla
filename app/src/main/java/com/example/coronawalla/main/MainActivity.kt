package com.example.coronawalla.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.coronawalla.R
import com.example.coronawalla.main.ui.local.PostClass
import com.example.coronawalla.main.ui.profile.UserClass
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val TAG: String? = MainActivity::class.simpleName
    private lateinit var flp: FusedLocationProviderClient
    private lateinit var locReq: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var viewModel:MainActivityViewModel
    private val sw = ServerWorker()
    private lateinit var storage :FirebaseStorage
    private val permOptions = QuickPermissionsOptions(
        handleRationale = true,
        rationaleMessage = R.string.loc_rationale.toString(),
        handlePermanentlyDenied = true,
        permanentlyDeniedMessage = R.string.loc_rationale_perm_denied.toString())


    //TODO ADDD FIRESTORE INCREMNTS
    //todo profile stats
    //todo custom shaped toolbar?
    //todo should named posts only have 2x upvotes or 2x upvotes and downvotes?
    //todo animate the local/discussion tabs together, make posts votable by swipe
    //https://proandroiddev.com/complex-ui-animations-on-android-featuring-motionlayout-aa82d83b8660
    //https://github.com/nikhilpanju/FabFilter/blob/master/app/src/main/java/com/nikhilpanju/fabfilter/filter/FiltersLayout.kt

    override fun onResume() {
        super.onResume()
        if(this::locReq.isInitialized && this::locationCallback.isInitialized){
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        if(this::locReq.isInitialized && this::locationCallback.isInitialized){
            stopLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =  ViewModelProvider(this).get(MainActivityViewModel::class.java)
        //mAuth = viewModel.mAuth
        storage = sw.storage
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        val tb = ToolbarWorker(this)
        flp = LocationServices.getFusedLocationProviderClient(this)

        getLocationUpdates()
        if(!sw.mAuth.currentUser!!.isAnonymous){
            updateVMUserValues(sw.mAuth.currentUser!!.uid)
        }

        viewModel.currentLocation.observe(this, Observer {
            sw.getPostsFromLocation(it){posts ->
                viewModel.localPostList.value = posts as ArrayList<PostClass>
            }
        })

        viewModel.toolbarMode.observe(this, Observer {
            tb.switchBox(it)
        })

    }

    fun updateVMUserValues(uid:String){
        sw.getUserClassFromUID(uid){user->
            viewModel.currentUser.value = user
            if(user.profile_image_url != null){
                sw.getBitmapFromUID(uid){bmp ->
                    viewModel.currentProfileBitmap.value = bmp
                }
            }
        }
    }

    private fun getLocationUpdates() = runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, options = permOptions){
        locReq = LocationRequest()
        locReq.interval = 300000 // 5minute updates
        locReq.fastestInterval = 300000
        locReq.smallestDisplacement = 170f // 170m = 0.1 miles
        locReq.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()) {
                    // get latest location

                    val location = locationResult.lastLocation
                    // use your location object
                    // get latitude , longitude and other info from this
                    viewModel.currentLocation.value = location
                    Log.e(TAG,"location updated")

                }
            }
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        flp.requestLocationUpdates(
            locReq,
            locationCallback,
            null /* Looper */
        )
    }
    private fun stopLocationUpdates() {
        flp.removeLocationUpdates(locationCallback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val sw = ServerWorker()
        //we are only using on activity result for the image so we just dont bother with request code
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uid = viewModel.currentUser.value!!.user_id
                val storageRef = storage.reference.child("images/$uid")
                val fileUri = data?.data
                try{
                    fileUri?.let {
                        if(Build.VERSION.SDK_INT<28){
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, fileUri)
                            sw.uploadBitmapToReference(bitmap,storageRef){
                                //update profileImageURL
                                viewModel.currentUser.value!!.profile_image_url = it
                            }
                            viewModel.currentProfileBitmap.value = bitmap
                        }else{
                            val source = ImageDecoder.createSource(this.contentResolver, fileUri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            sw.uploadBitmapToReference(bitmap,storageRef){
                                //update profileImageURL
                                viewModel.currentUser.value!!.profile_image_url = it
                            }
                            viewModel.currentProfileBitmap.value = bitmap
                            Log.e(TAG, "main-activity- bitmap uploaded and viewmodel changed")
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
