package com.example.chillrate

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillrate.model.GroupMember

class GroupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        setupSideMenu()

        try {
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewGroupMembers)

            val sampleMembers = listOf(
                GroupMember(1, "Иванов Иван", 78, 45),
                GroupMember(2, "Петрова Анна", 95, 72),
                GroupMember(3, "Сидоров Алексей", 65, 33),
                GroupMember(4, "Кузнецова Мария", 112, 81)
            )

            val adapter = GroupMemberAdapter(sampleMembers)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка инициализации группы: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}