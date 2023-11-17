package ca.uwaterloo.cs346project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object CourseList: ViewModel() {
    private val courseCodes: MutableList<String> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                val courseData = JSONArray(UWAPIHelper.apiGet("/v3/Courses/${UWAPIHelper.curTerm}"))
                for (i in 0 until courseData.length()) {
                    val element = courseData.getJSONObject(i)
                    courseCodes.add(
                        element.getString("subjectCode") + element.getString("catalogNumber")
                    )
                }
            }
        }
    }

    fun get(): List<String> {
        return courseCodes
    }
}

object UWAPIHelper: ViewModel() {
    var curTerm: String

    init {
        curTerm = getString(apiGet("/v3/Terms/current"), "termCode")
    }

    ///
    /// HTTP Gets
    ///

    fun apiGet(path: String): String {
        return runBlocking {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.get("https://openapi.data.uwaterloo.ca$path") {
                header("x-api-key", "95808C46CE8F460FA23F6EAC00316045")
            }
            return@runBlocking response.bodyAsText()
        }
    }

    ///
    /// JSON Gets
    ///

    private fun getString(jsonString: String, key: String): String {
        return JSONObject(jsonString).getString(key)
    }

    private fun getStrings(jsonString: String, keys: List<String>): List<String> {
        val json: JSONObject = JSONObject(jsonString)
        return keys.map { x -> json.getString(x) }
    }

    private inline fun <reified T> getValue(jsonString: String, key: String): T? {
        val ret = JSONObject(jsonString).get(key)
        return if (ret is T) {
            ret
        } else null
    }

    private inline fun <reified T> getValues(jsonString: String, keys: List<String>): List<T?> {
        val json: JSONObject = JSONObject(jsonString)
        return keys.map { key ->
            val value = json.get(key)
            if (value is T) {
                value
            } else null
        }
    }

    ///
    /// Wrapper Methods (Public)
    ///

    fun getCourseData(courseCode: String): String {
        val numStart: Int = courseCode.toCharArray().indexOfFirst { c -> c.isDigit() }
        val catalogNum: String = courseCode.drop(numStart)
        val subject: String = courseCode.removeSuffix(catalogNum)
        return apiGet("/v3/Courses/$curTerm/$subject/$catalogNum")
    }

}