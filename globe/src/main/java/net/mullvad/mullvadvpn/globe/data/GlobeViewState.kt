package net.mullvad.mullvadvpn.globe.data

import androidx.compose.runtime.Immutable

@Immutable
class GlobeViewState(
    val cameraPosition: CameraPosition,
    val locationMarker: List<Marker> = emptyList(),
    val globeColors: GlobeColors = GlobeColors.default(),
) {
    companion object {
        fun default() = GlobeViewState(
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
