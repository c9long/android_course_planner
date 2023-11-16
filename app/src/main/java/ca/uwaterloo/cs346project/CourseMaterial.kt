package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.io.File


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


        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                //val fileName = getFileNameFromUri(it)
                //userDBHelper.addFile(fileName, it.toString())
                //val intent = intent
                //finish()
                //startActivity(intent)
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, flags)

                val fileName = getFileNameFromUri(it)
                userDBHelper.addFile(fileName, it.toString())
                val intent = intent
                finish()
                startActivity(intent)
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
        fileList = userDBHelper.getAllFiles()
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
                    //onOpen = { openPdfFile(context, file.uri) },
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
                if (userDBHelper.renameFile(selectedFileForRename!!.id, newName)) {
                    fileList = userDBHelper.getAllFiles() // Refresh the list from the database
                    selectedFileForRename = null
                }
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
                    if (userDBHelper.deleteFile(fileToDelete!!.id)) {
                        fileList = userDBHelper.getAllFiles() // Refresh the list after deletion
                        fileToDelete = null
                    }
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
    //onOpen: (string) -> Unit,
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
            //.clickable { /* deal open operation*/ }
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


//fun isFileAccessible(context: Context, fileUri: Uri): Boolean {
//    return try {
//        val inputStream = context.contentResolver.openInputStream(fileUri)
//        inputStream?.close()
//        true
//    } catch (e: Exception) {
//        Log.e("FileAccess", "Unable to access file at URI: $fileUri", e)
//        false
//    }
//}

//fun isFileAccessible(context: Context, fileUriString: String): Boolean {
//    val fileUri = Uri.parse(fileUriString)
//    return try {
//        //获取外部文件访问路径
//        val externalPubPath = Environment.getExternalStorageDirectory()
//        val picPath = File(externalPubPath, fileUri.path!!.split(":")[1])
//        if (picPath.exists()) {
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                setDataAndType(Uri.fromFile(picPath), "application/pdf")
//                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
//            }
//            ContextCompat.startActivity(context, intent, null)
//        }
//        picPath.exists()
//    } catch (e: Exception) {
//        Log.e("FileAccess", "Unable to access file at URI: $fileUri", e)
//        false
//    }
//}



fun openPdfFile(context: Context, fileUriString: String) {
    val fileUri = Uri.parse(fileUriString)




    //if (!isFileAccessible(context, fileUri)) {
    //    Toast.makeText(context, "File is not accessible", Toast.LENGTH_SHORT).show()
    //    return
    //}

    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            //flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            //addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
        //ContextCompat.startActivity(context, intent, null)

    } catch (e: Exception) {
        Log.e("openPdfFile", "Error opening file: $fileUriString", e)
        Toast.makeText(context, "No application found to open PDF", Toast.LENGTH_SHORT).show()
    }
}

