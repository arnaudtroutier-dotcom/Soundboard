package com.soundboard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import com.soundboard.app.data.entities.Tile
import com.soundboard.app.ui.components.SoundTile
import com.soundboard.app.ui.components.TileEditorDialog
import com.soundboard.app.ui.theme.SurfaceDark
import com.soundboard.app.viewmodel.SoundboardViewModel
import com.soundboard.app.viewmodel.TileWithSounds

@Composable
fun SoundboardCanvas(
    soundboardId: Long,
    tiles: List<TileWithSounds>,
    isEditMode: Boolean,
    viewModel: SoundboardViewModel
) {
    var containerWidthPx by remember { mutableStateOf(1f) }
    var containerHeightPx by remember { mutableStateOf(1f) }

    var editingTile by remember { mutableStateOf<TileWithSounds?>(null) }

    val playbackStates by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .onGloballyPositioned { coords ->
                containerWidthPx = coords.size.width.toFloat().coerceAtLeast(1f)
                containerHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
            }
    ) {
        tiles.forEach { tileWithSounds ->
            val tile = tileWithSounds.tile
            val playback = playbackStates.playbackStates[tile.id]
                ?: com.soundboard.app.audio.PlaybackState()

            SoundTile(
                tile = tile,
                isEditMode = isEditMode,
                playbackState = playback,
                hasSounds = tileWithSounds.sounds.isNotEmpty(),
                containerWidthPx = containerWidthPx,
                containerHeightPx = containerHeightPx,
                onPress = {
                    if (!isEditMode) {
                        viewModel.handleTilePress(tileWithSounds)
                    }
                },
                onEditRequest = {
                    if (isEditMode) editingTile = tileWithSounds
                },
                onMoveEnd = { newX, newY ->
                    viewModel.updateTilePosition(tile, newX, newY)
                },
                onResizeEnd = { newW, newH ->
                    viewModel.updateTileSize(tile, newW, newH)
                }
            )
        }
    }

    // Tile editor dialog
    editingTile?.let { tws ->
        TileEditorDialog(
            tile = tws.tile,
            sounds = tws.sounds,
            onDismiss = { editingTile = null },
            onSave = { updatedTile ->
                viewModel.updateTile(updatedTile)
                editingTile = null
            },
            onAddSound = { uri, displayName ->
                viewModel.addSoundToTile(tws.tile.id, uri, displayName)
            },
            onRemoveSound = { soundFile ->
                viewModel.removeSoundFromTile(soundFile)
            },
            onDeleteTile = {
                viewModel.deleteTile(tws.tile)
                editingTile = null
            }
        )
    }
}
