package com.example.coronawalla.main.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.LauncherActivity
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivity
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private val TAG: String? = ProfileFragment::class.simpleName

    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = -1

        val profImg:ImageView = requireView().findViewById(R.id.profile_iv)
        if(viewModel!!.currentProfileBitmap.value!=null){
            profImg.setImageBitmap(viewModel!!.currentProfileBitmap.value)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigation()

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val act = activity as MainActivity
        act.updateVMUserValues(uid)

        //get the users values
        //plug them into the profile
        viewModel!!.currentUser.observe(viewLifecycleOwner, Observer{
            updateProfileView(view, it)
        })

        util_sign_out_prof_Img()
    }

    private fun updateProfileView(view:View, currentUser:UserClass){
        Log.e(TAG, "update profile view")
        //set view items
        val handle = view.findViewById<TextView>(R.id.handle_tv)
        val nickname = view.findViewById<TextView>(R.id.username_tv)
        val postsCount = view.findViewById<TextView>(R.id.posts_tv)
        val karma = view.findViewById<TextView>(R.id.karma_tv)
        val followers = view.findViewById<TextView>(R.id.followers_tv)
        val following = view.findViewById<TextView>(R.id.following_tv)

        //handle text
        handle.text = "@"+currentUser.handle
        nickname.text = currentUser.username
        postsCount.text = currentUser.posts.size.toString()
        karma.text = currentUser.karma.toString()
        followers.text=currentUser.followers.size.toString()
        following.text = currentUser.following.size.toString()
    }

    private fun navigation(){
        val backToLocalImage = requireActivity().findViewById<ImageView>(R.id.right_button_iv)
        val toEditProfile = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        backToLocalImage.setOnClickListener { findNavController().navigate(R.id.action_profile_to_local) }
        toEditProfile.setOnClickListener { findNavController().navigate(R.id.action_profile_to_profileEditFragment) }
    }

    private fun util_sign_out_prof_Img(){
        val profpic = requireActivity().findViewById<ImageView>(R.id.profile_iv)
        profpic.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, LauncherActivity::class.java)
            startActivity(intent)
        }
    }



}
