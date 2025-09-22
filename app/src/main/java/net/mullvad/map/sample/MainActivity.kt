package net.mullvad.map.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.mullvad.map.Map
import net.mullvad.map.data.CameraPosition
import net.mullvad.map.data.LatLong
import net.mullvad.map.data.Latitude
import net.mullvad.map.data.Longitude
import net.mullvad.map.sample.ui.theme.SampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Map(
                        modifier = Modifier.padding(innerPadding), cameraPosition = CameraPosition(
                            latLong = LatLong(
                                latitude = Latitude(57.7089f), longitude = Longitude(11.9746f)
                            ), zoom = 3f, verticalBias = 0.5f
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleTheme {
        Greeting("Android")
    }
}