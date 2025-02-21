package si.pocketalbum

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns

/**
 * References:
 * - https://www.programcreek.com/java-api-examples/?code=MLNO/airgram/airgram-master/TMessagesProj/src/main/java/ir/hamzad/telegram/MediaController.java
 * - https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
 *
 * @author Manish@bit.ly/2HjxA0C
 * Created on: 03-07-2020
 */
object UriUtils {
    private const val CONTENT_SIZE_INVALID: Long = -1

    /**
     * @param context context
     * @param contentUri content Uri, i.e, of the scheme `content://`
     * @return The Display name and size for content. In case of non-determination, display name
     * would be null and content size would be [.CONTENT_SIZE_INVALID]
     */
    fun getDisplayNameSize(context: Context, contentUri: Uri): Pair<String?, Long> {
        val scheme = contentUri.scheme
        if (scheme == null || scheme != ContentResolver.SCHEME_CONTENT) {
            throw RuntimeException("Only scheme content:// is accepted")
        }

        var size: Long = CONTENT_SIZE_INVALID
        var displayName: String? = null

        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE
        )
        val cursor = context.contentResolver.query(contentUri, projection, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Try extracting content size

                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }

                // Try extracting display name
                var name: String? = null

                // Strategy: The column name is NOT guaranteed to be indexed by DISPLAY_NAME
                // so, we try two methods
                var nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }

                if (nameIndex == -1 || name == null) {
                    nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex)
                    }
                }
                displayName = name
            }
        } finally {
            cursor?.close()
        }

        // We tried querying the ContentResolver...didn't work out
        // Try extracting the last path segment
        return Pair(displayName ?: contentUri.lastPathSegment, size)
    }
}
