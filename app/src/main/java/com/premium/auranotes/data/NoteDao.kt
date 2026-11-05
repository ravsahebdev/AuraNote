package com.premium.auranotes.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' AND isArchived = 0 ORDER BY timestamp DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long
    @Update
    suspend fun update(note: Note)
    @Delete
    suspend fun delete(note: Note)
}
