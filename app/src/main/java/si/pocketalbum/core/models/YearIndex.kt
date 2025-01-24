package si.pocketalbum.core.models

data class YearIndex(
    val year: Int,
    val count: Int,
    val crc: UInt,
    val size: ULong
)