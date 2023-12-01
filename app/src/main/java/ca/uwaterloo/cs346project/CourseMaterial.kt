package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CourseMaterial : ComponentActivity() {
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDBHelper = UserDBHelper(this)

        fun getFileNameFromUri(uri: Uri): String {
            var name = "New file" // Default name if original name can't be found
            val cursor = contentResolver.query(uri, null, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    // Get the column index of MediaStore.Images.Media.DISPLAY_NAME
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = it.getString(nameIndex)
                    }
                }
            }

            return name
        }

        fun copyFileToInternalStorage(context: Context, uri: Uri, fileName: String): String {
            val inputStream = context.contentResolver.openInputStream(uri)
            val newFile = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(newFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input?.copyTo(output)
                }
            }

            return newFile.absolutePath
        }


        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                //val fileName = getFileNameFromUri(it)
                //userDBHelper.addFile(fileName, it.toString())
                //val intent = intent
                //finish()
                //startActivity(intent)
                val context = this@CourseMaterial
                val fileName = getFileNameFromUri(it)
                userDBHelper.doesFileExist(fileName, object: ResponseCallback {
                    override fun onSuccess(responseBody: String) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "A file with the same name already exists.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(e: IOException) {
                        // File with the same name does not exist, proceed with copying
                        val internalFilePath = copyFileToInternalStorage(context, it, fileName)
                        userDBHelper.addFile(fileName, internalFilePath, object: ResponseCallback {
                            override fun onSuccess(responseBody: String) {
                                println("file added")
                            }
                            override fun onFailure(e: IOException) {
                                println("file not added")
                                e.printStackTrace()
                            }
                        }) // Store the internal path
                        val intent = intent
                        finish()
                        startActivity(intent)
                    }
                })
            }
        }

        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CourseMaterialPage(userDBHelper, filePickerLauncher)
                }
            }
        }


    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMaterialPage(userDBHelper: UserDBHelper, filePickerLauncher: ActivityResultLauncher<String>) {
    var fileList by remember { mutableStateOf(listOf<FileRecord>()) }
    var selectedFileForRename by remember { mutableStateOf<FileRecord?>(null) }
    var fileToDelete by remember { mutableStateOf<FileRecord?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        userDBHelper.getAllFiles() { allFiles, error ->
            if (error != null) {
                println("Error fetching reviews: ${error.message}")
            } else if (allFiles != null) {
                println("initial fetch of all files")
                fileList = allFiles
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                filePickerLauncher.launch("application/pdf")
                //.launch("application/pdf")
            }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(scrollState)
        ) {
            fileList.forEach { file ->
                FileFolderItem(
                    fileName = file.name,
                    fileUri = file.uri,
                    onDelete = { fileToDelete = file },
                    onRename = { selectedFileForRename = file }
                )
            }
        }
    }

    if (selectedFileForRename != null) {
        RenameFileDialog(
            initialName = selectedFileForRename!!.name,
            onRename = { newName ->
                userDBHelper.renameFile(selectedFileForRename!!.id, newName, object: ResponseCallback {
                    override fun onSuccess(responseBody: String) {
                        userDBHelper.getAllFiles() { allFiles, error ->
                            if (error != null) {
                                println("Error fetching reviews: ${error.message}")
                            } else if (allFiles != null) {
                                println("update fetch of all files after renaming")
                                fileList = allFiles
                            }
                        }
                        selectedFileForRename = null
                    }

                    override fun onFailure(e: IOException) {
                        e.printStackTrace()
                    }
                })
            },
            onDismiss = { selectedFileForRename = null }
        )
    }

    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete \"${fileToDelete?.name}\"?")  },
            confirmButton = {
                TextButton(onClick = {
                    userDBHelper.getFilepath(fileToDelete!!.id, object: ResponseCallback{
                        override fun onSuccess(responseBody: String) {
                            val physicalFileToDelete = File(responseBody)
                            if (!physicalFileToDelete.delete()) {
                                Log.e("UserDBHelper", "Failed to delete file: $responseBody")
                                return
                            }
                            userDBHelper.deleteFile(fileToDelete!!.id, object: ResponseCallback{
                                override fun onSuccess(responseBody: String) {
                                    userDBHelper.getAllFiles() { allFiles, error ->
                                        if (error != null) {
                                            println("Error fetching reviews: ${error.message}")
                                        } else if (allFiles != null) {
                                            println("updated fetch of all files after deletion")
                                            fileList = allFiles
                                        }
                                    }
                                    fileToDelete = null
                                }

                                override fun onFailure(e: IOException) {
                                    e.printStackTrace()
                                }
                            })
                        }
                        override fun onFailure(e: IOException) {
                            e.printStackTrace()
                        }
                    })
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FileFolderItem(fileName: String,
                   fileUri: String, // Add this to pass the URI of the file
                   onDelete: () -> Unit,
                   onRename: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = { openPdfFile(context, fileUri) })
        ) {
            Text(text = fileName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRename) { // Separate button for rename
                Icon(Icons.Default.Edit, "Rename")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameFileDialog(initialName: String, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    var newName by remember { mutableStateOf(initialName) }
    val maxLength = 20 // Maximum length for file name

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename File") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { if (it.length <= maxLength) newName = it },
                label = { Text("New Name") },
                singleLine = true,
                isError = newName.length > maxLength
            )
        },
        confirmButton = {
            TextButton(onClick = { onRename(newName) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



fun openPdfFile(context: Context, internalFilePath: String) {
    try {
        val file = File(internalFilePath)
        val fileUri = FileProvider.getUriForFile(
            context,
            "ca.uwaterloo.cs346project.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read permission to the content URI
        }
        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        Log.e("openPdfFile", "Error opening file: $internalFilePath", e)
        Toast.makeText(context, "No application found to open PDF", Toast.LENGTH_SHORT).show()
    }
}


