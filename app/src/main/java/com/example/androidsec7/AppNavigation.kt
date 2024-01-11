package com.example.androidsec7

import androidx.compose.runtime.Composable
import androidx.health.connect.client.HealthConnectClient
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidsec7.ui.screens.calendar.CalendarScreen
import com.example.androidsec7.ui.screens.main.MainScreen
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val calendarState = rememberSelectableCalendarState(
        initialMonth = YearMonth.now(),
        initialSelection = listOf(LocalDate.now()),
        initialSelectionMode = SelectionMode.Single,
    )

    NavHost(
        navController = navController,
        startDestination = AppRoutes.MainScreen.name
    ) {
        composable(AppRoutes.MainScreen.name) {
            MainScreen(navController = navController, calendarState = calendarState)
        }
//        composable(AppRoutes.CalendarScreen.name) {
//            CalendarScreen(navController = navController, calendarState = calendarState)
//        }
    }
}

enum class AppRoutes {
    MainScreen, //CalendarScreen
}