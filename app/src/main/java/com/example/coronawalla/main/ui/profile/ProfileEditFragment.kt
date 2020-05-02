package com.example.coronawalla.main.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.ByteArrayOutputStream

//TODO: add background color selector a nd profile image selector
class ProfileEditFragment : Fragment() {
    private val TAG: String? = ProfileEditFragment::class.simpleName

    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    val IMAGE_REQUEST_CODE = 0x1

    override fun onPause() {
        super.onPause()

        //TODO: update user info from viewmodel

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
        val storage = FirebaseStorage.getInstance().reference
        val uid = viewModel!!.currentUser.value!!.mUserID

        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE){
            profileImageViewEdit.setImageURI(data?.data)
            val userImageStorage = storage.child("images/$uid")
            val bitmap = (profileImageViewEdit.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageData = baos.toByteArray()
            val uploadTask = userImageStorage.putBytes(imageData)

            uploadTask.addOnCompleteListener{
                if(it.isSuccessful){
                    Log.d(TAG,"Image is uploaded!")
                    viewModel!!.currentUser.value!!.mProfileImageURL = userImageStorage.downloadUrl.result.toString()
                }else{
                    Log.e(TAG, it.exception.toString())
                }
            }

        }
    }


}
