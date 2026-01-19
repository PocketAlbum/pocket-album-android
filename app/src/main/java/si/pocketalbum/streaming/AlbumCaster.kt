package si.pocketalbum.streaming

import android.content.Context
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import si.pocketalbum.core.models.ImageInfo
import java.net.Inet4Address
import java.net.NetworkInterface

class AlbumCaster(val context: Context, private val albumStreamer: AlbumStreamer) {
    private val sessionListener =
        object : SessionManagerListener<CastSession> {

            override fun onSessionStarted(
                session: CastSession,
                sessionId: String
            ) {
                albumStreamer.start()
                currentImage?.let {
                    castPhoto(it)
                }
            }

            override fun onSessionEnded(
                session: CastSession,
                error: Int
            ) {
                albumStreamer.stop()
            }

            override fun onSessionResumed(
                session: CastSession,
                wasSuspended: Boolean
            ) {
                albumStreamer.start()
                currentImage?.let {
                    castPhoto(it)
                }
            }

            override fun onSessionSuspended(
                session: CastSession,
                reason: Int
            ) {
                albumStreamer.stop()
            }

            override fun onSessionStarting(session: CastSession) {}
            override fun onSessionStartFailed(session: CastSession, error: Int) {}
            override fun onSessionEnding(session: CastSession) {}
            override fun onSessionResuming(session: CastSession, sessionId: String) {}
            override fun onSessionResumeFailed(session: CastSession, error: Int) {}
        }

    val castContext = CastContext.getSharedInstance(context)
        .sessionManager
        .addSessionManagerListener(sessionListener, CastSession::class.java)

    private var currentImage: ImageInfo? = null

    fun getLocalIpAddress(): String? {
        NetworkInterface.getNetworkInterfaces().toList().forEach { iface ->
            iface.inetAddresses.toList().forEach { addr ->
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
        return null
    }

    fun castPhoto(imageInfo: ImageInfo) {
        currentImage = imageInfo

        val castSession = CastContext.getSharedInstance(context)
            .sessionManager.currentCastSession ?: return

        val address = getLocalIpAddress() ?: return
        val url = "http://${address}:8080/images/${imageInfo.id}/data"

        val meta = MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO).apply {
            putString(MediaMetadata.KEY_TITLE, imageInfo.filename)
        }

        val mediaInfo = MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_NONE)
            .setContentType("image/jpeg")
            .setMetadata(meta)
            .build()

        castSession.remoteMediaClient?.load(
            mediaInfo,
            MediaLoadOptions.Builder().build()
        )
    }
}