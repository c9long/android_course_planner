package ca.uwaterloo.cs346project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme

class CourseMaterialFolder : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDBHelper = UserDBHelper(this)
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CourseMaterialFolderPage(userDBHelper)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMaterialFolderPage(userDBHelper: UserDBHelper) {
    var folderList by remember { mutableStateOf(listOf<FolderRecord>()) }
    var selectedFolderForRename by remember { mutableStateOf<FolderRecord?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderRecord?>(null) }
    var folderToEnter by remember { mutableStateOf<FolderRecord?>(null) }
    val scrollState = rememberScrollState()
    var showAddFolderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        folderList = userDBHelper.getAllFolders()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddFolderDialog = true
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
            Text(
                text = "Course Material Folder",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (folderList.isEmpty()) {
                Text(
                    text = "No course material folder has been added.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                folderList.forEach { folder ->
                    FolderItem(
                        folderName = folder.name,
                        onFolderClick = {folderToEnter = folder},
                        onDelete = { folderToDelete = folder },
                        onRename = { selectedFolderForRename = folder }
                    )
                }
            }
        }
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            onAddFolder = { folderName ->
                userDBHelper.addFolder(folderName)
                folderList = userDBHelper.getAllFolders()
                showAddFolderDialog = false
            },
            onDismiss = {
                showAddFolderDialog = false
            },
            folderNameExists = { folderName ->
                userDBHelper.folderNameExists(folderName)
            }
        )
    }

    if (folderToEnter != null) {
        val context = LocalContext.current
        val CMIntent = Intent(context, CourseMaterial::class.java)
        CMIntent.putExtra("FOLDER_ID", folderToEnter!!.id)
        CMIntent.putExtra("FOLDER_NAME", folderToEnter!!.name)
        context.startActivity(CMIntent)
        folderToEnter = null
    }

    if (selectedFolderForRename != null) {
        RenameFolderDialog(
            initialName = selectedFolderForRename!!.name,
            onRename = { newName ->
                if (userDBHelper.renameFolder(selectedFolderForRename!!.id, newName)) {
                    folderList = userDBHelper.getAllFolders()
                    selectedFolderForRename = null
                }
            },
            onDismiss = { selectedFolderForRename = null }
        )
    }

    if (folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete \"${folderToDelete?.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    if (userDBHelper.deleteFolder(folderToDelete!!.id)) {
                        folderList = userDBHelper.getAllFolders()
                        var fileList = userDBHelper.getFilesInFolder(folderToDelete!!.id)
                        for (file in fileList) {
                            userDBHelper.deleteFile(file.id)
                        }
                        folderToDelete = null
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun FolderItem(folderName: String, onFolderClick: () -> Unit, onDelete: () -> Unit, onRename: () -> Unit) {
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
                .clickable {onFolderClick()}
        ) {
            Text(text = folderName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRename) {
                Icon(Icons.Default.Edit, contentDescription = "Rename")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderDialog(
    onAddFolder: (String) -> Unit,
    onDismiss: () -> Unit,
    folderNameExists: (String) -> Boolean
) {
    var newFolderName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String>("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Folder") },
        text = {
            TextField(
                value = newFolderName,
                onValueChange = {
                    newFolderName = it
                    errorMessage = "" },
                label = { Text("Folder Name    $errorMessage") },
                isError = errorMessage != ""
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderNameExists(newFolderName)) {
                        errorMessage = "name already exists."
                    } else {
                        onAddFolder(newFolderName)
                        newFolderName = ""
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameFolderDialog(initialName: String, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    var newName by remember { mutableStateOf(initialName) }
    val maxLength = 20

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder") },
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



