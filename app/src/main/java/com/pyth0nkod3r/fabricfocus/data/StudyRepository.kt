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
            FROM questions q LEFT JOIN topics t ON t.id=q.topic_id
            GROUP BY t.name ORDER BY COUNT(q.id) DESC
        """.trimIndent(), null).use { c ->
            buildList { while (c.moveToNext()) add(TopicStat(c.getString(0), c.getInt(1))) }
        }
        Dashboard(total, images, topics)
    }

    fun question(offset: Int = 0): Question? = db().use { db ->
        db.rawQuery("""
            SELECT q.id, COALESCE(t.name,'General'), q.question_text, COALESCE(q.explanation,'')
            FROM questions q LEFT JOIN topics t ON t.id=q.topic_id
            ORDER BY q.id LIMIT 1 OFFSET ?
        """.trimIndent(), arrayOf(offset.toString())).use { c ->
            if (!c.moveToFirst()) null else Question(c.getLong(0), c.getString(1), c.getString(2), c.getString(3))
        }
    }

    fun choices(questionId: Long): List<Choice> = db().use { db ->
        db.rawQuery("SELECT option_key, option_text, is_correct FROM options WHERE question_id=? ORDER BY option_key", arrayOf(questionId.toString())).use { c ->
            buildList { while (c.moveToNext()) add(Choice(c.getString(0), c.getString(1), c.getInt(2) == 1)) }
        }
    }

    fun images(questionId: Long, role: String? = null): List<QuestionImage> = db().use { db ->
        val sql = "SELECT i.local_path, qi.role FROM question_images qi JOIN images i ON i.id=qi.image_id WHERE qi.question_id=?" + if (role != null) " AND qi.role=?" else "" + " ORDER BY qi.sort_order"
        val args = if (role == null) arrayOf(questionId.toString()) else arrayOf(questionId.toString(), role)
        db.rawQuery(sql, args).use { c -> buildList { while (c.moveToNext()) add(QuestionImage(c.getString(0), c.getString(1))) } }
    }
}
