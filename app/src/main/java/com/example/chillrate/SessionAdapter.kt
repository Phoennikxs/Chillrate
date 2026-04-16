package com.example.chillrate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chillrate.data.SessionEntity
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private val onSessionClick: (SessionEntity) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private val sessions = mutableListOf<SessionEntity>()

    fun submitList(newList: List<SessionEntity>) {
        sessions.clear()
        sessions.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount() = sessions.size

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val tvTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val tvHR: TextView = itemView.findViewById(R.id.textViewHR)
        private val tvDuration: TextView = itemView.findViewById(R.id.textViewDuration)
        // можно добавить stress и strength позже

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(session: SessionEntity) {
            tvDate.text = dateFormat.format(session.startTime)
            tvTime.text = timeFormat.format(session.startTime)

            tvHR.text = "${session.averageHR}"
            tvDuration.text = formatDuration(session.durationSeconds)

            itemView.setOnClickListener {
                onSessionClick(session)
            }
        }

        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format("%02d:%02d", minutes, secs)
        }
    }
}