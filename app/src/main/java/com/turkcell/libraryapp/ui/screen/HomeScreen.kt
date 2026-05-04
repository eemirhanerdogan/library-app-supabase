package com.turkcell.libraryapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkcell.libraryapp.data.model.Book
import com.turkcell.libraryapp.ui.screen.components.BookCard
import com.turkcell.libraryapp.ui.viewmodel.AuthViewModel
import com.turkcell.libraryapp.ui.viewmodel.BookViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    bookViewModel: BookViewModel,
    onNavigateToBorrowRecords: () -> Unit
) {
    val profileState by authViewModel.profile.collectAsState()
    val books by bookViewModel.books.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val isBorrowing by bookViewModel.isBorrowing.collectAsState()
    val searchQuery by bookViewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var bookToBorrow by remember { mutableStateOf<Book?>(null) }

    if (bookToEdit != null) {
        EditBookDialog(
            book = bookToEdit!!,
            onDismiss = { bookToEdit = null },
            onConfirm = { updatedBook ->
                bookViewModel.updateBook(
                    book = updatedBook,
                    onSuccess = {
                        bookToEdit = null
                        Toast.makeText(context, "Kitap güncellendi", Toast.LENGTH_SHORT).show()
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (bookToBorrow != null) {
        BorrowBookDialog(
            book = bookToBorrow!!,
            onDismiss = { bookToBorrow = null },
            onConfirm = { days ->
                bookViewModel.borrowBook(
                    book = bookToBorrow!!,
                    days = days,
                    onSuccess = {
                        bookToBorrow = null
                        Toast.makeText(context, "Kitap ödünç alındı", Toast.LENGTH_SHORT).show()
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kütüphane",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = profileState?.fullName ?: "Kullanıcı",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row {
                            IconButton(onClick = onNavigateToBorrowRecords) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Kiralamalarım",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { authViewModel.signOut() }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Çıkış Yap",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { bookViewModel.searchBooks(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Kitap, yazar veya kategori ara...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && books.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (books.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Kitap bulunamadı.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                ) {
                    items(books, key = { it.id ?: "" }) { book ->
                        BookCard(
                            book = book,
                            onDeleteClick = { book.id?.let { bookViewModel.deleteBook(it) } },
                            onEditClick = { bookToEdit = book },
                            onBorrowClick = { bookToBorrow = book },
                            isBorrowing = isBorrowing
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var days by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Ödünç Al: ${book.title}") },
        text = {
            Column {
                Text("Yazar: ${book.author}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Kaç gün ödünç almak istiyorsunuz? (Maks 5)")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..5).forEach { day ->
                        FilterChip(
                            selected = days == day,
                            onClick = { days = day },
                            label = { Text(day.toString()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(days) }) {
                Text("Onayla")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun EditBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: (Book) -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var isbn by remember { mutableStateOf(book.isbn) }
    var category by remember { mutableStateOf(book.category) }
    var pageCount by remember { mutableStateOf(book.pageCount.toString()) }
    var totalCopies by remember { mutableStateOf(book.totalCopies.toString()) }
    var availableCopies by remember { mutableStateOf(book.availableCopies.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Kitabı Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Kitap Adı") }, modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Yazar") }, modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(value = isbn, onValueChange = { isbn = it }, label = { Text("ISBN") }, modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(
                    value = pageCount,
                    onValueChange = { pageCount = it },
                    label = { Text("Sayfa Sayısı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = totalCopies,
                    onValueChange = { totalCopies = it },
                    label = { Text("Toplam Kopya") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = availableCopies,
                    onValueChange = { availableCopies = it },
                    label = { Text("Mevcut Kopya") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    book.copy(
                        title = title,
                        author = author,
                        isbn = isbn,
                        category = category,
                        pageCount = pageCount.toIntOrNull() ?: book.pageCount,
                        totalCopies = totalCopies.toIntOrNull() ?: book.totalCopies,
                        availableCopies = availableCopies.toIntOrNull() ?: book.availableCopies
                    )
                )
            }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
