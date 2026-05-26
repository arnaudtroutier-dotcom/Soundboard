package com.soundboard.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.soundboard.app.data.entities.SoundFile
import com.soundboard.app.data.entities.Tile
import com.soundboard.app.ui.theme.*

@Composable
fun TileEditorDialog(
    tile: Tile,
    sounds: List<SoundFile>,
    onDismiss: () -> Unit,
    onSave: (Tile) -> Unit,
    onAddSound: (uri: String, displayName: String) -> Unit,
    onRemoveSound: (SoundFile) -> Unit,
    onDeleteTile: () -> Unit
) {
    var name by remember { mutableStateOf(tile.name) }
    var selectedColor by remember { mutableStateOf(Color(tile.color)) }
    var onClickMode by remember { mutableStateOf(tile.onClickDuringPlayback) }
    var loopEnabled by remember { mutableStateOf(tile.loopEnabled) }
    var volume by remember { mutableStateOf(tile.volume) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            // Persist permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val displayName = uri.lastPathSegment
                ?.substringAfterLast('/')
                ?.substringAfterLast(':')
                ?: uri.toString().takeLast(30)
            onAddSound(uri.toString(), displayName)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Surface1,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface2)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Amber400, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Modifier la tuile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = OnSurfaceDim)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Name
                    item {
                        SectionLabel("NOM")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber400,
                                unfocusedBorderColor = Surface4,
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                cursorColor = Amber400,
                                focusedContainerColor = Surface2,
                                unfocusedContainerColor = Surface2
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Color
                    item {
                        SectionLabel("COULEUR")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TilePresets.forEach { presetColor ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(presetColor)
                                        .border(
                                            width = if (selectedColor == presetColor) 3.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = presetColor }
                                )
                            }
                            // Custom color button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Surface3)
                                    .border(1.dp, Surface4, CircleShape)
                                    .clickable { showColorPicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = "Couleur personnalisée",
                                    tint = OnSurfaceDim,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Sons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionLabel("SONS", modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ajouter", color = Amber400, fontSize = 13.sp)
                            }
                        }

                        if (sounds.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Surface2, RoundedCornerShape(10.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Aucun son associé.\nAjoutez un ou plusieurs fichiers audio.",
                                    color = OnSurfaceDim,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                sounds.forEach { soundFile ->
                                    SoundFileRow(
                                        soundFile = soundFile,
                                        onRemove = { onRemoveSound(soundFile) }
                                    )
                                }
                                if (sounds.size > 1) {
                                    Text(
                                        "• Si plusieurs sons sont associés, un est joué aléatoirement.",
                                        color = OnSurfaceDim,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Playback settings
                    item {
                        SectionLabel("LECTURE")

                        // Loop toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface2, RoundedCornerShape(10.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = OnSurfaceDim, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Boucle (loop)", color = OnSurface, modifier = Modifier.weight(1f))
                            Switch(
                                checked = loopEnabled,
                                onCheckedChange = { loopEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Amber400,
                                    checkedTrackColor = Amber600
                                )
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // On click during playback
                        Text(
                            "Si on clique pendant la lecture :",
                            color = OnSurfaceDim,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClickModeChip(
                                label = "Pause / Reprendre",
                                selected = onClickMode == "PAUSE",
                                icon = Icons.Default.PauseCircle,
                                onClick = { onClickMode = "PAUSE" }
                            )
                            ClickModeChip(
                                label = "Arrêt total",
                                selected = onClickMode == "STOP",
                                icon = Icons.Default.StopCircle,
                                onClick = { onClickMode = "STOP" }
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Volume
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = OnSurfaceDim, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Volume", color = OnSurfaceDim, fontSize = 12.sp, modifier = Modifier.width(56.dp))
                            Slider(
                                value = volume,
                                onValueChange = { volume = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Amber400,
                                    activeTrackColor = Amber500
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${(volume * 100).toInt()}%",
                                color = OnSurfaceDim,
                                fontSize = 12.sp,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }

                    // Danger zone
                    item {
                        Divider(color = Surface4)
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed)
                            Spacer(Modifier.width(6.dp))
                            Text("Supprimer cette tuile", color = DangerRed)
                        }
                    }
                }

                // Bottom save bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface2)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Surface4),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceDim)
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = {
                            onSave(
                                tile.copy(
                                    name = name.ifBlank { "Tuile" },
                                    color = selectedColor.toArgb().toLong() and 0xFFFFFFFFL,
                                    onClickDuringPlayback = onClickMode,
                                    loopEnabled = loopEnabled,
                                    volume = volume
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enregistrer", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Delete confirm dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            tonalElevation = 0.dp,
            title = { Text("Supprimer la tuile ?", color = OnSurface) },
            text = { Text("Cette action est irréversible. Les sons associés seront également supprimés.", color = OnSurfaceDim) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteTile()
                    onDismiss()
                }) {
                    Text("Supprimer", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler", color = OnSurfaceDim)
                }
            }
        )
    }
}

@Composable
fun SoundFileRow(soundFile: SoundFile, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AudioFile, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            soundFile.displayName,
            color = OnSurface,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = DangerRed, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ClickModeChip(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Amber500 else Surface2,
        border = BorderStroke(1.dp, if (selected) Amber400 else Surface4),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) Color(0xFF1A0F00) else OnSurfaceDim,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = if (selected) Color(0xFF1A0F00) else OnSurfaceDim,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = Amber500,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = modifier.padding(bottom = 6.dp)
    )
}
