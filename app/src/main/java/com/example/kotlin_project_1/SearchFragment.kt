package com.example.kotlin_project_1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment() {

    private lateinit var adapter: TrashAdapter
    private val trashItems = listOf(
        TrashItem("Pizza Box", "Paper", android.graphics.Color.BLUE),
        TrashItem("Old Charger", "E-Waste", android.graphics.Color.RED),
        TrashItem("Glass Bottle", "Glass", android.graphics.Color.GREEN),
        TrashItem("Plastic Bag", "Plastic", android.graphics.Color.YELLOW),
        TrashItem("Banana Peel", "Organic", android.graphics.Color.rgb(255, 165, 0)),
        TrashItem("Newspaper", "Paper", android.graphics.Color.BLUE),
        TrashItem("Broken Phone", "E-Waste", android.graphics.Color.RED),
        TrashItem("Wine Glass", "Glass", android.graphics.Color.GREEN),
        TrashItem("Yogurt Pot", "Plastic", android.graphics.Color.YELLOW),
        TrashItem("Egg Shells", "Organic", android.graphics.Color.rgb(255, 165, 0))
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerSearch)
        val editSearch: EditText = view.findViewById(R.id.editSearch)
        
        adapter = TrashAdapter(trashItems)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(text: String) {
        val filteredList = trashItems.filter { it.name.contains(text, ignoreCase = true) }
        adapter.updateList(filteredList)
    }
}

data class TrashItem(val name: String, val binType: String, val color: Int)

class TrashAdapter(private var items: List<TrashItem>) : RecyclerView.Adapter<TrashAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(android.R.id.text1)
        val txtBin: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtName.text = item.name
        holder.txtBin.text = "Bin: ${item.binType}"
        holder.txtBin.setTextColor(item.color)
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<TrashItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
