package si.kordez.pocketalbum.core.sqlite

data class SQLiteImage(
    val id: Int,
    val filename: String,
    val created: String,  // Use String for ISO 8601 date format; convert to/from Date as needed.
    val width: Int,
    val height: Int,
    val size: Long,
    val latitude: Double?,
    val longitude: Double?,
    val checksum: String,
    val thumbnail: ByteArray,
    val data: ByteArray
)