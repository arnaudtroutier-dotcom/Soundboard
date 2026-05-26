package com.soundboard.app.data.dao

import androidx.room.*
import com.soundboard.app.data.entities.Soundboard
import com.soundboard.app.data.entities.Tile
import com.soundboard.app.data.entities.SoundFile
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundboardDao {
    @Query("SELECT * FROM soundboards ORDER BY orderIndex ASC, createdAt ASC")
    fun getAllSoundboards(): Flow<List<Soundboard>>

    @Query("SELECT * FROM soundboards WHERE id = :id")
    suspend fun getSoundboardById(id: Long): Soundboard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(soundboard: Soundboard): Long

    @Update
    suspend fun update(soundboard: Soundboard)

    @Delete
    suspend fun delete(soundboard: Soundboard)

    @Query("UPDATE soundboards SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)
}

@Dao
interface TileDao {
    @Query("SELECT * FROM tiles WHERE soundboardId = :soundboardId")
    fun getTilesForSoundboard(soundboardId: Long): Flow<List<Tile>>

    @Query("SELECT * FROM tiles WHERE id = :id")
    suspend fun getTileById(id: Long): Tile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tile: Tile): Long

    @Update
    suspend fun update(tile: Tile)

    @Delete
    suspend fun delete(tile: Tile)

    @Query("DELETE FROM tiles WHERE id = :tileId")
    suspend fun deleteById(tileId: Long)
}

@Dao
interface SoundFileDao {
    @Query("SELECT * FROM sound_files WHERE tileId = :tileId ORDER BY orderIndex ASC")
    fun getSoundFilesForTile(tileId: Long): Flow<List<SoundFile>>

    @Query("SELECT * FROM sound_files WHERE tileId = :tileId ORDER BY orderIndex ASC")
    suspend fun getSoundFilesForTileOnce(tileId: Long): List<SoundFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(soundFile: SoundFile): Long

    @Delete
    suspend fun delete(soundFile: SoundFile)

    @Query("DELETE FROM sound_files WHERE tileId = :tileId")
    suspend fun deleteAllForTile(tileId: Long)

    @Query("DELETE FROM sound_files WHERE id = :id")
    suspend fun deleteById(id: Long)
}
