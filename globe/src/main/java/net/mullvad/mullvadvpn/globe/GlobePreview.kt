package net.mullvad.mullvadvpn.globe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker1D
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import net.mullvad.mullvadvpn.globe.data.COMPLETE_ANGLE
import net.mullvad.mullvadvpn.globe.data.CameraPosition
import net.mullvad.mullvadvpn.globe.data.LatLong
import net.mullvad.mullvadvpn.globe.data.Latitude
import net.mullvad.mullvadvpn.globe.data.LocationMarkerColors
import net.mullvad.mullvadvpn.globe.data.Longitude
import net.mullvad.mullvadvpn.globe.data.Marker
import kotlin.math.abs

@Preview(showBackground = false, showSystemUi = false)
@Composable
private fun GlobePreview() {
    val berlin = LatLong(Latitude(52.5200f), Longitude(13.4050f))
    Globe(cameraPosition = CameraPosition(latLong = berlin, zoom = 1.9f))
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
                durationMillis = 30000,
                easing = LinearEasing
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

@Preview
@Composable
private fun InteractiveMapPreview(
) {
    // Starting position
    var selectedLocation by remember {
        // Berlin
        mutableStateOf(locations[9])
    }

    // Create some markers
    val markers = locations.map {
        Marker(
            id = it.toString(),
            latLong = it,
            colors = if (it == selectedLocation) selectLocationMarkerColors
            else unselectLocationMarkerColors,
        )
    }
    val zoomRange = 1.2f..2f

    val zoomAnimatable = remember {
        Animatable(zoomRange.start).also {
            it.updateBounds(zoomRange.start, zoomRange.endInclusive)
        }
    }
    val latLngAnimatable = remember {
        Animatable(
            selectedLocation.toOffset(),
            typeConverter = Offset.VectorConverter
        ).also {
            it.updateBounds(
                lowerBound = Offset(x = Float.NEGATIVE_INFINITY, y = -40f),
                upperBound = Offset(x = Float.POSITIVE_INFINITY, y = 60f)
            )
        }
    }

    LaunchedEffect(selectedLocation) {
        // Decide duration of animation based on distance
        val distance = selectedLocation.seppDistanceTo(latLngAnimatable.value.toLatLng())
        val duration = distance.toAnimationDurationMillis()

        launch {
            latLngAnimatable.snapTo(latLngAnimatable.value.unwind())
            latLngAnimatable.animateTo(
                selectedLocation.toOffset(),
                animationSpec = tween(duration),
            )
        }
        launch { zoomAnimatable.animateTo(zoomRange.start, animationSpec = tween(duration)) }
    }

    val tracker = remember { DiffVelocityTracker() }
    val scope = rememberCoroutineScope()

    InteractiveGlobe(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGesturesWithEnd(
                    true,
                    onGestureStart = {
                        tracker.resetTracking()
                        scope.launch { zoomAnimatable.stop() }
                        scope.launch { latLngAnimatable.stop() }
                    },
                    onGesture = { centroid: Offset, pan: Offset, zoomChange: Float, rotation: Float ->
                        val currentPosition = latLngAnimatable.value
                        val zoom = zoomAnimatable.value

                        val latLngOffsetDiff = Offset(x = -pan.x * zoom / 50f, pan.y * zoom / 40f)

                        val newPosition = currentPosition + latLngOffsetDiff
                        val newZoom = (zoom + (1 - zoomChange) * 0.5f)

                        val isZooming = zoomChange != 1f

                        if (!isZooming) {
                            tracker.addPosition(
                                System.currentTimeMillis(),
                                latLngOffsetDiff
                            )
                        } else {
                            tracker.resetTracking()
                        }

                        scope.launch {
                            latLngAnimatable.snapTo(
                                newPosition
                            )
                        }

                        if (isZooming) {
                            scope.launch { zoomAnimatable.snapTo(newZoom) }
                        }
                    },
                    onGestureEnd = {
                        scope.launch {
                            val velocity = tracker.calculateVelocity()

                            var latVelocity = velocity.y
                            var longVelocity = velocity.x

                            do {
                                val res = latLngAnimatable.animateDecay(
                                    Offset(
                                        longVelocity,
                                        latVelocity
                                    ), exponentialDecay(1f)
                                )

                                longVelocity = res.endState.velocityVector.v1
                                latVelocity = -res.endState.velocityVector.v2
                            } while (res.endReason == AnimationEndReason.BoundReached)

                            launch {
                                latLngAnimatable.animateTo(
                                    calculateClosestOffset(
                                        latLngAnimatable.value,
                                        selectedLocation.toOffset()
                                    ),
                                    tween(durationMillis = 1000, delayMillis = 3000)
                                )
                            }
                            launch {
                                zoomAnimatable.animateTo(
                                    zoomRange.start,
                                    tween(1000, delayMillis = 3000)
                                )
                            }
                        }
                    },
                )
            },
        cameraPosition =
            CameraPosition(
                latLngAnimatable.value.toLatLng(),
                zoomAnimatable.value
            ),
        markers = markers,
        onMarkerClick = { selectedLocation = it.latLong }
    )
}

