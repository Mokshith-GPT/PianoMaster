package com.pianomaster.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PracticeSessionEntity::class], version = 1, exportSchema = false)
abstract class PianoDatabase : RoomDatabase() { abstract fun practiceDao(): PracticeDao }
