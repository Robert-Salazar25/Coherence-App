package com.example.coherence.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.coherence.data.database.converter.Converters
import com.example.coherence.data.local.BiofeedbackDataEntity
import com.example.coherence.data.database.local.SessionDao
import com.example.coherence.data.local.SessionEntity

@Database(
    entities = [SessionEntity::class, BiofeedbackDataEntity::class],
    version = 1,
    exportSchema = false
)
// Aquí referenciamos la clase que acabamos de crear
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coherence_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}