package com.example.androidsec7

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.example.androidsec7.ui.common.PermissionAlertDialog
import com.example.androidsec7.ui.theme.AndroidSec7Theme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class)
        )
    private lateinit var requestPermissions: ActivityResultLauncher<Set<String>>
    private var accessGranted by mutableStateOf(true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionActivityContract =
            PermissionController.createRequestPermissionResultContract()

        requestPermissions =
            registerForActivityResult(requestPermissionActivityContract) { granted ->
                accessGranted = granted.containsAll(PERMISSIONS)
            }

        setContent {
            AndroidSec7Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val providerPackageName = "com.google.android.apps.healthdata"
                    val context = LocalContext.current

                    val coroutineScoupe = rememberCoroutineScope()

                    when (HealthConnectClient.getSdkStatus(context, providerPackageName)) {
                        HealthConnectClient.SDK_UNAVAILABLE -> {
                            Toast.makeText(LocalContext.current, "SDK_UNV", Toast.LENGTH_LONG)
                                .show()
                        }

                        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                            val uriString =
                                "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setPackage("com.android.vending")
                                    data = Uri.parse(uriString)
                                    putExtra("overlay", true)
                                    putExtra("callerId", context.packageName)
                                }
                            )
                        }

                        HealthConnectClient.SDK_AVAILABLE -> {
                            val healthConnectClient = HealthConnectClient.getOrCreate(context)
                            LaunchedEffect(Unit) {
                                accessGranted = checkPermissionsAndRun(healthConnectClient)
                            }
                            if (!accessGranted)
                                PermissionAlertDialog(
                                    onGotItClick = {
                                        coroutineScoupe.launch {
                                            accessGranted = checkPermissionsAndRun(healthConnectClient)
                                            if (!accessGranted) {
                                                Toast.makeText(this@MainActivity, "Permission denied", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onSettingsClick = { requestPermissions.launch(PERMISSIONS) },
                                    dialogTitle = getString(R.string.no_access_title),
                                    dialogText = getString(R.string.no_access_text),
                                )
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()

        return granted.containsAll(PERMISSIONS)
    }
}
