package net.mullvad.mullvadvpn.globe.data

data class Sphere(val center: Vector3, val radius: Float) {
    companion object {
        const val RADIUS = 1f
    }
}
