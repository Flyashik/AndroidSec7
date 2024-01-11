package com.example.androidsec7.ui.screens.main

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getString
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.androidsec7.R
import io.github.boguszpawlowski.composecalendar.CalendarState
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    calendarState: CalendarState<DynamicSelectionState>,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(getString(context, R.string.app_name)) },
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onSecondary
            ),
        )
    }) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(contentPadding)
                .padding(12.dp),
        ) {
            val steps by remember { viewModel.steps }

            val start = calendarState.selectionState.selection.firstOrNull()?.atStartOfDay(
                ZoneId.systemDefault()
            )
                ?.withFixedOffsetZone()
            val end = calendarState.selectionState.selection.lastOrNull()?.atStartOfDay(
                ZoneId.systemDefault()
            )?.plusDays(1)
                ?.withFixedOffsetZone()

            val coroutineScope = rememberCoroutineScope()

            coroutineScope.launch {
                if (start != null && end != null) {
                    viewModel.startOfDay.value = start
                    viewModel.endOfDay.value = end
                    viewModel.readSteps()
                }
            }

            SelectableCalendar(calendarState = calendarState)

            Spacer(Modifier.padding(10.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Current day",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (calendarState.selectionState.selection.isNotEmpty() && calendarState.selectionState.selection[0].toString() != "null") {
                    Text(
                        text = LocalDate.parse(calendarState.selectionState.selection[0].toString())
                            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(Modifier.padding(10.dp))

            Column {
                var showAddDialog by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Steps",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            showAddDialog = true
                        },
                    ) {
                        Icon(Icons.Default.Add, "add")
                    }
                    if (showAddDialog) {
                        AddDialog(
                            context = context,
                            viewModel = viewModel,
                            start = start!!,
                            onClose = { showAddDialog = false }
                        )
                    }
                }
                if (steps.isNotEmpty() && start != null && end != null) {
                    for (record in steps) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var showEdit by remember { mutableStateOf(false) }

                            val formatter = DateTimeFormatter.ofPattern("HH:mm")

                            val formatStartTime = record.startTime.atZone(start.zone).toLocalDateTime().format(formatter)
                            val formatEndTime = record.endTime.atZone(start.zone).toLocalDateTime().format(formatter)

                            Text(
                                text = "$formatStartTime - $formatEndTime"
                            )
                            Text(
                                text = "${record.count} step(s)",
                                fontSize = 20.sp,
                            )
                            Row {

                                IconButton(
                                    onClick = { showEdit = true },
                                ) {
                                    Icon(Icons.Default.Edit, "edit")
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.removeSteps(record)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, "delete")
                                }
                            }
                            if (showEdit) {
                                EditDialog(
                                    context = context,
                                    viewModel = viewModel,
                                    currentValue = record.count.toString(),
                                    record = record,
                                    onClose = { showEdit = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    context: Context,
    viewModel: MainViewModel,
    start: ZonedDateTime,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var newCount by remember { mutableStateOf("") }
    var newStartTime by remember { mutableStateOf(start) }
    var newEndTime by remember { mutableStateOf(start) }


    Dialog(onDismissRequest = { onClose() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Start time")
                            TimePickerWithDialog(
                                currentTime = newStartTime,
                                onTimeSelected = {
                                    newStartTime = it
                                }
                            )
                        }

                        Column {
                            Text("End time")
                            TimePickerWithDialog(
                                currentTime = newEndTime,
                                onTimeSelected = {
                                    newEndTime = it
                                }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = newCount,
                            label = { Text("Steps") },
                            onValueChange = { newCount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.writeSteps(newStartTime, newEndTime, newCount)

                                if (viewModel.errorMessage.value != "") {
                                    Toast.makeText(
                                        context,
                                        viewModel.errorMessage.value,
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    onClose()
                                }
                            }
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                    TextButton(
                        onClick = { onClose() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerWithDialog(
    currentTime: ZonedDateTime,
    onTimeSelected: (ZonedDateTime) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true,
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(color = Color.LightGray)
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timeState)
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog = false }) {
                        Text(text = "Dismiss")
                    }
                    TextButton(onClick = {
                        showDialog = false
                        onTimeSelected(
                            ZonedDateTime.of(
                                LocalDate.from(currentTime),
                                LocalTime.of(timeState.hour, timeState.minute),
                                currentTime.zone
                            )
                        )
                    }) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }

    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(currentTime.toLocalDateTime().format(formatter))
        Button(onClick = { showDialog = true }) {
            Text(text = "Set time")
        }
    }
}

@Composable
fun EditDialog(
    context: Context,
    viewModel: MainViewModel,
    currentValue: String,
    record: StepsRecord,
    onClose: () -> Unit
) {
    var newValue by remember { mutableStateOf(currentValue) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onClose() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = newValue,
                        label = { Text("Steps") },
                        onValueChange = { newValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.updateSteps(record, newValue)

                                if (viewModel.errorMessage.value != "") {
                                    Toast.makeText(
                                        context,
                                        viewModel.errorMessage.value,
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    onClose()
                                }
                            }
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                    TextButton(
                        onClick = { onClose() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}