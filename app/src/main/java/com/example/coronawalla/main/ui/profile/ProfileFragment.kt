package com.example.coronawalla.main.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.coronawalla.LauncherActivity
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }

    lateinit var currentUser:UserClass

    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = -1
        if(viewModel?.currentUser?.value != null){
            currentUser = viewModel!!.currentUser.value!!
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storage = FirebaseStorage.getInstance().reference
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val userImageStorage = storage.child("images/$uid")

        val profileImageview = view.findViewById<CircularImageView>(R.id.profileImageViewEdit)
        val handleTV = view.findViewById<TextView>(R.id.handleTV)
        val nicknameTV = view.findViewById<TextView>(R.id.nicknameTV)
        val postsCountTV = view.findViewById<TextView>(R.id.postsTV)
        val karamTV = view.findViewById<TextView>(R.id.karmaTV)
        val followersTV = view.findViewById<TextView>(R.id.followersTV)
        val postRatioTV = view.findViewById<TextView>(R.id.ratioTV)
        val followingTV = view.findViewById<TextView>(R.id.followingTV)

        profileImageview.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent  = Intent(activity, LauncherActivity::class.java)
            startActivity(intent)
        }

        if(this::currentUser.isInitialized){
            handleTV.text = currentUser.handle
            nicknameTV.text = currentUser.username
            postsCountTV.text=currentUser.posts.size.toString()
            karmaTV.text = currentUser.karma.toString()
            followersTV.text = currentUser.followers_count.toString()
            followingTV.text = currentUser.following_count.toString()
            postRatioTV.text = currentUser.ratio.toString()

        }else {
            viewModel!!.currentUser.observe(viewLifecycleOwner, Observer {
                currentUser=it
                handleTV.text = currentUser.handle
                nicknameTV.text = currentUser.username
                postsCountTV.text=currentUser.posts.size.toString()
                karmaTV.text = currentUser.karma.toString()
                followersTV.text = currentUser.followers_count.toString()
                followingTV.text = currentUser.following_count.toString()
                postRatioTV.text = currentUser.ratio.toString()
            })
        }

        viewModel!!.currentProfileBitmap.observe(viewLifecycleOwner, Observer{
            profileImageview.setImageBitmap(it)
        })

        if(viewModel!!.currentUser.value!!.profile_image_url != null){
            val url =viewModel!!.currentUser.value!!.profile_image_url.toString()

            //TODO: add a .placeholder at some point
        }

    }
}
