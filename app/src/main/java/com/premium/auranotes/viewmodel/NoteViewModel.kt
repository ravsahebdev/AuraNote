package com.premium.auranotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premium.auranotes.data.*
import com.premium.auranotes.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val folders: List<FolderWithCount> = emptyList(),
    val selectedCategory: NoteCategory = NoteCategory.ALL,
    val selectedFolderId: Long? = null,
    val currentDrawerScreen: DrawerScreen = DrawerScreen.ALL_NOTES,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val isMultiSelectMode: Boolean = false,
    val selectedNoteIds: Set<Int> = emptySet(),
    val isPinnedSectionExpanded: Boolean = true,
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOrder: SortOrder = SortOrder.DATE_MODIFIED,
    val activeCount: Int = 0,
    val favoriteCount: Int = 0,
    val scheduledCount: Int = 0,
    val archivedCount: Int = 0,
    val trashCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(NoteCategory.ALL)
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    private val _currentDrawerScreen = MutableStateFlow(DrawerScreen.ALL_NOTES)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchExpanded = MutableStateFlow(false)
    private val _isMultiSelectMode = MutableStateFlow(false)
    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _isPinnedSectionExpanded = MutableStateFlow(true)

    val userPreferences = preferencesManager.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val foldersWithCount: StateFlow<List<FolderWithCount>> = repository.getFoldersWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount = repository.getActiveCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val favoriteCount = repository.getFavoriteCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val scheduledCount = repository.getScheduledCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val archivedCount = repository.getArchivedCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val trashCount = repository.getTrashCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Clean trash older than 30 days automatically on app start
        viewModelScope.launch {
            repository.cleanOldTrash()
        }
    }

    private val rawNotesFlow: Flow<List<Note>> = combine(
        _currentDrawerScreen,
        _selectedCategory,
        _selectedFolderId,
        _searchQuery
    ) { screen, category, folderId, query ->
        when {
            query.isNotBlank() -> repository.searchNotes(query.trim())
            screen == DrawerScreen.FAVORITES -> repository.getFavoriteNotes()
            screen == DrawerScreen.REMINDERS -> repository.getScheduledNotes()
            screen == DrawerScreen.ARCHIVE -> repository.getArchivedNotes()
            screen == DrawerScreen.TRASH -> repository.getTrashedNotes()
            screen == DrawerScreen.FOLDER && folderId != null -> repository.getNotesInFolder(folderId)
            category != NoteCategory.ALL -> repository.getNotesByCategory(category.name)
            else -> repository.getActiveNotes()
        }
    }.flatMapLatest { it }

    val uiState: StateFlow<HomeUiState> = combine(
        rawNotesFlow,
        foldersWithCount,
        _selectedCategory,
        _selectedFolderId,
        _currentDrawerScreen,
        _searchQuery,
        _isSearchExpanded,
        _isMultiSelectMode,
        _selectedNoteIds,
        _isPinnedSectionExpanded,
        userPreferences,
        activeCount,
        favoriteCount,
        scheduledCount,
        archivedCount,
        trashCount
    ) { notes, folders, category, folderId, screen, query, searchExp, multiSelect, selectedIds, pinnedExp, prefs, actCnt, favCnt, schCnt, arcCnt, trshCnt ->

        val sortedNotes = sortNotes(notes, prefs.sortOrder)

        HomeUiState(
            notes = sortedNotes,
            folders = folders,
            selectedCategory = category,
            selectedFolderId = folderId,
            currentDrawerScreen = screen,
            searchQuery = query,
            isSearchExpanded = searchExp,
            isMultiSelectMode = multiSelect,
            selectedNoteIds = selectedIds,
            isPinnedSectionExpanded = pinnedExp,
            viewMode = prefs.viewMode,
            sortOrder = prefs.sortOrder,
            activeCount = actCnt,
            favoriteCount = favCnt,
            scheduledCount = schCnt,
            archivedCount = arcCnt,
            trashCount = trshCnt
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )

    private fun sortNotes(notes: List<Note>, sortOrder: SortOrder): List<Note> {
        return when (sortOrder) {
            SortOrder.DATE_MODIFIED -> notes.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
            )
            SortOrder.DATE_CREATED -> notes.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt }
            )
            SortOrder.TITLE_AZ -> notes.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() }
            )
            SortOrder.TITLE_ZA -> notes.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.title.lowercase() }
            )
        }
    }

    // UI actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearchExpanded(expanded: Boolean) {
        _isSearchExpanded.value = expanded
        if (!expanded) {
            _searchQuery.value = ""
        }
    }

    fun selectCategory(category: NoteCategory) {
        _selectedCategory.value = category
        _currentDrawerScreen.value = DrawerScreen.ALL_NOTES
        _selectedFolderId.value = null
    }

    fun selectFolder(folderId: Long) {
        _selectedFolderId.value = folderId
        _currentDrawerScreen.value = DrawerScreen.FOLDER
    }

    fun selectDrawerScreen(screen: DrawerScreen) {
        _currentDrawerScreen.value = screen
        if (screen != DrawerScreen.FOLDER) {
            _selectedFolderId.value = null
        }
    }

    fun togglePinnedSection() {
        _isPinnedSectionExpanded.value = !_isPinnedSectionExpanded.value
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    fun moveToTrash(note: Note) {
        viewModelScope.launch {
            repository.moveToTrash(note.id)
        }
    }

    fun restoreFromTrash(noteId: Int) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
        }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = false, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deletePermanently(noteId: Int) {
        viewModelScope.launch {
            repository.deletePermanently(noteId)
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            repository.clearTrash()
        }
    }

    // Multi-select
    fun startMultiSelect(initialNoteId: Int) {
        _isMultiSelectMode.value = true
        _selectedNoteIds.value = setOf(initialNoteId)
    }

    fun toggleNoteSelection(noteId: Int) {
        val current = _selectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) {
            current.remove(noteId)
            if (current.isEmpty()) {
                _isMultiSelectMode.value = false
            }
        } else {
            current.add(noteId)
        }
        _selectedNoteIds.value = current
    }

    fun selectAll(notes: List<Note>) {
        _selectedNoteIds.value = notes.map { it.id }.toSet()
    }

    fun exitMultiSelect() {
        _isMultiSelectMode.value = false
        _selectedNoteIds.value = emptySet()
    }

    fun bulkPin(pin: Boolean) {
        viewModelScope.launch {
            repository.bulkSetPinned(_selectedNoteIds.value.toList(), pin)
            exitMultiSelect()
        }
    }

    fun bulkArchive(archive: Boolean) {
        viewModelScope.launch {
            repository.bulkSetArchived(_selectedNoteIds.value.toList(), archive)
            exitMultiSelect()
        }
    }

    fun bulkMoveToTrash() {
        viewModelScope.launch {
            repository.bulkMoveToTrash(_selectedNoteIds.value.toList())
            exitMultiSelect()
        }
    }

    fun bulkSetFolder(folderId: Long?) {
        viewModelScope.launch {
            repository.bulkSetFolder(_selectedNoteIds.value.toList(), folderId)
            exitMultiSelect()
        }
    }

    fun bulkSetColor(color: Long) {
        viewModelScope.launch {
            repository.bulkSetColor(_selectedNoteIds.value.toList(), color)
            exitMultiSelect()
        }
    }

    fun bulkDeletePermanently() {
        viewModelScope.launch {
            repository.bulkDeletePermanently(_selectedNoteIds.value.toList())
            exitMultiSelect()
        }
    }

    // Folders
    fun createFolder(name: String, emoji: String = "📁") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertFolder(Folder(name = name.trim(), iconEmoji = emoji))
        }
    }

    fun renameFolder(folder: Folder, newName: String, emoji: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateFolder(folder.copy(name = newName.trim(), iconEmoji = emoji))
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
            if (_selectedFolderId.value == folder.id) {
                _currentDrawerScreen.value = DrawerScreen.ALL_NOTES
                _selectedFolderId.value = null
            }
        }
    }

    // Preferences
    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            preferencesManager.setViewMode(mode)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            preferencesManager.setSortOrder(order)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDynamicColor(enabled)
        }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAppLockEnabled(enabled)
        }
    }

    // Backup & Restore
    fun importBackup(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val backup = NoteExportUtils.importFromJson(json)
            if (backup != null) {
                repository.insertAllFolders(backup.folders)
                repository.insertAllNotes(backup.notes)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }
}
