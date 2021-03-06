package com.example.coronawalla.main.ui.local

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel


class PostFragment : Fragment() {
    private lateinit var  viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =  ViewModelProvider(this.requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.toolbarMode.value = 2
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigation(view)

        val postET = view.findViewById<EditText>(R.id.postText_ET)
        val titleTV = requireActivity().findViewById<TextView>(R.id.toolbar_title_tv)
        val sendTV = requireActivity().findViewById<TextView>(R.id.toolbar_send_tv)

        postET.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val currentText = p0.toString()
                val num = 250 - currentText.length
                if(num == 250){
                    titleTV.text = "Post"
                }else{
                    titleTV.text = num.toString()
                }
                if(num < 0){
                    sendTV.visibility = View.INVISIBLE
                }else{
                    sendTV.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun navigation(view:View){
        val cancelTV = requireActivity().findViewById<TextView>(R.id.toolbar_cancel_tv)
        val sendTV = requireActivity().findViewById<TextView>(R.id.toolbar_send_tv)
        val postET = view.findViewById<EditText>(R.id.postText_ET)

        sendTV.setOnClickListener {
            val bundle = bundleOf("postText" to postET.text.toString())
            findNavController().navigate(R.id.action_postFragment_to_postPreviewFragment, bundle)
        }

        cancelTV.setOnClickListener {
            findNavController().navigate(R.id.action_postFragment_to_local)
        }
    }

}
