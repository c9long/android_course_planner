package ca.uwaterloo.cs346project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MainActivity : ComponentActivity() {

    companion object {
        data class UserSettingsData(var darkMode: Boolean = false): java.io.Serializable

        var settings: UserSettingsData = UserSettingsData()
    }

    fun readSettings() {
        try {
            val file = File(applicationContext.filesDir, "settings.json")
            val stream = ObjectInputStream(file.inputStream())

            settings = stream.readObject() as UserSettingsData
            settings
        } catch (e: Exception) {
            println(e.message)
        }
    }

    fun writeSettings() {
        try {
            val file = File(applicationContext.filesDir, "settings.json")
            val stream = ObjectOutputStream(file.outputStream())

            stream.writeObject(settings)
        } catch (e: Exception) {
            println(e.message)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        readSettings()
        super.onCreate(savedInstanceState)
        setContent {
            Cs346projectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
        writeSettings()
    }
}















