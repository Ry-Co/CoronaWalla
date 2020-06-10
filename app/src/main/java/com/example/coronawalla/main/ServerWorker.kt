package com.example.coronawalla.main

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.coronawalla.main.ui.discussion.CommentClass
import com.example.coronawalla.main.ui.discussion.CommentsRecyclerViewAdapter
import com.example.coronawalla.main.ui.local.PostClass
import com.example.coronawalla.main.ui.profile.UserClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_discussion.*
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import java.io.ByteArrayOutputStream

class ServerWorker constructor(private val activity: Activity){
    private val TAG: String? = ServerWorker::class.simpleName
    private val vw = VoteWorker()
    private val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance()

    private fun getDocumentsFromLocation(loc: Location, callback: (List<DocumentSnapshot>) -> Unit){
        val usersGP = GeoPoint(loc.latitude, loc.longitude)
        val radiusInKm = 5 * 1.60934 //5 miles
        val geoFirestore = GeoFirestore(db.collection("posts"))
        geoFirestore.getAtLocation(usersGP,radiusInKm){ docs, ex ->
            if(ex != null){
                Log.e(TAG, "Error:: "+ex.message)
                return@getAtLocation
            }else{
                callback.invoke(docs!!)
            }
        }
    }

    private fun buildPostsFromDocumentSnapshotList(docs:List<DocumentSnapshot>, callback: (MutableList<PostClass>) -> Unit){
        val posts = ArrayList<PostClass>()
        for(d in docs){
            val p = d.toObject(PostClass::class.java)
            if(p != null){
                posts.add(p)
            }
        }
        val postsSorted = posts.sortedWith(vw.postComparator)
        callback.invoke(postsSorted.toMutableList())
    }

