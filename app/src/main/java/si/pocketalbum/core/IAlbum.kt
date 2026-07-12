package si.pocketalbum.core

import si.pocketalbum.core.models.AlbumInfo
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.ImageInfo
import si.pocketalbum.core.models.ImageThumbnail
import si.pocketalbum.core.models.Interval
import si.pocketalbum.core.models.MetadataModel
import si.pocketalbum.core.models.YearIndex

interface IAlbum {
    suspend fun getMetadata(): MetadataModel

    suspend fun getInfo(filter: FilterModel): AlbumInfo

    suspend fun getImageInfo(id: String): ImageInfo

    suspend fun getImageData(id: String): ByteArray

    suspend fun getImageThumbnail(id: String): ByteArray

    suspend fun list(filter: FilterModel, paging: Interval): List<ImageInfo>

    suspend fun listThumbnails(filter: FilterModel, paging: Interval): List<ImageThumbnail>

    suspend fun imageExists(id: String): Boolean

    suspend fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray)

    suspend fun getYearIndex(): List<YearIndex>

    suspend fun storeYearIndex(yearIndex: YearIndex)

    suspend fun removeYearIndex(year: Int)

    fun close()
}