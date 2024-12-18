package si.kordez.pocketalbum.core.sqlite

import DatabaseHelper
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import getDoubleOrNull
import si.kordez.pocketalbum.core.AlbumInfo
import si.kordez.pocketalbum.core.IAlbum
import si.kordez.pocketalbum.core.ImageInfo
import si.kordez.pocketalbum.core.ImageThumbnail

class SQLiteAlbum(context: Context) : IAlbum {
    private val dbHelper = DatabaseHelper(context, "album.sqlite")

    override fun getInfo(): AlbumInfo {
        val db = dbHelper.readableDatabase
        return try {
            val imageCount = queryNumber(db, "SELECT COUNT(*) FROM Image").toInt()
            val dateCount = queryNumber(db, "SELECT COUNT(DISTINCT DATE(Created)) FROM Image").toInt()
            val thumbnailsSize = queryNumber(db, "SELECT SUM(LENGTH(Thumbnail)) FROM Image")
            val imagesSize = queryNumber(db, "SELECT SUM(LENGTH(Data)) FROM Image")
            AlbumInfo(imageCount, dateCount, thumbnailsSize, imagesSize)
        } catch (e: SQLiteException) {
            throw RuntimeException("Error fetching album info", e)
        } finally {
            db.close()
        }
    }

    override fun getImage(id: String): ImageInfo {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT Id, Filename, Created, Width, Height, Size, Latitude, Longitude
            FROM Image WHERE Id = ?
        """
        val cursor = db.rawQuery(query, arrayOf(id))
        cursor.use {
            if (cursor.moveToFirst()) {
                return convertImage(cursor)
            } else {
                throw IllegalArgumentException("Image with id $id not found")
            }
        }
    }

    override fun getImage(number: Int): ImageThumbnail {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT Id, Filename, Created, Width, Height, Size, Latitude, Longitude, Thumbnail
            FROM Image
            ORDER BY Created DESC
            LIMIT ?, 1
        """
        val cursor = db.rawQuery(query, arrayOf(number.toString()))
        cursor.use {
            cursor.moveToFirst()
            val image = convertImage(cursor)
            val thumbnail = cursor.getBlob(cursor.getColumnIndexOrThrow("Thumbnail"))
            return ImageThumbnail(image, thumbnail)
        }
    }

    override fun getImages(from: Int, to: Int): List<ImageThumbnail> {
        if (to < from) {
            throw IllegalArgumentException("From index must be lower than to")
        }

        val db = dbHelper.readableDatabase
        val limit = to - from + 1
        val query = """
            SELECT Id, Filename, Created, Width, Height, Size, Latitude, Longitude, Thumbnail
            FROM Image
            ORDER BY Created DESC
            LIMIT ?, ?
        """
        val cursor = db.rawQuery(query, arrayOf(from.toString(), limit.toString()))
        val thumbnails = mutableListOf<ImageThumbnail>()
        cursor.use {
            while (cursor.moveToNext()) {
                val image = convertImage(cursor)
                val thumbnail = cursor.getBlob(cursor.getColumnIndexOrThrow("Thumbnail"))
                thumbnails.add(ImageThumbnail(image, thumbnail))
            }
        }
        return thumbnails
    }

    override fun getThumbnail(id: String): ByteArray {
        val db = dbHelper.readableDatabase
        val query = "SELECT Thumbnail FROM Image WHERE Id = ?"
        val cursor = db.rawQuery(query, arrayOf(id))
        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(cursor.getColumnIndexOrThrow("Thumbnail"))
            } else {
                throw IllegalArgumentException("Thumbnail for image id $id not found")
            }
        }
    }

    override fun getThumbnail(number: Int): ByteArray {
        val db = dbHelper.readableDatabase
        val query = "SELECT Thumbnail FROM Image ORDER BY Created DESC LIMIT ?, 1"
        val cursor = db.rawQuery(query, arrayOf(number.toString()))
        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(cursor.getColumnIndexOrThrow("Thumbnail"))
            } else {
                throw IllegalArgumentException("Thumbnail for image $number not found")
            }
        }
    }

    override fun getData(id: String): ByteArray {
        val db = dbHelper.readableDatabase
        val query = "SELECT Data FROM Image WHERE Id = ?"
        val cursor = db.rawQuery(query, arrayOf(id))
        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(cursor.getColumnIndexOrThrow("Data"))
            } else {
                throw IllegalArgumentException("Data for image id $id not found")
            }
        }
    }

    override fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray) {
        val db = dbHelper.writableDatabase
        val query = """
            INSERT INTO Image 
            (Id, Filename, Created, Width, Height, Size, Latitude, Longitude, Thumbnail, Data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        db.execSQL(
            query,
            arrayOf(
                image.id,
                image.filename,
                image.created,
                image.width,
                image.height,
                image.size,
                image.latitude,
                image.longitude,
                thumbnail,
                data
            )
        )
    }

    override fun imageExists(id: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT EXISTS(SELECT 1 FROM Image WHERE Id = ?)
        """
        val cursor = db.rawQuery(query, arrayOf(id))
        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 1
            }
        }
        return false
    }

    private fun queryNumber(db: SQLiteDatabase, query: String): Long {
        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return 0
    }

    private fun convertImage(cursor: Cursor): ImageInfo {
        return ImageInfo(
            id = cursor.getString(cursor.getColumnIndexOrThrow("Id")),
            filename = cursor.getString(cursor.getColumnIndexOrThrow("Filename")),
            created = cursor.getString(cursor.getColumnIndexOrThrow("Created")),
            width = cursor.getInt(cursor.getColumnIndexOrThrow("Width")),
            height = cursor.getInt(cursor.getColumnIndexOrThrow("Height")),
            size = cursor.getLong(cursor.getColumnIndexOrThrow("Size")),
            latitude = cursor.getDoubleOrNull("Latitude"),
            longitude = cursor.getDoubleOrNull("Longitude"),
        )
    }
}
