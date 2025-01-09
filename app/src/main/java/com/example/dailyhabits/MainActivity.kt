package com.example.dailyhabits

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.example.dailyhabits.ui.theme.DailyHabitsTheme
import java.util.*

data class Goal(
    val name: String,
    var frequency: GoalFrequency,
    var completed: Boolean = false,
    var startDate: String = "2025-01-01",
    var endDate: String = "2025-01-01",
    var startTime: String = "08:00",
    var endTime: String = "18:00",
    val completionDates: MutableList<String> = mutableListOf()
)

enum class GoalFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyHabitsTheme {
                GoalManagementScreen()
            }
        }
    }
}

@Composable
fun GoalManagementScreen() {
    var goals by remember { mutableStateOf(mutableListOf<Goal>()) }
    var newGoal by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(GoalFrequency.DAILY) }
    var startDate by remember { mutableStateOf("2025-01-01") }
    var endDate by remember { mutableStateOf("2025-01-01") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text("我的目標", style = MaterialTheme.typography.h6)

        goals.forEach { goal ->
            GoalCard(
                goal,
                onCompleteChange = { completed ->
                    goals = goals.map {
                        if (it == goal) it.copy(completed = completed) else it
                    }.toMutableList()
                },
                onDelete = {
                    goals = goals.filter { it != goal }.toMutableList()
                },
                onFrequencyChange = { frequency ->
                    goals = goals.map {
                        if (it == goal) it.copy(frequency = frequency) else it
                    }.toMutableList()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 輸入新目標名稱
        BasicTextField(
            value = newGoal,
            onValueChange = { newGoal = it },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, Color.Black)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 設置目標頻率
        FrequencySelector(selectedFrequency) { frequency ->
            selectedFrequency = frequency
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 開始日期選擇
        DateSelector("開始日期: ", startDate) { selectedDate ->
            startDate = selectedDate
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 結束日期選擇
        DateSelector("結束日期: ", endDate) { selectedDate ->
            endDate = selectedDate
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 添加目標按鈕
        Button(
            onClick = {
                if (newGoal.isNotEmpty()) {
                    goals = (goals + Goal(name = newGoal, frequency = selectedFrequency, startDate = startDate, endDate = endDate)).toMutableList()
                    newGoal = ""  // 清空目標輸入框
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加目標")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 顯示進度報告
        ProgressReport(goals)

        // 顯示回顧報告
        ReviewReport(goals)
    }
}

@Composable
fun GoalCard(goal: Goal, onCompleteChange: (Boolean) -> Unit, onDelete: () -> Unit, onFrequencyChange: (GoalFrequency) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(goal.name, style = MaterialTheme.typography.h6)
            Text("開始日期: ${goal.startDate}", style = MaterialTheme.typography.body2)
            Text("結束日期: ${goal.endDate}", style = MaterialTheme.typography.body2)
            Text("頻率: ${goal.frequency.name}", style = MaterialTheme.typography.body2)

            FrequencySelector(goal.frequency) { frequency ->
                onFrequencyChange(frequency)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("完成進度: ${if (goal.completed) "完成" else "未完成"}")
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(
                    checked = goal.completed,
                    onCheckedChange = {
                        if (it) markGoalAsCompleted(goal)
                        onCompleteChange(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onDelete() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("刪除目標", color = Color.White)
            }
        }
    }
}

@Composable
fun FrequencySelector(selectedFrequency: GoalFrequency, onFrequencySelected: (GoalFrequency) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = GoalFrequency.values()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("頻率: ")
        Text(selectedFrequency.name, style = MaterialTheme.typography.h6)

        TextButton(onClick = { expanded = !expanded }) {
            Text("修改頻率")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { frequency ->
                DropdownMenuItem(onClick = {
                    onFrequencySelected(frequency)
                    expanded = false
                }) {
                    Text(frequency.name)
                }
            }
        }
    }
}

@Composable
fun DateSelector(label: String, selectedDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        TextButton(onClick = {
            DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
                onDateSelected(formattedDate)
            }, year, month, dayOfMonth).show()
        }) {
            Text(selectedDate)
        }
    }
}

@Composable
fun ProgressReport(goals: List<Goal>) {
    val completedGoals = goals.count { it.completed }
    val totalGoals = goals.size
    val completionRate = if (totalGoals > 0) (completedGoals / totalGoals.toFloat()) * 100 else 0f

    Text("進度報告", style = MaterialTheme.typography.h6)
    Text("完成率: ${"%.2f".format(completionRate)}%", style = MaterialTheme.typography.body1)
    Text("已完成目標: $completedGoals / $totalGoals", style = MaterialTheme.typography.body1)
}

@Composable
fun ReviewReport(goals: List<Goal>) {
    val streak = calculateStreak(goals)
    val totalCompleted = goals.sumOf { it.completionDates.size }
    val frequencyCompletion = calculateCompletionRate(goals)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("回顧報告", style = MaterialTheme.typography.h6)
        Text("連續完成天數: $streak 天", style = MaterialTheme.typography.body1)
        Text("累計完成目標: $totalCompleted", style = MaterialTheme.typography.body1)

        Spacer(modifier = Modifier.height(8.dp))
        Text("按頻率分類的完成率：", style = MaterialTheme.typography.h6)
        frequencyCompletion.forEach { (frequency, rate) ->
            Text("$frequency: ${"%.2f".format(rate)}%", style = MaterialTheme.typography.body1)
        }
    }
}

fun calculateStreak(goals: List<Goal>): Int {
    val today = Calendar.getInstance()
    var streak = 0

    // 找出最大的完成日期數量，若為 null 則默認為 0
    val maxDays = goals.maxOfOrNull { it.completionDates.size } ?: 0

    // 遍歷從 0 到 maxDays 的範圍
    for (i in 0 until maxDays) {
        // date
        val dateToCheck = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -i)
        }
        val formattedDate = "${dateToCheck.get(Calendar.YEAR)}-${dateToCheck.get(Calendar.MONTH) + 1}-${dateToCheck.get(Calendar.DAY_OF_MONTH)}"

        // finish goal
        if (goals.any { it.completionDates.contains(formattedDate) }) {
            streak++
        } else {
            break
        }
    }

    return streak
}

fun calculateCompletionRate(goals: List<Goal>): Map<String, Float> {
    val grouped = goals.groupBy { it.frequency }
    return grouped.mapValues { (_, goals) ->
        val completed = goals.sumOf { it.completionDates.size }
        val total = goals.size
        if (total > 0) (completed / total.toFloat()) * 100 else 0f
    }.mapKeys { it.key.name }
}

fun markGoalAsCompleted(goal: Goal) {
    val currentDate = Calendar.getInstance().time.toString().substring(0, 10) // 獲取今天日期
    if (!goal.completionDates.contains(currentDate)) {
        goal.completionDates.add(currentDate)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DailyHabitsTheme {
        GoalManagementScreen()
    }
}