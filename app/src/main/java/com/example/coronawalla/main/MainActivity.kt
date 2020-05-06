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
import android.view.View
import android.widget.ImageView
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
import kotlinx.android.synthetic.main.toolbar.*
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import java.io.ByteArrayOutputStream

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
        //updateCurrentUserValues()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        val navController = findNavController(R.id.main_nav_host_fragment)
        bottomNavigation.setupWithNavController(navController)

        flp = LocationServices.getFusedLocationProviderClient(this)
        getLocationUpdates()
        getCurrentUser()

        viewModel.currentLocation.observe(this, Observer{ loc ->
            Log.e(TAG,"location updated")
            updateLocalPostList(loc)
        })

        val tb = ToolbarWorker(this)
        viewModel.toolbarMode.observe(this, Observer {
            tb.switchBox(it)
        })
    }

    private fun getCurrentUser(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                viewModel.currentUser.value = buildUserObject(it.result!!)
            }else{
                Log.d(TAG, "Error:: "+it.exception)
            }
        }
    }

    fun updateLocalPostList(loc:Location){
        getLocalDocs(loc){docs ->
            val posts = buildPostList(docs)
            viewModel.localPostList.value = posts
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
            val p = buildPostObject(d)
            out.add(p)
        }
        return out
    }

    private fun buildPostObject(docSnap : DocumentSnapshot): PostClass {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        var userVote : Boolean? = null
        var list :ArrayList<String> = docSnap.get("mUpvoteIDs") as ArrayList<String>
        val upvotes = list.toHashSet()
        list = docSnap.get("mDownvoteIDs") as ArrayList<String>
        val downvotes = list.toHashSet()
        userVote = if(!upvotes.contains(uid) && !downvotes.contains(uid)) { null
        }else upvotes.contains(uid)

        val voteCountLong:Long = docSnap.get("mVoteCount") as Long
        val mMultiplerLong:Long = docSnap.get("mMultiplier") as Long

        return PostClass(
            mPostID = docSnap.id,
            mPostText = docSnap.get("mPostText") as String,
            mPosterID = docSnap.get("mPosterID") as String,
            mPostGeoPoint= docSnap.get("mPostGeoPoint") as GeoPoint,
            mVoteCount = voteCountLong.toInt(),
            mPostDateLong = docSnap.get("mPostDateLong") as Long,
            mMultiplier = mMultiplerLong.toInt(),
            mPayoutDateLong = docSnap.get("mPayoutDateLong") as Long,
            mUpvoteIDs = upvotes,
            mDownvoteIDs = downvotes,
            mUserVote = userVote
        )
    }

    private fun buildUserObject(docSnap:DocumentSnapshot): UserClass? {
        var profImageURL:String? = null
        val namedPosts:Long = docSnap.get("mNamedPostCount") as Long
        val anonPosts:Long = docSnap.get("mAnonPostCount") as Long
        val followerCount:Long = docSnap.get("mFollowersCount") as Long
        val followingCount:Long = docSnap.get("mFollowingCount") as Long
        val karmaCount:Long = docSnap.get("mKarmaCount") as Long
        val ratio = namedPosts.toDouble()/(namedPosts.toDouble()+anonPosts.toDouble()) as Double
        if(docSnap.get("mProfileImageURL") != null){
            profImageURL = docSnap.get("mProfileImageURL") as String
        }


        val userObject = docSnap.get("mAuthUser") as Map<*, *>
        return UserClass(
            mHandle = docSnap.get("mHandle") as String,
            mUsername = docSnap.get("mUserName") as String,
            mUserID = docSnap.id as String,
            mPostsCount = namedPosts.toInt()+anonPosts.toInt(),
            mKarmaCount = karmaCount.toInt(),
            mFollowerCount = followerCount.toInt(),
            mFollowingCount = followingCount.toInt(),
            mNamedPostCount = namedPosts.toInt(),
            mAnonPostCount = anonPosts.toInt(),
            mRatio = "%.3f".format(ratio).toDouble(),
            mAuthUserObject = userObject,
            mProfileImageURL = profImageURL
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val db = FirebaseFirestore.getInstance()
            val uid = viewModel.currentUser.value!!.mUserID
            val storageRef = FirebaseStorage.getInstance().reference.child("images/$uid")

            //Image Uri will not be null for RESULT_OK
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

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
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
                //todo: update viewmodel for user with their image url
                viewModel.currentUser.value!!.mProfileImageURL = downloadURL.toString()
            }else{
                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateCurrentUserValues(){
        val dbUserRef = FirebaseFirestore.getInstance().collection("users").document(viewModel.currentUser.value!!.mUserID)
        val mAuth = FirebaseAuth.getInstance()
        val currentUser = viewModel.currentUser.value
        val activePostMap = HashMap<String, String>()
        //update all users fields
        val userUpdate = HashMap<String, Any>()
        userUpdate["mActivePosts"] = activePostMap
        userUpdate["mAuthUser"] = mAuth.currentUser!!
        userUpdate["mHandle"] = currentUser!!.mHandle
        userUpdate["mUserName"] = currentUser.mUsername
        userUpdate["mKarmaCount"] = currentUser.mKarmaCount
        userUpdate["mFollowersCount"] = currentUser.mFollowerCount
        userUpdate["mFollowingCount"] = currentUser.mFollowingCount
        userUpdate["mNamedPostCount"] = currentUser.mNamedPostCount
        userUpdate["mAnonPostCount"] = currentUser.mAnonPostCount
        userUpdate["mRatio"] = currentUser.mRatio
        userUpdate["mProfileImageURL"] = currentUser.mProfileImageURL.toString()

        dbUserRef.update(userUpdate).addOnCompleteListener{
            if(it.isSuccessful){
                Log.i(TAG,"UserUpdateSuccessful")
            }else{
                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}
