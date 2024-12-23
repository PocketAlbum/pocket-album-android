package si.kordez.pocketalbum.core.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

class DatabaseHelper(context: Context, databaseName: String) : SQLiteOpenHelper(
    context,
    File(context.filesDir, databaseName).absolutePath, // Use database from filesDir
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // No schema creation needed; database file is pre-existing
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle upgrades if necessary
    }
}
