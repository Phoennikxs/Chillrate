package com.example.chillrate

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chillrate.data.SessionEntity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Locale

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
        private val tvStress: TextView = itemView.findViewById(R.id.textViewStress)
        private val tvDuration: TextView = itemView.findViewById(R.id.textViewDuration)
        private val chart: LineChart = itemView.findViewById(R.id.chartSession)

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        private var isExpanded = false

        fun bind(session: SessionEntity) {
            tvDate.text = dateFormat.format(session.startTime)
            tvTime.text = timeFormat.format(session.startTime)
            tvHR.text = "${session.averageHR}"
            tvDuration.text = formatDuration(session.durationSeconds)

            // Отображение стресса
            tvStress.text = if (session.stressLevel != null) {
                "${session.stressLevel.toInt()}%"
            } else {
                "—"
            }

            // Цвет стресса
            tvStress.setTextColor(
                when {
                    (session.stressLevel ?: 0f) > 70 -> Color.parseColor("#E53935")  // высокий стресс — красный
                    (session.stressLevel ?: 0f) > 50 -> Color.parseColor("#FB8C00")  // средний — оранжевый
                    else -> Color.parseColor("#2E7D32")                              // нормальный — зелёный
                }
            )

            // График изначально скрыт
            chart.visibility = if (isExpanded) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                isExpanded = !isExpanded
                chart.visibility = if (isExpanded) View.VISIBLE else View.GONE

                if (isExpanded) {
                    showChart(session)
                }
            }
        }

        private fun showChart(session: SessionEntity) {
            val hrList = session.hrDataJson.split(",")
                .mapNotNull { it.trim().toIntOrNull() }

            val entries = hrList.mapIndexed { index, value ->
                Entry(index.toFloat(), value.toFloat())
            }

            val dataSet = LineDataSet(entries, "Пульс").apply {
                color = Color.parseColor("#7C0202")
                lineWidth = 3f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.LINEAR
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }

        private fun formatDuration(seconds: Int): String {
            val min = seconds / 60
            val sec = seconds % 60
            return String.format("%02d:%02d", min, sec)
        }
    }
}