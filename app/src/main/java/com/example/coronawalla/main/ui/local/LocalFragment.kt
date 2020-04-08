package com.example.coronawalla.main.ui.local

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.fragment_local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import java.util.*
import kotlin.collections.ArrayList

class LocalFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val GEO_QUERY_RADIUS_IN_KM = 5 * 1.60934 //5 miles
    private val permOptions = QuickPermissionsOptions(
        handleRationale = true,
        rationaleMessage = "Location permissions are required for core functionality!",
        handlePermanentlyDenied = true,
        permanentlyDeniedMessage = "Location permissions are needed for the core functionality of this app. Please enable these permissions to continue")


    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = 0
        updatePostList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_local, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //posts = buildPostList(t!!)
        //recyclerView.adapter = RecyclerViewAdapter(buildPostList(t!!))
        viewModel!!.localPostList.observe(viewLifecycleOwner, Observer{
            recyclerView.adapter = RecyclerViewAdapter(it)
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        refreshLayout.setOnRefreshListener {
            updatePostList()
            viewModel!!.localPostList.observe(viewLifecycleOwner, Observer{
                recyclerView.adapter = RecyclerViewAdapter(it)
            })
            refreshLayout.isRefreshing = false
        }
    }

    private fun updatePostList(){
        getUsersCurrentLocation{loc ->
            getLocalDocs(loc){docs ->
                val posts = buildPostList(docs)
                viewModel!!.localPostList.value = posts
            }
        }
    }

    private fun getUsersCurrentLocation(callback:(Location) -> Unit) = runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, options = permOptions){
        val flp = LocationServices.getFusedLocationProviderClient(requireActivity())
        flp.lastLocation.addOnCompleteListener{
            if (it.isSuccessful){
                callback.invoke(it.result!!)
            }else{
                Log.e("TAG", it.exception.toString())
            }
        }
    }

    private fun getLocalDocs(loc:Location, callback: (List<DocumentSnapshot>) -> Unit){
        val usersGP = GeoPoint(loc.latitude, loc.longitude)
        val geoFirestore = GeoFirestore(FirebaseFirestore.getInstance().collection("posts"))
        geoFirestore.getAtLocation(usersGP,GEO_QUERY_RADIUS_IN_KM){docs, ex ->
            if(ex != null){
                Log.e("TAGG::", ex.message)
                return@getAtLocation
            }else{
                //viewModel!!.localDocList.value = docs!!
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

        var post = PostClass(
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

        return post
    }

}
