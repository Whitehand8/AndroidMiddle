package com.example.godparttimejob.ui.companydetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R

class JobHistoryAdapter(
    private val jobHistoryList: List<JobHistory>
) : RecyclerView.Adapter<JobHistoryAdapter.JobHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job_history, parent, false)
        return JobHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobHistoryViewHolder, position: Int) {
        val jobHistory = jobHistoryList[position]
        holder.bind(jobHistory)
    }

    override fun getItemCount(): Int = jobHistoryList.size

    class JobHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textJobTitle: TextView = itemView.findViewById(R.id.textJobTitle)
        private val textJobDescription: TextView = itemView.findViewById(R.id.textJobDescription)
        private val textJobCreatedAt: TextView = itemView.findViewById(R.id.textJobCreatedAt)

        fun bind(jobHistory: JobHistory) {
            textJobTitle.text = jobHistory.title
            textJobDescription.text = jobHistory.description
            textJobCreatedAt.text = "등록일: ${formatDate(jobHistory.createdAt)}"
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }
}
