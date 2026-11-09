package com.premium.auranotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premium.auranotes.R
import com.premium.auranotes.data.BlockType

data class BlockItemDescriptor(
    val type: BlockType,
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector
)

val AvailableBlocks = listOf(
    BlockItemDescriptor(BlockType.TEXT, R.string.block_text, R.string.block_desc_text, Icons.Outlined.TextFields),
    BlockItemDescriptor(BlockType.TODO, R.string.block_todo, R.string.block_desc_todo, Icons.Outlined.CheckBox),
    BlockItemDescriptor(BlockType.HEADING1, R.string.block_heading1, R.string.block_desc_heading1, Icons.Default.Title),
    BlockItemDescriptor(BlockType.HEADING2, R.string.block_heading2, R.string.block_desc_heading2, Icons.Outlined.FormatSize),
    BlockItemDescriptor(BlockType.HEADING3, R.string.block_heading3, R.string.block_desc_heading3, Icons.Outlined.ShortText),
    BlockItemDescriptor(BlockType.BULLET, R.string.block_bullet, R.string.block_desc_bullet, Icons.Outlined.FormatListBulleted),
    BlockItemDescriptor(BlockType.NUMBERED, R.string.block_numbered, R.string.block_desc_numbered, Icons.Outlined.FormatListNumbered),
    BlockItemDescriptor(BlockType.TOGGLE, R.string.block_toggle, R.string.block_desc_toggle, Icons.Outlined.ArrowDropDownCircle),
    BlockItemDescriptor(BlockType.QUOTE, R.string.block_quote, R.string.block_desc_quote, Icons.Outlined.FormatQuote),
    BlockItemDescriptor(BlockType.CODE, R.string.block_code, R.string.block_desc_code, Icons.Outlined.Code),
    BlockItemDescriptor(BlockType.CALLOUT, R.string.block_callout, R.string.block_desc_callout, Icons.Outlined.Lightbulb),
    BlockItemDescriptor(BlockType.DIVIDER, R.string.block_divider, R.string.block_desc_divider, Icons.Outlined.HorizontalRule),
    BlockItemDescriptor(BlockType.IMAGE, R.string.block_image, R.string.block_desc_image, Icons.Outlined.Image)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicBlocksSheet(
    isTurnIntoMode: Boolean = false,
    onSelectBlockType: (BlockType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = if (isTurnIntoMode) "Turn Into" else "Basic Blocks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isTurnIntoMode) {
                // List style
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(AvailableBlocks) { block ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectBlockType(block.type)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = block.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(block.titleRes),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(block.descRes),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            } else {
                // Grid style like Notion
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    items(AvailableBlocks) { block ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable {
                                    onSelectBlockType(block.type)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = block.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(block.titleRes),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(block.descRes),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
