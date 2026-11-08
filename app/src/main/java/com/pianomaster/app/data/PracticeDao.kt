package com.pianomaster.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {
    @Insert suspend fun insert(session: PracticeSessionEntity): Long
    @Query("SELECT * FROM practice_sessions ORDER BY startedAt DESC") fun observeAll(): Flow<List<PracticeSessionEntity>>
    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM practice_sessions") fun totalPracticeSeconds(): Flow<Int>
}
