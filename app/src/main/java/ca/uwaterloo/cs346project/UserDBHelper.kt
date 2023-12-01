package ca.uwaterloo.cs346project

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

interface ResponseCallback {
    fun onSuccess(responseBody: String)
    fun onFailure(e: IOException)
}

@Serializable
data class FileRecord(var id: Int, var name: String, var uri: String)

class UserDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "UserDatabase.db"

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        private const val TABLE_COURSE_REVIEWS = "course_reviews"
        private const val COLUMN_REVIEW_ID = "review_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_REVIEW_DATE = "date"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_RATING = "rating"

        private const val TABLE_COURSES = "uw_courses"
        private const val COLUMN_COURSE_CODE = "course_code"
        private const val COLUMN_COURSE_NAME = "course_name"
        private const val COLUMN_COURSE_DESC = "course_description"

        private const val TABLE_FILES = "files"
        private const val COLUMN_FILE_ID = "file_id"
        private const val COLUMN_FILE_NAME = "file_name"

        private const val COLUMN_FILE_URI = "file_uri"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_PASSWORD TEXT)"

        val createReviewsTableStatement = "CREATE TABLE $TABLE_COURSE_REVIEWS (" +
                "$COLUMN_REVIEW_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_ID INTEGER," +
                "$COLUMN_COURSE_CODE TEXT, " +
                "$COLUMN_REVIEW_DATE TEXT, " +
                "$COLUMN_CONTENT TEXT, " +
                "$COLUMN_RATING INTEGER," +
                "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)," +
                "FOREIGN KEY($COLUMN_COURSE_CODE) REFERENCES $TABLE_COURSES($COLUMN_COURSE_CODE))"

        val createCourseTable = "CREATE TABLE $TABLE_COURSES (" +
                "$COLUMN_COURSE_CODE TEXT PRIMARY KEY," +
                "$COLUMN_COURSE_NAME TEXT," +
                "$COLUMN_COURSE_DESC TEXT)"

        val createFilesTableStatement = "CREATE TABLE $TABLE_FILES (" +
                "$COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_FILE_NAME TEXT, " +
                "$COLUMN_FILE_URI TEXT)"

        db.execSQL(createFilesTableStatement)
        db.execSQL(createTableStatement)
        db.execSQL(createReviewsTableStatement)
        db.execSQL(createCourseTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSE_REVIEWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FILES")
        onCreate(db)
    }

    private fun createOkHttpCallback(callback: ResponseCallback): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onFailure(IOException("Unexpected code $response"))
                    } else {
                        response.body?.string()?.let {
                            callback.onSuccess(it)
                        } ?: callback.onFailure(IOException("Response body is null"))
                    }
                }
            }
        }
    }

    fun addUser(username: String, password: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"username\":\"$username\", \"password\":\"$password\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/users/add")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun validateUser(username: String, password: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"username\":\"$username\", \"password\":\"$password\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/users/validate")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun addReview(courseReview: CourseReview, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"reviewer\":\"${courseReview.reviewer}\", \"courseCode\":\"${courseReview.courseCode}\"," +
                "\"date\":\"${courseReview.date}\", \"stars\":\"${courseReview.stars}\"," +
                "\"content\":\"${courseReview.content}\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/reviews/add")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun getAllReviewsFrom(courseCode: String, callback: (List<CourseReview>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/reviews/$courseCode")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val reviews = Json.decodeFromString<List<CourseReview>>(responseBody)
                                callback(reviews, null)
                            } catch (e: SerializationException) {
                                callback(null, IOException("Serialization error: ${e.message}"))
                            } catch (e: Exception) {
                                callback(null, IOException("General error: ${e.message}"))
                            }
                        } else {
                            callback(null, IOException("Response body is null"))
                        }
                    } else {
                        callback(null, IOException("Unexpected code $response"))
                    }
                }
            }
        })
    }

    fun addFile(fileName: String, fileUri: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"fileName\":\"$fileName\", \"fileUri\":\"$fileUri\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/files/add")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun deleteFile(fileId: Int, callback: ResponseCallback) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/files/$fileId") // Replace with your server URL
            .delete()
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun getFilepath(fileId: Int, callback: ResponseCallback) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/files/path/$fileId") // Replace with your server URL
            .get()
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun getAllFiles(callback: (List<FileRecord>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/files/getAll") // Replace with your server URL
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val files = Json.decodeFromString<List<FileRecord>>(responseBody)
                                callback(files, null)
                            } catch (e: Exception) {
                                callback(null, IOException("Error parsing JSON: ${e.message}"))
                            }
                        } else {
                            callback(null, IOException("Response body is null"))
                        }
                    } else {
                        callback(null, IOException("Failed to get files"))
                    }
                }
            }
        })
    }

    fun renameFile(fileId: Int, newName: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = "{\"fileId\":\"$fileId\", \"newName\":\"$newName\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/files/rename") // Replace with your server URL
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun doesFileExist(fileName: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/files/exists/$fileName")
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }
}