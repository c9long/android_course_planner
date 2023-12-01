package com.example.plugins

import com.example.data.CourseReview
import com.example.data.FileDB
import com.example.data.ReviewDB
import com.example.data.UserDB
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

fun Application.configureRouting() {

    val userDB = UserDB()
    val reviewDB = ReviewDB()
    val fileDB = FileDB()

    @Serializable
    data class UserCredentials(val username: String, val password: String)

    routing {
        get("/") {
            println("ping")
            call.respondText("Hello World!")
        }
        route("/users") {
            post("/add") {
                val userCredentials = call.receive<UserCredentials>()
                when (userDB.addUser(userCredentials.username, userCredentials.password)) {
                    1 -> {
                        call.respondText("User added successfully")
                    }
                    2 -> {
                        call.respondText("Username already exists")
                    }
                    else -> {
                        call.respondText("Failed to add user", status = HttpStatusCode.InternalServerError)
                    }
                }
            }

            post("/validate") {
                val userCredentials = call.receive<UserCredentials>()
                val validated = userDB.validateUser(userCredentials.username, userCredentials.password)
                if (validated) {
                    call.respondText("User successfully validated")
                }
                else {
                    call.respondText("User not validated", status = HttpStatusCode.InternalServerError)
                }
            }
        }

        route("/reviews") {
            post("/add") {
                val courseReview = call.receive<CourseReview>()
                val reviewedAdded = reviewDB.addReview(courseReview)
                if (reviewedAdded) {
                    call.respondText("Review successfully added")
                }
                else {
                    call.respondText("Review not added", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/{courseCode}") {
                val courseCode = call.parameters["courseCode"]
                if (courseCode == null) {
                    call.respond(HttpStatusCode.BadRequest, "Course code is required")
                    return@get
                }

                val reviews = reviewDB.getAllReviewsFrom(courseCode)
                call.respond(reviews)
            }
        }

        route("/files") {
            @Serializable
            data class FileData(val fileName: String, val fileUri: String)
            post("/add") {
                val fileData = try {
                    call.receive<FileData>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                    return@post
                }

                val fileAdded = fileDB.addFile(fileData.fileName, fileData.fileUri)
                if (fileAdded) {
                    call.respond(HttpStatusCode.OK, "File added successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add file")
                }
            }

            delete("/{fileId}") {
                val fileId = call.parameters["fileId"]?.toIntOrNull()
                if (fileId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid file ID")
                    return@delete
                }

                val fileDeleted = fileDB.deleteFile(fileId)
                if (fileDeleted) {
                    call.respond(HttpStatusCode.OK, "File deleted successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete file")
                }
            }

            get("path/{fileId}") {
                val fileId = call.parameters["fileId"]?.toIntOrNull()
                if (fileId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid file ID")
                    return@get
                }

                val filePath = fileDB.getFilePath(fileId)
                if (filePath != null) {
                    call.respond(HttpStatusCode.OK, filePath)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            }

            get("/getAll") {
                val files = fileDB.getAllFiles()
                call.respond(files)
            }

            @Serializable
            data class NewFileName(val fileId: Int, val newName: String)
            post("/rename") {
                val newFileName = try {
                    call.receive<NewFileName>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                    return@post
                }

                println(newFileName.fileId)
                println(newFileName.newName)

                val fileRenamed = fileDB.renameFile(newFileName.fileId, newFileName.newName)
                if (fileRenamed) {
                    call.respond(HttpStatusCode.OK, "File renamed successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to rename file")
                }
            }

            get("/exists/{fileName}") {
                val fileName = call.parameters["fileName"]
                if (fileName.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "File name is required")
                    return@get
                }

                val fileExists = fileDB.doesFileExist(fileName)
                if (fileExists) {
                    call.respond(HttpStatusCode.OK, "File exists")
                }
                else {
                    call.respond(HttpStatusCode.InternalServerError, "File DNE")
                }
            }
        }
    }
}
