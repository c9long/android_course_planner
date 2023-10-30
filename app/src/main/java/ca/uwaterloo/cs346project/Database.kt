package ca.uwaterloo.cs346project

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

object Database {
    fun connect(dbFileName: String): Connection? {
        var ret: Connection? = null
        try {
            ret = DriverManager.getConnection("jdbc::sqLite:$dbFileName")
        } catch (e: SQLException) {
            println(e.message)
        }
        return ret
    }

    fun query(conn: Connection?, query: String): ResultSet? {
        var ret: ResultSet? = null
        try {
            if (conn != null) {
                ret = conn.createStatement().executeQuery(query)
            }
        } catch (e: SQLException) {
            println(e.message)
        }
        return ret
    }

    fun close(conn: Connection?) {
        try {
            conn?.close()
        } catch (e: SQLException) {
            println(e.message)
        }
    }

}