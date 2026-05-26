package com.soundboard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundboard.app.data.entities.Soundboard
import com.soundboard.app.ui.theme.*
import com.soundboard.app.viewmodel.SoundboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SoundboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val soundboards = uiState.soundboards
    val selectedIndex = uiState.selectedSoundboardIndex.coerceAtMost(
        (soundboards.size - 1).coerceAtLeast(0)
    )
    val isEditMode = uiState.isEditMode

    var showNewBoardDialog by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Soundboard?>(null) }
    var showDeleteBoardDialog by remember { mutableStateOf<Soundboard?>(null) }
    var showBoardMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceDark)) {

        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface1)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon / title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = Amber400,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Soundboard",
                    color = OnSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.weight(1f))

            // Stop all button
            if (soundboards.isNotEmpty()) {
                IconButton(onClick = { viewModel.audioManager.stopAll() }) {
                    Icon(Icons.Default.StopCircle, contentDescription = "Tout arrêter", tint = OnSurfaceDim)
                }
            }

            // Edit mode toggle
            if (soundboards.isNotEmpty()) {
                IconButton(onClick = { viewModel.toggleEditMode() }) {
                    Icon(
                        if (isEditMode) Icons.Default.CheckCircle else Icons.Default.Edit,
                        contentDescription = if (isEditMode) "Quitter l'édition" else "Mode édition",
                        tint = if (isEditMode) Amber400 else OnSurfaceDim
                    )
                }
            }

            // Board options menu
            if (soundboards.isNotEmpty()) {
                Box {
                    IconButton(onClick = { showBoardMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = OnSurfaceDim)
                    }
                    DropdownMenu(
                        expanded = showBoardMenu,
                        onDismissRequest = { showBoardMenu = false }
                    ) {
                        // Add tile
                        DropdownMenuItem(
                            text = { Text("Ajouter une tuile", color = OnSurface) },
                            leadingIcon = { Icon(Icons.Default.Add, null, tint = Amber400) },
                            onClick = {
                                showBoardMenu = false
                                soundboards.getOrNull(selectedIndex)?.let {
                                    viewModel.addTile(it.id)
                                    viewModel.setEditMode(true)
                                }
                            }
                        )
                        Divider(color = Surface4)
                        // Rename board
                        DropdownMenuItem(
                            text = { Text("Renommer ce soundboard", color = OnSurface) },
                            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, null, tint = OnSurfaceDim) },
                            onClick = {
                                showBoardMenu = false
                                renaming = soundboards.getOrNull(selectedIndex)
                            }
                        )
                        // Delete board
                        DropdownMenuItem(
                            text = { Text("Supprimer ce soundboard", color = DangerRed) },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = DangerRed) },
                            onClick = {
                                showBoardMenu = false
                                showDeleteBoardDialog = soundboards.getOrNull(selectedIndex)
                            }
                        )
                    }
                }
            }

            // Add soundboard
            IconButton(onClick = { showNewBoardDialog = true }) {
                Icon(Icons.Default.AddBox, contentDescription = "Nouveau soundboard", tint = Amber400)
            }
        }

        // Edit mode banner
        if (isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Amber600.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Amber400, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Mode édition — Glisser pour déplacer, poignée pour redimensionner, appui long pour éditer",
                    color = Amber100,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.setEditMode(false) }) {
                    Text("Terminer", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Soundboard tabs ────────────────────────────────────────────────
        if (soundboards.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                soundboards.forEachIndexed { index, board ->
                    SoundboardTab(
                        name = board.name,
                        selected = index == selectedIndex,
                        onClick = { viewModel.selectSoundboard(index) }
                    )
                }
            }
        }

        // ── Content area ───────────────────────────────────────────────────
        if (soundboards.isEmpty()) {
            EmptyState(onCreateBoard = { showNewBoardDialog = true })
        } else {
            val currentBoard = soundboards.getOrNull(selectedIndex)
            if (currentBoard != null) {
                val tiles = uiState.tilesMap[currentBoard.id] ?: emptyList()
                SoundboardCanvas(
                    soundboardId = currentBoard.id,
                    tiles = tiles,
                    isEditMode = isEditMode,
                    viewModel = viewModel
                )
            }
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────

    if (showNewBoardDialog) {
        NameInputDialog(
            title = "Nouveau soundboard",
            placeholder = "Mon soundboard",
            confirmLabel = "Créer",
            onConfirm = { name ->
                viewModel.createSoundboard(name)
                showNewBoardDialog = false
            },
            onDismiss = { showNewBoardDialog = false }
        )
    }

    renaming?.let { board ->
        NameInputDialog(
            title = "Renommer",
            placeholder = board.name,
            initialValue = board.name,
            confirmLabel = "Renommer",
            onConfirm = { newName ->
                viewModel.renameSoundboard(board.id, newName)
                renaming = null
            },
            onDismiss = { renaming = null }
        )
    }

    showDeleteBoardDialog?.let { board ->
        AlertDialog(
            onDismissRequest = { showDeleteBoardDialog = null },
            tonalElevation = 0.dp,
            title = { Text("Supprimer « ${board.name} » ?", color = OnSurface) },
            text = { Text("Toutes les tuiles et sons associés seront supprimés. Cette action est irréversible.", color = OnSurfaceDim) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSoundboard(board)
                    showDeleteBoardDialog = null
                }) {
                    Text("Supprimer", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBoardDialog = null }) {
                    Text("Annuler", color = OnSurfaceDim)
                }
            }
        )
    }
}

@Composable
fun SoundboardTab(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Amber600 else Surface3,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(
                name,
                color = if (selected) Color(0xFF1A0F00) else OnSurfaceDim,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyState(onCreateBoard: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = Surface4,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Aucun soundboard",
                color = OnSurfaceDim,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Créez votre premier soundboard\npour commencer.",
                color = OnSurfaceFaint,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onCreateBoard,
                colors = ButtonDefaults.buttonColors()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Créer un soundboard", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun NameInputDialog(
    title: String,
    placeholder: String,
    initialValue: String = "",
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        tonalElevation = 0.dp,
        title = { Text(title, color = OnSurface) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(placeholder, color = OnSurfaceFaint) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber400,
                    unfocusedBorderColor = Surface4,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    cursorColor = Amber400,
                    focusedContainerColor = Surface1,
                    unfocusedContainerColor = Surface1
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.ifBlank { placeholder }) },
                colors = ButtonDefaults.buttonColors(),
                enabled = true
            ) {
                Text(confirmLabel, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = OnSurfaceDim)
            }
        }
    )
}
