package com.example.docscanner

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
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
        mutableStateOf<List<String>>(activity.filesDir
            .listFiles { _, name -> name.endsWith(".pdf") }
            ?.map { it.name } ?: emptyList()
        )
    }


    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                imageUris = result?.pages?.map { it.imageUri } ?: emptyList()

                result?.pdf?.let { pdf ->
                    val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    val fileName = "scan_${sdf.format(java.util.Date())}.pdf"
                    val fos = FileOutputStream(File(activity.filesDir, fileName))
                    activity.contentResolver.openInputStream(pdf.uri)?.use { it.copyTo(fos) }

                    // Refresh file list
                    scannedFiles = activity.filesDir
                        .listFiles { _, name -> name.endsWith(".pdf") }
                        ?.map { it.name } ?: emptyList()
                }
            }
        }
    )

    Scaffold(
        topBar = { CenterAlignedTopAppBar(
            title = { Text("Doc Scan")},
            actions = {
                IconButton(onClick = {
                    authViewModel.signOut()
                }) {
                    Icon(painterResource(R.drawable.log_out), null)
                }
            }) },
        content = { paddingValues ->
            ContentScreen(paddingValues, scannedFiles, activity, onDelete = {
                scannedFiles = activity.filesDir
                    .listFiles { _, name -> name.endsWith(".pdf") }
                    ?.map { it.name } ?: emptyList()
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener {
                        scannerLauncher.launch(IntentSenderRequest.Builder(it).build())
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Scan Failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }) {
               Icon(painter = painterResource(R.drawable.document_scanner_24px), contentDescription = "Document Icon")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    )

}

@Composable
fun PdfCard(
    filename: String,
    activity: MainActivity,
    onDelete : () -> Unit
) {
    val file = File(activity.filesDir, filename)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filename,
                style = MaterialTheme.typography.bodyLarge
            )

            Column{
                IconButton(
                    onClick = {
                        shareFile(activity, file)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share PDF"
                    )
                }

                IconButton(
                    onClick = {
                        deleteFile(file)
                        onDelete()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            }

        }
    }
}


fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*" // or "application/pdf", "image/*", etc.
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share File"))
}

@Composable
fun ContentScreen(paddingValues: PaddingValues, scannedFiles : List<String>, activity: MainActivity, onDelete: () -> Unit){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Text("Saved PDFs:", color = Color.Black) }
        items(scannedFiles) { filename ->
            PdfCard(filename = filename, activity = activity, onDelete = onDelete)
        }
    }
}

fun deleteFile(file: File){
    file.delete()
}