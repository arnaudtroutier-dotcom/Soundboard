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
import androidx.compose.ui.geometry.Offset
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

    // Pixel dimensions
    val tileWidthPx = tile.width * containerWidthPx
    val tileHeightPx = tile.height * containerHeightPx
    val tileXPx = tile.posX * containerWidthPx
    val tileYPx = tile.posY * containerHeightPx

    val tileWidthDp: Dp = with(density) { tileWidthPx.toDp() }
    val tileHeightDp: Dp = with(density) { tileHeightPx.toDp() }
    val tileXDp: Dp = with(density) { tileXPx.toDp() }
    val tileYDp: Dp = with(density) { tileYPx.toDp() }

    // Drag state
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Resize state
    var resizeDeltaW by remember { mutableStateOf(0f) }
    var resizeDeltaH by remember { mutableStateOf(0f) }

    val glowColor = when {
        playbackState.isPlaying -> PlayingGlow
        playbackState.isPaused  -> Amber400
        else                    -> Color.Transparent
    }

    val extraOffX = with(density) { dragOffsetX.toDp() }
    val extraOffY = with(density) { dragOffsetY.toDp() }
    val extraW = with(density) { resizeDeltaW.toDp() }
    val extraH = with(density) { resizeDeltaH.toDp() }

    Box(
        modifier = modifier
            .offset(x = tileXDp + extraOffX, y = tileYDp + extraOffY)
            .size(
                width = (tileWidthDp + extraW).coerceAtLeast(60.dp),
                height = (tileHeightDp + extraH).coerceAtLeast(40.dp)
            )
    ) {
        // Main tile body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (playbackState.isPlaying || playbackState.isPaused)
                        Modifier.shadow(12.dp, RoundedCornerShape(10.dp), ambientColor = glowColor, spotColor = glowColor)
                    else Modifier
                )
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            tileColor.copy(alpha = 0.95f),
                            tileColor.copy(alpha = 0.75f)
                        )
                    )
                )
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
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val newX = ((tileXPx + dragOffsetX) / containerWidthPx).coerceIn(0f, 1f)
                                val newY = ((tileYPx + dragOffsetY) / containerHeightPx).coerceIn(0f, 1f)
                                onMoveEnd(newX, newY)
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                isDragging = false
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount.x
                                dragOffsetY += dragAmount.y
                            }
                        )
                    } else {
                        detectTapGestures(onTap = { onPress() })
                    }
                }
                .pointerInput(isEditMode) {
                    if (isEditMode) {
                        detectTapGestures(onLongPress = { onEditRequest() })
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Playback indicator
                if (playbackState.isPlaying) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                } else if (playbackState.isPaused) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                }

                Text(
                    text = tile.name,
                    color = contentColor,
                    fontSize = when {
                        tileWidthDp < 80.dp -> 10.sp
                        tileWidthDp < 120.dp -> 12.sp
                        else -> 14.sp
                    },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!hasSounds && isEditMode) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Aucun son",
                        color = contentColor.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
            }

            // Edit overlay indicator
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DragIndicator,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Resize handle (bottom-right corner) — only in edit mode
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(topStart = 8.dp, bottomEnd = 10.dp)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val newW = ((tileWidthPx + resizeDeltaW) / containerWidthPx).coerceIn(0.08f, 1f)
                                val newH = ((tileHeightPx + resizeDeltaH) / containerHeightPx).coerceIn(0.05f, 1f)
                                onResizeEnd(newW, newH)
                                resizeDeltaW = 0f
                                resizeDeltaH = 0f
                            },
                            onDragCancel = {
                                resizeDeltaW = 0f
                                resizeDeltaH = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                resizeDeltaW += dragAmount.x
                                resizeDeltaH += dragAmount.y
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.OpenInFull,
                    contentDescription = "Redimensionner",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
