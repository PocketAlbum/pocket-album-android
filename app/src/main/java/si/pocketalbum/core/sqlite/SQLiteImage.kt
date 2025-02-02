package si.pocketalbum.core.sqlite

data class SQLiteImage(
    val id: Int,
    val filename: String,
    val created: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val latitude: Double?,
    val longitude: Double?,
    val thumbnail: ByteArray,
    val data: ByteArray,
    val crc: UInt
)