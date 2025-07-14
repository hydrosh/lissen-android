package org.grakovne.lissen.ui.screens.auto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.viewmodel.LibraryViewModel
import org.grakovne.lissen.ui.navigation.AppNavigationService

@Composable
fun AutoBookSelectionScreen(
    navController: AppNavigationService,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val books: List<Book> by libraryViewModel.library.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Select a Book",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn {
            items(books) { book ->
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            navController.showPlayer(
                                bookId = book.id,
                                bookTitle = book.title,
                                bookSubtitle = book.subtitle ?: ""
                            )
                        }
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}
