package com.premium.auranotes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.premium.auranotes.R
import com.premium.auranotes.data.BlockType
import com.premium.auranotes.data.NoteCategory
import com.premium.auranotes.ui.components.*
import com.premium.auranotes.util.MarkdownConverter
import com.premium.auranotes.util.NoteExportUtils
import com.premium.auranotes.viewmodel.EditorViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    navController: NavController,
    noteId: Int,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showBasicBlocksSheet by remember { mutableStateOf(false) }
    var isTurnIntoMode by remember { mutableStateOf(false) }
    var activeBlockIdForInsert by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "img_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                viewModel.addImageBlock(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Export as .md file SAF launcher
    val exportMdLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                val mdText = MarkdownConverter.toMarkdown(viewModel.getCurrentNote())
                outputStream?.write(mdText.toByteArray())
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Date formatting
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
    val createdStr = remember(uiState.createdAt) { dateFormatter.format(Date(uiState.createdAt)) }
    val editedStr = remember(uiState.updatedAt) { dateFormatter.format(Date(uiState.updatedAt)) }

    val noteColor = Color(uiState.color)

    DisposableEffect(Unit) {
        onDispose {
            if (!uiState.isDeleted) {
                viewModel.saveImmediate()
            }
        }
    }

    Scaffold(
        containerColor = noteColor,
        topBar = {
            TopAppBar(
                title = {
                    // Folder & category breadcrumb
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showFolderDialog = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = uiState.folderName ?: uiState.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    // Pin toggle
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.action_pin),
                            tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Favorite toggle
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.action_favorite),
                            tint = if (uiState.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Reminder bell
                    IconButton(onClick = { showReminderSheet = true }) {
                        Icon(
                            imageVector = if (uiState.reminderTime != null && uiState.reminderTime!! > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                            contentDescription = stringResource(R.string.action_set_reminder),
                            tint = if (uiState.reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Share
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.action_share)
                        )
                    }

                    // 3-dot overflow
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_color)) },
                                onClick = {
                                    showOptionsMenu = false
                                    showColorPicker = true
                                },
                                leadingIcon = { Icon(Icons.Outlined.Palette, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_move_to_folder)) },
                                onClick = {
                                    showOptionsMenu = false
                                    showFolderDialog = true
                                },
                                leadingIcon = { Icon(Icons.Outlined.Folder, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Category") },
                                onClick = {
                                    showOptionsMenu = false
                                    showCategoryDialog = true
                                },
                                leadingIcon = { Icon(Icons.Outlined.Category, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_move_checked_bottom)) },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.moveCheckedTodosToBottom()
                                },
                                leadingIcon = { Icon(Icons.Outlined.LowPriority, null) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(if (uiState.isArchived) stringResource(R.string.action_unarchive) else stringResource(R.string.action_archive))
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.toggleArchive()
                                },
                                leadingIcon = { Icon(Icons.Outlined.Archive, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.deleteNote()
                                    navController.popBackStack()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            EditorBottomBar(
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onAddBlockClick = {
                    activeBlockIdForInsert = null
                    isTurnIntoMode = false
                    showBasicBlocksSheet = true
                },
                onTurnIntoClick = {
                    activeBlockIdForInsert = uiState.blocks.lastOrNull()?.id
                    isTurnIntoMode = true
                    showBasicBlocksSheet = true
                },
                onBoldClick = {},
                onItalicClick = {},
                onUnderlineClick = {},
                onStrikethroughClick = {},
                onLinkClick = {},
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onIndentClick = {
                    uiState.blocks.lastOrNull()?.let { viewModel.indentBlock(it.id) }
                },
                onOutdentClick = {
                    uiState.blocks.lastOrNull()?.let { viewModel.outdentBlock(it.id) }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Note Title
            item {
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    textStyle = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    decorationBox = { innerTextField ->
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.editor_title_hint),
                                style = TextStyle(
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Metadata row: Created / Edited & Live Word Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edited $editedStr · ${uiState.wordCount} words",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                }
            }

            // Tags row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (tag in uiState.tags) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (tag.startsWith("#")) tag else "#$tag",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.removeTag(tag) },
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Add Tag pill
                    Surface(
                        onClick = { showTagDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.editor_add_tag),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Blocks List
            itemsIndexed(uiState.blocks, key = { _, block -> block.id }) { index, block ->
                BlockItem(
                    block = block,
                    index = index,
                    isFirst = index == 0,
                    isLast = index == uiState.blocks.size - 1,
                    onTextChange = { text -> viewModel.updateBlockText(block.id, text) },
                    onCheckedToggle = { viewModel.toggleBlockChecked(block.id) },
                    onEnter = { viewModel.handleEnter(block.id) },
                    onBackspaceOnEmpty = { viewModel.handleBackspaceOnEmpty(block.id) },
                    onSelectBlockType = { type -> viewModel.changeBlockType(block.id, type) },
                    onMoveUp = { if (index > 0) viewModel.moveBlock(index, index - 1) },
                    onMoveDown = { if (index < uiState.blocks.size - 1) viewModel.moveBlock(index, index + 1) },
                    onDelete = { viewModel.deleteBlock(block.id) }
                )
            }

            // Bottom spacer so user can scroll content well above keyboard
            item {
                Spacer(modifier = Modifier.height(180.dp))
            }
        }
    }

    // Basic Blocks Sheet / Turn Into Sheet
    if (showBasicBlocksSheet) {
        BasicBlocksSheet(
            isTurnIntoMode = isTurnIntoMode,
            onSelectBlockType = { selectedType ->
                if (isTurnIntoMode && activeBlockIdForInsert != null) {
                    viewModel.changeBlockType(activeBlockIdForInsert!!, selectedType)
                } else {
                    viewModel.insertBlock(activeBlockIdForInsert, selectedType)
                }
                showBasicBlocksSheet = false
            },
            onDismiss = { showBasicBlocksSheet = false }
        )
    }

    // Color Picker Sheet
    if (showColorPicker) {
        ColorPickerSheet(
            selectedColor = uiState.color,
            onColorSelected = { color ->
                viewModel.setColor(color)
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Reminder Sheet
    if (showReminderSheet) {
        ReminderSheet(
            existingReminderTime = uiState.reminderTime,
            onSetReminder = { time ->
                viewModel.setReminder(time)
            },
            onCancelReminder = {
                viewModel.setReminder(null)
            },
            onDismiss = { showReminderSheet = false }
        )
    }

    // Share Sheet
    if (showShareSheet) {
        val currentNote = viewModel.getCurrentNote()
        ShareSheet(
            onShareText = { NoteExportUtils.shareText(context, currentNote) },
            onShareMarkdown = { NoteExportUtils.shareMarkdown(context, currentNote) },
            onSharePdf = { NoteExportUtils.generateAndSharePdf(context, currentNote) },
            onExportMarkdownFile = {
                val safeTitle = (currentNote.title.ifBlank { "AuraNote" }).replace(Regex("[^a-zA-Z0-9.-]"), "_")
                exportMdLauncher.launch("$safeTitle.md")
            },
            onDismiss = { showShareSheet = false }
        )
    }

    // Folder selection dialog
    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Select Folder", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                viewModel.setFolder(null, null)
                                showFolderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No Folder", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    items(folders.size) { i ->
                        val folder = folders[i]
                        TextButton(
                            onClick = {
                                viewModel.setFolder(folder.id, folder.name)
                                showFolderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(folder.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(folder.name, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Category selection dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    for (cat in listOf(NoteCategory.PERSONAL, NoteCategory.WORK, NoteCategory.TASKS)) {
                        TextButton(
                            onClick = {
                                viewModel.setCategory(cat)
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (uiState.category == cat) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Tag Dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Add Tag", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    placeholder = { Text(stringResource(R.string.editor_tag_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            viewModel.addTag(newTagInput)
                            newTagInput = ""
                            showTagDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
