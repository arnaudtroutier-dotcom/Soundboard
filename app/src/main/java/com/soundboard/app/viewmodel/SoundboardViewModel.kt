package com.soundboard.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soundboard.app.audio.AudioManager
import com.soundboard.app.audio.PlaybackState
import com.soundboard.app.data.database.AppDatabase
import com.soundboard.app.data.entities.Soundboard
import com.soundboard.app.data.entities.SoundFile
import com.soundboard.app.data.entities.Tile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TileWithSounds(
    val tile: Tile,
    val sounds: List<SoundFile>
)

data class SoundboardUiState(
    val soundboards: List<Soundboard> = emptyList(),
    val selectedSoundboardIndex: Int = 0,
    val tilesMap: Map<Long, List<TileWithSounds>> = emptyMap(),
    val isEditMode: Boolean = false,
    val playbackStates: Map<Long, PlaybackState> = emptyMap()
)

class SoundboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val soundboardDao = db.soundboardDao()
    private val tileDao = db.tileDao()
    private val soundFileDao = db.soundFileDao()

    val audioManager = AudioManager(application)

    private val _uiState = MutableStateFlow(SoundboardUiState())
    val uiState: StateFlow<SoundboardUiState> = _uiState.asStateFlow()

    // Per-soundboard tile flows
    private val tileFlows = mutableMapOf<Long, Job?>()
    private val tilesMap = MutableStateFlow<Map<Long, List<TileWithSounds>>>(emptyMap())

    init {
        viewModelScope.launch {
            soundboardDao.getAllSoundboards().collect { boards ->
                _uiState.update { it.copy(soundboards = boards) }
                // Subscribe to tiles for each board
                boards.forEach { board ->
                    if (!tileFlows.containsKey(board.id)) {
                        tileFlows[board.id] = viewModelScope.launch {
                            tileDao.getTilesForSoundboard(board.id).collect { tiles ->
                                val withSounds = tiles.map { tile ->
                                    TileWithSounds(
                                        tile = tile,
                                        sounds = soundFileDao.getSoundFilesForTileOnce(tile.id)
                                    )
                                }
                                val current = tilesMap.value.toMutableMap()
                                current[board.id] = withSounds
                                tilesMap.value = current
                                _uiState.update { s -> s.copy(tilesMap = tilesMap.value) }
                            }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            audioManager.playbackStates.collect { states ->
                _uiState.update { it.copy(playbackStates = states) }
            }
        }
    }

    // ── Soundboard operations ────────────────────────────────────────────────

    fun createSoundboard(name: String) {
        viewModelScope.launch {
            val order = _uiState.value.soundboards.size
            val id = soundboardDao.insert(Soundboard(name = name, orderIndex = order))
            val newIndex = _uiState.value.soundboards.indexOfFirst { it.id == id }
            if (newIndex >= 0) selectSoundboard(newIndex)
        }
    }

    fun renameSoundboard(id: Long, newName: String) {
        viewModelScope.launch { soundboardDao.rename(id, newName) }
    }

    fun deleteSoundboard(soundboard: Soundboard) {
        viewModelScope.launch {
            audioManager.stopAll()
            soundboardDao.delete(soundboard)
            tileFlows.remove(soundboard.id)
            val current = tilesMap.value.toMutableMap()
            current.remove(soundboard.id)
            tilesMap.value = current
        }
    }

    fun selectSoundboard(index: Int) {
        _uiState.update { it.copy(selectedSoundboardIndex = index.coerceIn(0, it.soundboards.size - 1)) }
    }

    // ── Edit mode ────────────────────────────────────────────────────────────

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled) }
    }

    // ── Tile operations ──────────────────────────────────────────────────────

    fun addTile(soundboardId: Long) {
        viewModelScope.launch {
            tileDao.insert(
                Tile(
                    soundboardId = soundboardId,
                    name = "Tuile",
                    posX = tileData.tile.posX,
                    posY = tileData.tile.posY,
                    width = 0.25f,
                    height = 0.13f
                )
            )
        }
    }

    fun updateTile(tile: Tile) {
        viewModelScope.launch { tileDao.update(tile) }
    }

    fun updateTilePosition(tile: Tile, posX: Float, posY: Float) {
        viewModelScope.launch {
            tileDao.update(tile.copy(posX = posX.coerceIn(0f, 1f), posY = posY.coerceIn(0f, 1f)))
        }
    }

    fun updateTileSize(tile: Tile, width: Float, height: Float) {
        viewModelScope.launch {
            tileDao.update(
                tile.copy(
                    width = width.coerceIn(0.08f, 1f),
                    height = height.coerceIn(0.05f, 1f)
                )
            )
        }
    }

    fun deleteTile(tile: Tile) {
        viewModelScope.launch {
            audioManager.getOrCreatePlayer(tile.id).stop()
            tileDao.delete(tile)
        }
    }

    // ── Sound file operations ────────────────────────────────────────────────

    fun addSoundToTile(tileId: Long, uri: String, displayName: String) {
        viewModelScope.launch {
            val existing = soundFileDao.getSoundFilesForTileOnce(tileId)
            soundFileDao.insert(
                SoundFile(
                    tileId = tileId,
                    uri = uri,
                    displayName = displayName,
                    orderIndex = existing.size
                )
            )
            // Refresh sounds for the tile's soundboard
            refreshTileSounds(tileId)
        }
    }

    fun removeSoundFromTile(soundFile: SoundFile) {
        viewModelScope.launch {
            soundFileDao.delete(soundFile)
            refreshTileSounds(soundFile.tileId)
        }
    }

    private suspend fun refreshTileSounds(tileId: Long) {
        val tile = tileDao.getTileById(tileId) ?: return
        val sounds = soundFileDao.getSoundFilesForTileOnce(tileId)
        val current = tilesMap.value.toMutableMap()
        val boardTiles = current[tile.soundboardId]?.toMutableList() ?: return
        val idx = boardTiles.indexOfFirst { it.tile.id == tileId }
        if (idx >= 0) boardTiles[idx] = TileWithSounds(tile, sounds)
        current[tile.soundboardId] = boardTiles
        tilesMap.value = current
        _uiState.update { s -> s.copy(tilesMap = tilesMap.value) }
    }

    // ── Playback ─────────────────────────────────────────────────────────────

    fun getSoundsFlow(tileId: Long) = soundFileDao.getSoundFilesForTile(tileId)
    fun handleTilePress(tileWithSounds: TileWithSounds) {
        val tile = tileWithSounds.tile
        val uris = tileWithSounds.sounds.map { it.uri }
        audioManager.handleTilePress(
            tileId = tile.id,
            soundUris = uris,
            loop = tile.loopEnabled,
            volume = tile.volume,
            onClickDuringPlayback = tile.onClickDuringPlayback
        )
    }

    // ── Import / Export ──────────────────────────────────────────────────────

    fun exportSoundboard(
        context: android.content.Context,
        soundboard: com.soundboard.app.data.entities.Soundboard,
        onSuccess: (java.io.File) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val tiles = tileDao.getTilesForSoundboard(soundboard.id).first()
                val soundsMap = tiles.associate { tile ->
                    tile.id to soundFileDao.getSoundFilesForTileOnce(tile.id)
                }
                val file = com.soundboard.app.data.exportSoundboard(
                    context, soundboard, tiles, soundsMap
                )
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(file)
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError(e.message ?: "Erreur inconnue")
                }
            }
        }
    }

    fun importSoundboard(
        context: android.content.Context,
        uri: android.net.Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val result = com.soundboard.app.data.importSoundboard(context, uri)
                val order = _uiState.value.soundboards.size
                val boardId = soundboardDao.insert(
                    com.soundboard.app.data.entities.Soundboard(
                        name = result.soundboardName,
                        orderIndex = order
                    )
                )
                result.tiles.forEach { tileData ->
                    val tile = tileData.tile
                    val tileId = tileDao.insert(
                        com.soundboard.app.data.entities.Tile(
                            soundboardId = boardId,
                            name = tile.name,
                            color = tile.color,
                            posX = tileData.tile.posX,
                            posY = tileData.tile.posY,
                            width = tile.width,
                            height = tile.height,
                            onClickDuringPlayback = tile.onClickDuringPlayback,
                            loopEnabled = tile.loopEnabled,
                            volume = tile.volume
                        )
                    )
                    tile.sounds.forEach { sf ->
                        val audioFile = tileData.audioFiles[sf.fileName]
                        if (audioFile != null && audioFile.exists()) {
                            soundFileDao.insert(
                                com.soundboard.app.data.entities.SoundFile(
                                    tileId = tileId,
                                    uri = android.net.Uri.fromFile(audioFile).toString(),
                                    displayName = sf.displayName,
                                    orderIndex = sf.orderIndex
                                )
                            )
                        }
                    }
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(result.soundboardName)
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError(e.message ?: "Erreur inconnue")
                }
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        audioManager.releaseAll()
    }
}

// Alias for coroutine Job used in map
private typealias Job = kotlinx.coroutines.Job
