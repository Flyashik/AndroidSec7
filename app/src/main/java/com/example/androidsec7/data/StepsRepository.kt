package com.example.androidsec7.data

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime

class StepsRepository(healthConnectClient: HealthConnectClient) {
    private val logSpace = "Steps Repository"

    private val _healthConnectClient = healthConnectClient

    suspend fun readSteps(startTime: Instant, endTime: Instant): List<StepsRecord> {
        return try {
            val response =
                _healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            startTime,
                            endTime
                        )
                    )
                )

            response.records
        } catch (e: Exception) {
            Log.e(logSpace, "Read error. ", e)
            emptyList()
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

    suspend fun updateSteps(record: StepsRecord, count: Long) {
        try {
            val stepsRecord = StepsRecord(
                count = count,
                startTime = record.startTime,
                endTime = record.endTime,
                startZoneOffset = record.startZoneOffset,
                endZoneOffset = record.endZoneOffset,
                metadata = record.metadata
            )
            _healthConnectClient.updateRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            Log.e(logSpace, "Update error. ", e)
        }
    }

    suspend fun removeSteps(record: StepsRecord) {
        try {
            _healthConnectClient.deleteRecords(
                StepsRecord::class,
                recordIdsList = listOf(record.metadata.id),
                clientRecordIdsList = emptyList()
            )
        } catch (e: Exception) {
            Log.e(logSpace, "Remove error. ", e)
        }
    }
}