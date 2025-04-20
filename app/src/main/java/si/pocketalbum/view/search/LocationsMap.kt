package si.pocketalbum.view.search

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.is_a.jakakordez.mapsforge.heatmap.HeatmapRenderer
import dev.is_a.jakakordez.mapsforge.heatmap.HeatmapTileLayer
import org.mapsforge.core.model.Dimension
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.util.LatLongUtils
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.datastore.MultiMapDataStore
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.ExternalRenderTheme
import org.mapsforge.map.view.InputListener
import si.pocketalbum.R
import si.pocketalbum.core.HeatmapCache
import si.pocketalbum.core.models.BoundingBox
import java.io.File
import java.io.FileOutputStream
import java.util.function.Consumer

class LocationsMap  (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val map: MapView;
    private val lblZoomMessage: TextView
    private var zoom: Byte = 2
    private var listener: Consumer<BoundingBox?>? = null
    private var lastBox: BoundingBox? = null
    private var heatmapLayer: HeatmapTileLayer? = null

    init {
        inflate(context, R.layout.view_locations_map, this)
        lblZoomMessage = findViewById(R.id.lblZoomMessage)
        map = findViewById(R.id.mapView)

        AndroidGraphicFactory.createInstance(context)

        map.isClickable = true
        map.mapScaleBar.isVisible = false
        map.setBuiltInZoomControls(false)

        val mapStream = context.assets.open("world.map")
        val cachedMap = File(context.filesDir, "world.map")
        FileOutputStream(cachedMap).use { mapStream.copyTo(it) }

        val themeStream = context.assets.open("render_theme.xml")
        val cachedTheme = File(context.filesDir, "render_theme.xml")
        FileOutputStream(cachedTheme).use { themeStream.copyTo(it) }

        val mapCache = AndroidUtil.createTileCache(context,
            "mapcache",
            map.model.displayModel.tileSize,
            2f,
            map.model.frameBufferModel.overdrawFactor)
        val mapStore: MapDataStore = MapFile(cachedMap)
        val mapLayer = TileRendererLayer(mapCache, mapStore, map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE)
        mapLayer.setXmlRenderTheme(ExternalRenderTheme(cachedTheme))

        map.layerManager.layers.add(mapLayer)
        map.setCenter(LatLong(0.0, 0.0))
        map.setZoomLevel(zoom)
        map.setZoomLevelMin(2)
        map.setZoomLevelMax(12)

        findViewById<ImageButton>(R.id.btnZoomIn).setOnClickListener {
            map.model.mapViewPosition.zoomIn(true)
            mapViewMoved()
        }

        findViewById<ImageButton>(R.id.btnZoomOut).setOnClickListener {
            map.model.mapViewPosition.zoomOut(true)
            mapViewMoved()
        }

        map.addInputListener(object : InputListener {
            override fun onMoveEvent() {
                mapViewMoved()
            }

            override fun onZoomEvent() {
                mapViewMoved()
            }
        })
    }

    private fun mapViewMoved() {
        removeCallbacks(triggerListener)
        postDelayed(triggerListener, 1000)
    }

    private val triggerListener: Runnable = Runnable {
        if (map.model.mapViewPosition.zoom > 7) {
            val bb = map.boundingBox
            Log.i(
                "MAP", "Map bounding box: " +
                        "lat: ${bb.minLatitude} - ${bb.maxLatitude}" +
                        "lon: ${bb.minLongitude} - ${bb.maxLongitude}"
            )
            val box = BoundingBox.fromMapsforge(bb)
            listener?.accept(box)
            lastBox = box
            lblZoomMessage.visibility = GONE
        }
        else if (lastBox != null) {
            listener?.accept(null)
            lastBox = null
            lblZoomMessage.visibility = VISIBLE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height: Int = width * 2 / 3

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    fun setOnChangeListener(listener: Consumer<BoundingBox?>) {
        this.listener = listener
    }

    fun showHeatmap(heatmap: HeatmapCache) {
        val heatmapCache = AndroidUtil.createTileCache(context,
            "heatmapCache",
            map.model.displayModel.tileSize,
            2f,
            map.model.frameBufferModel.overdrawFactor)

        val layer = HeatmapTileLayer(heatmapCache, MultiMapDataStore(),
            map.model.mapViewPosition, AndroidGraphicFactory.INSTANCE, heatmap.years.values,
            HeatmapRenderer.Options(
                minColor = ContextCompat.getColor(context, R.color.heatmapMin),
                maxColor = ContextCompat.getColor(context, R.color.heatmapMax)))
        heatmapLayer = layer

        val bb = layer.getBoundingBox((map.model.mapViewPosition.zoomLevel + 3).toByte())
        bb?.let {
            val tileSize: Int = map.model.displayModel.tileSize
            val zoomLevel = LatLongUtils.zoomForBounds(
                Dimension(tileSize * 4, tileSize * 4),
                bb,
                tileSize
            )
            map.setCenter(it.centerPoint)
            map.setZoomLevel(zoomLevel)
        }
        map.layerManager.layers.add(layer)

        heatmap.subscribe { layer.heatmapChanged() }
    }
}