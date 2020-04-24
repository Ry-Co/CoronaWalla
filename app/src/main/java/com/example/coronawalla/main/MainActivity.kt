package com.example.coronawalla.main

import android.Manifest
import android.location.Location
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
import com.example.coronawalla.main.ui.local.PostClass
import com.example.coronawalla.main.ui.profile.UserClass
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
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
    private val TAG: String? = MainActivity::class.simpleName

    private val viewModel by lazy{
        this.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val permOptions = QuickPermissionsOptions(
        handleRationale = true,
        rationaleMessage = "Location permissions are required for core functionality!",
        handlePermanentlyDenied = true,
        permanentlyDeniedMessage = "Location permissions are needed for the core functionality of this app. Please enable these permissions to continue")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val navController = findNavController(R.id.main_nav_host_fragment)
        bottomNavigation.setupWithNavController(navController)
        updatePostList()
        getCurrentUser()
        viewModel.toolbarMode.observe(this, Observer {
            when(viewModel.toolbarMode.value){
                 1 -> roamToolbar() //roam
                 0 -> localToolbar() //local
                -1 -> profileToolbar() //profile
                 2 -> postToolbar() //post creation toolbar
                 3 -> postPreviewToolbar()
            }
        })
    }


    //TODO: make profile edit toolbar, with check in top right and X in top left

    private fun profileToolbar(){
        Log.d(TAG, "Setting Toolbar to Profile")
        toolbar_title_tv.text = "Profile"
        post_IV.visibility = View.INVISIBLE
        editProfile_IV.visibility = View.VISIBLE
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE

        editProfile_IV.setOnClickListener{
            Log.e(TAG, "Edit Profile!!!!")
        }
    }
    private fun localToolbar(){
        Log.d(TAG, "Setting Toolbar to Local")
        toolbar_title_tv.text = "Local"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        editProfile_IV.visibility = View.INVISIBLE
        post_IV.visibility = View.VISIBLE
        post_IV.setOnClickListener{
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_local_to_postFragment)
            Toast.makeText(this, "POST", Toast.LENGTH_SHORT).show()
        }
    }
    private fun roamToolbar(){
        Log.d(TAG, "Setting Toolbar to Roam")
        toolbar_title_tv.text = "Roam"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        post_IV.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        editProfile_IV.visibility = View.INVISIBLE

    }
    private fun postToolbar(){
        Log.d(TAG, "Setting Toolbar to Post")
        toolbar_title_tv.text = "Post"
        post_IV.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.INVISIBLE
        toolbar_send_tv.visibility = View.VISIBLE
        toolbar_cancel_tv.visibility = View.VISIBLE
        editProfile_IV.visibility = View.INVISIBLE

        toolbar_cancel_tv.setOnClickListener {
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_postFragment_to_local)
        }
    }
    private fun postPreviewToolbar(){
        Log.d(TAG, "Setting Toolbar to Post-Preview")
        toolbar_title_tv.text = "Post"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        post_IV.visibility = View.INVISIBLE
        editProfile_IV.visibility = View.INVISIBLE


    }

    public fun getCurrentUser(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                viewModel.currentUser.value = buildUserObject(it.result!!)
            }else{
                Log.d(TAG, "Error:: "+it.exception)
            }
        }
    }

    public fun updatePostList(){
         getUsersCurrentLocation{loc ->
            getLocalDocs(loc){docs ->
                val posts = buildPostList(docs)
                viewModel.currentLocation.value = loc
                viewModel.localPostList.value = posts
            }
        }
    }

    private fun getUsersCurrentLocation(callback:(Location) -> Unit) = runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, options = permOptions){
        val flp = LocationServices.getFusedLocationProviderClient(this)
        flp.lastLocation.addOnCompleteListener{
            if (it.isSuccessful){
                callback.invoke(it.result!!)
            }else{
                Log.e(TAG, "Error:: "+it.exception.toString())
            }
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
            val p = buildPostObject(d)
            out.add(p)
        }
        return out
    }

    private fun buildPostObject(docSnap : DocumentSnapshot): PostClass {
        val mAuth = FirebaseAuth.getInstance()
        var userVote : Boolean? = null
        var list :ArrayList<String> = docSnap.get("mUpvoteIDs") as ArrayList<String>
        val upvotes = list.toHashSet()
        list = docSnap.get("mDownvoteIDs") as ArrayList<String>
        val downvotes = list.toHashSet()
        userVote = if(!upvotes.contains(mAuth.currentUser!!.uid) && !downvotes.contains(mAuth.currentUser!!.uid)) { null
        }else upvotes.contains(mAuth.currentUser!!.uid)

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
        val namedPosts:Long = docSnap.get("mNamedPostCount") as Long
        val anonPosts:Long = docSnap.get("mAnonPostCount") as Long
        val followerCount:Long = docSnap.get("mFollowersCount") as Long
        val followingCount:Long = docSnap.get("mFollowingCount") as Long
        val karmaCount:Long = docSnap.get("mKarmaCount") as Long
        val ratio = namedPosts.toDouble()/(namedPosts.toDouble()+anonPosts.toDouble()) as Double


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
            mAuthUserObject = userObject
        )
    }
}
