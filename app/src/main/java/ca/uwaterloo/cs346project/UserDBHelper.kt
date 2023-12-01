package ca.uwaterloo.cs346project

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime
import android.util.Log
import java.io.File

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

        private const val TABLE_ENROLLMENTS = "enrollments"
        private const val COLUMN_ENROLLMENT_USER = "username"
        private const val COLUMN_ENROLLMENT_START = "enrollment_start"
        private const val COLUMN_ENROLLMENT_END = "enrollment_end"
        private const val COLUMN_ENROLLMENT_CODE = "enrollment_code"
        private const val COLUMN_ENROLLMENT_DESC = "enrollment_description"

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

        val createEnrollmentTable = "CREATE TABLE $TABLE_ENROLLMENTS (" +
                "$COLUMN_ENROLLMENT_USER TEXT," +
                "$COLUMN_ENROLLMENT_CODE TEXT," +
                "$COLUMN_ENROLLMENT_DESC TEXT," +
                "$COLUMN_ENROLLMENT_START TEXT," +
                "$COLUMN_ENROLLMENT_END TEXT," +
                "FOREIGN KEY($COLUMN_ENROLLMENT_USER) REFERENCES $TABLE_USERS($COLUMN_USERNAME)," +
                "FOREIGN KEY($COLUMN_ENROLLMENT_CODE) REFERENCES $TABLE_COURSES($COLUMN_COURSE_CODE)," +
                "FOREIGN KEY($COLUMN_ENROLLMENT_DESC) REFERENCES $TABLE_COURSES($COLUMN_COURSE_DESC))"

        val createFilesTableStatement = "CREATE TABLE $TABLE_FILES (" +
                "$COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_FILE_NAME TEXT, " +
                "$COLUMN_FILE_URI TEXT)"

        db.execSQL(createFilesTableStatement)
        db.execSQL(createTableStatement)
        db.execSQL(createReviewsTableStatement)
        db.execSQL(createCourseTable)
        db.execSQL(createEnrollmentTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSE_REVIEWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FILES")
        onCreate(db)
    }

    fun addUser(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password) // Hash the password before storing
        }

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS, arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME=?", arrayOf(username),
            null, null, null
        )

        val userExists = cursor.count > 0
        cursor.close()
        db.close()
        return userExists
    }

    fun validateUser(username: String, password: String): Boolean {
        val hashedPassword = hashPassword(password) // Hash the password before checking
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS, arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME=? AND $COLUMN_PASSWORD=?", arrayOf(username, hashedPassword),
            null, null, null
        )

        val isValidUser = cursor.count > 0
        cursor.close()
        db.close()
        return isValidUser
    }

    fun addReview(courseReview: CourseReview): Boolean {
        val db = this.writableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME=?",
            arrayOf(courseReview.reviewer),
            null, null, null
        )

        val userId: Int = if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_ID)
            if (columnIndex != -1) {
                cursor.getInt(columnIndex)
            } else {
                -1 // Handle the case where COLUMN_ID is not found
            }
        } else {
            -1 // User not found, return -1 as an error indicator
        }

        cursor.close()

        if (userId != -1) {
            val values = ContentValues().apply {
                put(COLUMN_USER_ID, userId)
                put(COLUMN_COURSE_CODE, courseReview.courseCode)
                put(COLUMN_REVIEW_DATE, courseReview.date)
                put(COLUMN_CONTENT, courseReview.content)
                put(COLUMN_RATING, courseReview.stars)
            }

            val result = db.insert(TABLE_COURSE_REVIEWS, null, values)
            db.close()
            return result != -1L
        } else {
            // User not found, cannot add review
            db.close()
            return false
        }
    }

    fun getAllReviews(): List<CourseReview> {
        val reviews = mutableListOf<CourseReview>()

        val tableExistsQuery =
            "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_COURSE_REVIEWS'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(tableExistsQuery, null)

        if (cursor != null) {
            if (cursor.count > 0) {
                // Query to retrieve all reviews with their corresponding usernames
                val query = "SELECT $TABLE_COURSE_REVIEWS.$COLUMN_REVIEW_DATE, " +
                        "$TABLE_COURSE_REVIEWS.$COLUMN_COURSE_CODE, " +
                        "$TABLE_COURSE_REVIEWS.$COLUMN_CONTENT, " +
                        "$TABLE_COURSE_REVIEWS.$COLUMN_RATING, " +
                        "$TABLE_USERS.$COLUMN_USERNAME " +
                        "FROM $TABLE_COURSE_REVIEWS " +
                        "INNER JOIN $TABLE_USERS " +
                        "ON $TABLE_COURSE_REVIEWS.$COLUMN_USER_ID = $TABLE_USERS.$COLUMN_ID " +
                        "ORDER BY $TABLE_COURSE_REVIEWS.$COLUMN_RATING DESC"

                val reviewCursor = db.rawQuery(query, null)

                if (reviewCursor != null) {
                    while (reviewCursor.moveToNext()) {
                        val usernameCol = reviewCursor.getColumnIndex(COLUMN_USERNAME)
                        val codeCol = reviewCursor.getColumnIndex(COLUMN_COURSE_CODE)
                        val reviewDateCol = reviewCursor.getColumnIndex(COLUMN_REVIEW_DATE)
                        val contentCol = reviewCursor.getColumnIndex(COLUMN_CONTENT)
                        val ratingCol = reviewCursor.getColumnIndex(COLUMN_RATING)
                        if (reviewDateCol == -1 || codeCol == -1 || contentCol == -1 || ratingCol == -1 || usernameCol == -1) {
                            break
                        }
                        val username = reviewCursor.getString(usernameCol)
                        val courseCode = reviewCursor.getString(codeCol)
                        val reviewDate = reviewCursor.getString(reviewDateCol)
                        val content = reviewCursor.getString(contentCol)
                        val rating = reviewCursor.getInt(ratingCol)

                        // Create a CourseReview object and add it to the list
                        val review = CourseReview(username, courseCode, reviewDate, content, rating)
                        reviews.add(review)
                    }
                }
                reviewCursor.close()
            }
        }

        cursor.close()
        db.close()

        return reviews
    }

    fun getAllReviewsFrom(courseCode: String): List<CourseReview> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USERNAME, $COLUMN_REVIEW_DATE, $COLUMN_CONTENT, $COLUMN_RATING " +
                    "FROM $TABLE_COURSE_REVIEWS INNER JOIN $TABLE_USERS " +
                    "ON $TABLE_COURSE_REVIEWS.$COLUMN_USER_ID = $TABLE_USERS.$COLUMN_ID " +
                    "WHERE $COLUMN_COURSE_CODE = '" + courseCode + "' " +
                    "ORDER BY $TABLE_COURSE_REVIEWS.$COLUMN_RATING DESC", null)

        // Add courses to list
        val ret: MutableList<CourseReview> = mutableListOf()
        while (cursor.moveToNext()) {
            val usernameCol = cursor.getColumnIndex(COLUMN_USERNAME)
            val reviewDateCol = cursor.getColumnIndex(COLUMN_REVIEW_DATE)
            val contentCol = cursor.getColumnIndex(COLUMN_CONTENT)
            val ratingCol = cursor.getColumnIndex(COLUMN_RATING)
            if (reviewDateCol == -1 || contentCol == -1 || ratingCol == -1 || usernameCol == -1) {
                break
            }
            val username = cursor.getString(usernameCol)
            val reviewDate = cursor.getString(reviewDateCol)
            val content = cursor.getString(contentCol)
            val rating = cursor.getInt(ratingCol)

            // Create a CourseReview object and add it to the list
            val review = CourseReview(username, courseCode, reviewDate, content, rating)
            ret.add(review)
        }

        cursor.close()
        db.close()
        return ret
    }

    fun addCourse(course: Course): Boolean {
        val db = this.writableDatabase

        val value = ContentValues().apply {
            put(COLUMN_COURSE_CODE, course.code)
            put(COLUMN_COURSE_NAME, course.title)
            put(COLUMN_COURSE_DESC, course.description)
        }

        val result = db.insert(TABLE_COURSES, null, value)
        db.close()
        return result != -1L
    }

    fun getAllCourses(): List<Course> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            arrayOf(COLUMN_COURSE_CODE, COLUMN_COURSE_NAME, COLUMN_COURSE_DESC),
            "",
            arrayOf(),
            null, null, null
        )

        // Add courses to list
        val ret: MutableList<Course> = mutableListOf()
        while (cursor.moveToNext()) {
            ret.add(Course(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }

        cursor.close()
        db.close()
        return ret
    }

    fun getAllEnrollments(currentUser: String): MutableList<Event> {
        val db = this.readableDatabase
        val query = "$COLUMN_ENROLLMENT_USER='$currentUser'"
        val cursor = db.query(
            TABLE_ENROLLMENTS,
            arrayOf(COLUMN_ENROLLMENT_CODE, COLUMN_ENROLLMENT_START, COLUMN_ENROLLMENT_END, COLUMN_ENROLLMENT_DESC),
            query,
            arrayOf(),
            null, null, null, null
        )
        val ret: MutableList<Event> = mutableListOf()
        while (cursor.moveToNext()) {
            val codeIdx = cursor.getColumnIndex(COLUMN_ENROLLMENT_CODE)
            val startIdx = cursor.getColumnIndex(COLUMN_ENROLLMENT_START)
            val endIdx = cursor.getColumnIndex(COLUMN_ENROLLMENT_END)
            val descIdx = cursor.getColumnIndex(COLUMN_ENROLLMENT_DESC)

            if (codeIdx != -1 && startIdx != -1 && endIdx != -1 && descIdx != -1) {
                val code = cursor.getString(codeIdx)
                val start = LocalDateTime.parse(cursor.getString(startIdx))
                val end = LocalDateTime.parse(cursor.getString(endIdx))
                val desc = cursor.getString(descIdx)

                ret.add(Event(code, start, end, desc))
            }
        }

        cursor.close()
        db.close()
        return ret
    }

    fun addEnrollment(currentUser: String, cs: CourseSchedule, course: Course) : Boolean {
        val db = this.writableDatabase

        val value = ContentValues().apply {
            put(COLUMN_ENROLLMENT_USER, currentUser)
            put(COLUMN_ENROLLMENT_CODE, course.code)
            put(COLUMN_ENROLLMENT_DESC, course.description)
            put(COLUMN_ENROLLMENT_START, cs.meetStart)
            put(COLUMN_ENROLLMENT_END, cs.meetEnd)
        }

        val result = db.insert(TABLE_ENROLLMENTS, null, value)
        db.close()
        return result != -1L
    }

    fun addFile(fileName: String, fileUri: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, fileName)
            put(COLUMN_FILE_URI, fileUri) // Store the URI
        }

        val result = db.insert(TABLE_FILES, null, values)
        db.close()
        return result != -1L
    }

    //fun deleteFile(fileId: Int): Boolean {
    //    val db = this.writableDatabase
    //    val result = db.delete(TABLE_FILES, "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()))
    //    db.close()
    //    return result > 0
    //}

    fun deleteFile(fileId: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FILES, arrayOf(COLUMN_FILE_URI),
            "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()),
            null, null, null
        )

        var filePath = ""
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_URI))
        }
        cursor.close()

        if (filePath.isNotEmpty()) {
            val fileToDelete = File(filePath)
            if (!fileToDelete.delete()) {
                Log.e("UserDBHelper", "Failed to delete file: $filePath")
            }
        }

        // Now delete the record from the database
        db.close()
        val dbWrite = this.writableDatabase
        val result = dbWrite.delete(TABLE_FILES, "$COLUMN_FILE_ID = ?", arrayOf(fileId.toString()))
        dbWrite.close()

        return result > 0
    }


    fun getAllFiles(): List<FileRecord> {
        val fileList = mutableListOf<FileRecord>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_FILES, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val fileId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILE_ID))
                val fileName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_NAME))
                val fileUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_URI)) // Retrieve the URI
                fileList.add(FileRecord(fileId, fileName, fileUri)) // Include the URI in the FileRecord
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

    fun isFileExist(fileName: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FILES, arrayOf(COLUMN_FILE_ID),
            "$COLUMN_FILE_NAME = ?", arrayOf(fileName),
            null, null, null
        )
        val fileExists = cursor.count > 0
        cursor.close()
        db.close()
        return fileExists
    }
}