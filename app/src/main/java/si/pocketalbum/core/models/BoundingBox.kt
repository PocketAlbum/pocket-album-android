package si.pocketalbum.core.models

data class BoundingBox(
    val maxLatitude: Double,
    val maxLongitude: Double,
    val minLatitude: Double,
    val minLongitude: Double
)
