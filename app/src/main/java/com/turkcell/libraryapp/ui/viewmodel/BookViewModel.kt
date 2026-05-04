package com.turkcell.libraryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.libraryapp.data.model.Book
import com.turkcell.libraryapp.data.model.BorrowRecord
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

    // Borrow states
    private val _borrowRecords = MutableStateFlow<List<BorrowRecord>>(emptyList())
    val borrowRecords: StateFlow<List<BorrowRecord>> = _borrowRecords.asStateFlow()

    private val _isBorrowing = MutableStateFlow(false)
    val isBorrowing: StateFlow<Boolean> = _isBorrowing.asStateFlow()

    private val _borrowError = MutableStateFlow<String?>(null)
    val borrowError: StateFlow<String?> = _borrowError.asStateFlow()

    private val _borrowSuccessMessage = MutableStateFlow<String?>(null)
    val borrowSuccessMessage: StateFlow<String?> = _borrowSuccessMessage.asStateFlow()

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
        loadBorrowRecords()
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

    fun loadBorrowRecords() {
        viewModelScope.launch {
            repository.getBorrowRecordsForCurrentUser()
                .onSuccess { 
                    _borrowRecords.value = it 
                    _borrowError.value = null
                }
                .onFailure { _borrowError.value = it.message }
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

    fun borrowBook(book: Book, days: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isBorrowing.value = true
            _borrowError.value = null
            repository.borrowBook(book, days)
                .onSuccess {
                    _borrowSuccessMessage.value = "${book.title} ödünç alındı"
                    loadBooks()
                    loadBorrowRecords()
                    onSuccess()
                }
                .onFailure {
                    _borrowError.value = it.message
                    onError(it.message ?: "Ödünç alma işlemi başarısız")
                }
            _isBorrowing.value = false
        }
    }

    fun returnBook(record: BorrowRecord) {
        viewModelScope.launch {
            _isBorrowing.value = true
            repository.returnBook(record)
                .onSuccess {
                    loadBooks()
                    loadBorrowRecords()
                }
                .onFailure { _borrowError.value = it.message }
            _isBorrowing.value = false
        }
    }

    fun clearBorrowMessages() {
        _borrowSuccessMessage.value = null
        _borrowError.value = null
    }
}
