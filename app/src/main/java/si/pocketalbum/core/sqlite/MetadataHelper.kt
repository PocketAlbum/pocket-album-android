@file:OptIn(ExperimentalUuidApi::class)

package si.pocketalbum.core.sqlite

import android.database.sqlite.SQLiteDatabase
import si.pocketalbum.core.models.MetadataModel
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MetadataHelper {
    companion object {
        const val query = """SELECT value FROM meta WHERE key = ?"""

        fun read(db: SQLiteDatabase): MetadataModel = MetadataModel(
            getUuid(db),
            get(db, "version"),
            get(db, "name"),
            get(db, "description"),
            getDateTime(db, "created"),
            getDateTime(db, "updated")
        )

        fun get(db: SQLiteDatabase, key: String): String {
            try {
                db.rawQuery(query, arrayOf(key)).use {
                    it.moveToFirst()
                    return it.getString(0)
                }
            }
            catch (_: Exception) {
                return ""
            }
        }

        fun getUuid(db: SQLiteDatabase): Uuid {
            try {
                return Uuid.parse(get(db, "id"))
            }
            catch (_: Exception) {
                return Uuid.random()
            }
        }

        fun getDateTime(db: SQLiteDatabase, key: String): LocalDateTime {
            try {
                return LocalDateTime.parse(get(db, key), MetadataModel.formatter)
            }
            catch (_: Exception) {
                return LocalDateTime.now()
            }
        }
    }
}