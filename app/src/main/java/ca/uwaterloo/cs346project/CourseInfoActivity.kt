package ca.uwaterloo.cs346project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CourseInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Extract course information from intent extras
            val courseCode = intent.getStringExtra("COURSE_CODE") ?: ""
            val courseName = intent.getStringExtra("COURSE_NAME") ?: ""
            val courseDescription = intent.getStringExtra("COURSE_DESCRIPTION") ?: ""
            val instructorName = intent.getStringExtra("INSTRUCTOR_NAME") ?: ""

            // Display course information using Composables
            CMI()
            CourseInfoScreen(courseCode, courseName, courseDescription, instructorName)
        }
    }
}

@Composable
fun CourseInfoScreen(courseCode: String, courseName: String, courseDescription: String, instructorName: String) {
    Column(
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 80.dp,
                end = 16.dp,
                bottom = 16.dp
            )
    ) {
        Text(text = "$courseCode: $courseName", fontSize = 20.sp)
        Text(text = courseDescription, fontSize = 16.sp)
        Text(text = "Instructor: $instructorName", fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMI() {
    var courseName by remember { mutableStateOf("") }

    val isExpanded by remember { mutableStateOf(true) }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Use AnimatedVisibility to animate the buttons
        AnimatedVisibility(visible = isExpanded) {
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                placeholder = { Text("Enter Course Name") },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCourseInfoScreen() {
    CourseInfoScreen(
        "CS111",
        "Introduction to Programming",
        "Learn the basics of programming using popular programming languages.",
        "John Doe"
    )
}
