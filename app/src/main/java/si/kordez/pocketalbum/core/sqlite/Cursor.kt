package si.kordez.pocketalbum.core.sqlite

import android.database.Cursor

fun Cursor.getDoubleOrNull(columnName: String): Double? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getDouble(index)
}