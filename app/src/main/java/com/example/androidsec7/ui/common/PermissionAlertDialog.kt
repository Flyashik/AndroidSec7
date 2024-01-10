package com.example.androidsec7.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionAlertDialog(
    onGotItClick: () -> Unit,
    onSettingsClick: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        icon = {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = "Warning"
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onGotItClick()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSettingsClick()
                }
            ) {
                Text("Settings")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onGotItClick()
                }
            ) {
                Text("Got It")
            }
        }
    )
}