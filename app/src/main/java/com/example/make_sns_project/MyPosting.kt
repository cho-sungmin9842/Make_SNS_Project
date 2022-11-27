package com.example.make_sns_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.make_sns_project.databinding.ActivityMyPostingBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyPosting : AppCompatActivity() {
    private lateinit var binding: ActivityMyPostingBinding
    private var adapter: MyAdapter? = null
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("myposting")
    private var snapshotListener: ListenerRegistration? = null
    var date=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        date=getdateandtime()
        binding.datetime.isEnabled=false
        binding.datetime.setText(date)
        binding.checkAutoID.setOnClickListener {
            binding.editID.isEnabled = !binding.checkAutoID.isChecked
            if (!binding.editID.isEnabled) binding.editID.setText("")
        }
        // recyclerview setup
        binding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(this, emptyList())
        adapter?.setOnItemClickListener {
            queryItem(it)
        }
        binding.recyclerViewItems.adapter = adapter

        updateList()  // list items on recyclerview

        binding.buttonAddUpdate.setOnClickListener {
            addItem()
        }

        binding.buttonUpdateContext.setOnClickListener {
            updateContext()
        }

        binding.buttonDelete.setOnClickListener {
            deleteItem()
        }
    }

    override fun onStart() {
        super.onStart()
        snapshotListener = itemsCollectionRef.addSnapshotListener { snapshot, error ->
            binding.textSnapshotListener.text = StringBuilder().apply {
                for (doc in snapshot!!.documentChanges)
                {
                    append("${doc.type} ${doc.document.id} ${doc.document.data}")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }

    private fun updateList() {
        itemsCollectionRef.get().addOnSuccessListener {
            val items = mutableListOf<Item>()
            for (doc in it)
            {
                items.add(Item(doc))
            }
            adapter?.updateList(items)
        }
    }

    private fun addItem() {
        date=getdateandtime()
        binding.datetime.setText(date)
        if (binding.editContext.text.isEmpty())
        {
            Snackbar.make(binding.root, "Input context!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val context = binding.editContext.text.toString()
        val autoID = binding.checkAutoID.isChecked
        val itemID = binding.editID.text.toString()
        if (!autoID and itemID.isEmpty())
        {
            Snackbar.make(binding.root, "Input ID or check Auto-generate ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val itemMap = hashMapOf(
            "date" to date,
            "context" to context
        )
        if (autoID)
        {
            itemsCollectionRef.add(itemMap).addOnSuccessListener {
                updateList()
            }.addOnFailureListener {

            }
        }
        else
        {
            itemsCollectionRef.document(itemID).set(itemMap).addOnSuccessListener {
                updateList()
            }.addOnFailureListener {

            }
        }
    }

    private fun queryItem(itemID: String) {
        itemsCollectionRef.document(itemID).get()
            .addOnSuccessListener {
                binding.editID.setText(it.id)
                binding.checkAutoID.isChecked = false
                binding.editID.isEnabled = true
                binding.datetime.setText(it["date"].toString())
                binding.editContext.setText(it["context"].toString())
            }.addOnFailureListener {

            }
    }

    private fun updateContext() {
        val itemID = binding.editID.text.toString()
        val context = binding.editContext.text.toString()
        if (itemID.isEmpty())
        {
            Snackbar.make(binding.root, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        itemsCollectionRef.document(itemID).update("context", context)
            .addOnSuccessListener {
                queryItem(itemID)
                updateList()
            }
    }

    private fun deleteItem() {
        val itemID = binding.editID.text.toString()
        if (itemID.isEmpty())
        {
            Snackbar.make(binding.root, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        itemsCollectionRef.document(itemID).delete().addOnSuccessListener {
            updateList()
        }
    }
    private fun getdateandtime():String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")
        val formatted = current.format(formatter)
        return formatted
    }
}