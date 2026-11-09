package com.premium.auranotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    onShareText: () -> Unit,
    onShareMarkdown: () -> Unit,
    onSharePdf: () -> Unit,
    onExportMarkdownFile: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.action_share),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            ShareOptionRow(
                icon = Icons.Outlined.TextFields,
                title = stringResource(R.string.share_as_text),
                subtitle = "Share as plain text string",
                onClick = {
                    onShareText()
                    onDismiss()
                }
            )

            ShareOptionRow(
                icon = Icons.Outlined.Code,
                title = stringResource(R.string.share_as_markdown),
                subtitle = "Formatted with Markdown headings, lists & checkboxes",
                onClick = {
                    onShareMarkdown()
                    onDismiss()
                }
            )

            ShareOptionRow(
                icon = Icons.Outlined.PictureAsPdf,
                title = stringResource(R.string.share_as_pdf),
                subtitle = "Generate clean formatted PDF document",
                onClick = {
                    onSharePdf()
                    onDismiss()
                }
            )

            ShareOptionRow(
                icon = Icons.Outlined.FileDownload,
                title = stringResource(R.string.export_markdown_file),
                subtitle = "Save a .md file to your device storage",
                onClick = {
                    onExportMarkdownFile()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
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
    }
}
