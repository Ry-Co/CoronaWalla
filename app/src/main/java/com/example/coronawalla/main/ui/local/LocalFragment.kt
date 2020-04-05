package com.example.coronawalla.main.ui.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_local.*
import java.util.*
import kotlin.collections.ArrayList

class LocalFragment : Fragment() {
    //TODO: add location tracking
    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }


    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = 0
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_local, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val exampleList = genDummyList(50)
        recyclerView.adapter = RecyclerViewAdapter(exampleList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchNewList()
        refreshLayout.setOnRefreshListener {
            fetchNewList()
        }


        val time = System.currentTimeMillis()
        println("############"+time)
        val d = Date(time)
        println("############"+d)

    }

    private fun genDummyList(size:Int): List<PostClass>{
        val list = ArrayList<PostClass>()
//        for(i in 0 until size){
//            val item =
//                PostClass("This is some sample text for the posts in the test area","243", 1585550623000,null)
//            list+=item
//        }
        return list
    }
    private fun fetchNewList(){
        //todo:
        refreshLayout.isRefreshing = true
        refreshLayout.isRefreshing = false
        //Toast.makeText(context, "REFRESHING", Toast.LENGTH_SHORT).show()
        return
    }
    override fun onStart() {
        super.onStart()
        //TODO: contact server and update data
    }

}
