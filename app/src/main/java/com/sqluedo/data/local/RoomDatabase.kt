package com.sqluedo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sqluedo.data.local.dao.JeuProgressionDao
import com.sqluedo.data.local.entity.JeuProgression
import com.sqluedo.data.local.utils.DateTimeConverters

@Database(
    entities = [JeuProgression::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class SQLuedoDatabase : RoomDatabase() {

    abstract fun jeuProgressionDao(): JeuProgressionDao

    companion object {
        @Volatile
        private var INSTANCE: SQLuedoDatabase? = null

        fun getDatabase(context: Context): SQLuedoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SQLuedoDatabase::class.java,
                    "sqluedo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}