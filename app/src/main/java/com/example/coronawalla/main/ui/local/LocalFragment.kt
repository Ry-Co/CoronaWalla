package com.example.coronawalla.main.ui.local

import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_local.*

class LocalFragment : Fragment() {
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
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
            val mA:MainActivity = activity as MainActivity
            mA.updatePostList()
            viewModel!!.localPostList.observe(viewLifecycleOwner, Observer{
                recyclerView.adapter = RecyclerViewAdapter(it)
            })
            refreshLayout.isRefreshing = false
        }
    }
}
