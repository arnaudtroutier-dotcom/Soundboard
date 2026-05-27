package com.soundboard.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundboard.app.audio.PlaybackState
import com.soundboard.app.data.entities.Tile
import com.soundboard.app.ui.theme.*

@Composable
fun SoundTile(
    tile: Tile,
    isEditMode: Boolean,
    playbackState: PlaybackState,
    hasSounds: Boolean,
    containerWidthPx: Float,
    containerHeightPx: Float,
    onPress: () -> Unit,
    onEditRequest: () -> Unit,
    onMoveEnd: (Float, Float) -> Unit,
    onResizeEnd: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val tileColor = Color(tile.color)
    val isLight = tileColor.luminance() > 0.4f
    val contentColor = if (isLight) Color(0xFF1A1A1A) else Color.White

    // Use refs (not state) for gesture tracking — no recomposition side effects
    val isDragging = remember(tile.id) { mutableStateOf(false) }
    val isResizing = remember(tile.id) { mutableStateOf(false) }

    // Local display values — only updated from DB when fully idle
    val localX = remember(tile.id) { mutableStateOf(tile.posX) }
    val localY = remember(tile.id) { mutableStateOf(tile.posY) }
    val localW = remember(tile.id) { mutableStateOf(tile.width) }
    val localH = remember(tile.id) { mutableStateOf(tile.height) }

    // Sync from DB only when not gesturing
    if (!isDragging.value) {
        localX.value = tile.posX
        localY.value = tile.posY
    }
    if (!isResizing.value) {
        localW.value = tile.width
        localH.value = tile.height
    }

    val xDp: Dp = with(density) { (localX.value * containerWidthPx).toDp() }
    val yDp: Dp = with(density) { (localY.value * containerHeightPx).toDp() }
    val wDp: Dp = with(density) { (localW.value * containerWidthPx).toDp() }.coerceAtLeast(60.dp)
    val hDp: Dp = with(density) { (localH.value * containerHeightPx).toDp() }.coerceAtLeast(40.dp)

    val glowColor = when {
        playbackState.isPlaying -> PlayingGlow
        playbackState.isPaused  -> Amber400
        else                    -> Color.Transparent
    }

    Box(
        modifier = modifier
            .offset(x = xDp, y = yDp)
            .size(width = wDp, height = hDp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (playbackState.isPlaying || playbackState.isPaused)
                        Modifier.shadow(12.dp, RoundedCornerShape(10.dp), ambientColor = glowColor, spotColor = glowColor)
                    else Modifier
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(tileColor.copy(alpha = 0.95f), tileColor.copy(alpha = 0.75f))))
                .border(
                    width = if (playbackState.isPlaying) 2.dp else if (isEditMode) 1.dp else 0.5.dp,
                    color = when {
                        playbackState.isPlaying -> glowColor
                        playbackState.isPaused  -> Amber400.copy(alpha = 0.7f)
                        isEditMode              -> contentColor.copy(alpha = 0.4f)
                        else                    -> contentColor.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(10.dp)
                )
                .pointerInput(isEditMode) {
                    if (isEditMode) {
                        detectDragGestures(
                            onDragStart = { isDragging.value = true },
                            onDragEnd = {
                                onMoveEnd(localX.value, localY.value)
                                isDragging.value = false
                            },
                            onDragCancel = {
                                isDragging.value = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                localX.value = (localX.value + dragAmount.x / containerWidthPx).coerceIn(0f, 1f)
                                localY.value = (localY.value + dragAmount.y / containerHeightPx).coerceIn(0f, 1f)
                            }
                        )
                    } else {
                        detectTapGestures(onTap = { onPress() })
                    }
                }
                .pointerInput(isEditMode) {
                    if (isEditMode) detectTapGestures(onLongPress = { onEditRequest() })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (playbackState.isPlaying) {
                    Icon(Icons.Default.GraphicEq, null, tint = contentColor.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.height(2.dp))
                } else if (playbackState.isPaused) {
                    Icon(Icons.Default.Pause, null, tint = contentColor.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = tile.name,
                    color = contentColor,
                    fontSize = when { wDp < 80.dp -> 10.sp; wDp < 120.dp -> 12.sp; else -> 14.sp },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!hasSounds && isEditMode) {
                    Spacer(Modifier.height(2.dp))
                    Text("Aucun son", color = contentColor.copy(alpha = 0.5f), fontSize = 9.sp)
                }
            }
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DragIndicator, null, tint = contentColor.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                }
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 8.dp, bottomEnd = 10.dp))
                    .pointerInput(tile.id) {
                        detectDragGestures(
                            onDragStart = { isResizing.value = true },
                            onDragEnd = {
                                onResizeEnd(localW.value, localH.value)
                                isResizing.value = false
                            },
                            onDragCancel = {
                                isResizing.value = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                localW.value = (localW.value + dragAmount.x / containerWidthPx).coerceIn(0.08f, 1f)
                                localH.value = (localH.value + dragAmount.y / containerHeightPx).coerceIn(0.05f, 1f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.OpenInFull, "Redimensionner", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
            }
        }
    }
}