package com.example.chillrate.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    suspend fun insertSession(session: SessionEntity) {
        sessionDao.insertSession(session)
    }

    fun getAllSessions(): Flow<List<SessionEntity>> {
        return sessionDao.getAllSessions()
    }

    fun getSessionsByUser(email: String): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsByUser(email)
    }
}