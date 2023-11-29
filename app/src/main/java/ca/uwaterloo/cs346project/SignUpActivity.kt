package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import java.security.MessageDigest

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignupPage()
                }
            }
        }
    }
}


fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupPage() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    val dbHelper = UserDBHelper(LocalContext.current)

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Sign Up",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (username.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please fill in all fields."
                    } else if (dbHelper.checkUser(username)) {
                        errorMessage = "Username already exists."
                    } else {
                        val hashedPassword = hashPassword(password)
                        if (dbHelper.addUser(username, hashedPassword)) {
                            success = true
                        } else {
                            errorMessage = "Failed to create account. Please try again."
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Sign Up", fontSize = 18.sp)
            }

            Button(
                onClick = {
                    success = true
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Back", fontSize = 18.sp)
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }

    if (success) {
        LocalContext.current.startActivity(Intent(LocalContext.current, HomePageActivity::class.java))
        success = false
    }
}
