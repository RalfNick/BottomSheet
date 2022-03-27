package com.ralf.bottomsheet.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ralf.bottomsheet.R

class MyAdapter(private val list: List<Item>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        ) {

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.item_title).text = list[position].text
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "item - " + list[position].text, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun getItemCount() = list.size
}