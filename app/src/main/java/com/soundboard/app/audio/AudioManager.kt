package com.soundboard.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentUri: String? = null
)

class TileAudioPlayer(
    private val context: Context,
    val tileId: Long
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    fun play(
        uri: String,
        loop: Boolean,
        volume: Float,
        onComplete: () -> Unit
    ) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(uri))
                isLooping = loop
                setVolume(volume, volume)
                setOnCompletionListener {
                    if (!loop) {
                        _state.value = PlaybackState()
                        onComplete()
                    }
                }
                prepare()
                start()
            }
            _state.value = PlaybackState(isPlaying = true, isPaused = false, currentUri = uri)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = PlaybackState()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _state.value = _state.value.copy(isPlaying = false, isPaused = true)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _state.value = _state.value.copy(isPlaying = true, isPaused = false)
        }
    }

    fun stop() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
        _state.value = PlaybackState()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = PlaybackState()
    }

    val isPlaying: Boolean get() = mediaPlayer?.isPlaying == true
    val isPaused: Boolean get() = _state.value.isPaused
}

class AudioManager(private val context: Context) {

    private val players = mutableMapOf<Long, TileAudioPlayer>()

    private val _playbackStates = MutableStateFlow<Map<Long, PlaybackState>>(emptyMap())
    val playbackStates: StateFlow<Map<Long, PlaybackState>> = _playbackStates.asStateFlow()

    fun getOrCreatePlayer(tileId: Long): TileAudioPlayer {
        return players.getOrPut(tileId) { TileAudioPlayer(context, tileId) }
    }

    fun handleTilePress(
        tileId: Long,
        soundUris: List<String>,
        loop: Boolean,
        volume: Float,
        onClickDuringPlayback: String // "PAUSE" or "STOP"
    ) {
        if (soundUris.isEmpty()) return

        val player = getOrCreatePlayer(tileId)

        when {
            player.isPlaying -> {
                when (onClickDuringPlayback) {
                    "PAUSE" -> {
                        player.pause()
                        updateState(tileId, player.state.value)
                    }
                    "STOP" -> {
                        player.stop()
                        updateState(tileId, PlaybackState())
                    }
                }
            }
            player.isPaused -> {
                player.resume()
                updateState(tileId, player.state.value)
            }
            else -> {
                val uri = soundUris.random()
                player.play(uri, loop, volume) {
                    updateState(tileId, PlaybackState())
                }
                updateState(tileId, PlaybackState(isPlaying = true, currentUri = uri))
            }
        }
    }

    private fun updateState(tileId: Long, state: PlaybackState) {
        val current = _playbackStates.value.toMutableMap()
        current[tileId] = state
        _playbackStates.value = current
    }

    fun stopAll() {
        players.forEach { (id, player) ->
            player.stop()
            updateState(id, PlaybackState())
        }
    }

    fun releaseAll() {
        players.forEach { (_, player) -> player.release() }
        players.clear()
        _playbackStates.value = emptyMap()
    }

    fun getStateForTile(tileId: Long): PlaybackState {
        return _playbackStates.value[tileId] ?: PlaybackState()
    }
}
