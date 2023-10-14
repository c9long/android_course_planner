package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    //LoginPage()
                    HomePage()
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginPage() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(
            text = "Course Planner",
            fontSize = 26.sp,
            fontWeight = Bold,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Username") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Password") }
        )

        Button(
            onClick = {
                // Handle login action here
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Login")
        }

        Text(
            text = "Don't have an account? Sign up.",
            color = Color.Blue,
            modifier = Modifier.clickable {
                // Handle signup action here
            }
        )
    }

}


@Composable
fun HomePage() {
    var isExpanded by remember { mutableStateOf(false) }

    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(if (isExpanded) "Collapse" else "Expand", fontSize = 24.sp)
        }

        // Use AnimatedVisibility to animate the buttons
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { /* Handle button click */ },
                    modifier = buttonModifier
                ) {
                    Text("Schedule of Courses", fontSize = 24.sp)
                }
                Button(
                    onClick = { /* Handle button click */ },
                    modifier = buttonModifier
                ) {
                    Text("Course Materials and Info", fontSize = 24.sp)
                }
                Button(
                    onClick = { /* Handle button click */ },
                    modifier = buttonModifier
                ) {
                    Text("Ratings for Courses", fontSize = 24.sp)
                }
                Button(
                    onClick = { /* Handle button click */ },
                    modifier = buttonModifier
                ) {
                    Text("Mode", fontSize = 24.sp)
                }
                Button(
                    onClick = { /* Handle button click */ },
                    modifier = buttonModifier
                ) {
                    Text("Quit", fontSize = 24.sp)
                }
            }
        }
    }
}


@Composable
fun CMI() {}


@Composable
fun rating() {}


@Composable
fun Mode() {}


@Composable
fun Quit() {}