fun LatLong.toOffset(): Offset =
    Offset(
        longitude.value,
        latitude.value,
    )

fun Offset.unwind(): Offset = Offset(Longitude.unwind(x), Latitude.unwind(y))
fun Offset.toLatLng(): LatLong =
    LatLong(Latitude.fromFloat(y), Longitude.fromFloat(x))

fun Float.closestTarget(target: Float): Float {
    val deg = rem(COMPLETE_ANGLE)
    val base = this - deg

    val targetRemainder = target.rem(COMPLETE_ANGLE)
    val newTarget = base + targetRemainder

    val diff = this - newTarget
    return when {
        diff > 180f -> newTarget + COMPLETE_ANGLE
        diff < -180f -> newTarget - COMPLETE_ANGLE
        else -> newTarget
    }
}

fun calculateClosestOffset(current: Offset, target: Offset): Offset =
    Offset(current.x.closestTarget(target.x), target.y)


suspend fun PointerInputScope.detectTransformGesturesWithEnd(
    panZoomLock: Boolean = false,
    onGestureStart: () -> Unit,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit,
    onGestureEnd: () -> Unit,
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)
        onGestureStart()
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion =
                        abs(rotation * kotlin.math.PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop || rotationMotion > touchSlop || panMotion > touchSlop) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f || zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange, effectiveRotation)
                    }
                    event.changes.forEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })

        onGestureEnd()
    }
}

