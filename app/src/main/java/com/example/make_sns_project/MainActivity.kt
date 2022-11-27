package com.example.make_sns_project

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.make_sns_project.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    lateinit var binding: ActivityMainBinding
    val db: FirebaseFirestore = Firebase.firestore

    companion object {
        const val REQUEST_CODE = 1
        const val UPLOAD_FOLDER = "img/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Firebase.auth.currentUser == null)
        {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        Firebase.auth.currentUser ?: finish()  // if not authenticated, finish this activity
        binding.followerscount.text = "0"
        binding.username.text = Firebase.auth.currentUser?.uid ?: "No User"
        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        val imageRef1 = storageRef.child("img/profile_default.png")
        displayImageRef(imageRef1, binding.imageView)

        binding.updateProfile.setOnClickListener {
            uploadDialog()
        }
        binding.signout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.posting.setOnClickListener {
            startActivity(Intent(this, MyPosting::class.java))
        }
        notifyposting()
        notifyFollow()
        binding.unOrFollowing.setOnClickListener {
            if (binding.unOrFollowing.text.equals("팔로우 하기"))
            {
                binding.unOrFollowing.text = "언팔로우 하기"
            }
            else if (binding.unOrFollowing.text.equals("언팔로우 하기"))
            {
                binding.unOrFollowing.text = "팔로우 하기"
            }
            val firestore = FirebaseFirestore.getInstance()
            val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
            val txDocTargetUser = firestore?.collection("followlist")?.document(Firebase.auth.currentUser?.uid.toString())
            //Firestore에 데이터 저장 : runTransaction{...}
            firestore.runTransaction {
                // it : Transaction
                // it.get(Document) : 해당 Document 받아오기
                // it.set(Document, Dto 객체) : 해당 Document에 Dto 객체 저장하기
                var followDto = it.get(txDocTargetUser!!).toObject(FollowDto::class.java)
                if (followDto == null)
                {
                    followDto = FollowDto().apply {
                        followers[currentUid!!] = true
                        followings[currentUid!!]=true
                        notifyFollow()
                    }
                }
                else
                {
                    with(followDto) {
                        if (followers.containsKey(currentUid!!))
                        {
                            // 언팔로우
                            followers.remove(currentUid!!)
                            followings.remove(currentUid!!)
                            notifyFollow()
                        }
                        else
                        {
                            // 팔로우
                            followers[currentUid!!] = true
                            followings[currentUid!!]=true
                            notifyFollow()
                        }
                    }
                }
                it.set(txDocTargetUser!!, followDto)
                return@runTransaction
            }

        }
    }

    override fun onStart() {
        super.onStart()
        notifyposting()
        notifyFollow()
    }
    private fun notifyposting()
    {
        db.collection("myposting").get().addOnSuccessListener {
            binding.postcount.text = it.size().toString()
        }
    }
    private fun notifyFollow(){
        db.collection("followlist").document(binding.username.text.toString()).get().addOnSuccessListener {
            val hashmap= it["followers"] as HashMap<String,Boolean>
            var count=0
            for(i in hashmap)
            {
                if(i.value.equals(true))
                {
                    count++
                }
            }
            Snackbar.make(binding.root,"count=${count}",Snackbar.LENGTH_SHORT).show()
            binding.followerscount.text=Integer.toString(count)
        }
        db.collection("followlist").document(binding.username.text.toString()).get().addOnSuccessListener {
            val hashmap= it["followings"] as HashMap<String,Boolean>
            var count=0
            for(i in hashmap)
            {
                if(i.value.equals(true))
                {
                    count++
                }
            }
            binding.followingscount.text=Integer.toString(count)
        }
    }
    private fun uploadDialog() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,null)
            Snackbar.make(binding.root, "권한 있음", Snackbar.LENGTH_SHORT).show()
            AlertDialog.Builder(this)
                .setTitle("Choose Photo")
                .setCursor(cursor, { _, i ->
                    cursor?.run {
                        moveToPosition(i)
                        val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                        val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                        uploadFile(getLong(idIdx), getString(nameIdx))
                    }
                }, MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()
        }
        else
        {
            Snackbar.make(binding.root, "권한 없음", Snackbar.LENGTH_SHORT).show()
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    private fun uploadFile(file_id: Long?, fileName: String?) {
        file_id ?: return
        val imageRef = storage.reference.child("${UPLOAD_FOLDER}${fileName}")
        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file_id)
        imageRef.putFile(contentUri).addOnCompleteListener {
            if (it.isSuccessful)
            {
                // upload success
                Snackbar.make(binding.root, "Upload completed.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<String>,grantResults:IntArray)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode == REQUEST_CODE)
        {
            if((grantResults.isNotEmpty()&&grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                uploadDialog()
            }
        }
    }

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
            // Failed to download the image
        }
    }
}