package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.app.appsearch.SearchResults
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    var isExpanded by remember { mutableStateOf(false) }

    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    var showColumn1 by remember { mutableStateOf(true) }
    var showColumn2 by remember { mutableStateOf(false) }
    var showColumn3 by remember { mutableStateOf(false) }
    var showColumn4 by remember { mutableStateOf(false) }
    var showColumn5 by remember { mutableStateOf(false) }



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
                    onClick = {
                        showColumn1 = true
                        showColumn2 = false
                        showColumn3 = false
                        showColumn4 = false
                        showColumn5 = false
                        isExpanded = false
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
                        isExpanded = false
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
                        isExpanded = false
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
                        isExpanded = false
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
                        isExpanded = false
                    },
                    modifier = buttonModifier
                ) {
                    Text("Quit", fontSize = 24.sp)
                }
            }
        }

        if (showColumn1) {
            Text("Course Schedule")
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
            showColumn2 = false
        }

        if (showColumn3) {
            Ratings()
        }

        if (showColumn4) {
            Text("Mode")
        }

        if (showColumn5) {
            // here should quit to loginpage
            LoginPage()
        }
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



