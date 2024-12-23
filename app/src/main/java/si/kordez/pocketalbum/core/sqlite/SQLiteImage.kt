package si.kordez.pocketalbum.core.sqlite

data class SQLiteImage(
    val id: Int,
    val filename: String,
    val created: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val latitude: Double?,
    val longitude: Double?,
    val checksum: String,
    val thumbnail: ByteArray,
    val data: ByteArray
)