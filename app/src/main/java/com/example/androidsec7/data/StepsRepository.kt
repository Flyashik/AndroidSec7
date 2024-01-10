package com.example.androidsec7.data

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.ZonedDateTime

class StepsRepository(healthConnectClient: HealthConnectClient) {
    private val logSpace = "Steps Repository"

    private val _healthConnectClient = healthConnectClient

    suspend fun readSteps(startTime: ZonedDateTime, endTime: ZonedDateTime): Long? {
        return try {
            val response = _healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                )
            )
            // The result may be null if no data is available in the time range
            response[StepsRecord.COUNT_TOTAL]
        } catch (e: Exception) {
            Log.e(logSpace, "Read error. ", e)
            null
        }
    }

    suspend fun writeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime, count: Long) {
        try {
            val stepsRecord = StepsRecord(
                count = count,
                startTime = startTime.toInstant(),
                endTime = endTime.toInstant(),
                startZoneOffset = startTime.offset,
                endZoneOffset = endTime.offset,
            )
            _healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            Log.e(logSpace, "Write error. ", e)
        }
    }

    suspend fun removeSteps(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        try {
            _healthConnectClient.deleteRecords(
                StepsRecord::class, TimeRangeFilter.between(
                    startTime.toInstant(),
                    endTime.toInstant()
                )
            )
        } catch (e: Exception) {
            Log.e(logSpace, "Remove error. ", e)
        }
    }
}