package net.mullvad.mullvadvpn.globe.data

import androidx.compose.runtime.Immutable

@Immutable
data class Marker(
    val latLong: LatLong,
    val size: Float = DEFAULT_MARKER_SIZE,
    val colors: LocationMarkerColors,
    val id: Any? = null,
) {
    companion object {
        private const val DEFAULT_MARKER_SIZE = 0.02f
    }
}
