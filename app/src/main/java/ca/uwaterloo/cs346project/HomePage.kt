package ca.uwaterloo.cs346project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme

class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    aTitle("Placeholder")
                }
            }
        }
    }
}

@Composable
fun aTitle(title: String) {
    Box(
        Modifier.fillMaxSize().padding(15.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

}