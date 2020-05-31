package com.example.coronawalla.main.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore

//add background color selection?

class ProfileEditFragment : Fragment() {
    private val TAG: String? = ProfileEditFragment::class.simpleName
    private lateinit var viewModel: MainActivityViewModel
    private var takenHandles = hashSetOf<String>()
    private var prevHandle:String? = null


    override fun onResume() {
        super.onResume()
        viewModel?.toolbarMode?.value = -2
        val profImg:ImageView = requireView().findViewById(R.id.profile_edit_iv)
        if(viewModel!!.currentProfileBitmap.value!=null){
            profImg.setImageBitmap(viewModel!!.currentProfileBitmap.value)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this.requireActivity()).get(MainActivityViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set current values
        setCurrentUserVals(view, viewModel!!.currentUser.value!!)
        getHandles {
            Log.d(TAG, "Taken handels retrived")
            takenHandles = it
        }

        //handle image
        val profImg:ImageView = view.findViewById(R.id.profile_edit_iv)
        if(viewModel!!.currentProfileBitmap.value!=null){
            profImg.setImageBitmap(viewModel!!.currentProfileBitmap.value)
        }
        val profEditTV = view.findViewById<TextView>(R.id.changeProfilePic_TV)
        profImg.setOnClickListener{
            getImageFromGallery()
        }
        profEditTV.setOnClickListener {
            getImageFromGallery()
        }
        //handle text

        //navgiation
        val confirmImageButton = requireActivity().findViewById<ImageView>(R.id.right_button_iv)
        val cancelImageButton = requireActivity().findViewById<ImageView>(R.id.left_button_iv)
        confirmImageButton.setOnClickListener {
            updateUserVals(view)
            findNavController().navigate(R.id.action_profileEditFragment_to_profile)
        }
        cancelImageButton.setOnClickListener { findNavController().navigate(R.id.action_profileEditFragment_to_profile) }

    }

    private fun getImageFromGallery(){
        ImagePicker.with(requireActivity())
            .galleryOnly()
            .cropSquare()
            .compress(512)
            .start(1)
    }

    private fun setCurrentUserVals(view:View, user:UserClass){
        val notavail:TextView = view.findViewById(R.id.notavailable_tv)
        val confirm:ImageView = requireActivity().findViewById(R.id.right_button_iv)
        notavail.visibility = View.INVISIBLE
        val handleET: EditText = view.findViewById(R.id.handle_et)
        val usernameET:EditText = view.findViewById(R.id.username_et)
        handleET.setText(user.handle)
        usernameET.setText(user.username)


        handleET.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val currentHandle = p0.toString()
                if(currentHandle.toLowerCase() == prevHandle){
                    //is users previous handle so they can use it
                    confirm.visibility = View.VISIBLE
                    notavail.visibility = View.INVISIBLE
                }else{
                    if(takenHandles.contains(currentHandle.toLowerCase())){
                        //unavailable
                        confirm.visibility = View.INVISIBLE
                        notavail.visibility = View.VISIBLE
                    }else{
                        //available
                        confirm.visibility = View.VISIBLE
                        notavail.visibility = View.INVISIBLE
                    }
                }


            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    private fun getHandles(callback:(HashSet<String>) ->Unit ){
        val takenHandleSet = hashSetOf<String>()
        FirebaseFirestore.getInstance().collection("handles").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(doc in it.result!!){
                    if(doc.get("user_id") == viewModel!!.currentUser.value!!.user_id){
                        prevHandle = doc.id
                    }
                }
                for(doc in it.result!!){
                    takenHandleSet.add(doc.id)
                }
                callback.invoke(takenHandleSet)
            }else{
                Log.e(TAG,it.exception.toString() )
            }
        }
    }

    private fun updateUserVals(view:View){
        val handleET: EditText = view.findViewById(R.id.handle_et)
        val usernameET:EditText = view.findViewById(R.id.username_et)
        val username = usernameET.text.toString()
        val handle = handleET.text.toString()
        val db = FirebaseFirestore.getInstance()
        Log.e(TAG, "Server Call: Updating user vals")
        db.collection("users").document(viewModel!!.currentUser.value!!.user_id).update(
            "username", username,
            "handle", handle).addOnCompleteListener{
            if(it.isSuccessful){
                Log.d(TAG, "User updated!")
            }else{
                Log.e(TAG, it.exception.toString())
            }
        }
        //updating handles
        if(prevHandle != null){
            val update = hashMapOf("user_id" to viewModel!!.currentUser.value!!.user_id)
            Log.e(TAG, "Server Call: Deleting Users previous handle")
            db.collection("handles").document(prevHandle.toString()).delete()
            Log.e(TAG, "Server Call: Adding Users new handle to collection")
            db.collection("handles").document(handle.toLowerCase()).set(update).addOnCompleteListener{
                if(it.isSuccessful){
                    Log.d(TAG, "handles updated")
                }else{
                    Log.e(TAG, it.exception.toString())
                }
            }
        }else{
            val update = hashMapOf("user_id" to viewModel!!.currentUser.value!!.user_id)
            Log.e(TAG, "Server Call: Adding users handle to collection")
            db.collection("handles").document(handle.toLowerCase()).set(update).addOnCompleteListener{
                if(it.isSuccessful){
                    Log.d(TAG, "handles updated")
                }else{
                    Log.e(TAG, it.exception.toString())
                }
            }

        }

        viewModel!!.currentUser.value!!.username = username
        viewModel!!.currentUser.value!!.handle= handle
    }
}
