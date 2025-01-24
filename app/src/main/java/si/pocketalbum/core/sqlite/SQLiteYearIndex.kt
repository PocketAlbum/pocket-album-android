package si.pocketalbum.core.sqlite

data class SQLiteYearIndex(
    val year: Int,
    val count: Int,
    val crc: UInt,
    val size: ULong
)