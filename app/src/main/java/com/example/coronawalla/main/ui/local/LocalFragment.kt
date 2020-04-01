package com.example.coronawalla.main.ui.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronawalla.R
import kotlinx.android.synthetic.main.fragment_local.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class LocalFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_local, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val exampleList = genDummyList(50)
        recyclerView.adapter =
            RecyclerViewAdapter(exampleList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val time = System.currentTimeMillis()
        println("############"+time)
        val d = Date(time)
        println("############"+d)

    }

    private fun genDummyList(size:Int): List<PostClass>{
        val list = ArrayList<PostClass>()
        for(i in 0 until size){
            val item =
                PostClass("This is some sample text","243", 1585550623000,null)
            list+=item
        }
        return list
    }
}
