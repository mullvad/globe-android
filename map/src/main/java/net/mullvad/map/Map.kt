package net.mullvad.map

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.mullvad.map.data.CameraPosition
import net.mullvad.map.data.GlobeColors
import net.mullvad.map.data.LatLong
import net.mullvad.map.data.MapViewState
import net.mullvad.map.data.Marker
import net.mullvad.map.internal.MapGLSurfaceView


@Composable
fun AnimatedMap(
    modifier: Modifier,
    cameraLocation: LatLong,
    cameraBaseZoom: Float,
    cameraVerticalBias: Float,
    markers: List<Marker>,
    globeColors: GlobeColors,
) {
    InteractiveMap(
        modifier = modifier,
        cameraPosition = animatedCameraPosition(
            baseZoom = cameraBaseZoom,
            targetCameraLocation = cameraLocation,
            cameraVerticalBias = cameraVerticalBias,
        ),
        markers = markers,
        globeColors = globeColors,
    )
}

@Composable
fun Map(
    cameraPosition: CameraPosition,
    modifier: Modifier = Modifier,
    markers: List<Marker> = emptyList(),
    globeColors: GlobeColors = GlobeColors.default(),
) {
    val mapViewState = MapViewState(cameraPosition, markers, globeColors)
    Map(
        modifier = modifier,
        mapViewState = mapViewState
    )
}

@Composable
internal fun Map(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState,
) {
    val lifeCycleState = LocalLifecycleOwner.current.lifecycle

    AndroidView(
        modifier = modifier,
        factory = { MapGLSurfaceView(it) },
        update = { glSurfaceView ->
            glSurfaceView.lifecycle = lifeCycleState
            glSurfaceView.setData(mapViewState)
        },
        onRelease = { it.lifecycle = null },
    )
}


@Composable
fun InteractiveMap(
    cameraPosition: CameraPosition,
    modifier: Modifier = Modifier,
    markers: List<Marker> = emptyList(),
    globeColors: GlobeColors = GlobeColors.default(),
    onClickRelayItemId: (Marker) -> Unit = {},
    onLongClickRelayItemId: (Offset, Marker) -> Unit = { _, _ -> },
) {
    val mapViewState = MapViewState(cameraPosition, markers, globeColors)
    InteractiveMap(
        modifier = modifier,
        mapViewState = mapViewState,
        onClickRelayItemId,
        onLongClickRelayItemId,
    )
}

@Composable
internal fun InteractiveMap(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState,
    onClick: (Marker) -> Unit = {},
    onLongClick: (Offset, Marker) -> Unit = { _, _ -> },
) {
    var view: MapGLSurfaceView? = remember { null }

    val lifeCycleState = LocalLifecycleOwner.current.lifecycle

    AndroidView(
        modifier = Modifier
            .pointerInput(lifeCycleState) {
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
        factory = { MapGLSurfaceView(it) },
        update = { glSurfaceView ->
            glSurfaceView.lifecycle = lifeCycleState
            view = glSurfaceView
            glSurfaceView.setData(mapViewState)
        },
        onRelease = { it.lifecycle = null },
    )
}
