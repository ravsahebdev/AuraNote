package com.premium.auranotes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premium.auranotes.data.*
import com.premium.auranotes.reminders.ReminderManager
import com.premium.auranotes.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditorSnapshot(
    val title: String,
    val blocks: List<Block>,
    val tags: List<String>
)

data class EditorUiState(
    val noteId: Int = 0,
    val title: String = "",
    val blocks: List<Block> = listOf(Block()),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val color: Long = 0xFF1E1E1E,
    val folderId: Long? = null,
    val category: NoteCategory = NoteCategory.PERSONAL,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isDeleted: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val folderName: String? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val reminderManager: ReminderManager,
    private val preferencesManager: PreferencesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navNoteId: Int = savedStateHandle.get<Int>("noteId") ?: 0
    private var actualNoteId: Int = navNoteId

    private val _uiState = MutableStateFlow(EditorUiState(noteId = navNoteId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val folders = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()

    private var autoSaveJob: Job? = null
    private var isInitialized = false

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            if (actualNoteId != 0) {
                val note = repository.getNoteById(actualNoteId)
                if (note != null) {
                    val folderName = if (note.folderId != null) {
                        repository.getFolderById(note.folderId)?.name
                    } else null

                    _uiState.value = EditorUiState(
                        noteId = note.id,
                        title = note.title,
                        blocks = if (note.content.isEmpty()) listOf(Block()) else note.content,
                        isPinned = note.isPinned,
                        isFavorite = note.isFavorite,
                        isArchived = note.isArchived,
                        color = note.color,
                        folderId = note.folderId,
                        category = note.category,
                        reminderTime = note.reminderTime,
                        createdAt = note.createdAt,
                        updatedAt = note.updatedAt,
                        tags = note.tags,
                        wordCount = note.wordCount,
                        folderName = folderName
                    )
                }
            } else {
                // New note: get default note color from user preferences
                val prefs = preferencesManager.userPreferencesFlow.first()
                val initialType = savedStateHandle.get<String>("initialType")
                val initialBlocks = if (initialType == "CHECKLIST") {
                    listOf(Block(type = BlockType.TODO, text = ""))
                } else {
                    listOf(Block(type = BlockType.TEXT, text = ""))
                }

                _uiState.value = _uiState.value.copy(
                    color = prefs.defaultColor,
                    blocks = initialBlocks
                )
            }
            saveSnapshot()
            isInitialized = true
        }
    }

    private fun saveSnapshot() {
        val current = _uiState.value
        val snapshot = EditorSnapshot(
            title = current.title,
            blocks = current.blocks.map { it.copy() },
            tags = current.tags.toList()
        )
        undoStack.addLast(snapshot)
        if (undoStack.size > 50) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        updateUndoRedoAvailability()
    }

    private fun updateUndoRedoAvailability() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.size > 1) {
            val current = undoStack.removeLast()
            redoStack.addLast(current)
            val previous = undoStack.last()
            _uiState.update {
                it.copy(
                    title = previous.title,
                    blocks = previous.blocks.map { b -> b.copy() },
                    tags = previous.tags,
                    wordCount = Note.calculateWordCount(previous.title, previous.blocks)
                )
            }
            updateUndoRedoAvailability()
            triggerAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeLast()
            undoStack.addLast(next)
            _uiState.update {
                it.copy(
                    title = next.title,
                    blocks = next.blocks.map { b -> b.copy() },
                    tags = next.tags,
                    wordCount = Note.calculateWordCount(next.title, next.blocks)
                )
            }
            updateUndoRedoAvailability()
            triggerAutoSave()
        }
    }

    fun updateTitle(newTitle: String) {
        if (_uiState.value.isDeleted) return
        _uiState.update {
            it.copy(
                title = newTitle,
                wordCount = Note.calculateWordCount(newTitle, it.blocks),
                updatedAt = System.currentTimeMillis()
            )
        }
        triggerAutoSave()
    }

    fun updateBlockText(blockId: String, newText: String) {
        if (_uiState.value.isDeleted) return

        // Auto-detect hashtag addition: e.g. typing "#ideas"
        val detectedTags = Regex("#([a-zA-Z0-9_-]+)").findAll(newText).map { it.groupValues[1] }.toList()
        val currentTags = _uiState.value.tags.toMutableList()
        var tagsChanged = false
        for (tag in detectedTags) {
            if (!currentTags.contains(tag)) {
                currentTags.add(tag)
                tagsChanged = true
            }
        }

        _uiState.update { state ->
            val updatedBlocks = state.blocks.map { block ->
                if (block.id == blockId) block.copy(text = newText) else block
            }
            state.copy(
                blocks = updatedBlocks,
                tags = if (tagsChanged) currentTags else state.tags,
                wordCount = Note.calculateWordCount(state.title, updatedBlocks),
                updatedAt = System.currentTimeMillis()
            )
        }
        triggerAutoSave()
    }

    fun toggleBlockChecked(blockId: String) {
        if (_uiState.value.isDeleted) return
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(checked = !block.checked) else block
            }
            state.copy(blocks = updated, updatedAt = System.currentTimeMillis())
        }
        saveSnapshot()
        triggerAutoSave()
    }

    fun handleEnter(blockId: String) {
        if (_uiState.value.isDeleted) return
        val currentBlocks = _uiState.value.blocks
        val index = currentBlocks.indexOfFirst { it.id == blockId }
        if (index == -1) return

        val currentBlock = currentBlocks[index]

        // If Enter pressed on an empty list block, turn it back into regular TEXT
        if (currentBlock.text.isBlank() && (currentBlock.type == BlockType.TODO || currentBlock.type == BlockType.BULLET || currentBlock.type == BlockType.NUMBERED)) {
            changeBlockType(blockId, BlockType.TEXT)
            return
        }

        // Otherwise continue list types, or create TEXT block
        val nextType = when (currentBlock.type) {
            BlockType.TODO -> BlockType.TODO
            BlockType.BULLET -> BlockType.BULLET
            BlockType.NUMBERED -> BlockType.NUMBERED
            else -> BlockType.TEXT
        }

        val newBlock = Block(
            id = UUID.randomUUID().toString(),
            type = nextType,
            text = "",
            indent = currentBlock.indent
        )

        val newBlocks = currentBlocks.toMutableList()
        newBlocks.add(index + 1, newBlock)

        _uiState.update {
            it.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
        }
        saveSnapshot()
        triggerAutoSave()
    }

    fun handleBackspaceOnEmpty(blockId: String) {
        if (_uiState.value.isDeleted) return
        val currentBlocks = _uiState.value.blocks
        val index = currentBlocks.indexOfFirst { it.id == blockId }
        if (index <= 0) {
            // First block: if not text, convert to text
            if (index == 0 && currentBlocks[0].type != BlockType.TEXT) {
                changeBlockType(blockId, BlockType.TEXT)
            }
            return
        }

        val currentBlock = currentBlocks[index]
        if (currentBlock.text.isEmpty()) {
            val newBlocks = currentBlocks.toMutableList()
            newBlocks.removeAt(index)
            _uiState.update {
                it.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
            }
            saveSnapshot()
            triggerAutoSave()
        }
    }

    fun changeBlockType(blockId: String, newType: BlockType) {
        if (_uiState.value.isDeleted) return
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(type = newType) else block
            }
            state.copy(blocks = updated, updatedAt = System.currentTimeMillis())
        }
        saveSnapshot()
        triggerAutoSave()
    }

    fun insertBlock(afterBlockId: String?, type: BlockType = BlockType.TEXT) {
        if (_uiState.value.isDeleted) return
        val currentBlocks = _uiState.value.blocks.toMutableList()
        val newBlock = Block(type = type)

        if (afterBlockId == null) {
            currentBlocks.add(newBlock)
        } else {
            val idx = currentBlocks.indexOfFirst { it.id == afterBlockId }
            if (idx != -1) {
                currentBlocks.add(idx + 1, newBlock)
            } else {
                currentBlocks.add(newBlock)
            }
        }

        _uiState.update { it.copy(blocks = currentBlocks, updatedAt = System.currentTimeMillis()) }
        saveSnapshot()
        triggerAutoSave()
    }

    fun deleteBlock(blockId: String) {
        if (_uiState.value.isDeleted) return
        val currentBlocks = _uiState.value.blocks
        if (currentBlocks.size <= 1) {
            // Keep at least one block
            _uiState.update { it.copy(blocks = listOf(Block())) }
            return
        }
        val updated = currentBlocks.filter { it.id != blockId }
        _uiState.update { it.copy(blocks = updated, updatedAt = System.currentTimeMillis()) }
        saveSnapshot()
        triggerAutoSave()
    }

    fun indentBlock(blockId: String) {
        if (_uiState.value.isDeleted) return
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId && block.indent < 4) block.copy(indent = block.indent + 1) else block
            }
            state.copy(blocks = updated)
        }
        triggerAutoSave()
    }

    fun outdentBlock(blockId: String) {
        if (_uiState.value.isDeleted) return
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId && block.indent > 0) block.copy(indent = block.indent - 1) else block
            }
            state.copy(blocks = updated)
        }
        triggerAutoSave()
    }

    fun moveBlock(fromIndex: Int, toIndex: Int) {
        if (_uiState.value.isDeleted) return
        val currentBlocks = _uiState.value.blocks.toMutableList()
        if (fromIndex in currentBlocks.indices && toIndex in currentBlocks.indices) {
            val item = currentBlocks.removeAt(fromIndex)
            currentBlocks.add(toIndex, item)
            _uiState.update { it.copy(blocks = currentBlocks, updatedAt = System.currentTimeMillis()) }
            saveSnapshot()
            triggerAutoSave()
        }
    }

    fun moveCheckedTodosToBottom() {
        if (_uiState.value.isDeleted) return
        val unchecked = _uiState.value.blocks.filter { !(it.type == BlockType.TODO && it.checked) }
        val checked = _uiState.value.blocks.filter { it.type == BlockType.TODO && it.checked }
        _uiState.update { it.copy(blocks = unchecked + checked, updatedAt = System.currentTimeMillis()) }
        saveSnapshot()
        triggerAutoSave()
    }

    fun addImageBlock(imagePath: String) {
        if (_uiState.value.isDeleted) return
        insertBlock(null, BlockType.IMAGE)
        val lastBlock = _uiState.value.blocks.lastOrNull()
        if (lastBlock != null) {
            updateBlockText(lastBlock.id, imagePath)
        }
    }

    fun addTag(tag: String) {
        val cleanTag = tag.trim().removePrefix("#")
        if (cleanTag.isBlank()) return
        val current = _uiState.value.tags.toMutableList()
        if (!current.contains(cleanTag)) {
            current.add(cleanTag)
            _uiState.update { it.copy(tags = current) }
            saveSnapshot()
            triggerAutoSave()
        }
    }

    fun removeTag(tag: String) {
        val current = _uiState.value.tags.toMutableList()
        if (current.remove(tag)) {
            _uiState.update { it.copy(tags = current) }
            saveSnapshot()
            triggerAutoSave()
        }
    }

    fun setColor(color: Long) {
        _uiState.update { it.copy(color = color, updatedAt = System.currentTimeMillis()) }
        triggerAutoSave()
    }

    fun setCategory(category: NoteCategory) {
        _uiState.update { it.copy(category = category, updatedAt = System.currentTimeMillis()) }
        triggerAutoSave()
    }

    fun setFolder(folderId: Long?, folderName: String?) {
        _uiState.update { it.copy(folderId = folderId, folderName = folderName, updatedAt = System.currentTimeMillis()) }
        triggerAutoSave()
    }

    fun setReminder(time: Long?) {
        _uiState.update { it.copy(reminderTime = time, updatedAt = System.currentTimeMillis()) }
        if (time != null && actualNoteId != 0) {
            reminderManager.scheduleReminder(
                noteId = actualNoteId,
                noteTitle = _uiState.value.title.ifBlank { "Reminder" },
                triggerAtMillis = time,
                previewContent = _uiState.value.blocks.firstOrNull { it.text.isNotBlank() }?.text ?: ""
            )
        } else if (time == null && actualNoteId != 0) {
            reminderManager.cancelReminder(actualNoteId)
        }
        triggerAutoSave()
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned, updatedAt = System.currentTimeMillis()) }
        triggerAutoSave()
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite, updatedAt = System.currentTimeMillis()) }
        triggerAutoSave()
    }

    fun toggleArchive() {
        _uiState.update { it.copy(isArchived = !it.isArchived, updatedAt = System.currentTimeMillis()) }
        triggerAutoSave()
    }

    fun deleteNote() {
        // Set deleted flag immediately so onDispose and auto-save NEVER re-save this note
        _uiState.update { it.copy(isDeleted = true) }
        autoSaveJob?.cancel()

        if (actualNoteId != 0) {
            viewModelScope.launch {
                repository.moveToTrash(actualNoteId)
                reminderManager.cancelReminder(actualNoteId)
            }
        }
    }

    private fun triggerAutoSave() {
        if (_uiState.value.isDeleted || !isInitialized) return
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500) // 500 ms debounce
            saveImmediate()
        }
    }

    fun saveImmediate() {
        val state = _uiState.value
        if (state.isDeleted) return

        val isBlank = state.title.isBlank() && state.blocks.all { it.text.isBlank() }
        if (isBlank && actualNoteId == 0) {
            // Don't save empty new note
            return
        }

        viewModelScope.launch {
            val noteToSave = Note(
                id = actualNoteId,
                title = state.title.trim(),
                content = state.blocks,
                timestamp = state.createdAt,
                isPinned = state.isPinned,
                isArchived = state.isArchived,
                color = state.color,
                folderId = state.folderId,
                category = state.category,
                isFavorite = state.isFavorite,
                isTrashed = false,
                trashedAt = null,
                reminderTime = state.reminderTime,
                createdAt = state.createdAt,
                updatedAt = System.currentTimeMillis(),
                tags = state.tags,
                wordCount = state.wordCount
            )

            if (actualNoteId == 0) {
                val newId = repository.insertNote(noteToSave).toInt()
                actualNoteId = newId
                _uiState.update { it.copy(noteId = newId) }

                // Schedule reminder if already set on new note
                if (state.reminderTime != null) {
                    reminderManager.scheduleReminder(
                        noteId = newId,
                        noteTitle = state.title.ifBlank { "Reminder" },
                        triggerAtMillis = state.reminderTime,
                        previewContent = state.blocks.firstOrNull { it.text.isNotBlank() }?.text ?: ""
                    )
                }
            } else {
                repository.updateNote(noteToSave)
            }
        }
    }

    fun getCurrentNote(): Note {
        val state = _uiState.value
        return Note(
            id = actualNoteId,
            title = state.title,
            content = state.blocks,
            timestamp = state.createdAt,
            isPinned = state.isPinned,
            isArchived = state.isArchived,
            color = state.color,
            folderId = state.folderId,
            category = state.category,
            isFavorite = state.isFavorite,
            isTrashed = false,
            reminderTime = state.reminderTime,
            createdAt = state.createdAt,
            updatedAt = state.updatedAt,
            tags = state.tags,
            wordCount = state.wordCount
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (!_uiState.value.isDeleted) {
            saveImmediate()
        }
    }
}
