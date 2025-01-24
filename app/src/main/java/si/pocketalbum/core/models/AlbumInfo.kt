package si.pocketalbum.core.models

data class AlbumInfo(
    val imageCount: Int,
    val dateCount: Int,
    val thumbnailsSize: Long,
    val imagesSize: Long,
    val years: List<Int>
)
