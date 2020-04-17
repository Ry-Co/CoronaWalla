package com.example.coronawalla.main.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.example.coronawalla.LauncherActivity

import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView


class ProfileFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = -1
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
        val profileImageview = view.findViewById<CircularImageView>(R.id.profileImageView)
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
    }

}
