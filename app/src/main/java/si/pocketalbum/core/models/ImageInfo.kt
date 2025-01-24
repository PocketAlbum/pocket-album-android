package si.pocketalbum.core.models

data class ImageInfo (
    val id: String,
    val filename: String,
    val created: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val latitude: Double?,
    val longitude: Double?,
    val crc: UInt
)