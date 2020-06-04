package com.example.coronawalla.main.ui.local

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.login.LoginActivity
import com.example.coronawalla.main.MainActivity
import com.example.coronawalla.main.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_local.*

class LocalFragment : Fragment() {
    private lateinit var viewModel:MainActivityViewModel
    private val TAG: String? = LocalFragment::class.simpleName

    override fun onPause() {
        super.onPause()
        if(posts_recyclerView.adapter != null){
            updatePostsServer {}
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.toolbarMode.value = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =  ViewModelProvider(this.requireActivity()).get(MainActivityViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_local, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        posts_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        navigation()
        recyclerHandling()
    }

    private fun updatePostsServer(callback:(Boolean) -> Unit ){
        val t = posts_recyclerView.adapter as PostsRecyclerViewAdapter
        t.getChangedList()
        val db = viewModel.db
        val oldPostList = t.getChangedList()
        val batch = db.batch()
        val colRef = db.collection("posts")
        for(post in oldPostList){
            val docRef = colRef.document(post.post_id)
            batch.update(docRef,"votes_map",post.votes_map)
        }

        batch.commit().addOnCompleteListener{
            if(it.isSuccessful){
                Log.i(TAG,"Posts updated!")
                callback.invoke(true)
            }else{
                Log.e(TAG,it.exception.toString())
            }
        }
    }

    private fun showSignInDialog(){
        AlertDialog.Builder(activity)
            .setCancelable(false)
            .setTitle("Create Account")
            .setMessage("You must create an account to continue")
            .setPositiveButton("Ok"){ dialog, _ ->
                Toast.makeText(activity, "GO TO PHONE NUMBER", Toast.LENGTH_SHORT).show()
                val intent  = Intent(activity, LoginActivity::class.java)
                intent.putExtra("phone", true)
                requireActivity().startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("cancel"){ dialog, _ ->
                Toast.makeText(activity, "Cancel", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
            .show()

    }

    private fun navigation(){
        val anon = viewModel.mAuth.currentUser!!.isAnonymous
        val postImageButton = requireActivity().findViewById<ImageView>(R.id.right_button_iv)
        val profileImageButton = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        val townTextView = requireActivity().findViewById<TextView>(R.id.toolbar_title_tv)

        postImageButton.setOnClickListener {
            if(anon){
                showSignInDialog()
            }else{
                findNavController().navigate(R.id.action_local_to_postFragment)
            }
        }
        profileImageButton.setOnClickListener {
            if(anon){
                showSignInDialog()
            }else{
                findNavController().navigate(R.id.action_local_to_profile)
            }
        }
        townTextView.setOnClickListener {
            //todo start autocomplete town select activity
            Toast.makeText(activity, "OPEN TOWN SELECT", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recyclerHandling(){
        //if the list is null or empty, set an observer and wait for values. Otherwise use the one currently available
        when {
            viewModel.localPostList.value == null -> {
                viewModel.localPostList.observe(viewLifecycleOwner, Observer{
                    posts_recyclerView.adapter = PostsRecyclerViewAdapter(it, findNavController())
                })
            }
            viewModel.localPostList.value!!.isEmpty() -> {
                posts_recyclerView.adapter = PostsRecyclerViewAdapter(viewModel.localPostList.value!!, findNavController())
                posts_recyclerView.visibility = View.INVISIBLE
                empty_view_posts.visibility = View.VISIBLE
            }
            else -> {
                val list = viewModel.localPostList.value
                posts_recyclerView.visibility = View.VISIBLE
                empty_view_posts.visibility = View.GONE
                posts_recyclerView.adapter = PostsRecyclerViewAdapter(list!!.toList(), findNavController())
            }
        }
        posts_refreshLayout.setOnRefreshListener {
            //This is a safe cast because of the fragment we are in
            updatePostsServer {
                if(it){
                    val mA:MainActivity = activity as MainActivity
                    mA.getPostsFromServer(){
                        posts_recyclerView.adapter = PostsRecyclerViewAdapter(it, findNavController())
                    }
                    posts_refreshLayout.isRefreshing = false
                }
            }

        }
    }

}
