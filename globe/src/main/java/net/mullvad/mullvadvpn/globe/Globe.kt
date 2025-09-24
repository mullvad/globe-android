package net.mullvad.mullvadvpn.globe

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.mullvad.mullvadvpn.globe.data.CameraPosition
import net.mullvad.mullvadvpn.globe.data.GlobeColors
import net.mullvad.mullvadvpn.globe.data.GlobeViewState
import net.mullvad.mullvadvpn.globe.data.Marker
import net.mullvad.mullvadvpn.globe.internal.GlobeSurfaceView

@Composable
fun Globe(
    cameraPosition: CameraPosition,
    modifier: Modifier = Modifier,
    markers: List<Marker> = emptyList(),
    globeColors: GlobeColors = GlobeColors.default(),
) {
    val globeViewState = GlobeViewState(cameraPosition, markers, globeColors)
    Globe(modifier = modifier, globeViewState = globeViewState)
}

@Composable
private fun Globe(modifier: Modifier = Modifier, globeViewState: GlobeViewState) {
    val lifeCycleState = LocalLifecycleOwner.current.lifecycle

    AndroidView(
        modifier = modifier,
        factory = { GlobeSurfaceView(it) },
        update = { glSurfaceView ->
            glSurfaceView.lifecycle = lifeCycleState
            glSurfaceView.setData(globeViewState)
        },
        onRelease = { it.lifecycle = null },
    )
}

@Composable
fun Globe(
    cameraPosition: CameraPosition,
    modifier: Modifier = Modifier,
    markers: List<Marker> = emptyList(),
    globeColors: GlobeColors = GlobeColors.default(),
    onMarkerClick: (Marker) -> Unit = {},
    onMarkerLongPress: (Offset, Marker) -> Unit = { _, _ -> },
) {
    val globeViewState = GlobeViewState(cameraPosition, markers, globeColors)
    Globe(modifier = modifier, globeViewState = globeViewState, onMarkerClick, onMarkerLongPress)
}

@Composable
private fun Globe(
    modifier: Modifier = Modifier,
    globeViewState: GlobeViewState,
    onClick: (Marker) -> Unit = {},
    onLongClick: (Offset, Marker) -> Unit = { _, _ -> },
) {
    var view: GlobeSurfaceView? = remember { null }

    val lifeCycleState = LocalLifecycleOwner.current.lifecycle

    AndroidView(
        modifier =
            Modifier.pointerInput(lifeCycleState) {
                    detectTapGestures(
                        onTap = {
                            val result = view?.closestMarker(it) ?: return@detectTapGestures
                            onClick(result.first)
                        },
                        onLongPress = {
                            val result = view?.closestMarker(it) ?: return@detectTapGestures
                            onLongClick(result.second, result.first)
                        },
                    )
                }
                .then(modifier),
        factory = { GlobeSurfaceView(it) },
        update = { glSurfaceView ->
            glSurfaceView.lifecycle = lifeCycleState
            view = glSurfaceView
            glSurfaceView.setData(globeViewState)
        },
        onRelease = { it.lifecycle = null },
    )
}
