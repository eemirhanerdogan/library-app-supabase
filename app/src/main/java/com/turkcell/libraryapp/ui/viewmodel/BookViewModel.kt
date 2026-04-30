package com.turkcell.libraryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.libraryapp.data.model.Book
import com.turkcell.libraryapp.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class BookViewModel : ViewModel() {
    private val repository = BookRepository()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtrelenmiş kitap listesi - Uygulama tarafında normalize edilmiş arama
    val books: StateFlow<List<Book>> = combine(_allBooks, _searchQuery) { books, query ->
        if (query.isBlank()) {
            books
        } else {
            val normalizedQuery = normalizeString(query)
            books.filter { book ->
                normalizeString(book.title).contains(normalizedQuery) ||
                normalizeString(book.author).contains(normalizedQuery) ||
                normalizeString(book.category).contains(normalizedQuery)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadBooks()
    }

    private fun normalizeString(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
            .replace("İ", "i")
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllBooks()
                .onSuccess { 
                    _allBooks.value = it 
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun searchBooks(query: String) {
        _searchQuery.value = query
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteBook(id)
                .onSuccess {
                    loadBooks()
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun updateBook(book: Book, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateBook(book)
                .onSuccess {
                    loadBooks()
                    onSuccess()
                    _error.value = null
                }
                .onFailure { 
                    onError(it.message ?: "Güncelleme başarısız")
                    _error.value = it.message 
                }
            _isLoading.value = false
        }
    }
}
