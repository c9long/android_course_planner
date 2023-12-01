package com.example.data

import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

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

private const val TABLE_FILES = "files"
private const val COLUMN_FILE_ID = "file_id"
private const val COLUMN_FILE_NAME = "file_name"
private const val COLUMN_FILE_URI = "file_uri"

@Serializable
data class FileRecord(var id: Int, var name: String, var uri: String)
@Serializable
data class CourseReview(val reviewer: String, val courseCode: String, val date: String, val content: String, val stars: Int)

object DatabaseConnection {
    init {
        // Load the JDBC driver
        Class.forName("org.sqlite.JDBC")
        createTables()
    }

    fun getConnection(): Connection = DriverManager.getConnection("jdbc:sqlite:$DATABASE_NAME")

    private fun createTables() {
        val connection: Connection? = null
        try {
            val statement = getConnection().createStatement()

//            Drop existing tables
//            val dropUsersTableSQL = "DROP TABLE IF EXISTS $TABLE_USERS;"
//            val dropCourseReviewsTableSQL = "DROP TABLE IF EXISTS $TABLE_COURSE_REVIEWS;"
//            val dropCoursesTableSQL = "DROP TABLE IF EXISTS $TABLE_COURSES;"
//            val dropFilesTableSQL = "DROP TABLE IF EXISTS $TABLE_FILES;"
//
//            statement.execute(dropUsersTableSQL)
//            statement.execute(dropCourseReviewsTableSQL)
//            statement.execute(dropCoursesTableSQL)
//            statement.execute(dropFilesTableSQL)

            val createUsersTableSQL = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USERNAME TEXT NOT NULL,
                    $COLUMN_PASSWORD TEXT NOT NULL
                );
            """.trimIndent()

            val createReviewsTableStatement = """
                CREATE TABLE IF NOT EXISTS $TABLE_COURSE_REVIEWS (
                    $COLUMN_REVIEW_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_ID INTEGER,
                    $COLUMN_COURSE_CODE TEXT,
                    $COLUMN_REVIEW_DATE TEXT,
                    $COLUMN_CONTENT TEXT,
                    $COLUMN_RATING INTEGER,
                    FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID),
                );
            """.trimIndent()

            val createFilesTableStatement = """
                CREATE TABLE IF NOT EXISTS $TABLE_FILES (
                    $COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_FILE_NAME TEXT,
                    $COLUMN_FILE_URI TEXT
                );
            """.trimIndent()

            statement.execute(createUsersTableSQL)
            statement.execute(createReviewsTableStatement)
            statement.execute(createFilesTableStatement)

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}

class UserDB {
    private fun checkUser(username: String): Boolean {
        val sql = "SELECT COUNT($COLUMN_ID) FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, username)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0
            }
            return false
        }
    }
    fun addUser(username: String, password: String): Int {
        if (checkUser(username)) {
            return 2
        }
        val hashedPassword = hashPassword(password)
        val sql = "INSERT INTO $TABLE_USERS ($COLUMN_USERNAME, $COLUMN_PASSWORD) VALUES (?, ?)"
        DatabaseConnection.getConnection().use { connection ->
            val statement: PreparedStatement = connection.prepareStatement(sql).apply {
                setString(1, username)
                setString(2, hashedPassword)
            }
            return statement.executeUpdate()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun validateUser(username: String, password: String): Boolean {
        val hashedPassword = hashPassword(password)
        val sql = "SELECT COUNT($COLUMN_ID) FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, username)
                setString(2, hashedPassword)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0
            }
            return false
        }
    }
}

class ReviewDB {
    fun addReview(courseReview: CourseReview): Boolean {
        val userId = getUserId(courseReview.reviewer)
        if (userId == -1) {
            return false
        }
        val sql = """
            INSERT INTO $TABLE_COURSE_REVIEWS ($COLUMN_USER_ID, $COLUMN_COURSE_CODE, $COLUMN_REVIEW_DATE, $COLUMN_CONTENT, $COLUMN_RATING) 
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setInt(1, userId)
                setString(2, courseReview.courseCode)
                setString(3, courseReview.date)
                setString(4, courseReview.content)
                setInt(5, courseReview.stars)
            }
            val rowsInserted = statement.executeUpdate()
            return rowsInserted > 0
        }
    }

    private fun getUserId(username: String): Int {
        val sql = "SELECT $COLUMN_ID FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, username)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1)
            }
            return -1
        }
    }

    fun getAllReviewsFrom(courseCode: String): List<CourseReview> {
        val sql = """
            SELECT u.$COLUMN_USERNAME, r.$COLUMN_REVIEW_DATE, r.$COLUMN_CONTENT, r.$COLUMN_RATING
            FROM $TABLE_COURSE_REVIEWS r
            INNER JOIN $TABLE_USERS u ON r.$COLUMN_USER_ID = u.$COLUMN_ID
            WHERE r.$COLUMN_COURSE_CODE = ?
            ORDER BY r.$COLUMN_RATING DESC
        """.trimIndent()

        val reviews: MutableList<CourseReview> = mutableListOf()

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, courseCode)
            }
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val username = resultSet.getString(1)
                val reviewDate = resultSet.getString(2)
                val content = resultSet.getString(3)
                val rating = resultSet.getInt(4)

                val review = CourseReview(username, courseCode, reviewDate, content, rating)
                reviews.add(review)
            }
        }

        return reviews
    }
}

class FileDB {
    fun addFile(fileName: String, fileUri: String): Boolean {
        val sql = "INSERT INTO $TABLE_FILES ($COLUMN_FILE_NAME, $COLUMN_FILE_URI) VALUES (?, ?)"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, fileName)
                setString(2, fileUri)
            }
            val rowsInserted = statement.executeUpdate()
            return rowsInserted > 0
        }
    }

    fun getFilePath(fileId: Int): String? {
        val sql = "SELECT $COLUMN_FILE_URI FROM $TABLE_FILES WHERE $COLUMN_FILE_ID = ?"
        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setInt(1, fileId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getString(1)
            }
        }
        return null
    }

    fun deleteFile(fileId: Int): Boolean {
        val sql = "DELETE FROM $TABLE_FILES WHERE $COLUMN_FILE_ID = ?"
        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setInt(1, fileId)
            }
            val rowsDeleted = statement.executeUpdate()
            return rowsDeleted > 0
        }
    }

    fun getAllFiles(): List<FileRecord> {
        val fileList = mutableListOf<FileRecord>()
        val sql = "SELECT * FROM $TABLE_FILES"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(sql)
            while (resultSet.next()) {
                val fileId = resultSet.getInt(COLUMN_FILE_ID)
                val fileName = resultSet.getString(COLUMN_FILE_NAME)
                val fileUri = resultSet.getString(COLUMN_FILE_URI)
                fileList.add(FileRecord(fileId, fileName, fileUri))
            }
        }
        return fileList
    }

    fun renameFile(fileId: Int, newName: String): Boolean {
        val sql = "UPDATE $TABLE_FILES SET $COLUMN_FILE_NAME = ? WHERE $COLUMN_FILE_ID = ?"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, newName)
                setInt(2, fileId)
            }
            val rowsUpdated = statement.executeUpdate()
            return rowsUpdated > 0
        }
    }

    fun doesFileExist(fileName: String): Boolean {
        val sql = "SELECT COUNT(*) FROM $TABLE_FILES WHERE $COLUMN_FILE_NAME = ?"

        DatabaseConnection.getConnection().use { connection ->
            val statement = connection.prepareStatement(sql).apply {
                setString(1, fileName)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0
            }
            return false
        }
    }
}