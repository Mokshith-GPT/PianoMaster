package com.pianomaster.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val durationSeconds: Int,
    val lessonId: String?,
    val notesAttempted: Int,
    val notesCorrect: Int,
    val source: String // microphone, midi, virtual
)
