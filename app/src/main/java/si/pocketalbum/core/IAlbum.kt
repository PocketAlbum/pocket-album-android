package si.pocketalbum.core

interface IAlbum {
    fun getInfo(): AlbumInfo

    fun getImage(id: String): ImageInfo

    fun getThumbnail(id: String): ByteArray

    fun getThumbnail(number: Int): ByteArray

    fun getData(id: String): ByteArray

    fun getImage(number: Int): ImageThumbnail

    fun getImages(from: Int, to: Int): List<ImageThumbnail>

    fun imageExists(id: String): Boolean

    fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray)
}