package com.premium.auranotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorBottomBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onAddBlockClick: () -> Unit,
    onTurnIntoClick: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onLinkClick: () -> Unit,
    onImageClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onIndentClick: () -> Unit,
    onOutdentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // [+] Add block
            IconButton(
                onClick = onAddBlockClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Block",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // [Turn Into] pill
            Surface(
                onClick = onTurnIntoClick,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(30.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Turn into",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            VerticalDivider(
                modifier = Modifier.height(20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.width(4.dp))

            // B
            IconButton(onClick = onBoldClick, modifier = Modifier.size(36.dp)) {
                Text("B", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            // I
            IconButton(onClick = onItalicClick, modifier = Modifier.size(36.dp)) {
                Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            // U
            IconButton(onClick = onUnderlineClick, modifier = Modifier.size(36.dp)) {
                Text("U", style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            // S
            IconButton(onClick = onStrikethroughClick, modifier = Modifier.size(36.dp)) {
                Text("S", style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            // Link
            IconButton(onClick = onLinkClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Link, contentDescription = "Add Link", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }

            // Image
            IconButton(onClick = onImageClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Image, contentDescription = "Insert Image", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.width(4.dp))
            VerticalDivider(
                modifier = Modifier.height(20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Indent / Outdent
            IconButton(onClick = onOutdentClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.FormatIndentDecrease, contentDescription = "Outdent", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onIndentClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.FormatIndentIncrease, contentDescription = "Indent", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }

            // Undo
            IconButton(onClick = onUndoClick, enabled = canUndo, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Undo,
                    contentDescription = "Undo",
                    modifier = Modifier.size(18.dp),
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Redo
            IconButton(onClick = onRedoClick, enabled = canRedo, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Redo,
                    contentDescription = "Redo",
                    modifier = Modifier.size(18.dp),
                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Hide keyboard
            IconButton(
                onClick = { keyboardController?.hide() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.KeyboardHide,
                    contentDescription = "Hide Keyboard",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
