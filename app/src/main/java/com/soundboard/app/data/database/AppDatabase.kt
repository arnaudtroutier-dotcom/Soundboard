package com.soundboard.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.soundboard.app.data.dao.SoundboardDao
import com.soundboard.app.data.dao.SoundFileDao
import com.soundboard.app.data.dao.TileDao
import com.soundboard.app.data.entities.Soundboard
import com.soundboard.app.data.entities.SoundFile
import com.soundboard.app.data.entities.Tile

@Database(
    entities = [Soundboard::class, Tile::class, SoundFile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundboardDao(): SoundboardDao
    abstract fun tileDao(): TileDao
    abstract fun soundFileDao(): SoundFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soundboard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
