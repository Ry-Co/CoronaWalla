package com.example.coronawalla.main.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders

import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

//TODO: add background color selector and profile image selector
class ProfileEditFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    val IMAGE_REQUEST_CODE = 0x1

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
        val profileEdit = view.findViewById<ImageView>(R.id.profileImageViewEdit)
        val profEditTV = view.findViewById<TextView>(R.id.changeProfilePic_TV)
        profileEdit.setOnClickListener{
            getImageFromGallery()
        }
        profEditTV.setOnClickListener {
            getImageFromGallery()
        }
    }

    private fun getImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE){
            profileImageViewEdit.setImageURI(data?.data)
        }
    }
}
