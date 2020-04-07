package com.example.coronawalla.main.ui.local

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivity
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import java.util.*
import kotlin.collections.ArrayList

class LocalFragment : Fragment() {
    private var posts = ArrayList<PostClass>()
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = 0

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_local, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel!!.localDocList.observe(viewLifecycleOwner, object:Observer,
            androidx.lifecycle.Observer<List<DocumentSnapshot>> {
            override fun update(p0: Observable?, p1: Any?) {
            }
            override fun onChanged(t: List<DocumentSnapshot>?) {
                //TODO: we need to add a loading screen somewhere in here
                Log.e("LIST UPDATED:: ", t.toString())
                posts = buildPostList(t!!)
                recyclerView.adapter = RecyclerViewAdapter(buildPostList(t!!))
            }
        })


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //TODO: need to implement proper refresh functionality, maybe viewmodel switches
        refreshLayout.setOnRefreshListener {
            recyclerView.adapter = RecyclerViewAdapter(fetchPosts()) }
    }

    private fun fetchPosts():ArrayList<PostClass>{
        return posts
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