private val locations = listOf(
    LatLong(Latitude(-34.92123f), Longitude(138.5995f)),
    LatLong(Latitude(52.35f), Longitude(4.916667f)),
    LatLong(Latitude(39.043757f), Longitude(-77.487442f)),
    LatLong(Latitude(37.98381f), Longitude(23.727539f)),
    LatLong(Latitude(33.753746f), Longitude(-84.38633f)),
    LatLong(Latitude(-36.848461f), Longitude(174.763336f)),
    LatLong(Latitude(13.756331f), Longitude(100.501762f)),
    LatLong(Latitude(41.385063f), Longitude(2.173404f)),
    LatLong(Latitude(44.787197f), Longitude(20.457273f)),
    LatLong(Latitude(52.520008f), Longitude(13.404954f)),
    LatLong(Latitude(4.624335f), Longitude(-74.063644f)),
    LatLong(Latitude(44.837788f), Longitude(-0.57918f)),
    LatLong(Latitude(42.361145f), Longitude(-71.057083f)),
    LatLong(Latitude(48.148598f), Longitude(17.107748f)),
    LatLong(Latitude(-27.471f), Longitude(153.0234f)),
    LatLong(Latitude(50.833333f), Longitude(4.333333f)),
    LatLong(Latitude(44.433333f), Longitude(26.1f)),
    LatLong(Latitude(47.5f), Longitude(19.083333f)),
    LatLong(Latitude(51.037007f), Longitude(-114.058315f)),
    LatLong(Latitude(41.881832f), Longitude(-87.623177f)),
    LatLong(Latitude(32.89748f), Longitude(-97.040443f)),
    LatLong(Latitude(39.739236f), Longitude(-104.990251f)),
    LatLong(Latitude(42.331389f), Longitude(-83.045833f)),
    LatLong(Latitude(53.35014f), Longitude(-6.266155f)),
    LatLong(Latitude(51.233334f), Longitude(6.783333f)),
    LatLong(Latitude(-3.732714f), Longitude(-38.526997f)),
    LatLong(Latitude(50.110924f), Longitude(8.682127f)),
    LatLong(Latitude(55.86515f), Longitude(-4.25763f)),
    LatLong(Latitude(57.70887f), Longitude(11.97456f)),
    LatLong(Latitude(60.192059f), Longitude(24.945831f)),
    LatLong(Latitude(22.283333f), Longitude(114.15f)),
    LatLong(Latitude(29.749907f), Longitude(-95.358421f)),
    LatLong(Latitude(41.00824f), Longitude(28.978359f)),
    LatLong(Latitude(-6.17511f), Longitude(106.865036f)),
    LatLong(Latitude(-26.195246f), Longitude(28.034088f)),
    LatLong(Latitude(39.099789f), Longitude(-94.57856f)),
    LatLong(Latitude(3.139003f), Longitude(101.686852f)),
    LatLong(Latitude(50.4501f), Longitude(30.5234f)),
    LatLong(Latitude(6.524379f), Longitude(3.379206f)),
    LatLong(Latitude(-12.046373f), Longitude(-77.042755f)),
    LatLong(Latitude(38.736946f), Longitude(-9.142685f)),
    LatLong(Latitude(46.0569f), Longitude(14.5057f)),
    LatLong(Latitude(51.514125f), Longitude(-0.093689f)),
    LatLong(Latitude(34.052235f), Longitude(-118.243683f)),
    LatLong(Latitude(40.408566f), Longitude(-3.69222f)),
    LatLong(Latitude(55.607075f), Longitude(13.002716f)),
    LatLong(Latitude(53.5f), Longitude(-2.216667f)),
    LatLong(Latitude(14.599512f), Longitude(120.984222f)),
    LatLong(Latitude(43.29648f), Longitude(5.38107f)),
    LatLong(Latitude(26.203407f), Longitude(-98.230011f)),
    LatLong(Latitude(-37.815018f), Longitude(144.946014f)),
    LatLong(Latitude(25.761681f), Longitude(-80.191788f)),
    LatLong(Latitude(45.466667f), Longitude(9.2f)),
    LatLong(Latitude(45.5053f), Longitude(-73.5525f)),
    LatLong(Latitude(40.73061f), Longitude(-73.935242f)),
    LatLong(Latitude(35.17025f), Longitude(33.3587f)),
    LatLong(Latitude(34.672314f), Longitude(135.484802f)),
    LatLong(Latitude(59.916667f), Longitude(10.75f)),
    LatLong(Latitude(38.115688f), Longitude(13.361267f)),
    LatLong(Latitude(48.866667f), Longitude(2.333333f)),
    LatLong(Latitude(-31.953512f), Longitude(115.857048f)),
    LatLong(Latitude(33.448376f), Longitude(-112.074036f)),
    LatLong(Latitude(50.083333f), Longitude(14.466667f)),
    LatLong(Latitude(20.592774f), Longitude(-100.390225f)),
    LatLong(Latitude(35.787743f), Longitude(-78.644257f)),
    LatLong(Latitude(40.758701f), Longitude(-111.876183f)),
    LatLong(Latitude(37.338208f), Longitude(-121.886329f)),
    LatLong(Latitude(-33.448891f), Longitude(-70.669266f)),
    LatLong(Latitude(-23.533773f), Longitude(-46.62529f)),
    LatLong(Latitude(47.608013f), Longitude(-122.335167f)),
    LatLong(Latitude(40.789543f), Longitude(-74.0565f)),
    LatLong(Latitude(1.293056f), Longitude(103.855833f)),
    LatLong(Latitude(42.683333f), Longitude(23.316667f)),
    LatLong(Latitude(58.964432f), Longitude(5.72625f)),
    LatLong(Latitude(59.3289f), Longitude(18.0649f)),
    LatLong(Latitude(-33.861481f), Longitude(151.205475f)),
    LatLong(Latitude(59.436961f), Longitude(24.753575f)),
    LatLong(Latitude(32.0853f), Longitude(34.781768f)),
    LatLong(Latitude(41.327953f), Longitude(19.819025f)),
    LatLong(Latitude(35.685f), Longitude(139.751389f)),
    LatLong(Latitude(43.666667f), Longitude(-79.416667f)),
    LatLong(Latitude(39.466667f), Longitude(-0.375f)),
    LatLong(Latitude(49.25f), Longitude(-123.133333f)),
    LatLong(Latitude(48.210033f), Longitude(16.363449f)),
    LatLong(Latitude(52.25f), Longitude(21f)),
    LatLong(Latitude(38.889484f), Longitude(-77.035278f)),
    LatLong(Latitude(45.821f), Longitude(15.973f)),
    LatLong(Latitude(47.366667f), Longitude(8.55f)),
)

private val selectLocationMarkerColors =
    LocationMarkerColors(centerColor = Color(0xFF44AD4D.toInt()))

private val unselectLocationMarkerColors = LocationMarkerColors(
    perimeterColors = null,
    centerColor = Color(0xFF192E45.toInt()),
    ringBorderColor = Color(0xFFFFFFFF.toInt()),
)


class DiffVelocityTracker {
    private val xVelocityTracker = VelocityTracker1D(true)
    private val yVelocityTracker = VelocityTracker1D(true)

    internal var lastMoveEventTimeStamp = 0L

    fun addPosition(timeMillis: Long, delta: Offset) {
        xVelocityTracker.addDataPoint(timeMillis, delta.x)
        yVelocityTracker.addDataPoint(timeMillis, delta.y)
    }

    fun calculateVelocity(): Velocity =
        calculateVelocity(Velocity(Float.MAX_VALUE, Float.MAX_VALUE))

    fun calculateVelocity(maximumVelocity: Velocity): Velocity {
        val velocityX = xVelocityTracker.calculateVelocity(maximumVelocity.x)
        val velocityY = yVelocityTracker.calculateVelocity(maximumVelocity.y)
        return Velocity(velocityX, velocityY)
    }

    /** Clears the tracked positions added by [addPosition]. */
    fun resetTracking() {
        xVelocityTracker.resetTracking()
        yVelocityTracker.resetTracking()
        lastMoveEventTimeStamp = 0L
    }
}

