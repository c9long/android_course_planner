package ca.uwaterloo.cs346project

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.clickable

@Composable
fun WeeklyScheduler() {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the current week
        Text(
            text = "Week of ${selectedDate.getWeekLabel()}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Weekdays header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (day in DayOfWeek.values()) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Week view
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (day in selectedDate.getWeekDays()) {
                DayCell(
                    date = day,
                    isSelected = day == selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(if (isSelected) Color.Blue else Color.Transparent)
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 16.sp,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

fun LocalDate.getWeekDays(): List<LocalDate> {
    val weekStart = this.with(DayOfWeek.MONDAY)
    return (0 until 7).map { weekStart.plusDays(it.toLong()) }
}

fun LocalDate.getWeekLabel(): String {
    val startDate = this.with(DayOfWeek.MONDAY)
    val endDate = startDate.plusDays(6)
    return "${startDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} " +
            "${startDate.dayOfMonth} - ${endDate.dayOfMonth}, ${startDate.year}"
}
