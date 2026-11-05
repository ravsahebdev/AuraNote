package com.premium.auranotes.ui.screens
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.premium.auranotes.data.Note
import com.premium.auranotes.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: NoteViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("editor?noteId=0") }, containerColor = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        topBar = { 
            TopAppBar(
                title = { Text("Aura Note", fontSize = 28.sp, fontWeight = FontWeight.Bold) }, 
                actions = {
                    IconButton(onClick = { navController.navigate("about") }) {
                        Icon(Icons.Default.Info, contentDescription = "About Aura Note")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) 
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { viewModel.updateSearchQuery(it) }, placeholder = { Text("Search notes...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant),
                singleLine = true
            )
            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No notes found. Tap + to create one.", color = Color.Gray) }
            } else {
                LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalItemSpacing = 12.dp, modifier = Modifier.fillMaxSize()) {
                    items(notes) { note -> NoteCard(note = note, onClick = { navController.navigate("editor?noteId=${note.id}") }) }
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(note.color)).clickable { onClick() }.padding(16.dp)) {
        if (note.title.isNotBlank()) { Text(text = note.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(modifier = Modifier.height(8.dp)) }
        if (note.content.isNotBlank()) { Text(text = note.content, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), maxLines = 6, overflow = TextOverflow.Ellipsis) }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.timestamp)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
}
