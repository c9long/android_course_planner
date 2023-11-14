package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme



class CourseMaterial : ComponentActivity() {
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDBHelper = UserDBHelper(this)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Assuming you have the file name and URI here
                val fileName = "Your file name" // You need to get or set the file name
                val fileUri = it.toString()

                // Store in database
                userDBHelper.addFile(fileName, fileUri)
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
    var fileList by remember { mutableStateOf(listOf<FileRecord>()) } // Change to FileRecord list
    var selectedFileForRename by remember { mutableStateOf<FileRecord?>(null) }
    var fileToDelete by remember { mutableStateOf<FileRecord?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        fileList = userDBHelper.getAllFiles()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                //val newFileName = "New File ${fileList.size + 1}"
                //if (userDBHelper.addFile(newFileName)) {
                //    fileList = userDBHelper.getAllFiles() // Update list after adding a file
                //}
                filePickerLauncher.launch("application/pdf")
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
            text = { Text("Are you sure you want to delete \"$fileToDelete\"?") },
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
fun FileFolderItem(fileName: String, onDelete: () -> Unit, onRename: () -> Unit) {
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
                .clickable { /* deal open operation*/ }
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