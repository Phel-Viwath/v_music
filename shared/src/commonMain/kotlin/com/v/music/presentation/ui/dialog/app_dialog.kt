package com.v.music.presentation.ui.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppDialog(
    showDialog: Boolean,
    title: String = "V-Music", // Default value allows omitting the title
    message: String,
    confirmText: String = "OK",
    dismissText: String? = null, // Optional: if null, the button won't show
    icon: ImageVector = Icons.Default.Warning,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        onDismiss() // Usually, you want to close after confirm
                    }
                ) {
                    Text(confirmText)
                }
            },
            // The dismiss button is only rendered if dismissText is provided
            dismissButton = dismissText?.let { text ->
                {
                    TextButton(onClick = onDismiss) {
                        Text(text)
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun AppDialogPreView(){
    AppDialog(
        showDialog = true,
        message = "Preview",
        onDismiss = {}
    )
}