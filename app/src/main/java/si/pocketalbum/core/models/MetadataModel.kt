package si.pocketalbum.core.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

public class MetadataModel (
    val id: UUID,
    val version: String,
    val name: String,
    val description: String?,
    val created: String,
    val updated: String
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    public fun create(name: String) : MetadataModel {
        return MetadataModel(
            id = UUID.randomUUID(),
            version = "PocketAlbum 1.0",
            name = name,
            description = null,
            created = LocalDateTime.now().format(formatter),
            updated = LocalDateTime.now().format(formatter)
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