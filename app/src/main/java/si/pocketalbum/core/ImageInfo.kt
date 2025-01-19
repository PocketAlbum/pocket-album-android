package si.pocketalbum.core

data class ImageInfo (
    val id: String,
    val filename: String,
    val created: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val latitude: Double?,
    val longitude: Double?
)