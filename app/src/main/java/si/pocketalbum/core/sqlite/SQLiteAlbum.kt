package si.pocketalbum.core.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import si.pocketalbum.core.models.AlbumInfo
import si.pocketalbum.core.IAlbum
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.ImageInfo
import si.pocketalbum.core.models.ImageThumbnail
import si.pocketalbum.core.models.YearIndex
import java.io.Closeable
import java.io.File

class SQLiteAlbum(context: Context) : IAlbum, Closeable {
    private val dbFile = File(context.filesDir, "album.sqlite")
    private val dbHelper = DatabaseHelper(context, dbFile.absolutePath)
    private val db = dbHelper.writableDatabase

    override fun getInfo(): AlbumInfo {
        val cursor = db.rawQuery("SELECT DISTINCT substr(Created, 1, 4) FROM Image", null)

        val years = mutableListOf<Int>()
        cursor.use {
            while (cursor.moveToNext()) {
                years.add(cursor.getInt(0))
            }
        }

        return try {
            val imageCount = queryNumber(db, "SELECT COUNT(*) FROM Image").toInt()
            val dateCount = queryNumber(db, "SELECT COUNT(DISTINCT DATE(Created)) FROM Image").toInt()
            val thumbnailsSize = queryNumber(db, "SELECT SUM(LENGTH(Thumbnail)) FROM Image")
            val imagesSize = queryNumber(db, "SELECT SUM(LENGTH(Data)) FROM Image")
            AlbumInfo(imageCount, dateCount, thumbnailsSize, imagesSize, years)
        } catch (e: SQLiteException) {
            throw RuntimeException("Error fetching album info", e)
        }
    }

    private fun getWhere(filter: FilterModel): String? {
        if (filter.year != null) {
            if (filter.year.singleValue) {
                return """"Created LIKE '${filter.year.to}-%' """
            }
            else
            {
                return """"substr(Created, 1, 4) >= ${filter.year.from}
                    | AND substr(Created, 1, 4) <= ${filter.year.to} """.trimMargin()
            }
        }
        return null
    }

    private fun getLimit(filter: FilterModel): String? {
        if (filter.index != null) {
            val count = filter.index.to - filter.index.from + 1;
            return """${filter.index.from}, $count""";
        }
        return null
    }

    override fun getImages(filter: FilterModel): List<ImageThumbnail> {
        if (!filter.valid) {
            throw IllegalArgumentException(
                "At least one property of filter model must be filled")
        }

        val cursor = db.query("Image",
            arrayOf("Id", "Filename", "Created", "Width", "Height",
                "Size", "Crc", "Latitude", "Longitude", "Thumbnail"),
            getWhere(filter), null,
            null, null,
            "Created ASC",
            getLimit(filter))

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

    override fun getData(id: String): ByteArray {
        val cursor = db.query("Image", arrayOf("Data"),
            "Id = ?", arrayOf(id),
            null, null, null, null)

        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(cursor.getColumnIndexOrThrow("Data"))
            } else {
                throw IllegalArgumentException("Data for image id $id not found")
            }
        }
    }

    override fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray) {
        db.insert("Image", null, ContentValues().apply {
            put("Id", image.id)
            put("Filename", image.filename)
            put("Created", image.created)
            put("Width", image.width)
            put("Height", image.height)
            put("Size", image.size)
            put("Latitude", image.latitude)
            put("Longitude", image.longitude)
            put("Crc", image.crc.toInt())
            put("Thumbnail", thumbnail)
            put("Data", data)
        })
    }

    override fun imageExists(id: String): Boolean {
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
            crc = cursor.getInt(cursor.getColumnIndexOrThrow("Crc")).toUInt()
        )
    }

    override fun storeYearIndex(yearIndex: YearIndex) {
        db.insert("YearIndex", null, ContentValues().apply {
            put("Year", yearIndex.year)
            put("Count", yearIndex.count)
            put("Crc", yearIndex.crc.toInt())
            put("Size", yearIndex.size.toLong())
        })
    }

    override fun getYearIndex(): List<YearIndex> {
        val cursor = db.query("YearIndex", arrayOf("Year, Count, Crc, Size"),
            null, null, null, null, "Year")

        val index = mutableListOf<YearIndex>()
        cursor.use {
            while (cursor.moveToNext()) {
                index.add(YearIndex(
                    cursor.getInt(cursor.getColumnIndexOrThrow("Year")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("Count")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("Crc")).toUInt(),
                    cursor.getLong(cursor.getColumnIndexOrThrow("Size")).toULong(),
                ))
            }
        }
        return index
    }

    override fun removeYearIndex(year: Int) {
        db.delete("YearIndex", "Year = ?", arrayOf(year.toString()))
    }

    override fun close() {
        db.close()
    }
}
