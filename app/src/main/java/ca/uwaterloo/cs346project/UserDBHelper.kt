package ca.uwaterloo.cs346project

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException


interface ResponseCallback {
    fun onSuccess(responseBody: String)
    fun onFailure(e: IOException)
}

@Serializable
data class FileRecord(var id: Int, var name: String, var uri: String)
@Serializable
data class FolderRecord(var id: Int, var name: String)
@Serializable
data class EnrollmentRequest(val currentUser: String, val cs: CourseSchedule, val course: Course)


class UserDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val URL = "127.0.0.1"
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "UserDatabase.db"

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        private const val TABLE_COURSE_REVIEWS = "course_reviews"
        private const val COLUMN_REVIEW_ID = "review_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_COURSE_CODE = "course_code"
        private const val COLUMN_REVIEW_DATE = "date"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_RATING = "rating"

        private const val TABLE_ENROLLMENTS = "enrollments"
        private const val COLUMN_ENROLLMENT_USER = "username"
        private const val COLUMN_ENROLLMENT_DAY = "enrollment_day"
        private const val COLUMN_ENROLLMENT_START = "enrollment_start"
        private const val COLUMN_ENROLLMENT_END = "enrollment_end"
        private const val COLUMN_ENROLLMENT_CODE = "enrollment_code"
        private const val COLUMN_ENROLLMENT_DESC = "enrollment_description"

        private const val TABLE_FILES = "files"
        private const val COLUMN_REF_COUNT = "ref_count"
        private const val COLUMN_FILE_ID = "file_id"
        private const val COLUMN_FILE_NAME = "file_name"
        private const val COLUMN_FILE_URI = "file_uri"

        private const val TABLE_FOLDERS = "folders"
        private const val COLUMN_FOLDER_ID = "folder_id"
        private const val COLUMN_FOLDER_NAME = "folder_name"

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
                "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"

        val createEnrollmentTable = "CREATE TABLE $TABLE_ENROLLMENTS (" +
                "$COLUMN_ENROLLMENT_USER TEXT," +
                "$COLUMN_ENROLLMENT_CODE TEXT," +
                "$COLUMN_ENROLLMENT_DESC TEXT," +
                "$COLUMN_ENROLLMENT_DAY TEXT," +
                "$COLUMN_ENROLLMENT_START TEXT," +
                "$COLUMN_ENROLLMENT_END TEXT," +
                "FOREIGN KEY($COLUMN_ENROLLMENT_USER) REFERENCES $TABLE_USERS($COLUMN_USERNAME))"

        val createFilesTableStatement = "CREATE TABLE $TABLE_FILES (" +
                "$COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_FILE_NAME TEXT, " +
                "$COLUMN_FILE_URI TEXT, " +
                "$COLUMN_FOLDER_ID INTEGER, " +
                "$COLUMN_REF_COUNT INTEGER DEFAULT 1, " +
                "FOREIGN KEY($COLUMN_FOLDER_ID) REFERENCES $TABLE_FOLDERS($COLUMN_FOLDER_ID))"


        val createFoldersTableStatement = "CREATE TABLE $TABLE_FOLDERS (" +
                "$COLUMN_FOLDER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_FOLDER_NAME TEXT)"

        db.execSQL(createFoldersTableStatement)
        db.execSQL(createFilesTableStatement)
        db.execSQL(createTableStatement)
        db.execSQL(createReviewsTableStatement)
        db.execSQL(createEnrollmentTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSE_REVIEWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ENROLLMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FILES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOLDERS")
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
            .url("http://$URL:8080/users/add")
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
            .url("http://$URL:8080/users/validate")
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
            .url("http://$URL:8080/reviews/add")
            .post(body)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun getAllReviewsFrom(courseCode: String, callback: (List<CourseReview>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$URL:8080/reviews/$courseCode")
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

    fun getAllEnrollments(user: String, callback: (List<Event>?, IOException?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$URL:8080/enrollments/$user")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val enrollments = Json.decodeFromString<List<Event>>(responseBody)
                                callback(enrollments, null)
                            } catch (e: Exception) {
                                callback(null, IOException("Error parsing JSON: ${e.message}"))
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

    fun addEnrollment(currentUser: String, cs: CourseSchedule, course: Course, callback: ResponseCallback) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody =
            Json.encodeToString(EnrollmentRequest(currentUser, cs, course)).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$URL:8080/enrollments/addEnrollment")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(createOkHttpCallback(callback))
    }

    fun addFileToFolder(folderId: Int, fileName: String, fileUri: String): Boolean {
        val db = this.writableDatabase
        var count = 1
        val cursor1 = db.query(
            TABLE_FILES, arrayOf(COLUMN_FILE_ID, COLUMN_REF_COUNT),
            "$COLUMN_FILE_URI = ?", arrayOf(fileUri),
            null, null, null
        )

        while (cursor1.moveToNext()) {
            count = cursor1.getInt(cursor1.getColumnIndexOrThrow(COLUMN_REF_COUNT))
        }
        cursor1.close()


        // Step 1: Insert the new file record
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, fileName)
            put(COLUMN_FILE_URI, fileUri)
            put(COLUMN_FOLDER_ID, folderId)
            put(COLUMN_REF_COUNT, count)  // Initial ref_count is set to 1
        }

        val result = db.insert(TABLE_FILES, null, values)
        val insertSuccessful = result != -1L

        // Step 2: If insertion is successful, update the ref_count of all records with the same fileUri
        if (insertSuccessful) {
            val cursor = db.query(
                TABLE_FILES, arrayOf(COLUMN_FILE_ID, COLUMN_REF_COUNT),
                "$COLUMN_FILE_URI = ?", arrayOf(fileUri),
                null, null, null
            )

            while (cursor.moveToNext()) {
                val fileId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILE_ID))
                val refCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_COUNT)) + 1
                val updateValues = ContentValues().apply {
                    put(COLUMN_REF_COUNT, refCount)
                }

                db.update(TABLE_FILES, updateValues, "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()))
            }
            cursor.close()
        }

        db.close()
        return insertSuccessful
    }



    fun deleteFile(fileId: Int): Boolean {
        val db = this.writableDatabase
        var filePath = ""
        var deleteFile = false

        // Get the file URI of the file being deleted
        val cursor = db.query(
            TABLE_FILES, arrayOf(COLUMN_FILE_URI),
            "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_URI))
        }
        cursor.close()

        // Delete the current file record
        val deleteResult = db.delete(TABLE_FILES, "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()))

        // Check other files with the same URI and decrement their ref_count
        if (deleteResult > 0 && filePath.isNotEmpty()) {
            val cursorUpdate = db.query(
                TABLE_FILES, arrayOf(COLUMN_FILE_ID, COLUMN_REF_COUNT),
                "$COLUMN_FILE_URI = ?", arrayOf(filePath),
                null, null, null
            )

            while (cursorUpdate.moveToNext()) {
                val otherFileId = cursorUpdate.getInt(cursorUpdate.getColumnIndexOrThrow(COLUMN_FILE_ID))
                var refCount = cursorUpdate.getInt(cursorUpdate.getColumnIndexOrThrow(COLUMN_REF_COUNT))
                refCount--

                if (refCount <= 0) {
                    deleteFile = true  // Mark for deletion if any ref_count reaches 0
                }

                val updateValues = ContentValues().apply {
                    put(COLUMN_REF_COUNT, refCount)
                }

                db.update(TABLE_FILES, updateValues, "$COLUMN_FILE_ID = ?", arrayOf(otherFileId.toString()))
            }
            cursorUpdate.close()
        }

        // If any ref_count reached 0, delete the file from the file system
        if (deleteFile) {
            val fileToDelete = File(filePath)
            if (!fileToDelete.delete()) {
                Log.e("UserDBHelper", "Failed to delete file: $filePath")
            }
        }

        db.close()
        return deleteResult > 0
    }



    fun getFilesInFolder(folderId: Int): List<FileRecord> {
        val fileList = mutableListOf<FileRecord>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FILES,
            null,
            "$COLUMN_FOLDER_ID = ?",
            arrayOf(folderId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val fileId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILE_ID))
                val fileName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_NAME))
                val fileUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_URI))
                fileList.add(FileRecord(fileId, fileName, fileUri))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return fileList
    }

    fun renameFile(fileId: Int, newName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, newName)
        }
        val result = db.update(TABLE_FILES, values, "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()))
        db.close()
        return result > 0
    }


    fun isFileExistInFolder(folderId: Int, fileName: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FILES, arrayOf(COLUMN_FILE_ID),
            "$COLUMN_FILE_NAME = ? AND $COLUMN_FOLDER_ID = ?", arrayOf(fileName, folderId.toString()),
            null, null, null
        )
        val fileExists = cursor.count > 0
        cursor.close()
        db.close()
        return fileExists
    }


    fun addFolder(folderName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOLDER_NAME, folderName)
        }

        val result = db.insert(TABLE_FOLDERS, null, values)
        db.close()
        return result != -1L
    }

    fun getAllFolders(): List<FolderRecord> {
        val folders = mutableListOf<FolderRecord>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_FOLDERS, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val folderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID))
                val folderName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME))
                folders.add(FolderRecord(folderId, folderName))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return folders
    }

    fun deleteFolder(folderId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FOLDERS, "$COLUMN_FOLDER_ID = ?", arrayOf(folderId.toString()))
        db.close()
        return result > 0
    }

    fun renameFolder(folderId: Int, newName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOLDER_NAME, newName)
        }
        val result = db.update(TABLE_FOLDERS, values, "$COLUMN_FOLDER_ID = ?", arrayOf(folderId.toString()))
        db.close()
        return result > 0
    }

    fun folderNameExists(name: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FOLDERS,
            arrayOf(COLUMN_FOLDER_ID),
            "$COLUMN_FOLDER_NAME = ?",
            arrayOf(name),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }
}