package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import androidx.compose.ui.text.font.FontWeight
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
            Text("Course Schedule", fontSize = 24.sp)
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
            Text("Course Search", fontSize = 24.sp)
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
            Text("Log Out", fontSize = 24.sp)
        }
    }

    if (showColumn1) {
        val context = LocalContext.current
        val scheduleIntent = Intent(context, ScheduleActivity::class.java)
        context.startActivity(scheduleIntent)
        showColumn1 = false
    }

    if (showColumn2) {
        val context = LocalContext.current
        val courseActivityIntent = Intent(context, CourseSearchActivity::class.java)
        
        context.startActivity(courseActivityIntent)
        showColumn2 = false
    }


    if (showColumn3) {
        LocalContext.current.startActivity(Intent(LocalContext.current, LoginActivity::class.java))
        showColumn3 = false
    }
}



