package com.example.coronawalla.main

import android.Manifest
import android.app.Activity
import android.content.Intent
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
    private lateinit var mAuth : FirebaseAuth
    private val sw = ServerWorker(this)
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
        storage = viewModel.storage
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        val tb = ToolbarWorker(this)
        flp = LocationServices.getFusedLocationProviderClient(this)

        getLocationUpdates()
//        if(!mAuth.currentUser!!.isAnonymous){
//            val uid = mAuth.currentUser!!.uid
//            updateVMUserValues(uid)
//        }
        if(!sw.mAuth.currentUser!!.isAnonymous){
            updateVMUserValues(sw.mAuth.currentUser!!.uid)
        }

//        viewModel.currentLocation.observe(this, Observer{
//            getPostsFromServer {posts ->
//                viewModel.localPostList.value = posts as ArrayList<PostClass>
//            }
//        })
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

//    private fun getUserClassFromUID(uid:String, callback:(UserClass)->Unit){
//        Log.e(TAG, "Server Call: ")
//        viewModel.db.collection("users").document(uid).get().addOnCompleteListener {
//            if (it.isSuccessful) {
//                val uidUserClass = it.result!!.toObject(UserClass::class.java)
//                callback.invoke(uidUserClass!!)
//            } else {
//                Log.d(TAG, "Error:: " + it.exception)
//            }
//        }
//
//    }

//    private fun getBitmapFromUID(uid:String, callback:(Bitmap)->Unit){
//        val profRef = storage.reference.child("images/$uid")
//        val ONE_MEGABYTE: Long = 1024 * 1024
//        profRef.getBytes(ONE_MEGABYTE).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
//                callback.invoke(bmp)
//            } else {
//                Log.e(TAG, it.exception.toString())
//            }
//        }
//    }

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
        flp.requestLocationUpdates(
            locReq,
            locationCallback,
            null /* Looper */
        )
    }
    private fun stopLocationUpdates() {
        flp.removeLocationUpdates(locationCallback)
    }

//    private fun getLocalDocs(loc:Location, callback: (List<DocumentSnapshot>) -> Unit){
//        val usersGP = GeoPoint(loc.latitude, loc.longitude)
//        val radiusInKm = 5 * 1.60934 //5 miles
//        val geoFirestore = GeoFirestore(viewModel.db.collection("posts"))
//        geoFirestore.getAtLocation(usersGP,radiusInKm){ docs, ex ->
//            if(ex != null){
//                Log.e(TAG, "Error:: "+ex.message)
//                return@getAtLocation
//            }else{
//                callback.invoke(docs!!)
//            }
//        }
//
//    }

//    private fun buildPostList(input:List<DocumentSnapshot>): ArrayList<PostClass>{
//        val out = ArrayList<PostClass>()
//        for(d in input){
//            val p = d.toObject(PostClass::class.java)
//            if(p != null){
//                out.add(p)
//            }
//        }
//        return out
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val sw = ServerWorker(this)
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

//    private fun uploadBitmap(bitmap: Bitmap, storageRef:StorageReference){
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
//        val data = baos.toByteArray()
//        val uploadTask = storageRef.putBytes(data)
//
//        uploadTask.addOnCompleteListener{
//            if (it.isSuccessful){
//                storageRef.downloadUrl.addOnCompleteListener{ dlURL ->
//                    if(dlURL.isSuccessful){
//                        val downloadURL = dlURL.result.toString()
//                        viewModel.currentUser.value!!.profile_image_url = downloadURL.toString()
//                        viewModel.db.collection("users").document(viewModel.currentUser.value!!.user_id).update("profile_image_url", downloadURL.toString())
//                    }else{
//                        Log.e(TAG, dlURL.exception.toString())
//                    }
//                }
//            }else{
//                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_LONG).show()
//            }
//        }
//
//    }

//    fun getPostsFromServer(callback: (MutableList<PostClass>) -> Unit){
//        Log.e(TAG, "contacting server for post List")
//        if(viewModel.currentLocation.value != null){
//            getLocalDocs(viewModel.currentLocation.value!!){ docs ->
//                val posts = buildPostList(docs)
//                //sorting posts by upvotes/hour
//                val vw = VoteWorker()
//                val postsSorted = posts.sortedWith(vw.postComparator)
//                callback.invoke(postsSorted.toMutableList())
//            }
//        }else{
//            getLocationUpdates()
//            viewModel.currentLocation.observe(this, Observer {
//                getLocalDocs(viewModel.currentLocation.value!!){ docs ->
//                    val posts = buildPostList(docs)
//                    //sorting posts by upvotes/hour
//                    val vw = VoteWorker()
//
//                    val postsSorted = posts.sortedWith(vw.postComparator)
//                    callback.invoke(postsSorted.toMutableList())
//                }
//            })
//        }
//    }
}
