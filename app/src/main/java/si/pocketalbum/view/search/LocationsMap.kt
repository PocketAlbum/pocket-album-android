package si.pocketalbum.view.search

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageButton
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.ExternalRenderTheme
import si.pocketalbum.R
import java.io.File
import java.io.FileOutputStream

class LocationsMap  (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val map: MapView;
    private var zoom: Byte = 2

    init {
        inflate(context, R.layout.view_locations_map, this)
        map = findViewById(R.id.mapView)

        AndroidGraphicFactory.createInstance(context)

        map.isClickable = true
        map.mapScaleBar.isVisible = false
        map.setBuiltInZoomControls(false)

        val tileCache = AndroidUtil.createTileCache(context,
            "mapcache",
            map.model.displayModel.tileSize,
            2f,
            map.model.frameBufferModel.overdrawFactor)

        val mapStream = context.assets.open("world.map")
        val cachedMap = File(context.filesDir, "world.map")
        FileOutputStream(cachedMap).use { mapStream.copyTo(it) }

        val themeStream = context.assets.open("render_theme.xml")
        val cachedTheme = File(context.filesDir, "render_theme.xml")
        FileOutputStream(cachedTheme).use { themeStream.copyTo(it) }

        val mapStore: MapDataStore = MapFile(cachedMap)

        val renderer = TileRendererLayer(tileCache, mapStore, map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE)

        renderer.setXmlRenderTheme(ExternalRenderTheme(cachedTheme))

        map.layerManager.layers.add(renderer)
        map.setCenter(LatLong(0.0, 0.0))
        map.setZoomLevel(zoom)
        map.setZoomLevelMin(2)
        map.setZoomLevelMax(10)

        findViewById<ImageButton>(R.id.btnZoomIn).setOnClickListener {
            map.model.mapViewPosition.zoomIn(true)
        }

        findViewById<ImageButton>(R.id.btnZoomOut).setOnClickListener {
            map.model.mapViewPosition.zoomOut(true)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height: Int = width / 2

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }
}