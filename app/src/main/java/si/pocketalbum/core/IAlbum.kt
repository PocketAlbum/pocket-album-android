package si.pocketalbum.core

import si.pocketalbum.core.models.AlbumInfo
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.ImageInfo
import si.pocketalbum.core.models.ImageThumbnail
import si.pocketalbum.core.models.Interval
import si.pocketalbum.core.models.MetadataModel
import si.pocketalbum.core.models.YearIndex

interface IAlbum {
    fun getMetadata(): MetadataModel

    fun getInfo(filter: FilterModel): AlbumInfo

    fun getData(id: String): ByteArray

    fun getImages(filter: FilterModel, paging: Interval): List<ImageThumbnail>

    fun imageExists(id: String): Boolean

    fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray)

    fun getYearIndex(): List<YearIndex>

    fun storeYearIndex(yearIndex: YearIndex)

    fun removeYearIndex(year: Int)

    fun close()
}