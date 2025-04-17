@file:OptIn(ExperimentalUuidApi::class)

package si.pocketalbum.core.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MetadataModel (
    val id: Uuid,
    val version: String,
    val name: String,
    val description: String?,
    val created: LocalDateTime,
    val updated: LocalDateTime
) {
    companion object {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    }

    public fun create(name: String) : MetadataModel {
        return MetadataModel(
            id = Uuid.random(),
            version = "PocketAlbum 1.0",
            name = name,
            description = null,
            created = LocalDateTime.now(),
            updated = LocalDateTime.now()
        )
    }

    fun validate() {
        val formatRegex = Regex("PocketAlbum (\\d+)\\.(\\d+)")
        val match = formatRegex.find(version)
        if (match == null)
        {
            throw IllegalArgumentException("""Version '$version' is not recognized""")
        }
        val major = match.groups[1]?.value?.toInt()
        if (major != 1)
        {
            throw IllegalArgumentException("""Unsupported version '$version'""")
        }

        if (name.trim().isBlank())
        {
            throw IllegalArgumentException("No album name found")
        }
    }

}