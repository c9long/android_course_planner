package ca.uwaterloo.cs346project

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
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

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_PASSWORD TEXT)"

        val createReviewsTableStatement = "CREATE TABLE $TABLE_COURSE_REVIEWS (" +
                "$COLUMN_REVIEW_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_ID INTEGER," +
                "$COLUMN_REVIEW_DATE TEXT, " +
                "$COLUMN_CONTENT TEXT, " +
                "$COLUMN_RATING INTEGER," +
                "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"

        db.execSQL(createTableStatement)
        db.execSQL(createReviewsTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSE_REVIEWS")
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
                        val reviewDateCol = reviewCursor.getColumnIndex(COLUMN_REVIEW_DATE)
                        val contentCol = reviewCursor.getColumnIndex(COLUMN_CONTENT)
                        val ratingCol = reviewCursor.getColumnIndex(COLUMN_RATING)
                        if (reviewDateCol == -1 || contentCol == -1 || ratingCol == -1 || usernameCol == -1) {
                            break
                        }
                        val username = reviewCursor.getString(usernameCol)
                        val reviewDate = reviewCursor.getString(reviewDateCol)
                        val content = reviewCursor.getString(contentCol)
                        val rating = reviewCursor.getInt(ratingCol)

                        // Create a CourseReview object and add it to the list
                        val review = CourseReview(username, reviewDate, content, rating)
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
}


