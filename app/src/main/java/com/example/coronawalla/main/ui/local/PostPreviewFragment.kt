package com.example.coronawalla.main.ui.local

import android.os.Bundle
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


class PostPreviewFragment : Fragment() {

    private val postViewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(PostViewModel::class.java) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            findNavController().navigate(R.id.action_postPreviewFragment_to_local)
        }


    }

}
