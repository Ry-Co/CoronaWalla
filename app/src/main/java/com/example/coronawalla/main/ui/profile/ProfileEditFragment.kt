package com.example.coronawalla.main.ui.profile

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions

//TODO: add background color selector a nd profile image selector
//TODO: turn this into an activity so the imagepicker works

class ProfileEditFragment : Fragment() {
    private val TAG: String? = ProfileEditFragment::class.simpleName
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    val IMAGE_REQUEST_CODE = 0x1
    private val permOptions = QuickPermissionsOptions(
        handleRationale = true,
        rationaleMessage = "Location permissions are required for core functionality!",
        handlePermanentlyDenied = true,
        permanentlyDeniedMessage = "Location permissions are needed for the core functionality of this app. Please enable these permissions to continue")

    override fun onPause() {
        super.onPause()
        //TODO: update user info from viewmodel
        viewModel!!.updateUserServer.value = true
    }

    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = -2
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //todo check if handle is available
        val profImg:ImageView = view.findViewById(R.id.profileImageViewEdit)
        val handleET: EditText = view.findViewById(R.id.handle_et)
        val usernameET:EditText = view.findViewById(R.id.username_et)
        val profEditTV = view.findViewById<TextView>(R.id.changeProfilePic_TV)
        profImg.setOnClickListener{
            getImageFromGallery()
        }
        profEditTV.setOnClickListener {
            getImageFromGallery()
        }
        viewModel!!.currentProfileBitmap.observe(viewLifecycleOwner, Observer{
            profImg.setImageBitmap(it)
        })



    }

    private fun getImageFromGallery(){
        ImagePicker.with(this)
            .galleryOnly()
            .cropSquare()
            .compress(1024)
            .start()
    }
}
