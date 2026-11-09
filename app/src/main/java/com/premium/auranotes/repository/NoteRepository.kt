package com.premium.auranotes.repository

import com.premium.auranotes.data.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {
    fun getActiveNotes(): Flow<List<Note>> = noteDao.getActiveNotes()

    fun getNotesByCategory(category: String): Flow<List<Note>> = noteDao.getNotesByCategory(category)

    fun getNotesInFolder(folderId: Long): Flow<List<Note>> = noteDao.getNotesInFolder(folderId)

    fun getFavoriteNotes(): Flow<List<Note>> = noteDao.getFavoriteNotes()

    fun getScheduledNotes(): Flow<List<Note>> = noteDao.getScheduledNotes()

    fun getArchivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes()

    fun getTrashedNotes(): Flow<List<Note>> = noteDao.getTrashedNotes()

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insert(note)

    suspend fun updateNote(note: Note) = noteDao.update(note)

    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    suspend fun moveToTrash(id: Int) = noteDao.moveToTrash(id)

    suspend fun restoreFromTrash(id: Int) = noteDao.restoreFromTrash(id)

    suspend fun deletePermanently(id: Int) = noteDao.deletePermanently(id)

    suspend fun clearTrash() = noteDao.clearTrash()

    suspend fun cleanOldTrash() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        noteDao.deleteOldTrash(thirtyDaysAgo)
    }

    // Bulk actions
    suspend fun bulkSetPinned(ids: List<Int>, isPinned: Boolean) = noteDao.bulkSetPinned(ids, isPinned)

    suspend fun bulkSetArchived(ids: List<Int>, isArchived: Boolean) = noteDao.bulkSetArchived(ids, isArchived)

    suspend fun bulkMoveToTrash(ids: List<Int>) = noteDao.bulkMoveToTrash(ids)

    suspend fun bulkSetFolder(ids: List<Int>, folderId: Long?) = noteDao.bulkSetFolder(ids, folderId)

    suspend fun bulkSetColor(ids: List<Int>, color: Long) = noteDao.bulkSetColor(ids, color)

    suspend fun bulkDeletePermanently(ids: List<Int>) = noteDao.bulkDeletePermanently(ids)

    // Drawer counts
    fun getActiveCount(): Flow<Int> = noteDao.getActiveCount()
    fun getFavoriteCount(): Flow<Int> = noteDao.getFavoriteCount()
    fun getScheduledCount(): Flow<Int> = noteDao.getScheduledCount()
    fun getArchivedCount(): Flow<Int> = noteDao.getArchivedCount()
    fun getTrashCount(): Flow<Int> = noteDao.getTrashCount()

    // Folders
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()

    fun getFoldersWithCount(): Flow<List<FolderWithCount>> = folderDao.getFoldersWithCount()

    suspend fun getFolderById(id: Long): Folder? = folderDao.getFolderById(id)

    suspend fun insertFolder(folder: Folder): Long = folderDao.insert(folder)

    suspend fun updateFolder(folder: Folder) = folderDao.update(folder)

    suspend fun deleteFolder(folder: Folder) {
        // Move notes inside to no folder before deleting
        noteDao.removeNotesFromFolder(folder.id)
        folderDao.delete(folder)
    }

    suspend fun getAllNotesSync(): List<Note> = noteDao.getAllNotesSync()
    suspend fun getAllFoldersSync(): List<Folder> = folderDao.getAllFoldersSync()
    suspend fun insertAllNotes(notes: List<Note>) = noteDao.insertAll(notes)
    suspend fun insertAllFolders(folders: List<Folder>) = folderDao.insertAll(folders)
}
