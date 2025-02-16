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
import si.pocketalbum.core.models.Interval
import si.pocketalbum.core.models.YearIndex
import java.io.Closeable
import java.io.File

class SQLiteAlbum(context: Context) : IAlbum, Closeable {
    private val dbFile = File(context.filesDir, "album.sqlite")
    private val dbHelper = DatabaseHelper(context, dbFile.absolutePath)
    private val db = dbHelper.writableDatabase

    private val yearQuery = "CAST(substr(created, 1, 4) AS SIGNED) AS y"
    private val hourQuery = "CAST(substr(created, 12, 2) AS SIGNED) AS h"

    override fun getInfo(filter: FilterModel): AlbumInfo {
        val where = " WHERE " + getWhere(filter)
        val cursor = db.rawQuery(
            "SELECT $yearQuery, COUNT(*), $hourQuery FROM image $where GROUP BY y;",
            null)

        val years = mutableListOf<YearIndex>()
        cursor.use {
            while (cursor.moveToNext()) {
                val year = cursor.getInt(0)
                val count = cursor.getInt(1)
                years.add(YearIndex(year, count, (0).toUInt(), (0).toULong()))
            }
        }

        return try {
            val imageCount = years.sumOf { it.count }
            val dateCount = queryNumber(db,
                "SELECT COUNT(DISTINCT DATE(created)), $yearQuery, $hourQuery FROM image $where").toInt()
            val thumbnailsSize = queryNumber(db,
                "SELECT SUM(LENGTH(thumbnail)), $yearQuery, $hourQuery FROM image $where")
            val imagesSize = queryNumber(db,
                "SELECT SUM(LENGTH(data)), $yearQuery, $hourQuery FROM image $where")
            AlbumInfo(imageCount, dateCount, thumbnailsSize, imagesSize, years)
        } catch (e: SQLiteException) {
            throw RuntimeException("Error fetching album info", e)
        }
    }

    private fun getWhere(filter: FilterModel): String {
        if (!filter.hasAny) {
            return "TRUE"
        }
        val conditions = mutableListOf<String>()
        if (filter.year != null) {
            if (filter.year.singleValue) {
                conditions.add("y = ${filter.year.to}")
            }
            else
            {
                conditions.add("y >= ${filter.year.from} AND y <= ${filter.year.to}")
            }
        }
        if (filter.timeOfDay != null) {
            conditions.add(when(filter.timeOfDay) {
                FilterModel.TimesOfDay.Morning -> "h >= 6 AND h < 12"
                FilterModel.TimesOfDay.Afternoon -> "h >= 12 AND h < 18"
                FilterModel.TimesOfDay.Evening -> "h >= 18"
                FilterModel.TimesOfDay.Night -> "h < 6"
            })
        }
        return conditions.joinToString(" AND ")
    }

    private fun getLimit(paging: Interval): String {
        val count = paging.to - paging.from + 1;
        return """${paging.from}, $count""";
    }

    override fun getImages(filter: FilterModel, paging: Interval): List<ImageThumbnail> {
        val cursor = db.query("image",
            arrayOf("id", "filename", "created", "width", "height", "size", "crc",
                "latitude", "longitude", "thumbnail", yearQuery, hourQuery),
            getWhere(filter), null,
            null, null,
            "created ASC",
            getLimit(paging))

        val thumbnails = mutableListOf<ImageThumbnail>()
        cursor.use {
            while (cursor.moveToNext()) {
                val image = convertImage(cursor)
                val thumbnail = cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail"))
                thumbnails.add(ImageThumbnail(image, thumbnail))
            }
        }
        return thumbnails
    }

    override fun getData(id: String): ByteArray {
        val cursor = db.query("image", arrayOf("data"),
            "Id = ?", arrayOf(id),
            null, null, null, null)

        cursor.use {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(cursor.getColumnIndexOrThrow("data"))
            } else {
                throw IllegalArgumentException("Data for image id $id not found")
            }
        }
    }

    override fun insert(image: ImageInfo, thumbnail: ByteArray, data: ByteArray) {
        db.insert("image", null, ContentValues().apply {
            put("id", image.id)
            put("filename", image.filename)
            put("created", image.created)
            put("width", image.width)
            put("height", image.height)
            put("size", image.size)
            put("latitude", image.latitude)
            put("longitude", image.longitude)
            put("crc", image.crc.toInt())
            put("thumbnail", thumbnail)
            put("data", data)
        })
    }

    override fun imageExists(id: String): Boolean {
        val query = """
            SELECT EXISTS(SELECT 1 FROM image WHERE id = ?)
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
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            filename = cursor.getString(cursor.getColumnIndexOrThrow("filename")),
            created = cursor.getString(cursor.getColumnIndexOrThrow("created")),
            width = cursor.getInt(cursor.getColumnIndexOrThrow("width")),
            height = cursor.getInt(cursor.getColumnIndexOrThrow("height")),
            size = cursor.getLong(cursor.getColumnIndexOrThrow("size")),
            latitude = cursor.getDoubleOrNull("latitude"),
            longitude = cursor.getDoubleOrNull("longitude"),
            crc = cursor.getInt(cursor.getColumnIndexOrThrow("crc")).toUInt()
        )
    }

    override fun storeYearIndex(yearIndex: YearIndex) {
        db.insert("index", null, ContentValues().apply {
            put("year", yearIndex.year)
            put("count", yearIndex.count)
            put("crc", yearIndex.crc.toInt())
            put("size", yearIndex.size.toLong())
        })
    }

    override fun getYearIndex(): List<YearIndex> {
        val cursor = db.query("index", arrayOf("year", "count", "crc", "size"),
            null, null, null, null, "year")

        val index = mutableListOf<YearIndex>()
        cursor.use {
            while (cursor.moveToNext()) {
                index.add(YearIndex(
                    cursor.getInt(cursor.getColumnIndexOrThrow("year")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("count")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("crc")).toUInt(),
                    cursor.getLong(cursor.getColumnIndexOrThrow("size")).toULong(),
                ))
            }
        }
        return index
    }

    override fun removeYearIndex(year: Int) {
        db.delete("index", "year = ?", arrayOf(year.toString()))
    }

    override fun close() {
        db.close()
    }
}
