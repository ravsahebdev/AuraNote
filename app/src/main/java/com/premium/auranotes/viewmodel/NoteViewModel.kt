package com.premium.auranotes.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premium.auranotes.data.Note
import com.premium.auranotes.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllNotes() else repository.searchNotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun saveNote(id: Int, title: String, content: String, isPinned: Boolean, color: Long) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            if (id == 0) repository.insertNote(Note(title = title, content = content, isPinned = isPinned, color = color))
            else repository.getNoteById(id)?.let { repository.updateNote(it.copy(title = title, content = content, timestamp = System.currentTimeMillis(), isPinned = isPinned, color = color)) }
        }
    }
    fun deleteNote(note: Note) = viewModelScope.launch { repository.deleteNote(note) }
    fun togglePin(note: Note) = viewModelScope.launch { repository.updateNote(note.copy(isPinned = !note.isPinned, timestamp = System.currentTimeMillis())) }
}
