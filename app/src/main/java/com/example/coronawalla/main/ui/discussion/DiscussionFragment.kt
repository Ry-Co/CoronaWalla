package com.example.coronawalla.main.ui.discussion

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.example.coronawalla.main.ui.local.PostClass
import com.google.firebase.firestore.FirebaseFirestore
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
        getCommentsFromServer(currentPost){commentsList->
            //recycler handeling
            comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
        }
        comments_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //todo add observer for comment changes?
        comments_refreshLayout.setOnRefreshListener {
            Log.e(TAG, "REFRESH-COMMENTS")
//            updatePostsServer()
//            val mA:MainActivity = activity as MainActivity
//            mA.updateLocalPostList(viewModel!!.currentLocation.value!!)
//            viewModel!!.localPostList.observe(viewLifecycleOwner, Observer{
//                posts_recyclerView.adapter = PostsRecyclerViewAdapter(it, findNavController())
//            })
            comments_refreshLayout.isRefreshing = false

        }
        setViewValues(view)
    }

    private fun setViewValues(v:View){
        val commentConfirmIV = v.findViewById<LinearLayout>(R.id.comment_confirm_iv)
        val currentCommentET = v.findViewById<EditText>(R.id.current_comment_et)
        val postText = v.findViewById<TextView>(R.id.post_text_tv)
        val postKarma = v.findViewById<TextView>(R.id.post_karma_tv)
        val postDuration = v.findViewById<TextView>(R.id.post_duration_tv)
        //val replyTV = v.findViewById<TextView>(R.id.reply_tv)
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


        shareTV.setOnClickListener {
            Log.e(TAG, "Share TV")
        }
        backButton.setOnClickListener {
            updateCommentsServer()
            findNavController().navigate(R.id.action_discussionFragment_to_local)
        }
        commentConfirmIV.setOnClickListener {
            val commentText = currentCommentET.text.toString()
            val postRef =FirebaseFirestore.getInstance().collection("posts").document(currentPost.post_id)
            if(commentText.isEmpty()){
                Toast.makeText(context, "Comments can't be empty!", Toast.LENGTH_SHORT).show()
            }else{
                // build comment and send text to server and refresh the layout after
                val commentMap = getCommentMap(commentText)
                postRef.collection("comments").add(commentMap).addOnCompleteListener { commentTask ->
                    if(commentTask.isSuccessful){
                        postRef.collection("comments").document(commentTask.result!!.id).update("comment_id", commentTask.result!!.id).addOnCompleteListener {
                            if(it.isSuccessful){
                                Log.d(TAG, "comment_id updated")
                                Toast.makeText(context, "Comments Sent!", Toast.LENGTH_SHORT).show()
                                currentCommentET.setText("")
                                hideKeyboard()
                                //todo refresh list
                            }else{
                                Log.e(TAG, it.exception.toString())
                            }
                        }
                    }else{
                        Log.e(TAG, commentTask.exception.toString())
                    }
                }

            }
        }
    }

    private fun updateCommentsServer(){
        val t = comments_recyclerView.adapter as CommentsRecyclerViewAdapter
        t.getChangedList()
        val db = FirebaseFirestore.getInstance()
        val oldCommentList = t.getChangedList()
        val batch = db.batch()

        val commentsColRef = db.collection("posts").document(currentPost.post_id).collection("comments")
        for(comment in oldCommentList){
            val docRef = commentsColRef.document(comment.comment_id)
            batch.update(docRef, "votes_map", comment.votes_map)
        }
        batch.commit().addOnCompleteListener{
            if(it.isSuccessful){
                Log.i(TAG,"comments updated!")
            }else{
                Log.e(TAG,it.exception.toString())
            }
        }

    }

    private fun getCommentsFromServer(currentPost: PostClass , callback:(MutableList<CommentClass>) -> Unit){
        val commentList = mutableListOf<CommentClass>()
        FirebaseFirestore.getInstance().collection("posts").document(currentPost.post_id).collection("comments").get().addOnCompleteListener {
            if(it.isSuccessful){
                for(item in it.result!!){
                    val comment = item.toObject(CommentClass::class.java)
                    commentList.add(comment)
                }
                callback.invoke(commentList)
            }else{
                Log.e(TAG, it.exception.toString())
                return@addOnCompleteListener
            }
        }
    }

    private fun getCommentMap(postText:String):CommentClass{
        val mVotes = mutableMapOf<String, Boolean?>()
        val uid = viewModel!!.currentUser.value!!.user_id
        val handle = viewModel!!.currentUser.value!!.handle.toString()
        mVotes[uid] = true

        return CommentClass(
            comment_id = "",
            comment_text = postText,
            votes_map = mVotes,
            commenter_id = uid,
            commenter_handle = handle
        )
    }

    // function triplet copy/paste for hiding keyboard functionality
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
