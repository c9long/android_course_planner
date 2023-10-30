package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class CourseReview(val reviewer: String, val date: String, val content: String, val stars: Int)

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
            var isDialogOpen by remember { mutableStateOf(false) }
            val courseReviewsList = remember {
                mutableListOf(
                    CourseReview("John Doe", "October 10, 2023", "Great course!", 4),
                    CourseReview("Jane Smith", "October 12, 2023", "I learned a lot.", 3),
                )
            }

            /*
            create table reviews ( \
                rnum            integer not null (make it auto-generated and unique)
                username?       varchar(60) not null, \
                date            varchar(20) not null, \
                content         varchar(MAX) not null, \
                stars           integer not null, \
                primary key (rnum), \
                foreign key (username?) references user(username?))

            insert into reviews values (...)
            */

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
                onClick = { isDialogOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Add a Review for $courseCode")
            }

            // Check if the dialog should be open
            if (isDialogOpen) {
                ReviewDialog(
                    onDismiss = { isDialogOpen = false },
                    onSubmitReview = { reviewContent, rating ->
                        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
                        val newReview = CourseReview("John Joe", currentDate, reviewContent, rating)
                        courseReviewsList += newReview
                    }
                )
            }

            CourseReviews(courseReviewsList)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally) // Align the box to the bottom
            ) {
                Button(
                    onClick = { onBackButtonClick() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    Text("Back to Main Screen")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseReviews(reviews: List<CourseReview>) {
    LazyColumn (
        modifier = Modifier.fillMaxHeight(0.85f)
    ){
        itemsIndexed(reviews) { _, review ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = review.reviewer, fontWeight = FontWeight.Bold)
                    Text(text = review.date)
                    RatingBar(
                        rating = review.stars
                    )
                    Text(text = review.content)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onSubmitReview: (String, Int) -> Unit,
) {
    var reviewContent by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add a Review") },
        text = {
            Column {
                RatingBar(
                    rating = rating,
                    isInteractable = true,
                    onRatingChanged = { newRating ->
                        rating = newRating
                    }
                )
//                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = reviewContent,
                    onValueChange = { reviewContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Review") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reviewContent.isNotEmpty()) {
                        onSubmitReview(reviewContent, rating)
                        reviewContent = ""
                        onDismiss()
                    }
                }
            ) {
                Text(text = "Submit")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun RatingBar(
    rating: Int,
    isInteractable: Boolean = false,
    onRatingChanged: (Int) -> Unit = {}
) {
    Row {
        val starIcon = painterResource(id = R.drawable.baseline_star_24)
        val starOutlineIcon = painterResource(id = R.drawable.baseline_star_border_24)
        for (index in 0 until 5) {
            Icon(
                painter = if (index < rating) starIcon else starOutlineIcon,
                contentDescription = null, // Decorative element
                tint = Color.Black,
                modifier = Modifier
                    .height(24.dp)
                    .padding(end = 4.dp)
                    .clickable(enabled = isInteractable) {
                        if (isInteractable) {
                            val newRating = index + 1
                            onRatingChanged(newRating)
                        }
                    }
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
        "John Doe",
        listOf("Monday 10:00 AM - 12:00 PM",
            "Wednesday 2:00 PM - 4:00 PM",
            "Friday 10:00 AM - 12:00 PM"),
        onBackButtonClick = {}
    )
}

