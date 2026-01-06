package com.example.coherence.data.database.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coherence.data.local.BiofeedbackDataEntity
import com.example.coherence.data.local.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    // Guardar una sesión nueva (o reemplazar si ya existe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    // Guardar los datos detallados (latido a latido)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiofeedbackData(data: BiofeedbackDataEntity)

    // Obtener todas las sesiones ordenadas por fecha (la más reciente primero)
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    // Obtener una sesión específica por ID
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    // Borrar una sesión
    @Delete
    suspend fun deleteSession(session: SessionEntity)

    // Borrar todo (útil para limpiar datos)
    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()
}