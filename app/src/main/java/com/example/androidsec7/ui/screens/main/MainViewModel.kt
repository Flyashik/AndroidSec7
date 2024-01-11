package com.example.androidsec7.ui.screens.main

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.AndroidViewModel
import com.example.androidsec7.data.StepsRepository
import java.time.Instant
import java.time.ZonedDateTime

class MainViewModel(app: Application): AndroidViewModel(app) {
    private val healthConnectClient = HealthConnectClient.getOrCreate(app)

    private val stepsRepository = StepsRepository(healthConnectClient)

    var steps = mutableStateOf(listOf<StepsRecord>())
    var errorMessage = mutableStateOf("")

    var startOfDay = mutableStateOf(ZonedDateTime.now())
    var endOfDay = mutableStateOf(ZonedDateTime.now())

    suspend fun readSteps() {
        steps.value = stepsRepository.readSteps(startOfDay.value.toInstant(), endOfDay.value.toInstant())
    }

    suspend fun writeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime, newRecord: String) {
        if (startTime.isAfter(endTime)) {
            errorMessage.value = "Invalid time"
            return
        }

        val numOfSteps = newRecord.toLongOrNull()
        if (numOfSteps == null || numOfSteps <= 0 || numOfSteps > 1000000) {
            errorMessage.value = "Value must not be empty, less or equal 0 or greater 1000000"
            return
        }
        stepsRepository.writeSteps(startTime, endTime, numOfSteps)
        readSteps()
        errorMessage.value = ""
    }

    suspend fun updateSteps(record: StepsRecord, newCount: String) {
        val numOfSteps = newCount.toLongOrNull()
        if (numOfSteps == null || numOfSteps <= 0 || numOfSteps > 1000000) {
            errorMessage.value = "Value must not be empty, less or equal 0 or greater 1000000"
            return
        }
        stepsRepository.updateSteps(record, numOfSteps)
        readSteps()
        errorMessage.value = ""
    }

    suspend fun removeSteps(record: StepsRecord) {
        stepsRepository.removeSteps(record)
        readSteps()
    }
}