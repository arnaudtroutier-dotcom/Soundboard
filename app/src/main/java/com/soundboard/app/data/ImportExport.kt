package com.soundboard.app.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.soundboard.app.data.entities.SoundFile
import com.soundboard.app.data.entities.Soundboard
import com.soundboard.app.data.entities.Tile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ExportedSoundFile(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("orderIndex") val orderIndex: Int
)

data class ExportedTile(
    @SerializedName("name") val name: String,
    @SerializedName("color") val color: Long,
    @SerializedName("posX") val posX: Float,
    @SerializedName("posY") val posY: Float,
    @SerializedName("width") val width: Float,
    @SerializedName("height") val height: Float,
    @SerializedName("onClickDuringPlayback") val onClickDuringPlayback: String,
    @SerializedName("loopEnabled") val loopEnabled: Boolean,
    @SerializedName("volume") val volume: Float,
    @SerializedName("sounds") val sounds: List<ExportedSoundFile>
)

data class ExportedSoundboard(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("name") val name: String,
    @SerializedName("tiles") val tiles: List<ExportedTile>
)

fun exportSoundboard(
    context: Context,
    soundboard: Soundboard,
    tiles: List<Tile>,
    soundsMap: Map<Long, List<SoundFile>>
): File {
    val gson = com.google.gson.GsonBuilder().serializeSpecialFloatingPointValues().create()
    val exportDir = File(context.filesDir, "exports").also { it.mkdirs() }
    val safeName = soundboard.name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    val outFile = File(exportDir, "$safeName.soundboard")

    ZipOutputStream(BufferedOutputStream(FileOutputStream(outFile))).use { zos ->
        val exportedTiles = tiles.map { tile ->
            val sounds = soundsMap[tile.id] ?: emptyList()
            val exportedSounds = sounds.mapIndexed { idx, sf ->
                val ext = sf.displayName.substringAfterLast('.', "").let { if (it.isNotEmpty()) ".$it" else "" }
                val fileName = "audio_${tile.id}_${idx}$ext"
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(sf.uri))
                    if (inputStream != null) {
                        zos.putNextEntry(ZipEntry("audio/$fileName"))
                        inputStream.copyTo(zos)
                        zos.closeEntry()
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ExportedSoundFile(displayName = sf.displayName, fileName = fileName, orderIndex = sf.orderIndex)
            }
            ExportedTile(
                name = tile.name, color = tile.color,
                posX = tile.posX, posY = tile.posY,
                width = tile.width, height = tile.height,
                onClickDuringPlayback = tile.onClickDuringPlayback,
                loopEnabled = tile.loopEnabled, volume = tile.volume,
                sounds = exportedSounds
            )
        }

        val json = gson.toJson(ExportedSoundboard(name = soundboard.name, tiles = exportedTiles))
        zos.putNextEntry(ZipEntry("data.json"))
        zos.write(json.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    return outFile
}

data class ImportResult(
    val soundboardName: String,
    val tiles: List<ImportedTileData>
)

data class ImportedTileData(
    val tile: ExportedTile,
    val audioFiles: Map<String, File>
)

fun importSoundboard(context: Context, uri: Uri): ImportResult {
    val gson = com.google.gson.GsonBuilder().serializeSpecialFloatingPointValues().create()
    val audioDir = File(context.filesDir, "imported_audio").also { it.mkdirs() }
    val audioFiles = mutableMapOf<String, File>()
    var exportData: ExportedSoundboard? = null

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "data.json" -> {
                        val json = zis.readBytes().toString(Charsets.UTF_8)
                        exportData = gson.fromJson(json, ExportedSoundboard::class.java)
                    }
                    entry.name.startsWith("audio/") -> {
                        val fileName = entry.name.removePrefix("audio/")
                        val outFile = File(audioDir, "${System.currentTimeMillis()}_$fileName")
                        outFile.outputStream().use { zis.copyTo(it) }
                        audioFiles[fileName] = outFile
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    val data = exportData ?: throw IllegalArgumentException("Fichier .soundboard invalide")
    return ImportResult(
        soundboardName = data.name,
        tiles = data.tiles.map { ImportedTileData(tile = it, audioFiles = audioFiles) }
    )
}