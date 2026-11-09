package com.premium.auranotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premium.auranotes.R
import com.premium.auranotes.data.FolderWithCount
import com.premium.auranotes.viewmodel.DrawerScreen

@Composable
fun AppNavigationDrawerContent(
    currentScreen: DrawerScreen,
    selectedFolderId: Long?,
    folders: List<FolderWithCount>,
    activeCount: Int,
    favoriteCount: Int,
    scheduledCount: Int,
    archivedCount: Int,
    trashCount: Int,
    onScreenSelect: (DrawerScreen) -> Unit,
    onFolderSelect: (Long) -> Unit,
    onNewFolderClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // App Branding Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
.background(colorResource(R.color.ic_launcher_background))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            // Primary Navigation Items
            item {
                DrawerItemRow(
                    icon = Icons.Outlined.Description,
                    label = stringResource(R.string.nav_all_notes),
                    count = activeCount,
                    isSelected = currentScreen == DrawerScreen.ALL_NOTES,
                    onClick = {
                        onScreenSelect(DrawerScreen.ALL_NOTES)
                        onCloseDrawer()
                    }
                )
                DrawerItemRow(
                    icon = Icons.Outlined.FavoriteBorder,
                    label = stringResource(R.string.nav_favorites),
                    count = favoriteCount,
                    isSelected = currentScreen == DrawerScreen.FAVORITES,
                    onClick = {
                        onScreenSelect(DrawerScreen.FAVORITES)
                        onCloseDrawer()
                    }
                )
                DrawerItemRow(
                    icon = Icons.Outlined.Notifications,
                    label = stringResource(R.string.nav_reminders),
                    count = scheduledCount,
                    isSelected = currentScreen == DrawerScreen.REMINDERS,
                    onClick = {
                        onScreenSelect(DrawerScreen.REMINDERS)
                        onCloseDrawer()
                    }
                )
                DrawerItemRow(
                    icon = Icons.Outlined.Archive,
                    label = stringResource(R.string.nav_archive),
                    count = archivedCount,
                    isSelected = currentScreen == DrawerScreen.ARCHIVE,
                    onClick = {
                        onScreenSelect(DrawerScreen.ARCHIVE)
                        onCloseDrawer()
                    }
                )
                DrawerItemRow(
                    icon = Icons.Outlined.Delete,
                    label = stringResource(R.string.nav_trash),
                    count = trashCount,
                    isSelected = currentScreen == DrawerScreen.TRASH,
                    onClick = {
                        onScreenSelect(DrawerScreen.TRASH)
                        onCloseDrawer()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            // Folders Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.nav_folders).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        letterSpacing = 1.sp
                    )
                    TextButton(
                        onClick = onNewFolderClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+ New",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // User Folders List
            if (folders.isEmpty()) {
                item {
                    Text(
                        text = "No folders created yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(folders) { folder ->
                    val isSelected = currentScreen == DrawerScreen.FOLDER && selectedFolderId == folder.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable {
                                onFolderSelect(folder.id)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = folder.iconEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = folder.name,
                            fontSize = 14.5.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (folder.noteCount > 0) {
                            Text(
                                text = folder.noteCount.toString(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }

            // Bottom Settings & About
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                DrawerItemRow(
                    icon = Icons.Outlined.Settings,
                    label = stringResource(R.string.nav_settings),
                    count = null,
                    isSelected = false,
                    onClick = {
                        onSettingsClick()
                        onCloseDrawer()
                    }
                )
                DrawerItemRow(
                    icon = Icons.Outlined.Info,
                    label = stringResource(R.string.nav_about),
                    count = null,
                    isSelected = false,
                    onClick = {
                        onAboutClick()
                        onCloseDrawer()
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerItemRow(
    icon: ImageVector,
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (count != null && count > 0) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
