package si.pocketalbum.core

import si.pocketalbum.core.models.AlbumInfo
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.ImageInfo
import si.pocketalbum.core.models.ImageThumbnail
import si.pocketalbum.core.models.YearIndex

interface IAlbum {
    fun getInfo(): AlbumInfo

    fun getData(id: String): ByteArray

    fun getImages(filter: FilterModel): List<ImageThumbnail>

    fun imageExists(id: String): Boolean

    fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray)

    fun getYearIndex(): List<YearIndex>

    fun storeYearIndex(yearIndex: YearIndex)

    fun removeYearIndex(year: Int)
}