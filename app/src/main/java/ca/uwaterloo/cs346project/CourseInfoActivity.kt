package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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

                    CourseInfoScreen(
                        courseCode, courseName, courseDescription, instructorName,
                        courseOfferings, onBackButtonClick = { navigateBackToMainActivity() }
                    )
                }
            }
            BackHandler {
                navigateBackToMainActivity()
            }
        }
    }
    private fun navigateBackToMainActivity() {
        // Create an Intent to navigate back to MainActivity
        val intent = Intent(this, HomePageActivity::class.java)

        startActivity(intent)
        finish()
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
    onBackButtonClick: () -> Unit,
) {
    MaterialTheme (
        typography = Typography(),
        shapes = Shapes()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$courseCode: $courseName",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            Button(
                onClick = { onBackButtonClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Back to Main Screen")
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
            "Friday 10:00 AM - 12:00 PM"),
        onBackButtonClick = {}
    )
}

