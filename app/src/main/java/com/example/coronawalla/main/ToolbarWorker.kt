package com.example.coronawalla.main

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PorterDuff
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import com.example.coronawalla.R
import com.example.coronawalla.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * We will handle visual changes here, but handle user input locally in fragment
 */

class ToolbarWorker constructor(activity: Activity){
    private val TAG: String? = ToolbarWorker::class.simpleName
    private val mActivity: Activity = activity
    private val toolbarTitleTV = mActivity.findViewById<TextView>(R.id.toolbar_title_tv)
    private val toolbarSendTv = mActivity.findViewById<TextView>(R.id.toolbar_send_tv)
    private val toolbarCancelTv = mActivity.findViewById<TextView>(R.id.toolbar_cancel_tv)
    private val rightImageButton =mActivity.findViewById<ImageView>(R.id.right_button_iv)
    private val leftImageButton =mActivity.findViewById<ImageView>(R.id.left_button_iv)

    fun switchBox(int:Int){
        when(int){
            3 -> postPreviewToolbar() //preview
            2 -> postToolbar() //post creation toolbar
            1 -> roamToolbar() //roam
            0 -> localToolbar() //local
            -1 -> profileToolbar() //profile
            -2 -> profileEditToolbar() // profile edit
            -3 -> discussionToolbar()
        }
    }

    private fun localToolbar(){
        Log.d(TAG, "Setting Toolbar to Local")
        toolbarTitleTV.text = "Curr. Town"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        rightImageButton.setImageResource(R.drawable.ic_create_white_24dp)
        leftImageButton.setImageResource(R.drawable.ic_person_white_24dp)
        rightImageButton.visibility = View.VISIBLE
        leftImageButton.visibility = View.VISIBLE
    }

    private fun profileToolbar(){
        Log.d(TAG, "Setting Toolbar to Profile")
        toolbarTitleTV.text = "Profile"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        leftImageButton.setImageResource(R.drawable.ic_settings_white_24dp)
        rightImageButton.setImageResource(R.drawable.ic_arrow_forward_white_24dp)
        leftImageButton.visibility = View.VISIBLE
        rightImageButton.visibility = View.VISIBLE
    }

    private fun profileEditToolbar(){
        Log.d(TAG, "Setting Toolbar to ProfileEdit")
        toolbarTitleTV.text = "Edit Profile"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        rightImageButton.setImageResource(R.drawable.ic_check_white_24dp)
        leftImageButton.setImageResource(R.drawable.ic_clear_white_24dp)
        rightImageButton.visibility = View.VISIBLE
        leftImageButton.visibility = View.VISIBLE
    }
    private fun roamToolbar(){
        Log.d(TAG, "Setting Toolbar to Roam")
        toolbarTitleTV.text = "Roam"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
    }

    private fun discussionToolbar(){
        Log.d(TAG, "Setting Toolbar to Discussion")
        toolbarTitleTV.text = "Discussion"
        rightImageButton.visibility = View.INVISIBLE
        leftImageButton.setImageResource(R.drawable.ic_arrow_back_white_24dp)
        leftImageButton.visibility = View.VISIBLE
    }
    private fun postToolbar(){
        Log.d(TAG, "Setting Toolbar to Post")
        toolbarTitleTV.text = "Post"
        toolbarSendTv.visibility = View.VISIBLE
        toolbarCancelTv.visibility = View.VISIBLE
        rightImageButton.visibility = View.INVISIBLE
        leftImageButton.visibility = View.INVISIBLE
    }
    private fun postPreviewToolbar(){
        Log.d(TAG, "Setting Toolbar to Post-Preview")
        toolbarTitleTV.text = "Post"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
    }

    //todo maybe remove
    fun buttonEffect(button: View) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background.setColorFilter(-0x1f0b8adf, PorterDuff.Mode.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            false
        }
    }

}