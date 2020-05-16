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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.login.LoginActivity
import com.example.coronawalla.main.MainActivity
import com.example.coronawalla.main.MainActivityViewModel
import com.example.coronawalla.main.ToolbarWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_local.*

class LocalFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val TAG: String? = LocalFragment::class.simpleName

    override fun onPause() {
        super.onPause()
        if(recyclerView.adapter != null){

            updatePostsServer()
        }
        //viewModel!!.updateUserServer.value = true
    }

    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_local, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //navigation handeling
        val tb = ToolbarWorker(requireActivity())
        val anon = FirebaseAuth.getInstance().currentUser!!.isAnonymous
        val townTextView = requireActivity().findViewById<TextView>(R.id.toolbar_title_tv)
        val postImageButton = requireActivity().findViewById<ImageView>(R.id.right_button_iv)
        val profileImageButton = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        postImageButton.setOnClickListener {
            if(anon){
                showSignInDialog()
            }else{
                tb.buttonEffect(postImageButton)

                findNavController().navigate(R.id.action_local_to_postFragment)
            }
        }
        profileImageButton.setOnClickListener {
            if(anon){
                showSignInDialog()
            }else{
                tb.buttonEffect(profileImageButton)
                findNavController().navigate(R.id.action_local_to_profile)
            }
        }
        townTextView.setOnClickListener {
            //todo start autocomplete activity
            Toast.makeText(activity, "OPEN TOWN SELECT", Toast.LENGTH_SHORT).show()
        }

        //post list recycler handeling
        if( viewModel!!.localPostList.value != null){
            recyclerView.adapter = RecyclerViewAdapter(viewModel!!.localPostList.value!!)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel!!.localPostList.observe(viewLifecycleOwner, Observer{
            recyclerView.adapter = RecyclerViewAdapter(it)

        })
        refreshLayout.setOnRefreshListener {
            //This is a safe cast because of the fragment we are in
            Log.e(TAG, "REFRESH")
            val mA:MainActivity = activity as MainActivity
            mA.updateLocalPostList(viewModel!!.currentLocation.value!!)
            viewModel!!.localPostList.observe(viewLifecycleOwner, Observer{
                recyclerView.adapter = RecyclerViewAdapter(it)
            })
            refreshLayout.isRefreshing = false
        }
    }

    private fun updatePostsServer(){
        val t = recyclerView.adapter as RecyclerViewAdapter
        t.getChangedList()
        val db = FirebaseFirestore.getInstance()
        val oldPostList = t.getChangedList()
        val batch = db.batch()
        val colRef = db.collection("posts")
        for(post in oldPostList){
            val docRef = colRef.document(post.post_id)
            batch.update(docRef,"vote_count",post.vote_count)
            batch.update(docRef,"votes_map",post.votes_map)
        }

        batch.commit().addOnCompleteListener{
            if(it.isSuccessful){
                Log.i(TAG,"Posts updated!")
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
            .setPositiveButton("Ok"){dialog, id->
                Toast.makeText(activity, "GO TO PHONE NUMBER", Toast.LENGTH_SHORT).show()
                val intent  = Intent(activity, LoginActivity::class.java)
                intent.putExtra("phone", true)
                requireActivity().startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("cancel"){dialog, id->
                Toast.makeText(activity, "Cancel", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
            .show()

    }
}
