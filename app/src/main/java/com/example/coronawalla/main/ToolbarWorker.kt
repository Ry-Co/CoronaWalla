package com.example.coronawalla.main

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.coronawalla.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ToolbarWorker constructor(activity: Activity){
    private val TAG: String? = ToolbarWorker::class.simpleName

    private val mActivity: Activity = activity
    private val toolbarTitleTV = mActivity.findViewById<TextView>(R.id.toolbar_title_tv)
    private val toolbarSendTv = mActivity.findViewById<TextView>(R.id.toolbar_send_tv)
    private val toolbarCancelTv = mActivity.findViewById<TextView>(R.id.toolbar_cancel_tv)
    private val postIv = mActivity.findViewById<ImageView>(R.id.post_IV)
    private val editProfileIV = mActivity.findViewById<ImageView>(R.id.editProfile_IV)
    private val bottomNavigation =mActivity.findViewById<BottomNavigationView>(R.id.bottomNavigation)
    private val editProfileCancel = mActivity.findViewById<ImageView>(R.id.editProfile_cancel)
    private val editProfileConfirm = mActivity.findViewById<ImageView>(R.id.editProfile_confirm)

    fun switchBox(int:Int){
        when(int){
            1 -> roamToolbar() //roam
            0 -> localToolbar() //local
            -1 -> profileToolbar() //profile
            -2 -> profileEditToolbar() // profile edit
            2 -> postToolbar() //post creation toolbar
            3 -> postPreviewToolbar() //preview
        }
    }

    private fun profileEditToolbar(){
        Log.d(TAG, "Setting Toolbar to ProfileEdit")
        toolbarTitleTV.text = "Edit Profile"
        postIv.visibility = View.INVISIBLE
        editProfileIV.visibility = View.INVISIBLE
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        editProfileCancel.visibility = View.VISIBLE
        editProfileConfirm.visibility = View.VISIBLE
        editProfileConfirm.setOnClickListener {
            //send changes server side and close edit
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profileEditFragment_to_profile)
            //todo send update to server and update viewmodel
        }
        editProfileCancel.setOnClickListener{
            //close edit
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profileEditFragment_to_profile)
        }
    }
    private fun profileToolbar(){
        Log.d(TAG, "Setting Toolbar to Profile")
        toolbarTitleTV.text = "Profile"
        postIv.visibility = View.INVISIBLE
        editProfileIV.visibility = View.VISIBLE
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        editProfileCancel.visibility = View.INVISIBLE
        editProfileConfirm.visibility = View.INVISIBLE

        editProfileIV.setOnClickListener{
            Log.e(TAG, "Edit Profile!!!!")
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profile_to_profileEditFragment)
        }
    }
    private fun localToolbar(){
        Log.d(TAG, "Setting Toolbar to Local")
        toolbarTitleTV.text = "Local"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        editProfileIV.visibility = View.INVISIBLE
        postIv.visibility = View.VISIBLE
        editProfileCancel.visibility = View.INVISIBLE
        editProfileConfirm.visibility = View.INVISIBLE
        postIv.setOnClickListener{
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_local_to_postFragment)
            Toast.makeText(mActivity, "POST", Toast.LENGTH_SHORT).show()
        }
    }
    private fun roamToolbar(){
        Log.d(TAG, "Setting Toolbar to Roam")
        toolbarTitleTV.text = "Roam"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        postIv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        editProfileIV.visibility = View.INVISIBLE
        editProfileCancel.visibility = View.INVISIBLE
        editProfileConfirm.visibility = View.INVISIBLE

    }
    private fun postToolbar(){
        Log.d(TAG, "Setting Toolbar to Post")
        toolbarTitleTV.text = "Post"
        postIv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.INVISIBLE
        toolbarSendTv.visibility = View.VISIBLE
        toolbarCancelTv.visibility = View.VISIBLE
        editProfileIV.visibility = View.INVISIBLE
        editProfileCancel.visibility = View.INVISIBLE
        editProfileConfirm.visibility = View.INVISIBLE

        toolbarCancelTv.setOnClickListener {
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_postFragment_to_local)
        }
    }
    private fun postPreviewToolbar(){
        Log.d(TAG, "Setting Toolbar to Post-Preview")
        toolbarTitleTV.text = "Post"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        postIv.visibility = View.INVISIBLE
        editProfileIV.visibility = View.INVISIBLE
        editProfileCancel.visibility = View.INVISIBLE
        editProfileConfirm.visibility = View.INVISIBLE
    }
}