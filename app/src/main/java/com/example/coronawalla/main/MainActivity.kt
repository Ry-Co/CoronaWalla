package com.example.coronawalla.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
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
import kotlinx.android.synthetic.main.activity_main.*
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import java.io.ByteArrayOutputStream
//todo check extras if user is anon
class MainActivity : AppCompatActivity() {
    private val TAG: String? = MainActivity::class.simpleName
    private lateinit var flp: FusedLocationProviderClient
    private lateinit var locReq: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val viewModel by lazy{
        this.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val permOptions = QuickPermissionsOptions(
        handleRationale = true,
        rationaleMessage = "Location permissions are required for core functionality!",
        handlePermanentlyDenied = true,
        permanentlyDeniedMessage = "Location permissions are needed for the core functionality of this app. Please enable these permissions to continue")

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        //todo: update user vals
        //updateCurrentUserValues()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        //val navController = findNavController(R.id.main_nav_host_fragment)
        val anon = FirebaseAuth.getInstance().currentUser!!.isAnonymous
        //val anon:Boolean = intent.getBooleanExtra("anon",true)
        //bottomNavigation.setupWithNavController(navController)
        flp = LocationServices.getFusedLocationProviderClient(this)
        getLocationUpdates()
        getCurrentUser()

        viewModel.currentLocation.observe(this, Observer{ loc ->
            Log.e(TAG,"location updated")
            updateLocalPostList(loc)
        })

        val tb = ToolbarWorker(this,anon)
        viewModel.toolbarMode.observe(this, Observer {
            tb.switchBox(it)
        })

        viewModel.updateUserServer.observe(this,Observer{
            //updateCurrentUserValues()
        })
    }

    private fun getCurrentUser(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                viewModel.currentUser.value =  it.result!!.toObject(UserClass::class.java)
            }else{
                Log.d(TAG, "Error:: "+it.exception)
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
                }
            }
        }
    }
    private fun startLocationUpdates() {
        flp.requestLocationUpdates(
            locReq,
            locationCallback,
            null /* Looper */
        )
    }
    private fun stopLocationUpdates() {
        flp.removeLocationUpdates(locationCallback)
    }

    fun updateLocalPostList(loc:Location){
        getLocalDocs(loc){docs ->
            val posts = buildPostList(docs)
            viewModel.localPostList.value = posts
        }
    }

    private fun getLocalDocs(loc:Location, callback: (List<DocumentSnapshot>) -> Unit){
        val usersGP = GeoPoint(loc.latitude, loc.longitude)
        val radiusInKm = 5 * 1.60934 //5 miles
        val geoFirestore = GeoFirestore(FirebaseFirestore.getInstance().collection("posts"))
        geoFirestore.getAtLocation(usersGP,radiusInKm){ docs, ex ->
            if(ex != null){
                Log.e(TAG, "Error:: "+ex.message)
                return@getAtLocation
            }else{
                callback.invoke(docs!!)
            }

        }

    }

    private fun buildPostList(input:List<DocumentSnapshot>): ArrayList<PostClass>{
        val out = ArrayList<PostClass>()
        for(d in input){
            val p = d.toObject(PostClass::class.java)
            if(p != null){
                out.add(p)
            }
        }
        return out
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uid = viewModel.currentUser.value!!.user_id
                val storageRef = FirebaseStorage.getInstance().reference.child("images/$uid")
                val fileUri = data?.data

                try{
                    fileUri?.let {
                        if(Build.VERSION.SDK_INT<28){
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, fileUri)
                            uploadBitmap(bitmap,storageRef)
                            viewModel.currentProfileBitmap.value = bitmap
                        }else{
                            val source = ImageDecoder.createSource(this.contentResolver, fileUri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            uploadBitmap(bitmap,storageRef)
                            viewModel.currentProfileBitmap.value = bitmap
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

    private fun uploadBitmap(bitmap: Bitmap, storageRef:StorageReference){
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnCompleteListener{
            if (it.isSuccessful){
                val downloadURL = it.result
                viewModel.currentUser.value!!.profile_image_url = downloadURL.toString()
            }else{
                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateCurrentUserValues(){
        val mAuth = FirebaseAuth.getInstance()
        val currentUser = viewModel.currentUser.value
        val dbUserRef = FirebaseFirestore.getInstance().collection("users").document(currentUser!!.user_id)
        val activePostMap = HashMap<String, String>()


//        dbUserRef.update(
//            "mActivePosts", activePostMap,
//            "mAnonPostCount",currentUser.mAnonPostCount,
//            "mAuthUser",mAuth.currentUser,
//            "mFollowersCount",currentUser.mFollowersCount,
//            "mFollowingCount",currentUser.mFollowingCount,
//            "field","value",
//            "field","value",
//            "field","value",
//            "field","value"
//        ).addOnCompleteListener{
//            if(it.isSuccessful){
//                Log.i(TAG,"UserUpdateSuccessful")
//            }else{
//                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_LONG).show()
//            }
//        }
    }
}
