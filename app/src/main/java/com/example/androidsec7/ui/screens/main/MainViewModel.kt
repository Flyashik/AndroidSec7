package com.example.androidsec7.ui.screens.main

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import com.example.androidsec7.data.StepsRepository
import java.time.ZonedDateTime

class MainViewModel(app: Application): AndroidViewModel(app) {
    private val healthConnectClient = HealthConnectClient.getOrCreate(app)

    private val stepsRepository = StepsRepository(healthConnectClient)

    var steps = mutableStateOf("")
    var newSteps = mutableStateOf("")

    var errorMessage = mutableStateOf("")

    suspend fun readSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        steps.value = stepsRepository.readSteps(startTime, endTime).toString()
    }

    suspend fun writeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        val numOfSteps = newSteps.value.toLongOrNull()
        if (numOfSteps == null || numOfSteps <= 0 || numOfSteps > 1000000) {
            errorMessage.value = "Value must not be empty, less or equal 0 or greater 1000000"
            return
        }
        stepsRepository.writeSteps(startTime, endTime, numOfSteps)
        steps.value = stepsRepository.readSteps(startTime, endTime).toString()
        errorMessage.value = ""
    }

    suspend fun removeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        stepsRepository.removeSteps(startTime, endTime)
        steps.value = stepsRepository.readSteps(startTime, endTime).toString()
    }
}