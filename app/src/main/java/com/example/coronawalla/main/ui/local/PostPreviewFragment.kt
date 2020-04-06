package com.example.coronawalla.main.ui.local

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore


class PostPreviewFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val postViewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(PostViewModel::class.java) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel?.toolbarMode?.value = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = FirebaseFirestore.getInstance()
        val mAuth = FirebaseAuth.getInstance()
        val postTextView = view.findViewById<TextView>(R.id.postTV)
        val multiplierTV = view.findViewById<TextView>(R.id.multiplierTV)
        val postAsText = view.findViewById<TextView>(R.id.postAsTV)
        val postAsSwitch = view.findViewById<Switch>(R.id.postAsSwitch)
        val postButton = view.findViewById<Button>(R.id.postButton)
        postTextView.text = postViewModel?.postText?.value.toString()

        postAsSwitch.setOnClickListener {
            if(postAsSwitch.isChecked){
                postAsText.text = "Post as: User Name"
                multiplierTV.text = "2x"
            }else{
                postAsText.text = "Post as: Anonymous"
                multiplierTV.text = "1x"
            }
        }

        postButton.setOnClickListener {
            val postTime = System.currentTimeMillis()
            val upvotes = ArrayList<String>()
            upvotes.add(mAuth.currentUser!!.uid)
            val downvotes = ArrayList<String>()
            val currentGeoPoint = GeoPoint(viewModel!!.currentLocation.value!!.latitude, viewModel!!.currentLocation.value!!.longitude)
            val post = HashMap<String, Any>()
            post["mPostText"] = postViewModel?.postText?.value.toString()
            post["mPosterID"] = mAuth.currentUser!!.uid
            post["mPostGeoPoint"] = currentGeoPoint
            post["mVoteCount"] = 1
            post["mPostDateLong"] = postTime
            post["mMultiplier"] = 1
            post["mPayoutDateLong"] = postTime + 3600000*24
            post["mUpvoteIDs"] = upvotes
            post["mDownvoteIDs"] = downvotes
            post["mUserVote"] =  ""

            db.collection("posts").add(post).addOnCompleteListener{
                if (it.isSuccessful){
                    Log.d("Post Sent:: ", "Post Sent!")
                    val geoFirestore = GeoFirestore(db.collection("posts"))
                    db.collection("posts").document(it.result!!.id).update("mPostID", it.result!!.id)
                    geoFirestore.setLocation(it!!.result!!.id,currentGeoPoint)
                }else{
                    Log.e("error pushing:: ", it.exception.toString())
                }
            }

            //send post to server
            findNavController().navigate(R.id.action_postPreviewFragment_to_local)
        }


    }

}
