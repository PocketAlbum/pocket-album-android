package si.pocketalbum.core.models

import org.mapsforge.core.model.LatLong

data class BoundingBox(
    val maxLatitude: Double,
    val maxLongitude: Double,
    val minLatitude: Double,
    val minLongitude: Double
) {
    companion object {
        fun fromMapsforge(bb: org.mapsforge.core.model.BoundingBox): BoundingBox {
            return BoundingBox(bb.maxLatitude, bb.maxLongitude, bb.minLatitude, bb.minLongitude)
        }
    }

    val width get() = maxLongitude - minLongitude
    val height get() = maxLatitude - minLatitude
    private val center get() = LatLong(
        (maxLatitude + minLatitude) / 2.0,
        (maxLongitude + minLongitude) / 2.0)
    val northWest get() = LatLong(maxLatitude, minLongitude)
    val northEast get() = LatLong(maxLatitude, maxLongitude)
    val southWest get() = LatLong(minLatitude, minLongitude)
    val southEast get() = LatLong(minLatitude, maxLongitude)

    fun containsLatLong(latLong: LatLong): Boolean {
         return !(latLong.latitude < minLatitude ||
            latLong.latitude > maxLatitude ||
            latLong.longitude < minLongitude ||
            latLong.longitude > maxLongitude)
    }

    fun getSquareBox(): BoundingBox {
        if (width < height) {
            val halfSpan = height / 2.0
            return BoundingBox(maxLatitude,
                center.longitude + halfSpan,
                minLatitude,
                center.longitude - halfSpan)
        }
        else {
            val halfSpan = width / 2.0
            return BoundingBox(
                center.latitude + halfSpan,
                maxLongitude,
                center.latitude - halfSpan,
                minLongitude)
        }
    }
}
