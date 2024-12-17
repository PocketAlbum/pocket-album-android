package si.kordez.pocketalbum.core

import android.R.bool
import android.R.string
import android.graphics.ImageDecoder.ImageInfo


interface IAlbum {
    fun getInfo(): AlbumInfo

    fun getImage(id: Int): ImageInfo

    fun getThumbnail(id: Int): ByteArray

    fun getData(id: Int): ByteArray

    fun getImages(from: Int, to: Int): List<ImageThumbnail>

    fun imageExists(checksum: string): bool

    fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray)
}