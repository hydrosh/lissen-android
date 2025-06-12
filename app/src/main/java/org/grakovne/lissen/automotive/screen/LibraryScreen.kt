package org.grakovne.lissen.automotive.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import org.grakovne.lissen.automotive.LissenCarEntryPoint
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.content.LissenMediaProvider
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences

class LibraryScreen(
    carContext: CarContext,
    private val errorMessage: String? = null
) : Screen(carContext), DefaultLifecycleObserver {

    private lateinit var preferences: LissenSharedPreferences
    private lateinit var mediaProvider: LissenMediaProvider

    private var libraries = listOf<Library>()
    private var books = listOf<Book>()
    private var currentLibrary: Library? = null
    private var isLoading = true

    init {
        lifecycle.addObserver(this)
        
        if (errorMessage == null) {
            // Initialize dependencies
            val entryPoint = EntryPointAccessors.fromApplication(
                carContext,
                LissenCarEntryPoint::class.java
            )
            
            preferences = entryPoint.preferences()
            mediaProvider = entryPoint.mediaProvider()
        }
    }

    override fun onGetTemplate(): Template {
        return when {
            errorMessage != null -> {
                createErrorTemplate()
            }
            isLoading -> {
                createLoadingTemplate()
            }
            currentLibrary == null -> {
                createLibrarySelectionTemplate()
            }
            else -> {
                createBookListTemplate()
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (errorMessage == null) {
            loadLibraries()
        }
    }

    private fun createErrorTemplate(): Template {
        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle("Setup Required")
                            .addText(errorMessage ?: "Please configure the app on your phone")
                            .build()
                    )
                    .build()
            )
            .setHeaderAction(Action.APP_ICON)
            .setTitle("Lissen")
            .build()
    }

    private fun createLoadingTemplate(): Template {
        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle("Loading...")
                            .addText("Please wait while we load your audiobooks")
                            .build()
                    )
                    .build()
            )
            .setHeaderAction(Action.APP_ICON)
            .setTitle("Lissen")
            .build()
    }

    private fun createLibrarySelectionTemplate(): Template {
        val listBuilder = ItemList.Builder()
        libraries.forEach { library ->
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(library.title)
                    .setOnClickListener { 
                        selectLibrary(library)
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .setTitle("Select Library")
            .build()
    }

    private fun createBookListTemplate(): Template {
        val listBuilder = ItemList.Builder()

        books.take(10).forEach { book -> // Limit to 10 items for car safety
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(book.title)
                    .addText(book.author ?: "Unknown Author")                    
                    .setOnClickListener {
                        playBook(book)
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .setTitle(currentLibrary?.title ?: "Books")
            .build()
    }

    private fun loadLibraries() {
        lifecycleScope.launch {
            try {
                when (val result = mediaProvider.fetchLibraries()) {
                    is ApiResult.Success -> {
                        val libs = result.data
                        libraries = libs
                        
                        // If there's a preferred library, use it directly
                        val preferredLibrary = preferences.getPreferredLibrary()
                        if (preferredLibrary != null && libs.contains(preferredLibrary)) {
                            selectLibrary(preferredLibrary)
                        } else if (libs.size == 1) {
                            // If only one library, select it automatically
                            selectLibrary(libs.first())
                        } else {
                            isLoading = false
                            invalidate()
                        }
                    }
                    is ApiResult.Error -> {
                        isLoading = false
                        invalidate()
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                invalidate()
            }
        }
    }    private fun selectLibrary(library: Library) {
        currentLibrary = library
        loadBooks(library.id)
    }

    private fun loadBooks(libraryId: String) {
        lifecycleScope.launch {
            try {
                when (val result = mediaProvider.fetchBooks(libraryId, 20, 0)) {
                    is ApiResult.Success -> {
                        books = result.data.items
                        isLoading = false
                        invalidate()
                    }
                    is ApiResult.Error -> {
                        isLoading = false
                        invalidate()
                    }
                }            } catch (e: Exception) {
                isLoading = false
                invalidate()
            }
        }
    }

    private fun playBook(book: Book) {
        lifecycleScope.launch {
            try {
                when (val result = mediaProvider.fetchBook(book.id)) {
                    is ApiResult.Success -> {
                        val detailedBook = result.data
                        // Navigate to player screen
                        screenManager.push(PlayerScreen(carContext, detailedBook))
                    }
                    is ApiResult.Error -> {
                        // Handle error - could show a toast or error message
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
