package com.alex.assistant.memory

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alex.assistant.core.model.MemoryRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val category: String,
    val content: String,
    val createdAtMillis: Long
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY createdAtMillis DESC")
    suspend fun getAll(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM memories")
    suspend fun clearAll()
}

@Database(entities = [MemoryEntity::class], version = 1, exportSchema = false)
abstract class AlexDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        fun build(context: Context): AlexDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AlexDatabase::class.java,
                "alex_memory.db"
            ).build()
        }
    }
}

class MemoryEngine(private val dao: MemoryDao) {

    private val blockedSecretPatterns = listOf(
        Regex("\\b(otp|pin|cvv|password|passcode|secret key)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}\\b")
    )

    fun observeMemories(): Flow<List<MemoryRecord>> {
        return dao.observeAll().map { rows ->
            rows.map { MemoryRecord(it.id, it.category, it.content, it.createdAtMillis) }
        }
    }

    suspend fun getAllMemories(): List<MemoryRecord> {
        return dao.getAll().map { MemoryRecord(it.id, it.category, it.content, it.createdAtMillis) }
    }

    suspend fun saveMemory(category: String, content: String): Result<MemoryRecord> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Memory content cannot be empty."))
        }
        if (blockedSecretPatterns.any { it.containsMatchIn(trimmed) }) {
            return Result.failure(
                SecurityException("For your security, ALEX never stores passwords, OTPs, PINs, or payment card numbers.")
            )
        }
        val record = MemoryRecord(category = category.ifBlank { "General" }, content = trimmed)
        dao.upsert(MemoryEntity(record.id, record.category, record.content, record.createdAtMillis))
        return Result.success(record)
    }

    suspend fun deleteMemory(id: String) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
