package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    //LoginPage()
                    HomePage()
                    //CMI()
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
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
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
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation() // Hide the password
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

        Text(
            text = "Forgot Password / Username?",
            color = Color.Gray,
            modifier = Modifier.clickable {
                // Handle "Forgot Password" action here
            }
        )
    }
}





@Composable
fun HomePage() {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    var showColumn1 by remember { mutableStateOf(false) }
    var showColumn2 by remember { mutableStateOf(false) }
    var showColumn3 by remember { mutableStateOf(false) }
    var showColumn4 by remember { mutableStateOf(false) }
    var showColumn5 by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Course Planner",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
        Button(
            onClick = {
                showColumn1 = true
                showColumn2 = false
                showColumn3 = false
                showColumn4 = false
                showColumn5 = false
            },
            modifier = buttonModifier
        ) {
            Text("Schedule of Courses", fontSize = 24.sp)
        }

        Button(
            onClick = {
                showColumn1 = false
                showColumn2 = true
                showColumn3 = false
                showColumn4 = false
                showColumn5 = false
            },
            modifier = buttonModifier
        ) {
            Text("Course Materials and Info", fontSize = 24.sp)
        }

        Button(
            onClick = {
                showColumn1 = false
                showColumn2 = false
                showColumn3 = true
                showColumn4 = false
                showColumn5 = false
            },
            modifier = buttonModifier
        ) {
            Text("Ratings for Courses", fontSize = 24.sp)
        }

        Button(
            onClick = {
                showColumn1 = false
                showColumn2 = false
                showColumn3 = false
                showColumn4 = true
                showColumn5 = false
            },
            modifier = buttonModifier
        ) {
            Text("Mode", fontSize = 24.sp)
        }

        Button(
            onClick = {
                showColumn1 = false
                showColumn2 = false
                showColumn3 = false
                showColumn4 = false
                showColumn5 = true
            },
            modifier = buttonModifier
        ) {
            Text("Quit", fontSize = 24.sp)
        }
    }

    if (showColumn1) {

    }

    if (showColumn2) {
        val context = LocalContext.current
        val courseInfoIntent = Intent(context, CourseInfoActivity::class.java)

        // Pass relevant course information using intent extras
        courseInfoIntent.putExtra("COURSE_CODE", "CS 111")
        courseInfoIntent.putExtra("COURSE_NAME", "Introduction to Programming")
        courseInfoIntent.putExtra("COURSE_DESCRIPTION", "Learn the basics of programming using popular programming languages.")
        courseInfoIntent.putExtra("INSTRUCTOR_NAME", "John Doe")
        courseInfoIntent.putExtra("COURSE_OFFERING", arrayListOf("Monday 10:00 AM - 12:00 PM",
            "Wednesday 2:00 PM - 4:00 PM",
            "Friday 10:00 AM - 12:00 PM")
        )

        context.startActivity(courseInfoIntent)
        //showColumn2 = false
    }

    if (showColumn3) {
        Ratings()
    }

    if (showColumn4) {

    }

    if (showColumn5) {
        // when click quit, should jump to loginPage
        //LoginPage()
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ratings() {

    var searchText by remember { mutableStateOf("") }
//    var searchResults by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search by Course Name") },
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (searchText.isNotEmpty()) {
            Text("Search Results for: $searchText", fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(16.dp))
            // Add your search result UI here
        }
    }
}




