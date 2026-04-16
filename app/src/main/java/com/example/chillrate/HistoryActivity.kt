package com.example.chillrate

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillrate.data.AppDatabase
import com.example.chillrate.data.SessionEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Настройка бокового меню из BaseActivity
        setupSideMenu()

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistory)

        sessionAdapter = SessionAdapter { session ->
            openSessionDetail(session)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = sessionAdapter

        loadSessions()
    }

    private fun loadSessions() {
        val db = AppDatabase.getDatabase(this)
        val userEmail = getCurrentUserEmail()

        lifecycleScope.launch {
            db.sessionDao().getSessionsByUser(userEmail)
                .collectLatest { sessions ->
                    if (sessions.isEmpty()) {
                        Toast.makeText(this@HistoryActivity, "Пока нет сохранённых сеансов", Toast.LENGTH_SHORT).show()
                    }
                    sessionAdapter.submitList(sessions)
                }
        }
    }

    private fun openSessionDetail(session: SessionEntity) {
        // Пока просто показываем информацию о сеансе
        Toast.makeText(
            this,
            "Сеанс от ${SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(session.startTime)}\n" +
                    "Длительность: ${formatDuration(session.durationSeconds)}\n" +
                    "Средний пульс: ${session.averageHR} уд/мин",
            Toast.LENGTH_LONG
        ).show()

        // В будущем здесь будет переход на детальный экран с графиком:
        // val intent = Intent(this, SessionDetailActivity::class.java)
        // intent.putExtra("SESSION_ID", session.id)
        // startActivity(intent)
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun getCurrentUserEmail(): String {
        return getSharedPreferences("app", MODE_PRIVATE)
            .getString("user_email", "unknown@user.com") ?: "unknown@user.com"
    }
}