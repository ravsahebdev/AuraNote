package com.premium.auranotes.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.premium.auranotes.R
import com.premium.auranotes.data.SortOrder
import com.premium.auranotes.data.ThemeMode
import com.premium.auranotes.data.ViewMode
import com.premium.auranotes.ui.components.ColorPickerSheet
import com.premium.auranotes.util.NoteExportUtils
import com.premium.auranotes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val userPrefs by viewModel.userPreferences.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showViewModeDialog by remember { mutableStateOf(false) }
    var showSortOrderDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showClearTrashConfirm by remember { mutableStateOf(false) }

    // Export JSON SAF launcher
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val notes = viewModel.uiState.value.notes
                    val folders = viewModel.foldersWithCount.value.map {
                        com.premium.auranotes.data.Folder(
                            id = it.id,
                            name = it.name,
                            iconEmoji = it.iconEmoji,
                            createdAt = it.createdAt
                        )
                    }
                    val json = NoteExportUtils.exportToJson(notes, folders)
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    outputStream?.write(json.toByteArray())
                    outputStream?.close()
                    snackbarHostState.showSnackbar("Backup exported successfully")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Export failed: ${e.message}")
                }
            }
        }
    }

    // Import JSON SAF launcher
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                reader.close()
                viewModel.importBackup(json) { success ->
                    scope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("Backup restored successfully")
                        } else {
                            snackbarHostState.showSnackbar("Failed to restore backup")
                        }
                    }
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Error reading file") }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Appearance section
            item {
                SettingsSectionHeader("Appearance")

                SettingsClickableItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_theme),
                    subtitle = when (userPrefs.themeMode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                    },
                    onClick = { showThemeDialog = true }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsToggleItem(
                        icon = Icons.Outlined.ColorLens,
                        title = stringResource(R.string.settings_dynamic_color),
                        subtitle = stringResource(R.string.settings_dynamic_color_desc),
                        checked = userPrefs.dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                }

                SettingsClickableItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.settings_default_color),
                    subtitle = "Customize the starting background tone for new notes",
                    onClick = { showColorPicker = true }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.GridView,
                    title = stringResource(R.string.settings_default_view),
                    subtitle = if (userPrefs.viewMode == ViewMode.GRID) "Grid" else "List",
                    onClick = { showViewModeDialog = true }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Sort,
                    title = stringResource(R.string.action_sort),
                    subtitle = when (userPrefs.sortOrder) {
                        SortOrder.DATE_MODIFIED -> stringResource(R.string.sort_date_modified)
                        SortOrder.DATE_CREATED -> stringResource(R.string.sort_date_created)
                        SortOrder.TITLE_AZ -> stringResource(R.string.sort_title_az)
                        SortOrder.TITLE_ZA -> stringResource(R.string.sort_title_za)
                    },
                    onClick = { showSortOrderDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Security Section
            item {
                SettingsSectionHeader("Security & Privacy")

                SettingsToggleItem(
                    icon = Icons.Outlined.Fingerprint,
                    title = stringResource(R.string.settings_app_lock),
                    subtitle = stringResource(R.string.settings_app_lock_desc),
                    checked = userPrefs.isAppLockEnabled,
                    onCheckedChange = { viewModel.setAppLock(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Data & Storage
            item {
                SettingsSectionHeader("Data & Storage")

                SettingsClickableItem(
                    icon = Icons.Outlined.UploadFile,
                    title = stringResource(R.string.settings_export_json),
                    subtitle = "Save a complete offline backup file of all notes and folders",
                    onClick = {
                        val fileName = "AuraNotes_Backup_${System.currentTimeMillis()}.json"
                        exportJsonLauncher.launch(fileName)
                    }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Download,
                    title = stringResource(R.string.settings_import_json),
                    subtitle = "Restore notes and folders from a previously exported JSON backup",
                    onClick = {
                        importJsonLauncher.launch("application/json")
                    }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.DeleteSweep,
                    title = stringResource(R.string.action_clear_trash),
                    subtitle = "Permanently remove all items currently stored in Trash",
                    onClick = { showClearTrashConfirm = true }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // About
            item {
                SettingsSectionHeader("About")

                SettingsClickableItem(
                    icon = Icons.Outlined.Info,
                    title = "About Aura Note",
                    subtitle = "Version, developer notes and privacy information",
                    onClick = { navController.navigate("about") }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_system),
                        isSelected = userPrefs.themeMode == ThemeMode.SYSTEM,
                        onClick = {
                            viewModel.setThemeMode(ThemeMode.SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_light),
                        isSelected = userPrefs.themeMode == ThemeMode.LIGHT,
                        onClick = {
                            viewModel.setThemeMode(ThemeMode.LIGHT)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_dark),
                        isSelected = userPrefs.themeMode == ThemeMode.DARK,
                        onClick = {
                            viewModel.setThemeMode(ThemeMode.DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }

    // View Mode Dialog
    if (showViewModeDialog) {
        AlertDialog(
            onDismissRequest = { showViewModeDialog = false },
            title = { Text(stringResource(R.string.settings_default_view), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeOptionRow(
                        label = "Grid View",
                        isSelected = userPrefs.viewMode == ViewMode.GRID,
                        onClick = {
                            viewModel.setViewMode(ViewMode.GRID)
                            showViewModeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        label = "List View",
                        isSelected = userPrefs.viewMode == ViewMode.LIST,
                        onClick = {
                            viewModel.setViewMode(ViewMode.LIST)
                            showViewModeDialog = false
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Sort Dialog
    if (showSortOrderDialog) {
        AlertDialog(
            onDismissRequest = { showSortOrderDialog = false },
            title = { Text(stringResource(R.string.action_sort), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeOptionRow(
                        label = stringResource(R.string.sort_date_modified),
                        isSelected = userPrefs.sortOrder == SortOrder.DATE_MODIFIED,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DATE_MODIFIED)
                            showSortOrderDialog = false
                        }
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.sort_date_created),
                        isSelected = userPrefs.sortOrder == SortOrder.DATE_CREATED,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DATE_CREATED)
                            showSortOrderDialog = false
                        }
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.sort_title_az),
                        isSelected = userPrefs.sortOrder == SortOrder.TITLE_AZ,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.TITLE_AZ)
                            showSortOrderDialog = false
                        }
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.sort_title_za),
                        isSelected = userPrefs.sortOrder == SortOrder.TITLE_ZA,
                        onClick = {
                            viewModel.setSortOrder(SortOrder.TITLE_ZA)
                            showSortOrderDialog = false
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Default note color picker
    if (showColorPicker) {
        ColorPickerSheet(
            selectedColor = userPrefs.defaultColor,
            onColorSelected = { color ->
                scope.launch {
                    val prefs = com.premium.auranotes.data.PreferencesManager(context)
                    prefs.setDefaultColor(color)
                }
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Clear trash confirm dialog
    if (showClearTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showClearTrashConfirm = false },
            title = { Text(stringResource(R.string.action_clear_trash), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.settings_clear_trash_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTrash()
                        showClearTrashConfirm = false
                        scope.launch { snackbarHostState.showSnackbar("Trash cleared") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_clear_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearTrashConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ThemeOptionRow(
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
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
