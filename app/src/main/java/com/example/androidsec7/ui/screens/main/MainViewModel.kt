package com.example.androidsec7.ui.screens.main

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import com.example.androidsec7.data.StepsRepository
import java.time.ZonedDateTime

class MainViewModel(app: Application, healthConnectClient: HealthConnectClient): AndroidViewModel(app) {
    private val stepsRepository = StepsRepository(healthConnectClient)

    var steps = mutableStateOf("")
    var newSteps = mutableStateOf("")

    suspend fun readSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        steps.value = stepsRepository.readSteps(startTime, endTime).toString()
    }

    suspend fun writeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        val numOfSteps = newSteps.value.toLongOrNull()
        if (numOfSteps == null || numOfSteps <= 0) {
            return
        }
        stepsRepository.writeSteps(startTime, endTime, numOfSteps)
        steps.value = stepsRepository.readSteps(startTime, endTime).toString()
    }

    suspend fun removeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        stepsRepository.removeSteps(startTime, endTime)
        steps.value = stepsRepository.readSteps(startTime, endTime).toString()
    }
}