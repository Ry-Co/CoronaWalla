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
import com.example.coronawalla.main.VoteWorker
import com.example.coronawalla.main.ui.local.PostClass
import com.google.firebase.auth.FirebaseAuth
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
    private val changedPosts = ArrayList<PostClass>()
    private lateinit var ph: String
    private var usersVote:Boolean? = null
    private val TAG: String? = DiscussionFragment::class.simpleName

    override fun onResume() {
        super.onResume()
        viewModel!!.toolbarMode.value = -3
    }

    override fun onPause() {
        super.onPause()
        if(comments_recyclerView.adapter != null){
            updateCommentsServer{}
        }
        updatePostServer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentPost = arguments?.get("post") as PostClass
        ph = arguments?.get("posterHandle") as String
        recyclerHandling()
        setViewValues(view)
    }

    private fun setViewValues(v:View){
        navigation(v)
        voting(v)
        commenting(v)
        //declare and set post values
        val postText = v.findViewById<TextView>(R.id.post_text_tv)
        val posterHandle = v.findViewById<TextView>(R.id.disc_posters_handle_tv)
        posterHandle.text = ph
        postText.text = currentPost.post_text
    }

    private fun updateCommentsServer(callback:(Boolean) -> Unit){
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
                callback.invoke(true)
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
                val myCustomComparator =  Comparator<CommentClass> { a, b ->
                    val vw = VoteWorker()
                    val aRate = vw.getVoteCount(a.votes_map!!)
                    val bRate = vw.getVoteCount(b.votes_map!!)
                    //Log.e(TAG, "a ="+a.post_id+" b ="+b.post_id+" aRate= $aRate  bRate= $bRate")
                    when {
                        aRate == bRate -> {
                            //Log.e(TAG, "EQUAL")
                            0
                        }
                        aRate < bRate -> {
                            //Log.e(TAG, "a < b")
                            1
                        }
                        else -> {
                            //Log.e(TAG, "a > b")
                            -1
                        }
                    }
                }
                val commentsSorted = commentList.sortedWith(myCustomComparator)

                callback.invoke(commentsSorted.toMutableList())
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

    private fun updatePostServer(){
        val db = FirebaseFirestore.getInstance()
        if(changedPosts.size > 0){
            val post = changedPosts[0]
            db.collection("posts").document(post.post_id).update("votes_map", post.votes_map)
        }
    }

    private fun navigation(v:View){
        val shareTV = v.findViewById<TextView>(R.id.share_tv)
        val backButton = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        shareTV.setOnClickListener {
            Log.e(TAG, "Share TV")
        }
        backButton.setOnClickListener {
            updateCommentsServer{}
            findNavController().navigate(R.id.action_discussionFragment_to_local)
        }
    }

    private fun voting(v:View){
        val voteWorker = VoteWorker()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val upvoteIV = v.findViewById<ImageView>(R.id.disc_upvote_iv)
        val downvoteIV = v.findViewById<ImageView>(R.id.disc_downvote_iv)
        val postKarma = v.findViewById<TextView>(R.id.post_karma_tv)
        //including postDuration here because it uses the voteWorker util function getAgeString()
        val postDuration = v.findViewById<TextView>(R.id.post_duration_tv)


        var voteCount = voteWorker.getVoteCount(currentPost.votes_map!!)
        postKarma.text = voteCount.toString()
        var prevVote = voteWorker.getPrevVote(uid, currentPost.votes_map!!)
        voteWorker.voteVisual(upvoteIV, downvoteIV, prevVote)
        postDuration.text = voteWorker.getAgeString(currentPost.post_date_long)


        upvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, true, upvoteIV, downvoteIV)
            currentPost.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentPost.votes_map!!)
            val voteCountString = voteWorker.getVoteCount(currentPost.votes_map!!).toString()
            postKarma.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedPosts.add(currentPost)
        }
        downvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, false,upvoteIV,downvoteIV)
            currentPost.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentPost.votes_map!!)
            val voteCountString = voteWorker.getVoteCount(currentPost.votes_map!!).toString()
            postKarma.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedPosts.add(currentPost)
        }

    }

    private fun commenting(v:View){
        val commentConfirmIV = v.findViewById<LinearLayout>(R.id.comment_confirm_iv)
        val currentCommentET = v.findViewById<EditText>(R.id.current_comment_et)
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

    private fun recyclerHandling(){
        getCommentsFromServer(currentPost){commentsList->
            //recycler handeling
            comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
        }
        comments_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        comments_refreshLayout.setOnRefreshListener {
            Log.e(TAG, "REFRESH-COMMENTS")
            updateCommentsServer {
                getCommentsFromServer(currentPost){commentsList->
                    //recycler handeling
                    comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
                    comments_refreshLayout.isRefreshing = false

                }
            }

        }
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
