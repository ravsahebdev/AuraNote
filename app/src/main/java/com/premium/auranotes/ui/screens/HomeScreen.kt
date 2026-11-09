package com.premium.auranotes.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.premium.auranotes.R
import com.premium.auranotes.data.Note
import com.premium.auranotes.data.NoteCategory
import com.premium.auranotes.data.SortOrder
import com.premium.auranotes.data.ViewMode
import com.premium.auranotes.ui.components.*
import com.premium.auranotes.util.NoteExportUtils
import com.premium.auranotes.viewmodel.DrawerScreen
import com.premium.auranotes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showFabMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMoveFolderDialog by remember { mutableStateOf(false) }
    var showBulkColorPicker by remember { mutableStateOf(false) }
    var noteToShare by remember { mutableStateOf<Note?>(null) }

    // Dialog state for new folder
    var newFolderName by remember { mutableStateOf("") }
    var newFolderEmoji by remember { mutableStateOf("📁") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerContent(
                currentScreen = uiState.currentDrawerScreen,
                selectedFolderId = uiState.selectedFolderId,
                folders = uiState.folders,
                activeCount = uiState.activeCount,
                favoriteCount = uiState.favoriteCount,
                scheduledCount = uiState.scheduledCount,
                archivedCount = uiState.archivedCount,
                trashCount = uiState.trashCount,
                onScreenSelect = { screen ->
                    viewModel.selectDrawerScreen(screen)
                },
                onFolderSelect = { folderId ->
                    viewModel.selectFolder(folderId)
                },
                onNewFolderClick = {
                    showCreateFolderDialog = true
                },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onAboutClick = {
                    navController.navigate("about")
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (uiState.isMultiSelectMode) {
                    // Multi-select top bar
                    TopAppBar(
                        title = {
                            Text(
                                text = "${uiState.selectedNoteIds.size} selected",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.exitMultiSelect() }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                            }
                        },
                        actions = {
                            TextButton(onClick = { viewModel.selectAll(uiState.notes) }) {
                                Text(
                                    text = stringResource(R.string.action_select_all),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    // Normal top bar with Notes title, search, overflow
                    Column {
                        TopAppBar(
                            title = {
                                if (uiState.isSearchExpanded) {
                                    TextField(
                                        value = uiState.searchQuery,
                                        onValueChange = { viewModel.updateSearchQuery(it) },
                                        placeholder = {
                                            Text(
                                                stringResource(R.string.action_search_hint),
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    val title = when (uiState.currentDrawerScreen) {
                                        DrawerScreen.ALL_NOTES -> "Notes"
                                        DrawerScreen.FAVORITES -> stringResource(R.string.nav_favorites)
                                        DrawerScreen.REMINDERS -> stringResource(R.string.nav_reminders)
                                        DrawerScreen.ARCHIVE -> stringResource(R.string.nav_archive)
                                        DrawerScreen.TRASH -> stringResource(R.string.nav_trash)
                                        DrawerScreen.FOLDER -> {
                                            uiState.folders.find { it.id == uiState.selectedFolderId }?.name ?: "Folder"
                                        }
                                    }
                                    Text(
                                        text = title,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            navigationIcon = {
                                if (uiState.isSearchExpanded) {
                                    IconButton(onClick = { viewModel.toggleSearchExpanded(false) }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Close search")
                                    }
                                } else {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                                    }
                                }
                            },
                            actions = {
                                if (!uiState.isSearchExpanded) {
                                    IconButton(onClick = { viewModel.toggleSearchExpanded(true) }) {
                                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                                    }
                                } else if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }

                                var showOverflowMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { showOverflowMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                                    }
                                    DropdownMenu(
                                        expanded = showOverflowMenu,
                                        onDismissRequest = { showOverflowMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.action_sort)) },
                                            onClick = {
                                                showOverflowMenu = false
                                                showSortDialog = true
                                            },
                                            leadingIcon = { Icon(Icons.Outlined.Sort, null) }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(if (uiState.viewMode == ViewMode.GRID) stringResource(R.string.action_list_view) else stringResource(R.string.action_grid_view))
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                val nextMode = if (uiState.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                                                viewModel.setViewMode(nextMode)
                                            },
                                            leadingIcon = {
                                                Icon(if (uiState.viewMode == ViewMode.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView, null)
                                            }
                                        )
                                        if (uiState.currentDrawerScreen == DrawerScreen.TRASH) {
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.action_clear_trash), color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    viewModel.clearTrash()
                                                },
                                                leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) }
                                            )
                                        }
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.nav_settings)) },
                                            onClick = {
                                                showOverflowMenu = false
                                                navController.navigate("settings")
                                            },
                                            leadingIcon = { Icon(Icons.Outlined.Settings, null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.nav_about)) },
                                            onClick = {
                                                showOverflowMenu = false
                                                navController.navigate("about")
                                            },
                                            leadingIcon = { Icon(Icons.Outlined.Info, null) }
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )

                        // Pill style chip tabs (All / Personal / Work / Tasks)
                        if (uiState.currentDrawerScreen == DrawerScreen.ALL_NOTES && !uiState.isSearchExpanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryChip(
                                    label = stringResource(R.string.tab_all),
                                    isSelected = uiState.selectedCategory == NoteCategory.ALL,
                                    onClick = { viewModel.selectCategory(NoteCategory.ALL) }
                                )
                                CategoryChip(
                                    label = stringResource(R.string.tab_personal),
                                    isSelected = uiState.selectedCategory == NoteCategory.PERSONAL,
                                    onClick = { viewModel.selectCategory(NoteCategory.PERSONAL) }
                                )
                                CategoryChip(
                                    label = stringResource(R.string.tab_work),
                                    isSelected = uiState.selectedCategory == NoteCategory.WORK,
                                    onClick = { viewModel.selectCategory(NoteCategory.WORK) }
                                )
                                CategoryChip(
                                    label = stringResource(R.string.tab_tasks),
                                    isSelected = uiState.selectedCategory == NoteCategory.TASKS,
                                    onClick = { viewModel.selectCategory(NoteCategory.TASKS) }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            },
            bottomBar = {
                if (uiState.isMultiSelectMode) {
                    MultiSelectBottomBar(
                        selectedCount = uiState.selectedNoteIds.size,
                        onPin = { viewModel.bulkPin(true) },
                        onFolder = { showMoveFolderDialog = true },
                        onArchive = { viewModel.bulkArchive(true) },
                        onColor = { showBulkColorPicker = true },
                        onDelete = { viewModel.bulkMoveToTrash() }
                    )
                }
            },
            floatingActionButton = {
                if (!uiState.isMultiSelectMode && uiState.currentDrawerScreen != DrawerScreen.TRASH) {
                    Column(horizontalAlignment = Alignment.End) {
                        AnimatedVisibility(
                            visible = showFabMenu,
                            enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                // New Folder item
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            showFabMenu = false
                                            showCreateFolderDialog = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.fab_new_folder),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                                }

                                // New Checklist item
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            showFabMenu = false
                                            navController.navigate("editor?noteId=0&initialType=CHECKLIST")
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.fab_new_checklist),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.CheckBox, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                if (showFabMenu) {
                                    showFabMenu = false
                                } else {
                                    navController.navigate("editor?noteId=0")
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = stringResource(R.string.fab_new_note)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.notes.isEmpty()) {
                    // Empty states
                    val emptyTitle = when {
                        uiState.searchQuery.isNotEmpty() -> stringResource(R.string.empty_search_title)
                        uiState.currentDrawerScreen == DrawerScreen.TRASH -> stringResource(R.string.empty_trash_title)
                        uiState.currentDrawerScreen == DrawerScreen.FAVORITES -> stringResource(R.string.empty_favorites_title)
                        uiState.currentDrawerScreen == DrawerScreen.ARCHIVE -> stringResource(R.string.empty_archive_title)
                        uiState.currentDrawerScreen == DrawerScreen.REMINDERS -> stringResource(R.string.empty_reminders_title)
                        else -> stringResource(R.string.empty_notes_title)
                    }

                    val emptySubtitle = when {
                        uiState.searchQuery.isNotEmpty() -> stringResource(R.string.empty_search_subtitle)
                        uiState.currentDrawerScreen == DrawerScreen.TRASH -> stringResource(R.string.empty_trash_subtitle)
                        uiState.currentDrawerScreen == DrawerScreen.FAVORITES -> stringResource(R.string.empty_favorites_subtitle)
                        uiState.currentDrawerScreen == DrawerScreen.ARCHIVE -> stringResource(R.string.empty_archive_subtitle)
                        uiState.currentDrawerScreen == DrawerScreen.REMINDERS -> stringResource(R.string.empty_reminders_subtitle)
                        else -> stringResource(R.string.empty_notes_subtitle)
                    }

                    val emptyIcon = when {
                        uiState.searchQuery.isNotEmpty() -> Icons.Outlined.SearchOff
                        uiState.currentDrawerScreen == DrawerScreen.TRASH -> Icons.Outlined.Delete
                        uiState.currentDrawerScreen == DrawerScreen.FAVORITES -> Icons.Outlined.FavoriteBorder
                        uiState.currentDrawerScreen == DrawerScreen.ARCHIVE -> Icons.Outlined.Archive
                        uiState.currentDrawerScreen == DrawerScreen.REMINDERS -> Icons.Outlined.Notifications
                        else -> Icons.Outlined.NoteAdd
                    }

                    EmptyStateView(
                        icon = emptyIcon,
                        title = emptyTitle,
                        subtitle = emptySubtitle
                    )
                } else {
                    // Notes list / grid with sections
                    val pinnedNotes = remember(uiState.notes) { uiState.notes.filter { it.isPinned } }
                    val unpinnedNotes = remember(uiState.notes) { uiState.notes.filter { !it.isPinned } }

                    if (uiState.viewMode == ViewMode.GRID) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalItemSpacing = 10.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                        ) {
                            // Pinned Section
                            if (pinnedNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    SectionHeader(
                                        title = stringResource(R.string.section_pinned),
                                        count = pinnedNotes.size,
                                        isExpanded = uiState.isPinnedSectionExpanded,
                                        onToggle = { viewModel.togglePinnedSection() }
                                    )
                                }
                                if (uiState.isPinnedSectionExpanded) {
                                    items(pinnedNotes, key = { it.id }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { navController.navigate("editor?noteId=${note.id}") },
                                            onLongClick = { viewModel.startMultiSelect(note.id) },
                                            isMultiSelectMode = uiState.isMultiSelectMode,
                                            isSelected = uiState.selectedNoteIds.contains(note.id),
                                            onSelectToggle = { viewModel.toggleNoteSelection(note.id) },
                                            onPinToggle = { viewModel.togglePin(note) },
                                            onFavoriteToggle = { viewModel.toggleFavorite(note) },
                                            onArchiveToggle = {
                                                viewModel.archiveNote(note)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Note archived")
                                                }
                                            },
                                            onDelete = {
                                                viewModel.moveToTrash(note)
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = context.getString(R.string.note_moved_to_trash),
                                                        actionLabel = context.getString(R.string.action_undo),
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreFromTrash(note.id)
                                                    }
                                                }
                                            },
                                            onShare = { noteToShare = note }
                                        )
                                    }
                                }
                            }

                            // Unpinned sections: Today, Previous 7 Days, Previous 30 Days, Older
                            val now = System.currentTimeMillis()
                            val oneDay = 24L * 60 * 60 * 1000
                            val sevenDays = 7L * oneDay
                            val thirtyDays = 30L * oneDay

                            val sections = listOf(
                                stringResource(R.string.section_today) to unpinnedNotes.filter { now - it.updatedAt < oneDay },
                                stringResource(R.string.section_previous_7_days) to unpinnedNotes.filter { (now - it.updatedAt) in oneDay until sevenDays },
                                stringResource(R.string.section_previous_30_days) to unpinnedNotes.filter { (now - it.updatedAt) in sevenDays until thirtyDays },
                                stringResource(R.string.section_older) to unpinnedNotes.filter { now - it.updatedAt >= thirtyDays }
                            ).filter { it.second.isNotEmpty() }

                            for ((sectionTitle, sectionNotes) in sections) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = sectionTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                    )
                                }

                                items(sectionNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { navController.navigate("editor?noteId=${note.id}") },
                                        onLongClick = { viewModel.startMultiSelect(note.id) },
                                        isMultiSelectMode = uiState.isMultiSelectMode,
                                        isSelected = uiState.selectedNoteIds.contains(note.id),
                                        onSelectToggle = { viewModel.toggleNoteSelection(note.id) },
                                        onPinToggle = { viewModel.togglePin(note) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(note) },
                                        onArchiveToggle = {
                                            viewModel.archiveNote(note)
                                            scope.launch { snackbarHostState.showSnackbar("Note archived") }
                                        },
                                        onDelete = {
                                            viewModel.moveToTrash(note)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = context.getString(R.string.note_moved_to_trash),
                                                    actionLabel = context.getString(R.string.action_undo),
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.restoreFromTrash(note.id)
                                                }
                                            }
                                        },
                                        onShare = { noteToShare = note }
                                    )
                                }
                            }

                            // Footer live count
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${uiState.notes.size} Notes",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    } else {
                        // List View Mode
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                        ) {
                            if (pinnedNotes.isNotEmpty()) {
                                item {
                                    SectionHeader(
                                        title = stringResource(R.string.section_pinned),
                                        count = pinnedNotes.size,
                                        isExpanded = uiState.isPinnedSectionExpanded,
                                        onToggle = { viewModel.togglePinnedSection() }
                                    )
                                }
                                if (uiState.isPinnedSectionExpanded) {
                                    items(pinnedNotes, key = { it.id }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { navController.navigate("editor?noteId=${note.id}") },
                                            onLongClick = { viewModel.startMultiSelect(note.id) },
                                            isMultiSelectMode = uiState.isMultiSelectMode,
                                            isSelected = uiState.selectedNoteIds.contains(note.id),
                                            onSelectToggle = { viewModel.toggleNoteSelection(note.id) },
                                            onPinToggle = { viewModel.togglePin(note) },
                                            onFavoriteToggle = { viewModel.toggleFavorite(note) },
                                            onArchiveToggle = { viewModel.archiveNote(note) },
                                            onDelete = {
                                                viewModel.moveToTrash(note)
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = context.getString(R.string.note_moved_to_trash),
                                                        actionLabel = context.getString(R.string.action_undo)
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreFromTrash(note.id)
                                                    }
                                                }
                                            },
                                            onShare = { noteToShare = note }
                                        )
                                    }
                                }
                            }

                            val now = System.currentTimeMillis()
                            val oneDay = 24L * 60 * 60 * 1000
                            val sevenDays = 7L * oneDay
                            val thirtyDays = 30L * oneDay

                            val listSections = listOf(
                                stringResource(R.string.section_today) to unpinnedNotes.filter { now - it.updatedAt < oneDay },
                                stringResource(R.string.section_previous_7_days) to unpinnedNotes.filter { (now - it.updatedAt) in oneDay until sevenDays },
                                stringResource(R.string.section_previous_30_days) to unpinnedNotes.filter { (now - it.updatedAt) in sevenDays until thirtyDays },
                                stringResource(R.string.section_older) to unpinnedNotes.filter { now - it.updatedAt >= thirtyDays }
                            ).filter { it.second.isNotEmpty() }

                            for ((sectionTitle, sectionNotes) in listSections) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = sectionTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                    )
                                }

                                items(sectionNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { navController.navigate("editor?noteId=${note.id}") },
                                        onLongClick = { viewModel.startMultiSelect(note.id) },
                                        isMultiSelectMode = uiState.isMultiSelectMode,
                                        isSelected = uiState.selectedNoteIds.contains(note.id),
                                        onSelectToggle = { viewModel.toggleNoteSelection(note.id) },
                                        onPinToggle = { viewModel.togglePin(note) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(note) },
                                        onArchiveToggle = { viewModel.archiveNote(note) },
                                        onDelete = {
                                            viewModel.moveToTrash(note)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = context.getString(R.string.note_moved_to_trash),
                                                    actionLabel = context.getString(R.string.action_undo)
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.restoreFromTrash(note.id)
                                                }
                                            }
                                        },
                                        onShare = { noteToShare = note }
                                    )
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${uiState.notes.size} Notes",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Share Sheet
    if (noteToShare != null) {
        val note = noteToShare!!
        ShareSheet(
            onShareText = { NoteExportUtils.shareText(context, note) },
            onShareMarkdown = { NoteExportUtils.shareMarkdown(context, note) },
            onSharePdf = { NoteExportUtils.generateAndSharePdf(context, note) },
            onExportMarkdownFile = { NoteExportUtils.shareMarkdown(context, note) },
            onDismiss = { noteToShare = null }
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text(stringResource(R.string.sort_by_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    SortOptionRow(
                        label = stringResource(R.string.sort_date_modified),
                        isSelected = uiState.sortOrder == SortOrder.DATE_MODIFIED,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DATE_MODIFIED)
                            showSortDialog = false
                        }
                    )
                    SortOptionRow(
                        label = stringResource(R.string.sort_date_created),
                        isSelected = uiState.sortOrder == SortOrder.DATE_CREATED,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DATE_CREATED)
                            showSortDialog = false
                        }
                    )
                    SortOptionRow(
                        label = stringResource(R.string.sort_title_az),
                        isSelected = uiState.sortOrder == SortOrder.TITLE_AZ,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.TITLE_AZ)
                            showSortDialog = false
                        }
                    )
                    SortOptionRow(
                        label = stringResource(R.string.sort_title_za),
                        isSelected = uiState.sortOrder == SortOrder.TITLE_ZA,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.TITLE_ZA)
                            showSortDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    // Create New Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(stringResource(R.string.nav_new_folder), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newFolderEmoji,
                        onValueChange = { newFolderEmoji = it },
                        label = { Text("Icon / Emoji") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName, newFolderEmoji.ifBlank { "📁" })
                            newFolderName = ""
                            newFolderEmoji = "📁"
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Move to folder dialog for multi-select
    if (showMoveFolderDialog) {
        AlertDialog(
            onDismissRequest = { showMoveFolderDialog = false },
            title = { Text("Move to Folder", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        TextButton(
                            onClick = {
                                viewModel.bulkSetFolder(null)
                                showMoveFolderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No Folder (Remove)", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    items(uiState.folders) { folder ->
                        TextButton(
                            onClick = {
                                viewModel.bulkSetFolder(folder.id)
                                showMoveFolderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(folder.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(folder.name, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveFolderDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Bulk color picker
    if (showBulkColorPicker) {
        ColorPickerSheet(
            selectedColor = 0xFF1E1E1E,
            onColorSelected = { color ->
                viewModel.bulkSetColor(color)
            },
            onDismiss = { showBulkColorPicker = false }
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "($count)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
