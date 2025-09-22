package net.mullvad.mullvadvpn.globe.internal

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import net.mullvad.mullvadvpn.globe.data.GlobeViewState
import net.mullvad.mullvadvpn.globe.data.Marker

internal class GlobeSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: GlobeRenderer = GlobeRenderer(context.resources)
    var lifecycle: Lifecycle? = null
        set(value) {
            field?.removeObserver(observer)
            value?.addObserver(observer)
            field = value
        }

    private val observer = LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            else -> {}
        }
    }

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

//        if (BuildConfig.DEBUG) {
//            debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
//        }

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setData(viewState: GlobeViewState) {
        renderer.setViewState(viewState)
        requestRender()
    }

    fun closestMarker(offset: Offset): Pair<Marker, Offset>? {
        val (marker, distance) = renderer.closestMarker(offset) ?: return null
        return if (distance < MIN_DISTANCE) {
            marker?.id?.let { marker to offset }
        } else {
            null
        }
    }

    companion object {
        private const val MIN_DISTANCE = 0.03f
    }
}
