package com.example.androidsec7.ui.screens.main

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getString
import androidx.health.connect.client.HealthConnectClient
import androidx.navigation.NavHostController
import com.example.androidsec7.R
import io.github.boguszpawlowski.composecalendar.CalendarState
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    calendarState: CalendarState<DynamicSelectionState>,
    healthConnectClient: HealthConnectClient,
    viewModel: MainViewModel = MainViewModel(Application(), healthConnectClient)
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
            var steps by remember { viewModel.steps }
            var newSteps by remember { viewModel.newSteps }

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
                    viewModel.readSteps(start, end)
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
                Text(
                    text = LocalDate.parse(calendarState.selectionState.selection[0].toString())
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.padding(10.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Steps",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (steps == "" || steps == "null") {
                        IconButton(
                            onClick = {
                                newSteps = "1"
                                coroutineScope.launch {
                                    if (start != null && end != null) {
                                        viewModel.writeSteps(start, end)
                                    }
                                }
                            },
                        ) {
                            Icon(Icons.Default.Add, "add")
                        }
                    }
                }
                if (steps != "" && steps != "null") {
                    var showEdit by remember { mutableStateOf(false) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$steps step(s)",
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
                                        if (start != null && end != null) {
                                            viewModel.removeSteps(start, end)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, "delete")
                            }
                        }
                    }
                    if (showEdit) {
                        viewModel.newSteps.value = viewModel.steps.value

                        Dialog(onDismissRequest = { showEdit = false }) {
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
                                    OutlinedTextField(
                                        value = newSteps,
                                        onValueChange = { viewModel.newSteps.value = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        label = { Text("New value") },
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    if (start != null && end != null) {
                                                        viewModel.writeSteps(start, end)
                                                    }
                                                }
                                                showEdit = false
                                            },
                                            modifier = Modifier.padding(8.dp),
                                        ) {
                                            Text("Confirm")
                                        }
                                        TextButton(
                                            onClick = { showEdit = false },
                                            modifier = Modifier.padding(8.dp),
                                        ) {
                                            Text("Dismiss")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
