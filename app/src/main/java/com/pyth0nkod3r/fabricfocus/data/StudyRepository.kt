package com.pyth0nkod3r.fabricfocus.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

data class Question(val id: Long, val topic: String, val text: String, val explanation: String)
data class Choice(val key: String, val text: String, val isCorrect: Boolean)
data class QuestionImage(val path: String, val role: String)
data class Dashboard(val total: Int, val imageCount: Int, val topics: List<TopicStat>)
data class TopicStat(val name: String, val questions: Int)

class StudyRepository(private val context: Context) {
    private val databaseFile = File(context.filesDir, "dp600.sqlite")

    private fun db(): SQLiteDatabase {
        if (!databaseFile.exists()) {
            context.assets.open("dp600.sqlite").use { input -> databaseFile.outputStream().use(input::copyTo) }
        }
        return SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun dashboard(): Dashboard = db().use { db ->
        val total = db.rawQuery("SELECT COUNT(*) FROM questions", null).use { it.moveToFirst(); it.getInt(0) }
        val images = db.rawQuery("SELECT COUNT(*) FROM images", null).use { it.moveToFirst(); it.getInt(0) }
        val topics = db.rawQuery("""
            SELECT COALESCE(t.name, 'General'), COUNT(q.id)
            FROM questions q LEFT JOIN topics t ON t.id = q.topic_id
            GROUP BY t.name ORDER BY COUNT(q.id) DESC
        """.trimIndent(), null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(TopicStat(cursor.getString(0), cursor.getInt(1))) }
        }
        Dashboard(total, images, topics)
    }

    fun question(offset: Int = 0, topic: String? = null): Question? = db().use { db ->
        val sql = """
            SELECT q.id, COALESCE(t.name, 'General'), q.question_text, COALESCE(q.explanation, '')
            FROM questions q LEFT JOIN topics t ON t.id = q.topic_id
            WHERE 1 = 1
            ${if (topic != null) "AND COALESCE(t.name, 'General') = ?" else ""}
            ORDER BY q.id LIMIT 1 OFFSET ?
        """.trimIndent()
        val args = if (topic != null) arrayOf(topic, offset.toString()) else arrayOf(offset.toString())
        db.rawQuery(sql, args).use { cursor ->
            if (!cursor.moveToFirst()) null else Question(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
        }
    }

    fun choices(questionId: Long): List<Choice> = db().use { db ->
        db.rawQuery("SELECT option_key, option_text, is_correct FROM options WHERE question_id = ? ORDER BY option_key", arrayOf(questionId.toString())).use { cursor ->
            buildList { while (cursor.moveToNext()) add(Choice(cursor.getString(0), cursor.getString(1), cursor.getInt(2) == 1)) }
        }
    }

    fun images(questionId: Long, role: String? = null): List<QuestionImage> = db().use { db ->
        val sql = "SELECT i.local_path, qi.role FROM question_images qi JOIN images i ON i.id = qi.image_id WHERE qi.question_id = ?" +
            (if (role != null) " AND qi.role = ?" else "") + " ORDER BY qi.sort_order"
        val args = if (role == null) arrayOf(questionId.toString()) else arrayOf(questionId.toString(), role)
        db.rawQuery(sql, args).use { cursor ->
            buildList { while (cursor.moveToNext()) add(QuestionImage(cursor.getString(0), cursor.getString(1))) }
        }
    }
}
