package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomePage()
                }
            }
        }
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
        val context = LocalContext.current
        val scheduleIntent = Intent(context, ScheduleActivity::class.java)
        context.startActivity(scheduleIntent)
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
        //Ratings()
    }

    if (showColumn4) {

    }

    if (showColumn5) {
        LocalContext.current.startActivity(Intent(LocalContext.current, LoginActivity::class.java))
    }
}



