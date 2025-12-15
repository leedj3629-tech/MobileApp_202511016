package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TimetableApp() } }
    }
}

enum class Day(val label: String) { MON("월"), TUE("화"), WED("수"), THU("목"), FRI("금") }

data class ClassItem(
    val id: Long,
    val title: String,
    val room: String,
    val day: Day,
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int
) {
    fun timeText(): String = "%02d:%02d ~ %02d:%02d".format(startHour, startMin, endHour, endMin)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableApp() {
    var selectedDay by remember { mutableStateOf(Day.MON) }
    var showAddDialog by remember { mutableStateOf(false) }

    // 메모리 저장(초간단)
    var classes by remember { mutableStateOf(listOf<ClassItem>()) }

    // 선택 요일 필터
    val todayList = classes
        .filter { it.day == selectedDay }
        .sortedWith(compareBy({ it.startHour }, { it.startMin }))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("동준의 시간표") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            DayTabs(selectedDay = selectedDay, onSelect = { selectedDay = it })
            Spacer(Modifier.height(12.dp))

            if (todayList.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("등록된 수업이 없어요.\n오른쪽 아래 +로 추가해보세요!",
                        style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(todayList, key = { it.id }) { item ->
                        ClassCard(
                            item = item,
                            onDelete = { classes = classes.filterNot { it.id == item.id } }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddClassDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newItem ->
                classes = classes + newItem
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DayTabs(selectedDay: Day, onSelect: (Day) -> Unit) {
    val days = Day.values()
    ScrollableTabRow(selectedTabIndex = days.indexOf(selectedDay)) {
        days.forEach { day ->
            Tab(
                selected = day == selectedDay,
                onClick = { onSelect(day) },
                text = { Text(day.label) }
            )
        }
    }
}

@Composable
fun ClassCard(item: ClassItem, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(item.timeText())
                if (item.room.isNotBlank()) Text("강의실: ${item.room}")
            }
            TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}

@Composable
fun AddClassDialog(onDismiss: () -> Unit, onAdd: (ClassItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(Day.MON) }

    var sh by remember { mutableStateOf("9") }
    var sm by remember { mutableStateOf("0") }
    var eh by remember { mutableStateOf("10") }
    var em by remember { mutableStateOf("0") }

    fun toIntOrNullSafe(s: String) = s.trim().toIntOrNull()

    val canAdd = title.isNotBlank()
            && toIntOrNullSafe(sh) != null && toIntOrNullSafe(sm) != null
            && toIntOrNullSafe(eh) != null && toIntOrNullSafe(em) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = canAdd,
                onClick = {
                    val startH = sh.trim().toInt()
                    val startM = sm.trim().toInt()
                    val endH = eh.trim().toInt()
                    val endM = em.trim().toInt()

                    // 아주 간단한 범위 체크(직접 만든 티)
                    val validRange = startH in 0..23 && endH in 0..23 && startM in 0..59 && endM in 0..59

                    if (!validRange) return@TextButton

                    onAdd(
                        ClassItem(
                            id = System.currentTimeMillis(),
                            title = title.trim(),
                            room = room.trim(),
                            day = day,
                            startHour = startH,
                            startMin = startM,
                            endHour = endH,
                            endMin = endM
                        )
                    )
                }
            ) { Text("추가") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        title = { Text("수업 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("과목명") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("강의실(선택)") },
                    singleLine = true
                )

                // 요일 선택(아주 단순)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("요일: ")
                    Spacer(Modifier.width(8.dp))
                    DayDropdown(selected = day, onSelect = { day = it })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sh, onValueChange = { sh = it }, label = { Text("시작(시)") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sm, onValueChange = { sm = it }, label = { Text("시작(분)") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eh, onValueChange = { eh = it }, label = { Text("끝(시)") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = em, onValueChange = { em = it }, label = { Text("끝(분)") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                Text("※ 과목명만 필수, 시간은 숫자로 입력", style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}

@Composable
fun DayDropdown(selected: Day, onSelect: (Day) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(selected.label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Day.values().forEach { d ->
                DropdownMenuItem(
                    text = { Text(d.label) },
                    onClick = { onSelect(d); expanded = false }
                )
            }
        }
    }
}
