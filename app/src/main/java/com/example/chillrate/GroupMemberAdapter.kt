package com.example.chillrate

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chillrate.model.GroupMember

class GroupMemberAdapter(
    private val members: List<GroupMember>
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount() = members.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.text_person_name)
        private val tvHR: TextView = itemView.findViewById(R.id.text_person_HR)
        private val tvStress: TextView = itemView.findViewById(R.id.text_person_stress)

        fun bind(member: GroupMember) {
            tvName.text = member.fullName
            tvHR.text = "${member.heartRate} уд/мин"
            tvStress.text = "${member.stressLevel}% стресс"

            tvHR.setTextColor(
                when {
                    member.heartRate >= 110 -> Color.parseColor("#E53935")
                    member.heartRate >= 90 -> Color.parseColor("#FB8C00")
                    else -> Color.parseColor("#2E7D32")
                }
            )
        }
    }
}