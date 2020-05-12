package com.example.coronawalla.main.ui.local

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivity
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_local.*

class LocalFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }
    private val TAG: String? = LocalFragment::class.simpleName


    override fun onPause() {
        super.onPause()
        updatePostsServer()
        viewModel!!.updateUserServer.value = true
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
}
