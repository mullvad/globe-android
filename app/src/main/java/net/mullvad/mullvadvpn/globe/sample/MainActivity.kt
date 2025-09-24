package net.mullvad.mullvadvpn.globe.sample

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
import net.mullvad.mullvadvpn.globe.Globe
import net.mullvad.mullvadvpn.globe.data.CameraPosition
import net.mullvad.mullvadvpn.globe.data.LatLong
import net.mullvad.mullvadvpn.globe.data.Latitude
import net.mullvad.mullvadvpn.globe.data.Longitude
import net.mullvad.mullvadvpn.globe.sample.ui.theme.SampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Globe(
                        modifier = Modifier.padding(innerPadding),
                        cameraPosition =
                            CameraPosition(
                                latLong =
                                    LatLong(
                                        latitude = Latitude(57.7089f),
                                        longitude = Longitude(11.9746f),
                                    ),
                                zoom = 3f,
                                verticalBias = 0.5f,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleTheme { Greeting("Android") }
}
