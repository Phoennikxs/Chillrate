package com.example.chillrate

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillrate.api.RetrofitClient
import com.example.chillrate.model.GroupMemberAddRequest
import com.example.chillrate.model.GroupMember
import kotlinx.coroutines.launch

class GroupActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupMemberAdapter
    private val members = mutableListOf<GroupMember>()

    // Пока используем первую группу (в будущем можно выбирать)
    private var currentGroupId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        setupSideMenu()

        recyclerView = findViewById(R.id.recyclerViewGroupMembers)
        adapter = GroupMemberAdapter(members)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnAddMember).setOnClickListener {
            showAddMemberDialog()
        }

        loadGroupMembers()
    }

    private fun showAddMemberDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_member, null)
        val editEmail = dialogView.findViewById<EditText>(R.id.editEmail)

        AlertDialog.Builder(this)
            .setTitle("Добавить участника")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val email = editEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    addMemberByEmail(email)
                } else {
                    Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addMemberByEmail(email: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.addMemberToGroup(
                    groupId = currentGroupId,
                    request = GroupMemberAddRequest(email = email)
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@GroupActivity, "Пользователь добавлен", Toast.LENGTH_SHORT).show()
                    loadGroupMembers() // обновляем список
                } else {
                    Toast.makeText(this@GroupActivity, "Пользователь не найден или уже в группе", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadGroupMembers() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getGroupMembers(currentGroupId)

                if (response.isSuccessful) {
                    response.body()?.let { serverList ->
                        // Преобразуем GroupMemberOut → GroupMember
                        val mappedList = serverList.map { server ->
                            GroupMember(
                                id = server.id,
                                fullName = server.full_name,
                                email = server.email,
                                heartRate = server.heart_rate ?: 75,
                                stressLevel = server.stress_level ?: 40
                            )
                        }

                        members.clear()
                        members.addAll(mappedList)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupActivity, "Не удалось загрузить участников", Toast.LENGTH_SHORT).show()
            }
        }
    }
}