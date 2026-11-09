package com.premium.auranotes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND isArchived = 0 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getActiveNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND isArchived = 0 
        AND (:category = 'ALL' OR category = :category)
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesByCategory(category: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND isArchived = 0 
        AND folderId = :folderId 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesInFolder(folderId: Long): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND isFavorite = 1 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getFavoriteNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND reminderTime IS NOT NULL AND reminderTime > 0 
        ORDER BY reminderTime ASC
    """)
    fun getScheduledNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND isArchived = 1 
        ORDER BY updatedAt DESC
    """)
    fun getArchivedNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 1 
        ORDER BY trashedAt DESC
    """)
    fun getTrashedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    @Query("""
        SELECT * FROM notes 
        WHERE isTrashed = 0 AND isArchived = 0 
        AND (
            title LIKE '%' || :query || '%' 
            OR content LIKE '%' || :query || '%' 
            OR tags LIKE '%' || :query || '%'
        )
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    // Badge counts for navigation drawer
    @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 0 AND isArchived = 0")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 0 AND isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 0 AND reminderTime IS NOT NULL AND reminderTime > 0")
    fun getScheduledCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 0 AND isArchived = 1")
    fun getArchivedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 1")
    fun getTrashCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    // Move to trash
    @Query("UPDATE notes SET isTrashed = 1, trashedAt = :time WHERE id = :id")
    suspend fun moveToTrash(id: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isTrashed = 0, trashedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Int)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletePermanently(id: Int)

    @Query("DELETE FROM notes WHERE isTrashed = 1")
    suspend fun clearTrash()

    @Query("DELETE FROM notes WHERE isTrashed = 1 AND trashedAt IS NOT NULL AND trashedAt < :cutoff")
    suspend fun deleteOldTrash(cutoff: Long)

    // Bulk actions
    @Query("UPDATE notes SET isPinned = :isPinned WHERE id IN (:ids)")
    suspend fun bulkSetPinned(ids: List<Int>, isPinned: Boolean)

    @Query("UPDATE notes SET isArchived = :isArchived WHERE id IN (:ids)")
    suspend fun bulkSetArchived(ids: List<Int>, isArchived: Boolean)

    @Query("UPDATE notes SET isTrashed = 1, trashedAt = :time WHERE id IN (:ids)")
    suspend fun bulkMoveToTrash(ids: List<Int>, time: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET folderId = :folderId WHERE id IN (:ids)")
    suspend fun bulkSetFolder(ids: List<Int>, folderId: Long?)

    @Query("UPDATE notes SET color = :color WHERE id IN (:ids)")
    suspend fun bulkSetColor(ids: List<Int>, color: Long)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun bulkDeletePermanently(ids: List<Int>)

    @Query("UPDATE notes SET folderId = NULL WHERE folderId = :folderId")
    suspend fun removeNotesFromFolder(folderId: Long)

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSync(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)
}
