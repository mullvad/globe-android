package net.mullvad.mullvadvpn.globe.preview

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import net.mullvad.mullvadvpn.globe.Globe
import net.mullvad.mullvadvpn.globe.data.CameraPosition
import net.mullvad.mullvadvpn.globe.data.LatLong
import net.mullvad.mullvadvpn.globe.data.Latitude
import net.mullvad.mullvadvpn.globe.data.Longitude

@Preview(showBackground = false, showSystemUi = false)
@Composable
private fun GlobePreview() {
    val berlin = LatLong(Latitude(52.5200f), Longitude(13.4050f))
    Globe(
        cameraPosition = CameraPosition(
            latLong = berlin, zoom = 1.9f
        )
    )
}

@Preview
@Composable
private fun SpinningGlobePreview() {
    val infinite = rememberInfiniteTransition()
    val rawLongitude by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 30000, easing = LinearEasing
            )
        ),
    )
    val longitude = Longitude.fromFloat(rawLongitude)

    Globe(
        cameraPosition = CameraPosition(
            latLong = LatLong(Latitude(0f), longitude),
            zoom = 1.9f,
            verticalBias = 0.5f,
        )
    )
}
