package com.premium.auranotes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.premium.auranotes.data.Block
import com.premium.auranotes.data.BlockType

@Composable
fun BlockItem(
    block: Block,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onTextChange: (String) -> Unit,
    onCheckedToggle: () -> Unit,
    onEnter: () -> Unit,
    onBackspaceOnEmpty: () -> Unit,
    onSelectBlockType: (BlockType) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSlashPopup by remember { mutableStateOf(false) }
    var showToggleContent by remember { mutableStateOf(true) }

    val indentPadding = (block.indent * 16).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indentPadding, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Block action / prefix icon based on block type
        when (block.type) {
            BlockType.TODO -> {
                Checkbox(
                    checked = block.checked,
                    onCheckedChange = { onCheckedToggle() },
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 6.dp)
                )
            }
            BlockType.BULLET -> {
                Box(
                    modifier = Modifier
                        .padding(top = 9.dp, end = 10.dp, start = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                )
            }
            BlockType.NUMBERED -> {
                Text(
                    text = "${index + 1}.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                )
            }
            BlockType.TOGGLE -> {
                IconButton(
                    onClick = { showToggleContent = !showToggleContent },
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = if (showToggleContent) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            BlockType.CALLOUT -> {
                Text(
                    text = "💡",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                )
            }
            BlockType.DIVIDER -> {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                )
                return@Row
            }
            BlockType.IMAGE -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (block.text.isNotBlank()) {
                        AsyncImage(
                            model = block.text,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                return@Row
            }
            else -> {
                // TEXT, HEADING1, HEADING2, HEADING3, QUOTE, CODE: no leading icon
            }
        }

        // Text styling based on block type
        val textStyle = when (block.type) {
            BlockType.HEADING1 -> TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            BlockType.HEADING2 -> TextStyle(
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            BlockType.HEADING3 -> TextStyle(
                fontSize = 17.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            BlockType.QUOTE -> TextStyle(
                fontSize = 15.5.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
            BlockType.CODE -> TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            BlockType.TODO -> TextStyle(
                fontSize = 15.sp,
                textDecoration = if (block.checked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (block.checked) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground
            )
            else -> TextStyle(
                fontSize = 15.5.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 22.sp
            )
        }

        val containerModifier = when (block.type) {
            BlockType.QUOTE -> Modifier
                .fillMaxWidth()
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
                )
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            BlockType.CODE -> Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(12.dp)
            BlockType.CALLOUT -> Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(10.dp)
            else -> Modifier.fillMaxWidth()
        }

        Box(modifier = containerModifier.weight(1f)) {
            BasicTextField(
                value = block.text,
                onValueChange = { newText ->
                    if (newText.startsWith("/") && (newText.length == 1 || !newText.contains(" "))) {
                        showSlashPopup = true
                    } else if (!newText.startsWith("/")) {
                        showSlashPopup = false
                    }
                    onTextChange(newText)
                },
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            if (keyEvent.key == Key.Enter) {
                                onEnter()
                                true
                            } else if (keyEvent.key == Key.Backspace && block.text.isEmpty()) {
                                onBackspaceOnEmpty()
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    },
                decorationBox = { innerTextField ->
                    if (block.text.isEmpty() && isFocused) {
                        Text(
                            text = when (block.type) {
                                BlockType.HEADING1 -> "Heading 1"
                                BlockType.HEADING2 -> "Heading 2"
                                BlockType.HEADING3 -> "Heading 3"
                                BlockType.TODO -> "To-do"
                                BlockType.BULLET -> "List"
                                BlockType.NUMBERED -> "List"
                                BlockType.QUOTE -> "Empty quote"
                                BlockType.CODE -> "Code snippet"
                                else -> "Type '/' for commands"
                            },
                            style = textStyle.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        )
                    }
                    innerTextField()
                }
            )

            if (showSlashPopup) {
                SlashCommandPopup(
                    filterText = block.text,
                    onSelectBlockType = { selectedType ->
                        showSlashPopup = false
                        onSelectBlockType(selectedType)
                        onTextChange("")
                    },
                    onDismiss = { showSlashPopup = false }
                )
            }
        }

        // Focused quick block actions: reorder and delete buttons
        if (isFocused && !showSlashPopup) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
