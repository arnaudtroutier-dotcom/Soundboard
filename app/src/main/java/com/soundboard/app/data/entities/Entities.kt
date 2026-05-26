package com.soundboard.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "soundboards")
data class Soundboard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)

@Entity(
    tableName = "tiles",
    foreignKeys = [
        ForeignKey(
            entity = Soundboard::class,
            parentColumns = ["id"],
            childColumns = ["soundboardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("soundboardId")]
)
data class Tile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val soundboardId: Long,
    val name: String,
    val color: Long = 0xFFD4901A, // amber/gold default
    // Position & size as fraction of container (0f..1f)
    val posX: Float = 0f,
    val posY: Float = 0f,
    val width: Float = 0.22f,
    val height: Float = 0.12f,
    // Playback settings
    val onClickDuringPlayback: String = "PAUSE", // "PAUSE" or "STOP"
    val loopEnabled: Boolean = false,
    val volume: Float = 1.0f
)

@Entity(
    tableName = "sound_files",
    foreignKeys = [
        ForeignKey(
            entity = Tile::class,
            parentColumns = ["id"],
            childColumns = ["tileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tileId")]
)
data class SoundFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tileId: Long,
    val uri: String,
    val displayName: String,
    val orderIndex: Int = 0
)
