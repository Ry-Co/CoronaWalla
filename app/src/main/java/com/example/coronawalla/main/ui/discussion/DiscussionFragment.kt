package com.example.coronawalla.main.ui.discussion

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.example.coronawalla.main.ui.local.PostClass
import kotlinx.android.synthetic.main.fragment_discussion.*

/***
 * to bold username/@_other_user
 * https://stackoverflow.com/questions/14371092/how-to-make-a-specific-text-on-textview-bold
 */
class DiscussionFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private lateinit var currentPost: PostClass
    private val TAG: String? = DiscussionFragment::class.simpleName


    override fun onResume() {
        super.onResume()
        viewModel!!.toolbarMode.value = -3
    }

    override fun onPause() {
        super.onPause()
        if(comments_recyclerView.adapter != null){
            updateCommentsServer()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discussion, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentPost = arguments?.get("post") as PostClass

        setViewValues(view)

    }

    private fun setViewValues(v:View){
        val postText = v.findViewById<TextView>(R.id.post_text_tv)
        val postKarma = v.findViewById<TextView>(R.id.post_karma_tv)
        val postDuration = v.findViewById<TextView>(R.id.post_duration_tv)
        val replyTV = v.findViewById<TextView>(R.id.report_tv)
        val shareTV = v.findViewById<TextView>(R.id.share_tv)
        val upvoteIV = v.findViewById<ImageView>(R.id.disc_upvote_iv)
        val downvoteIV = v.findViewById<ImageView>(R.id.disc_downvote_iv)
        val backButton = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        //set vote status


        //set post values
        var voteCount = 0
        if(currentPost.votes_map!!.isEmpty()){
            //do nothing
        }else{
            var counter = 0
            for(item in currentPost.votes_map!!){
                if(item.value == true){
                    counter += 1
                }
            }
            voteCount = counter
        }
        postKarma.text = voteCount.toString()

        val ageHours = (System.currentTimeMillis() - currentPost.post_date_long) / 3600000 // milliseconds per hour
        postDuration.text = ageHours.toString()+"h"

        postText.text = currentPost.post_text


        replyTV.setOnClickListener {
            Log.e(TAG, "Report TV")
        }
        shareTV.setOnClickListener {
            Log.e(TAG, "Share TV")
        }
        backButton.setOnClickListener {
            //update the post list locally and on the server
            findNavController().navigate(R.id.action_discussionFragment_to_local)
        }
    }

    private fun updateCommentsServer(){

    }
}
