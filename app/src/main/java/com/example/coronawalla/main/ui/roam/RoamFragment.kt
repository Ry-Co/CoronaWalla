package com.example.coronawalla.main.ui.roam

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders

import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel


class RoamFragment : Fragment() {

    private val viewModel by lazy{
        activity?.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }


    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_roam, container, false)
    }

}
