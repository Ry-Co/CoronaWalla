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
    private val TAG: String? = PostPreviewFragment::class.simpleName

    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val postViewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(PostViewModel::class.java) }
    }
    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel?.toolbarMode?.value = 3
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postTextView = view.findViewById<TextView>(R.id.postTV)
        val multiplierTV = view.findViewById<TextView>(R.id.multiplierTV)
        val postAsText = view.findViewById<TextView>(R.id.postAsTV)
        val postAsSwitch = view.findViewById<Switch>(R.id.postAsSwitch)
        val postButton = view.findViewById<Button>(R.id.postButton)
        val currentGeoPoint = GeoPoint(viewModel!!.currentLocation.value!!.latitude, viewModel!!.currentLocation.value!!.longitude)
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
            val post = getPostMap()
            db.collection("posts").add(post).addOnCompleteListener{ postTask ->
                if (postTask.isSuccessful){
                    Log.d(TAG, "Post Sent!")
                    val geoFirestore = GeoFirestore(db.collection("posts"))
                    db.collection("posts").document(postTask.result!!.id).update("post_id", postTask.result!!.id).addOnCompleteListener{
                        if(it.isSuccessful){
                            Log.i(TAG, "Updated mPostID")
                        }else{
                            Log.e(TAG, it.exception.toString())
                        }
                    }
                    geoFirestore.setLocation(postTask!!.result!!.id,currentGeoPoint)
                }else{
                    Log.e(TAG, "Error:: "+postTask.exception.toString())
                }
            }
            findNavController().navigate(R.id.action_postPreviewFragment_to_local)
        }
    }

    private fun getPostMap(): PostClass{
        val currentGeoPoint = GeoPoint(viewModel!!.currentLocation.value!!.latitude, viewModel!!.currentLocation.value!!.longitude)
        val postTime = System.currentTimeMillis()
        val mVotes = mutableMapOf<String, Boolean?>()
        mVotes[mAuth.currentUser!!.uid] = true
        return PostClass(
            post_id = "",
            post_text = postViewModel?.postText?.value.toString(),
            poster_id = mAuth.currentUser!!.uid,
            active = true,
            post_geo_point = currentGeoPoint,
            post_date_long = postTime,
            post_multiplier = 1,
            payout_date_long = postTime + 3600000*24,
            votes_map = mVotes
        )
    }
}
