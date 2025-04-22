package com.example.docscanner

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocScan(activity: MainActivity, authViewModel: AuthViewModel) {
    val options = GmsDocumentScannerOptions.Builder()
        .setScannerMode(SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true)
        .setPageLimit(100)
        .setResultFormats(RESULT_FORMAT_PDF, RESULT_FORMAT_JPEG)
        .build()

    val scanner = GmsDocumentScanning.getClient(options)

    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var scannedFiles by remember {
        mutableStateOf(loadPdfFiles(activity))
    }
    var showRenameDialog by remember { mutableStateOf(false) }
    var currentFileToRename by remember { mutableStateOf<File?>(null) }
    var newFileName by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingPdfUri by remember { mutableStateOf<Uri?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val darkTheme = isSystemInDarkTheme()

    // Dialog for logout confirmation
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirmLogout = { deleteFiles ->
                if (deleteFiles) {
                    deleteAllFiles(activity)
                }
                authViewModel.signOut()
            }
        )
    }

    // The rest of your existing dialog implementations
    if (showSaveDialog) {
        CustomFileNameDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { customName ->
                pendingPdfUri?.let { pdfUri ->
                    val fileName = if (customName.isNotBlank()) {
                        if (customName.endsWith(".pdf")) customName else "$customName.pdf"
                    } else {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        "${sdf.format(Date())}.pdf"
                    }

                    val file = File(activity.filesDir, fileName)
                    val fos = FileOutputStream(file)
                    activity.contentResolver.openInputStream(pdfUri)?.use { it.copyTo(fos) }

                    // Refresh file list
                    scannedFiles = loadPdfFiles(activity)

                    Toast.makeText(activity, "Document saved as $fileName", Toast.LENGTH_SHORT).show()
                }
                showSaveDialog = false
            }
        )
    }

    if (showRenameDialog && currentFileToRename != null) {
        RenameFileDialog(
            currentFileName = currentFileToRename!!.name,
            onDismiss = {
                showRenameDialog = false
                currentFileToRename = null
                newFileName = ""
            },
            onConfirm = { newName ->
                val newFileName = if (newName.endsWith(".pdf")) newName else "$newName.pdf"
                if (renameFile(currentFileToRename!!, newFileName)) {
                    Toast.makeText(activity, "File renamed successfully", Toast.LENGTH_SHORT).show()
                    scannedFiles = loadPdfFiles(activity)
                } else {
                    Toast.makeText(activity, "Failed to rename file", Toast.LENGTH_SHORT).show()
                }
                showRenameDialog = false
                currentFileToRename = null
            }
        )
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                imageUris = result?.pages?.map { it.imageUri } ?: emptyList()

                result?.pdf?.let { pdf ->
                    pendingPdfUri = pdf.uri
                    showSaveDialog = true
                }
            }
        }
    )

    Scaffold(
        topBar = {
            DocScanTopAppBar(
                onLogoutClick = { showLogoutDialog = true } // Changed to show dialog
            )
        },
        content = { paddingValues ->
            ContentScreen(
                paddingValues = paddingValues,
                scannedFiles = scannedFiles,
                activity = activity,
                onOpenFile = { file -> openPdfFile(activity, file) },
                onDeleteFile = { file ->
                    deleteFile(file)
                    scannedFiles = loadPdfFiles(activity)
                },
                onShareFile = { file -> shareFile(activity, file) },
                onRenameFile = { file ->
                    currentFileToRename = file
                    newFileName = file.nameWithoutExtension
                    showRenameDialog = true
                },
                darkTheme = darkTheme
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scanner.getStartScanIntent(activity)
                        .addOnSuccessListener {
                            scannerLauncher.launch(IntentSenderRequest.Builder(it).build())
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Scan Failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(R.drawable.document_scanner_24px),
                    contentDescription = "Scan Document",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocScanTopAppBar(onLogoutClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.document_scanner_24px),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Document Scanner",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    painter = painterResource(R.drawable.log_out),
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun ContentScreen(
    paddingValues: PaddingValues,
    scannedFiles: List<File>,
    activity: MainActivity,
    onOpenFile: (File) -> Unit,
    onDeleteFile: (File) -> Unit,
    onShareFile: (File) -> Unit,
    onRenameFile: (File) -> Unit,
    darkTheme: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (scannedFiles.isEmpty()) {
            EmptyStateView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        text = "Your Documents",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(scannedFiles) { file ->
                    PdfDocumentCard(
                        file = file,
                        onOpenFile = { onOpenFile(file) },
                        onDeleteFile = { onDeleteFile(file) },
                        onShareFile = { onShareFile(file) },
                        onRenameFile = { onRenameFile(file) },
                        darkTheme = darkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.document_scanner_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No documents yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to scan a document",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun PdfDocumentCard(
    file: File,
    onOpenFile: () -> Unit,
    onDeleteFile: () -> Unit,
    onShareFile: () -> Unit,
    onRenameFile: () -> Unit,
    darkTheme: Boolean
) {
    val creationDate = remember {
        val date = Date(file.lastModified())
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(date)
    }

    Card(
        onClick = onOpenFile,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.pdf_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.nameWithoutExtension,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = creationDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit button
                FilledTonalIconButton(
                    onClick = onRenameFile,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Share button
                FilledTonalIconButton(
                    onClick = onShareFile,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete button
                FilledTonalIconButton(
                    onClick = onDeleteFile,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}


@Composable
fun RenameFileDialog(
    currentFileName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val defaultName = remember { currentFileName.removeSuffix(".pdf") }
    var newFileName by remember { mutableStateOf(defaultName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rename Document",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Text(
                            ".pdf",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(newFileName) },
                        enabled = newFileName.isNotBlank() && newFileName != defaultName
                    ) {
                        Text("Rename")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFileNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Save Document",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Enter a name for your document",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Text(
                            ".pdf",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(fileName) }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Utility functions

fun loadPdfFiles(context: Context): List<File> {
    return context.filesDir
        .listFiles { file -> file.name.endsWith(".pdf") }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()
}

fun deleteFile(file: File): Boolean {
    return file.delete()
}

fun renameFile(file: File, newName: String): Boolean {
    val newFile = File(file.parentFile, newName)

    // Don't overwrite existing files
    if (newFile.exists()) {
        return false
    }

    return file.renameTo(newFile)
}

fun openPdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Open PDF"))
}

fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
}

// Extension property to get filename without extension
val File.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmLogout: (deleteFiles: Boolean) -> Unit
) {
    var deleteFiles by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to logout?",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deleteFiles,
                        onCheckedChange = { deleteFiles = it }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Delete all scanned documents",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { deleteFiles = !deleteFiles }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmLogout(deleteFiles) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun deleteAllFiles(context: Context) {
    context.filesDir
        .listFiles { file -> file.name.endsWith(".pdf") }
        ?.forEach { it.delete() }
}