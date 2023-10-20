package ca.uwaterloo.cs346project

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

class CourseSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SearchPage()
                }
            }
        }
    }
}

@Composable
fun SearchPage() {
    // Title
    Box(
        Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Text(
            text = "Schedule of Courses",
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
    }

    // Text under search bar
    Box(
        modifier = Modifier.fillMaxSize().padding(top = 135.dp, start = 15.dp, end = 15.dp)
    ) {
        Text(
            text = "View course schedule information and reviews from UW students"
        )
    }

    // Search Bar
    Search(listOf("CS348", "CS246", "ECON201", "SCI206"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(courses: List<String>) {
    var state by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(75.dp)) {
        TextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filtered: List<String> = if (!state.isEmpty()) {
                val result = ArrayList<String>()
                for (course in courses) {
                    if (course.lowercase().startsWith(state.lowercase())) {
                        result.add(course)
                    }
                }
                result
            } else listOf()

            items(filtered) { selected ->
                Row(
                    modifier = Modifier
                        .clickable(onClick = { /* ON CLICK */ })
                        .background(MaterialTheme.colorScheme.onSecondary)
                        .fillMaxWidth()
                        .padding(PaddingValues(8.dp, 16.dp))
                ) {
                    Text(text = selected, fontSize = 18.sp, color = Color.Black)
                }
            }
        }
    }
}
