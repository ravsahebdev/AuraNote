package com.premium.auranotes.repository
import com.premium.auranotes.data.Note
import com.premium.auranotes.data.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class NoteRepository @Inject constructor(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    fun searchNotes(query: String): Flow<List<Note>> = dao.searchNotes(query)
    suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)
    suspend fun insertNote(note: Note): Long = dao.insert(note)
    suspend fun updateNote(note: Note) = dao.update(note)
    suspend fun deleteNote(note: Note) = dao.delete(note)
}