    fun getUserClassFromUID(uid:String, callback:(UserClass)->Unit){
        db.collection("users").document(uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val uidUserClass = it.result!!.toObject(UserClass::class.java)
                callback.invoke(uidUserClass!!)
            } else {
                Log.d(TAG, "Error:: " + it.exception)
            }
        }
    }

    fun uploadBitmapToReference(bitmap: Bitmap, storageRef: StorageReference, callback:(String)->Unit){
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = storageRef.putBytes(data)

        uploadTask.addOnCompleteListener{
            if (it.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener{ dlURL ->
                    if(dlURL.isSuccessful){
                        val downloadURL = dlURL.result.toString()
                        callback.invoke(downloadURL)
                        //viewModel.currentUser.value!!.profile_image_url = downloadURL.toString()
                        //viewModel.db.collection("users").document(viewModel.currentUser.value!!.user_id).update("profile_image_url", downloadURL.toString())
                    }else{
                        Log.e(TAG, dlURL.exception.toString())
                    }
                }
            }else{
                Toast.makeText(activity,it.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getBitmapFromUID(uid:String, callback:(Bitmap)->Unit){
        val profRef = storage.reference.child("images/$uid")
        val ONE_MEGABYTE: Long = 1024 * 1024
        profRef.getBytes(ONE_MEGABYTE).addOnCompleteListener {
            if (it.isSuccessful) {
                val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
                callback.invoke(bmp)
            } else {
                Log.e(TAG, it.exception.toString())
            }
        }
    }

    fun updateCommentsOnServer(commentList:ArrayList<CommentClass>, currentPostID:String, callback:(Boolean) -> Unit){
        val batch = db.batch()
        val commentsColRef = db.collection("posts").document(currentPostID).collection("comments")
        for(comment in commentList){
            val docRef = commentsColRef.document(comment.comment_id)
            batch.update(docRef, "votes_map", comment.votes_map)
        }
        batch.commit().addOnCompleteListener{
            if(it.isSuccessful){
                Log.i(TAG,"comments updated!")
                callback.invoke(true)
            }else{
                Log.e(TAG,it.exception.toString())
                callback.invoke(false)
            }
        }
    }

    fun updatePostsOnServer(postList:ArrayList<PostClass>, callback: (Boolean) -> Unit){
        val batch = db.batch()
        val colRef = db.collection("posts")
        for(post in postList){
            val docRef = colRef.document(post.post_id)
            batch.update(docRef,"votes_map",post.votes_map)
        }

        batch.commit().addOnCompleteListener{
            if(it.isSuccessful){
                Log.i(TAG,"Posts updated!")
                callback.invoke(true)
            }else{
                Log.e(TAG,it.exception.toString())
                callback.invoke(false)
            }
        }
    }

    fun getCommentsFromServer(currentPostID:String , callback:(MutableList<CommentClass>) -> Unit){
        val commentList = mutableListOf<CommentClass>()
        db.collection("posts").document(currentPostID).collection("comments").get().addOnCompleteListener {
            if(it.isSuccessful){
                for(item in it.result!!){
                    val comment = item.toObject(CommentClass::class.java)
                    commentList.add(comment)
                }
                val vw = VoteWorker()
                val commentsSorted = commentList.sortedWith(vw.commentComparator)
                callback.invoke(commentsSorted.toMutableList())
            }else{
                Log.e(TAG, it.exception.toString())
                return@addOnCompleteListener
            }
        }
    }

    fun updateIndividualPost(postID:String, votes_map:MutableMap<String,Boolean?>?){
        db.collection("posts").document(postID).update("votes_map", votes_map)
    }

    fun updateCommentsOnPost(postID:String, comment:CommentClass, callback:(Boolean) -> Unit){
        val postRef = db.collection("posts").document(postID)
        val postCommentCollection = postRef.collection("comments")
        postCommentCollection.add(comment).addOnCompleteListener{commentTask ->
            if(commentTask.isSuccessful){
                postRef.collection("comments").document(commentTask.result!!.id).update("comment_id", commentTask.result!!.id).addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d(TAG, "comment_id updated")
                        callback.invoke(true)
                    }else{
                        Log.e(TAG, it.exception.toString())
                        callback.invoke(false)
                    }
                }
            }else{
                Log.e(TAG, commentTask.exception.toString())
            }
        }
    }

    fun getPostsFromLocation(location:Location?, callback: (MutableList<PostClass>?) -> Unit){
        if(location == null){
            callback.invoke(null)
        }else{
            getDocumentsFromLocation(location){docs ->
                buildPostsFromDocumentSnapshotList(docs){posts ->
                    val postsSorted = posts.sortedWith(vw.postComparator)
                    callback.invoke(postsSorted.toMutableList())
                }
            }
        }
    }

    fun addPostToServer(post:PostClass, callback: (Boolean) -> Unit){
        db.collection("posts").add(post).addOnCompleteListener{postTask ->
            if(postTask.isSuccessful){
                db.collection("posts").document(postTask.result!!.id).update("post_id", postTask.result!!.id).addOnCompleteListener{
                    if(it.isSuccessful){
                        Log.i(TAG, "Updated mPostID")
                        callback.invoke(true)
                    }else{
                        Log.e(TAG, it.exception.toString())
                        callback.invoke(false)
                    }
                }
            }else{
                Log.e(TAG, "Error:: "+postTask.exception.toString())
                callback.invoke(false)
            }
        }

    }

    fun getTakenHandleDocs(currentUserID:String, callback: (ArrayList<DocumentSnapshot>) -> Unit){
        val out = ArrayList<DocumentSnapshot>()
        db.collection("handles").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(doc in it.result!!){
                    out.add(doc)
                }
                callback.invoke(out)
            }else{
                Log.e(TAG,it.exception.toString() )
            }
        }
    }

    fun getTakenHandleHashSetFromDocs(docs:ArrayList<DocumentSnapshot>):HashSet<String>{
        val takenHandleSet = hashSetOf<String>()
        for(doc in docs){
            takenHandleSet.add(doc.id)
        }
        return takenHandleSet
    }

}