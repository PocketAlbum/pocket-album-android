package si.pocketalbum.streaming

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.services.AlbumService
import java.io.ByteArrayInputStream

class AlbumStreamer(
    private val service: AlbumService,
    port: Int = 8080
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        try {
            val connection: AlbumConnection
            try {
                connection = service.getConnectionDeferred().getCompleted()
            } catch (e: Exception) {
                return newFixedLengthResponse(
                    Response.Status.NOT_FOUND, "text/plain",
                    "Album not loaded"
                )
            }

            val imageDataMatch = Regex("/images/([a-f\\d]+)/data$").matchEntire(session.uri)
            if (imageDataMatch != null)
            {
                val data = connection.album.getImageData(imageDataMatch.groupValues[1])

                return newFixedLengthResponse(
                    Response.Status.OK,
                    "image/jpeg",
                    ByteArrayInputStream(data),
                    data.size.toLong()
                )
            }
            else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain",
                    "Invalid url")
            }
        }
        catch (e: Exception)
        {
            Log.e("HTTP", "Server error", e)
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                "Server error")
        }
    }
}