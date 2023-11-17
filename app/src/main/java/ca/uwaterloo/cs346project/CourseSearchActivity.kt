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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.platform.LocalContext

class CourseSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val currentlyLoggedInUser = intent.getStringExtra("CURRENT_USER") ?: ""
                    // TODO get from api
                    val dbHelper = UserDBHelper(LocalContext.current)
                    dbHelper.addCourse(Course("CS111", "Introduction to Programming", "Learn the basics of programming using popular programming languages."))
                    dbHelper.addCourse(Course("CS240", "Data Structures and Data Management", "Introduction to widely used and effective methods of data organization, focusing on data structures, their algorithms, and the performance of these algorithms. Specific topics include priority queues, sorting, dictionaries, data structures for text processing. [Note: Enrolment is restricted; see Note 1 above. Lab is not scheduled and students are expected to find time in open hours to complete their work. Offered: F,W,S]"))
                    dbHelper.addCourse(Course("MATH135", "Algebra for Honours Mathematics", "An introduction to the language of mathematics and proof techniques through a study of the basic algebraic systems of mathematics: the integers, the integers modulo n, the rational numbers, the real numbers, the complex numbers and polynomials. [Offered: F,W,S]"))

                    SearchPage(currentlyLoggedInUser)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(currentlyLoggedInUser: String) {
    var userSearchText by remember { mutableStateOf("") } // Current string the user is searching for
    var courseSelected by remember { mutableStateOf(false) }
    val dbHelper = UserDBHelper(LocalContext.current)
    val courses by remember { mutableStateOf(CourseList.get())}
    var select by remember { mutableStateOf(Course("", "", "")) }

    MaterialTheme (
        colorScheme = lightColorScheme(
            primary = Color(0xFF0056b3),
            secondary = Color(0xFF333333)
        ),
        typography = Typography(),
        shapes = Shapes()
    ) {
        // Title
        Column (
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            Text(
                text = "Schedule of Courses",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            TextField(
                value = userSearchText,
                onValueChange = { userSearchText = it },
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Box() {
                // Text under search bar
                Text(text = "View course schedule information and reviews from UW students", style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp))

                // Search Suggestions
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    val filtered: List<Course> = if (!userSearchText.isEmpty()) {
                        val result = ArrayList<Course>()
                        for (course in courses) {
                            if (course.code.lowercase().startsWith(userSearchText.lowercase())) {
                                result.add(course)
                            }
                        }
                        result
                    } else listOf()

                    items(filtered) { selected ->
                        Row(
                            modifier = Modifier
                                .clickable(onClick = {
                                    select = selected
                                    courseSelected = true
                                })
                                .background(Color.LightGray)
                                .fillMaxWidth()
                                .padding(PaddingValues(12.dp, 16.dp))
                        ) {
                            Text(text = "${selected.code} : ${selected.title}", fontSize = 18.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (courseSelected) {
        val context = LocalContext.current
        val courseInfoIntent = Intent(context, CourseInfoActivity::class.java)

        // Pass relevant course information using intent extras
        courseInfoIntent.putExtra("CURRENT_USER", currentlyLoggedInUser)
        courseInfoIntent.putExtra("COURSE_CODE", select.code)
        courseInfoIntent.putExtra("COURSE_NAME", select.title)
        courseInfoIntent.putExtra("COURSE_DESCRIPTION", select.description)
        courseInfoIntent.putExtra("INSTRUCTOR_NAME", "")
        courseInfoIntent.putExtra("COURSE_OFFERING", arrayListOf("Monday 10:00 AM - 12:00 PM",
            "Wednesday 2:00 PM - 4:00 PM",
            "Friday 10:00 AM - 12:00 PM")
        )

        courseSelected = false
        context.startActivity(courseInfoIntent)
    }
}

