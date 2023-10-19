package ca.uwaterloo.cs346project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme

class CourseInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Extract course information from intent extras
                    val courseCode = intent.getStringExtra("COURSE_CODE") ?: ""
                    val courseName = intent.getStringExtra("COURSE_NAME") ?: ""
                    val courseDescription = intent.getStringExtra("COURSE_DESCRIPTION") ?: ""
                    val instructorName = intent.getStringExtra("INSTRUCTOR_NAME") ?: ""
                    val courseOfferings =
                        intent.getStringArrayListExtra("COURSE_OFFERING") ?: emptyList()

                    var updatedCourseCode by remember { mutableStateOf(courseCode) }

                    // Display course information using MaterialTheme
                    CMI { updatedCourseCode = it }
                    CourseInfoScreen(
                        updatedCourseCode, courseName, courseDescription, instructorName, courseOfferings
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CourseInfoScreen(
    courseCode: String,
    courseName: String,
    courseDescription: String,
    instructorName: String,
    courseOfferings: List<String>,
) {
    MaterialTheme (
        colorScheme = lightColorScheme(
            primary = Color(0xFF0056b3),
            secondary = Color(0xFF333333)
        ),
        typography = Typography(),
        shapes = Shapes()
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 140.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Text(
                text = "$courseCode: $courseName",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = courseDescription, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Instructor: $instructorName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Course Offerings:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            courseOfferings.forEachIndexed { index, offering ->
                val offeringParts = offering.split(" ")
                if (offeringParts.size >= 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = offeringParts[0], style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = offeringParts.subList(1, offeringParts.size).joinToString(" "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Add a divider between rows except for the last row
                    if (index < courseOfferings.size - 1) {
                        Divider(modifier = Modifier.fillMaxWidth().height(1.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMI(onUpdateCourseInfo: (String) -> Unit) {
    var courseName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Use AnimatedVisibility to animate the buttons
        AnimatedVisibility(visible = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    placeholder = { Text("Enter Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Button to update course information
                Button(
                    onClick = {
                        // Call the callback function to update course information
                        onUpdateCourseInfo(courseName)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Update Course Info")
                }
            }
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
        "John Doe",
        listOf("Monday 10:00 AM - 12:00 PM",
        "Wednesday 2:00 PM - 4:00 PM",
        "Friday 10:00 AM - 12:00 PM")
    )
}
