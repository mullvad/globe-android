package net.mullvad.map.data

data class Sphere(val center: Vector3, val radius: Float) {
    companion object {
        const val RADIUS = 1f
    }
}
