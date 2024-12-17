package si.kordez.pocketalbum.core

import java.util.Date

data class ImageInfo (
    val id: Int,
    val filename: String,
    val created: Date,
    val width: Int,
    val height: Int,
    val size: Long,
    val latitude: Double,
    val longitude: Double,
    val checksum: String
)