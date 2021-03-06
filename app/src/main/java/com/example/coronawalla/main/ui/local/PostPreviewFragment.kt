package com.example.coronawalla.main.ui.local

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.example.coronawalla.main.ServerWorker
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore


class PostPreviewFragment : Fragment() {
    private val TAG: String? = PostPreviewFragment::class.simpleName
    private lateinit var viewModel:MainActivityViewModel
    private val sw = ServerWorker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =  ViewModelProvider(this.requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.toolbarMode.value = 3
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postText = arguments?.get("postText") as String
        val postAsUserHandle = view.findViewById<TextView>(R.id.user_handle_tv)
        val postTextView = view.findViewById<TextView>(R.id.postTV)
        val anonButton = view.findViewById<Button>(R.id.anon_post_button)
        val namedButton = view.findViewById<Button>(R.id.named_post_button)
        postTextView.text = postText
        postAsUserHandle.text = "Post as "+viewModel.currentUser.value!!.handle

        namedButton.setOnClickListener {
            val post = getPostMap(postText, 2, false)
            sw.addPostToServer(post){
                if(it){
                    val bundle = bundleOf("refresh" to true)
                    findNavController().navigate(R.id.action_postPreviewFragment_to_local, bundle)
                }else{
                    Toast.makeText(this.requireContext(), "There was an error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        anonButton.setOnClickListener {
            val post = getPostMap(postText, 1, true)
            sw.addPostToServer(post){
                if(it){
                    val bundle = bundleOf("refresh" to true)
                    findNavController().navigate(R.id.action_postPreviewFragment_to_local, bundle)
                }else{
                    Toast.makeText(this.requireContext(), "There was an error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPostMap(postText:String, multiplier:Int, anon:Boolean): PostClass{
        val mAuth = sw.mAuth
        //val currentGeoPoint = GeoPoint(viewModel.currentLocation.value!!.latitude, viewModel.currentLocation.value!!.longitude)
        val latitude = viewModel.currentLocation.value!!.latitude
        val longitude = viewModel.currentLocation.value!!.longitude
        val postTime = System.currentTimeMillis()
        val mVotes = mutableMapOf<String, Boolean?>()
        mVotes[mAuth.currentUser!!.uid] = true
        return PostClass(
            post_id = "",
            post_text = postText,
            poster_id = mAuth.currentUser!!.uid,
            active = true,
            post_longitude = longitude,
            post_latitude = latitude,
            post_anon = anon,
            //post_geo_point = currentGeoPoint,
            post_date_long = postTime,
            post_multiplier = multiplier,
            payout_date_long = postTime + 3600000*24,
            votes_map = mVotes
        )
    }
}
