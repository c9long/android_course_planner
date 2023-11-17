package ca.uwaterloo.cs346project

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking


object UWAPIHelper {
    suspend fun get(path: String): String {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.get("https://openapi.data.uwaterloo.ca/v3/Subjects") {
            header("x-api-key", "95808C46CE8F460FA23F6EAC00316045")
        }
        return response.bodyAsText()
    }

    fun getSubjects(): String {
       val ret: String = runBlocking {
           return@runBlocking get("")
       }
        return ret
    }
}