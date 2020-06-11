package com.example.coronawalla.main.ui.discussion

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivity
import com.example.coronawalla.main.MainActivityViewModel
import com.example.coronawalla.main.ServerWorker
import com.example.coronawalla.main.VoteWorker
import com.example.coronawalla.main.ui.local.PostClass
import io.grpc.Server
import kotlinx.android.synthetic.main.anon_named_dialog.*
import kotlinx.android.synthetic.main.fragment_discussion.*

/***
 * to bold username/@_other_user
 * https://stackoverflow.com/questions/14371092/how-to-make-a-specific-text-on-textview-bold
 */
class DiscussionFragment : Fragment() {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var currentPost: PostClass
    private val changedPosts = ArrayList<PostClass>()
    private lateinit var ph: String
    private var usersVote:Boolean? = null
    private lateinit var sw:ServerWorker
    val vw = VoteWorker()
    private val TAG: String? = DiscussionFragment::class.simpleName

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sw = ServerWorker()
    }

    override fun onResume() {
        super.onResume()
        viewModel.toolbarMode.value = -3
    }

    override fun onPause() {
        super.onPause()
        if(comments_recyclerView.adapter != null){
            //updateCommentsServer{}
            val t = comments_recyclerView.adapter as CommentsRecyclerViewAdapter
            sw.updateCommentsOnServer(t.getChangedList(), currentPost.post_id){}
        }
        //updatePostServer()
        if(changedPosts.size>0){
            val post = changedPosts[0]
            sw.updateIndividualPost(post.post_id, post.votes_map)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =  ViewModelProvider(this.requireActivity()).get(MainActivityViewModel::class.java)

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
        //commenting
        val commentConfirmIV = v.findViewById<LinearLayout>(R.id.comment_confirm_iv)
        commentConfirmIV.setOnClickListener {
            val currentCommentET = v.findViewById<EditText>(R.id.current_comment_et)
            val commentText = currentCommentET.text.toString()
            if(commentText.isEmpty()){
                Toast.makeText(context, "Comments can't be empty!", Toast.LENGTH_SHORT).show()
            }else{
                anonDialogHandeling(v)
            }
        }
        //declare and set post values
        val postShareTV = v.findViewById<TextView>(R.id.share_tv)
        val postText = v.findViewById<TextView>(R.id.post_text_tv)
        val posterHandle = v.findViewById<TextView>(R.id.disc_posters_handle_tv)
        posterHandle.text = ph
        postText.text = currentPost.post_text

        postShareTV.setOnClickListener {
            //abbreviate post text, put it as title
            //put quotes around post text
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareBody = "'"+currentPost.post_text+"'"+"- posted on soapBox, join the conversation @ [playstoreLink]"
            val abbrString = currentPost.post_text.take(38) + "..."
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, abbrString)
            startActivity(Intent.createChooser(shareIntent, "Share this post"))
        }
    }

    private fun getCommentMap(postText:String, anon:Boolean):CommentClass{
        val mVotes = mutableMapOf<String, Boolean?>()
        val uid = viewModel.currentUser.value!!.user_id
        val handle = viewModel.currentUser.value!!.handle.toString()
        val commentLong = System.currentTimeMillis()
        mVotes[uid] = true

        return CommentClass(
            comment_id = "",
            comment_text = postText,
            comment_anon = anon,
            votes_map = mVotes,
            comment_date_long = commentLong,
            commenter_id = uid,
            commenter_handle = handle
        )
    }

    private fun navigation(v:View){
        val shareTV = v.findViewById<TextView>(R.id.share_tv)
        val backButton = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        shareTV.setOnClickListener {
            Log.e(TAG, "Share TV")
        }
        backButton.setOnClickListener {
            val t = comments_recyclerView.adapter as CommentsRecyclerViewAdapter
            sw.updateCommentsOnServer(t.getChangedList(), currentPost.post_id){}
            findNavController().navigate(R.id.action_discussionFragment_to_local)
        }
    }

    private fun voting(v:View){
        val uid = sw.mAuth.currentUser!!.uid
        val upvoteIV = v.findViewById<ImageView>(R.id.disc_upvote_iv)
        val downvoteIV = v.findViewById<ImageView>(R.id.disc_downvote_iv)
        val postKarma = v.findViewById<TextView>(R.id.post_karma_tv)
        //including postDuration here because it uses the voteWorker util function getAgeString()
        val postDuration = v.findViewById<TextView>(R.id.post_duration_tv)
        var mult = 1
        if(!currentPost.post_anon){
            mult = 2
        }


        var voteCount = vw.getVoteCount(currentPost.votes_map!!,mult)
        postKarma.text = voteCount.toString()
        var prevVote = vw.getPrevVote(uid, currentPost.votes_map!!)
        vw.voteVisual(upvoteIV, downvoteIV, prevVote)
        postDuration.text = vw.getAgeString(currentPost.post_date_long)


        upvoteIV.setOnClickListener {
            usersVote = vw.vote(usersVote, true, upvoteIV, downvoteIV)
            currentPost.votes_map = vw.updateVoteMap(usersVote, uid, currentPost.votes_map!!)
            val voteCountString = vw.getVoteCount(currentPost.votes_map!!, mult).toString()
            postKarma.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedPosts.add(currentPost)
        }
        downvoteIV.setOnClickListener {
            usersVote = vw.vote(usersVote, false,upvoteIV,downvoteIV)
            currentPost.votes_map = vw.updateVoteMap(usersVote, uid, currentPost.votes_map!!)
            val voteCountString = vw.getVoteCount(currentPost.votes_map!!, mult).toString()
            postKarma.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedPosts.add(currentPost)
        }

    }

    private fun recyclerHandling(){
        sw.getCommentsFromServer(currentPost.post_id){commentsList->
            //recycler handeling
            if(commentsList.isEmpty()){
                comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
                comments_recyclerView.visibility = View.INVISIBLE
                empty_view.visibility = View.VISIBLE
            }else{
                comments_recyclerView.visibility = View.VISIBLE
                empty_view.visibility = View.GONE
                comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
            }

        }
        comments_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        comments_refreshLayout.setOnRefreshListener {
            Log.e(TAG, "REFRESH-COMMENTS")
            val t = comments_recyclerView.adapter as CommentsRecyclerViewAdapter
            sw.updateCommentsOnServer(t.getChangedList(), currentPost.post_id){
                sw.getCommentsFromServer(currentPost.post_id){commentsList ->
                    comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
                    comments_refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun anonDialogHandeling(v:View){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.anon_named_dialog, null)
        val mBuilder = AlertDialog.Builder(requireContext()).setView(dialogView).setTitle("Post as...")
        val alertDialog = mBuilder.show()
        alertDialog.anon_circ_iv.setImageResource(R.drawable.ic_person_black_24dp)
        alertDialog.named_circ_iv.setImageBitmap(viewModel.currentProfileBitmap.value)
        alertDialog.users_handle_tv_dialog.text = viewModel.currentUser.value!!.username
        alertDialog.anon_circ_iv.setOnClickListener {
            commentAnon(v)
            alertDialog.dismiss()
        }
        alertDialog.named_circ_iv.setOnClickListener {
            commentNamed(v)
            alertDialog.dismiss()
        }
    }

    private fun commentNamed(v:View){
        val currentCommentET = v.findViewById<EditText>(R.id.current_comment_et)
        val commentText = currentCommentET.text.toString()
        val commentMap = getCommentMap(commentText, false)
        sw.updateCommentsOnPost(currentPost.post_id, commentMap){
            Toast.makeText(context, "Comments Sent!", Toast.LENGTH_SHORT).show()
            currentCommentET.setText("")
            hideKeyboard()
            sw.getCommentsFromServer(currentPost.post_id){commentsList->
                //recycler handeling
                comments_recyclerView.visibility = View.VISIBLE
                empty_view.visibility = View.GONE
                comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
            }

        }
    }

    private fun commentAnon(v:View){
        val currentCommentET = v.findViewById<EditText>(R.id.current_comment_et)
        val commentText = currentCommentET.text.toString()
        // build comment and send text to server and refresh the layout after
        val commentMap = getCommentMap(commentText, true)
        sw.updateCommentsOnPost(currentPost.post_id, commentMap){
            Toast.makeText(context, "Comments Sent!", Toast.LENGTH_SHORT).show()
            currentCommentET.setText("")
            hideKeyboard()
            sw.getCommentsFromServer(currentPost.post_id){commentsList->
                //recycler handeling
                comments_recyclerView.visibility = View.VISIBLE
                empty_view.visibility = View.GONE
                comments_recyclerView.adapter = CommentsRecyclerViewAdapter(commentsList)
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
