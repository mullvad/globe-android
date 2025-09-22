package net.mullvad.map.data

import androidx.compose.runtime.Immutable

@Immutable
class MapViewState(
    val cameraPosition: CameraPosition,
    val locationMarker: List<Marker> = emptyList(),
    val globeColors: GlobeColors = GlobeColors.default(),
) {
    companion object {
        fun default() = MapViewState(
            CameraPosition(
                latLong =
                    LatLong(
                        latitude = Latitude(0f),
                        longitude = Longitude(0f)
                    )
            )
        )
    }
}
