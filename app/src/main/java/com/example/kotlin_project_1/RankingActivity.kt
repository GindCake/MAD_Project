package com.example.kotlin_project_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class RankingUser(
    val name: String = "",
    val points: Int = 0,
    val level: String = ""
)

class RankingActivity : AppCompatActivity() {
    private lateinit var rvRanking: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val db = FirebaseFirestore.getInstance()
    private val rankingList = mutableListOf<RankingUser>()
    private lateinit var adapter: RankingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvRanking = findViewById(R.id.rvRanking)
        progressBar = findViewById(R.id.progressBar)

        rvRanking.layoutManager = LinearLayoutManager(this)
        adapter = RankingAdapter(rankingList)
        rvRanking.adapter = adapter

        fetchRankingData()
    }

    private fun fetchRankingData() {
        progressBar.visibility = View.VISIBLE
        db.collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { documents ->
                rankingList.clear()
                for (doc in documents) {
                    val name = doc.getString("displayName") ?: doc.getString("email")?.substringBefore("@") ?: "Anonymous"
                    val points = doc.getLong("totalPoints")?.toInt() ?: 0
                    val level = doc.getString("level") ?: "Beginner"
                    rankingList.add(RankingUser(name, points, level))
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                // Handle error
            }
    }

    class RankingAdapter(private val users: List<RankingUser>) :
        RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRank: TextView = view.findViewById(R.id.tvRank)
            val tvUserName: TextView = view.findViewById(R.id.tvUserName)
            val tvUserLevel: TextView = view.findViewById(R.id.tvUserLevel)
            val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ranking, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            holder.tvRank.text = "#${position + 1}"
            holder.tvUserName.text = user.name
            holder.tvUserLevel.text = user.level
            holder.tvPoints.text = "${user.points} pts"
            
            // Highlight top 3
            when(position) {
                0 -> holder.tvRank.text = "🥇"
                1 -> holder.tvRank.text = "🥈"
                2 -> holder.tvRank.text = "🥉"
            }
        }

        override fun getItemCount() = users.size
    }
}
