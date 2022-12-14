package com.example.make_sns_project

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.make_sns_project.databinding.ItemBinding
import com.google.firebase.firestore.QueryDocumentSnapshot

data class Item(val id: String, val date: String, val context: String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id, doc["date"].toString(), doc["context"].toString())
    constructor(key: String, map: Map<*, *>) :
            this(key, map["date"].toString(), map["context"].toString())
}

class MyViewHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)

class MyAdapter(private val context: Context, private var items: List<Item>)
    : RecyclerView.Adapter<MyViewHolder>() {
    fun interface OnItemClickListener {
        fun onItemClick(student_id: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemBinding = ItemBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        //holder.binding.textID.text = item.id
        holder.binding.textDate.text = item.date
        holder.binding.textContext.text=item.context
        /*holder.binding.textID.setOnClickListener {
            AlertDialog.Builder(context).setMessage("You clicked ${student.name}.").show()
            itemClickListener?.onItemClick(item.id)
        }*/
        holder.binding.textDate.setOnClickListener {
            //AlertDialog.Builder(context).setMessage("You clicked ${student.name}.").show()
            itemClickListener?.onItemClick(item.id)
        }
        holder.binding.textContext.setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }
    }

    override fun getItemCount() = items.size
}
