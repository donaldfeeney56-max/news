package com.newsapp.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.newsapp.app.SportNewsApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }
    var attachedUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            attachedUri = it
            attachedFileName = it.lastPathSegment ?: "Attached file"
        }
    }

    fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (attachedUri != null) "*/*" else "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("donaldfeeney56@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "FonSport News App Feedback")
            putExtra(Intent.EXTRA_TEXT, feedbackText)
            if (attachedUri != null) {
                putExtra(Intent.EXTRA_STREAM, attachedUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        SportNewsApp.amplitude.track("Feedback Sent", mapOf("has_attachment" to (attachedUri != null).toString()))
        context.startActivity(Intent.createChooser(intent, "Send feedback"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "We'd love to hear from you! Describe the issue or share your ideas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                placeholder = { Text("Write your feedback here...") },
                maxLines = 12
            )

            // Attachment section
            Text(
                text = "Attachment",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (attachedUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = attachedUri,
                            contentDescription = "Attachment preview",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attachedFileName ?: "File",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = {
                            attachedUri = null
                            attachedFileName = null
                        }) {
                            Icon(Icons.Default.Close, "Remove attachment")
                        }
                    }
                }
            } else {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Attach screenshot or file",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { sendFeedback() },
                modifier = Modifier.fillMaxWidth(),
                enabled = feedbackText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Feedback")
            }
        }
    }
}
