package net.mullvad.map.data

import androidx.compose.runtime.Immutable

@Immutable
data class CameraPosition(
    val latLong: LatLong ,
    val zoom: Float = 1.5f,
    val verticalBias: Float = .5f,
    val fov: Float = DEFAULT_FIELD_OF_VIEW,
) {
    companion object {
        const val DEFAULT_FIELD_OF_VIEW = 70f
    }
}

