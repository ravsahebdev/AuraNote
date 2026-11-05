package com.premium.auranotes.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.premium.auranotes.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(navController: NavController, noteId: Int, viewModel: NoteViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState()
    val existingNote = notes.find { it.id == noteId }
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var color by remember { mutableStateOf(existingNote?.color ?: 0xFF1E1E1E) }
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) { onDispose { viewModel.saveNote(noteId, title, content, isPinned, color) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) { Icon(if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, "Pin") }
                    if (noteId != 0 && existingNote != null) { IconButton(onClick = { viewModel.deleteNote(existingNote); navController.popBackStack() }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) } }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(scrollState)) {
            TextField(value = title, onValueChange = { title = it }, placeholder = { Text("Title", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = content, onValueChange = { content = it }, placeholder = { Text("Start typing...", fontSize = 18.sp, color = Color.Gray) }, modifier = Modifier.fillMaxWidth().weight(1f, fill = false), textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 28.sp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
