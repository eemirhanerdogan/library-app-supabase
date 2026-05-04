package com.turkcell.libraryapp.data.repository

import com.turkcell.libraryapp.data.model.Book
import com.turkcell.libraryapp.data.model.BorrowRecord
import com.turkcell.libraryapp.data.model.Profile
import com.turkcell.libraryapp.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

class BookRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getAllBooks(): Result<List<Book>> = runCatching {
        supabase.postgrest["books"]
            .select()
            .decodeList<Book>()
    }

    suspend fun getBookById(id: String): Result<Book> = runCatching {
        supabase.postgrest["books"]
            .select { filter { eq("id", id) } }
            .decodeSingle<Book>()
    }

    suspend fun addBook(book: Book): Result<Unit> = runCatching {
        if (book.title.length < 3)
            throw Exception("Kitap adı en az 3 karakter olmalıdır")
        supabase.postgrest["books"].insert(book)
    }

    suspend fun updateBook(book: Book): Result<Unit> = runCatching {
        val id = book.id ?: throw Exception("Kitap ID bulunamadı")
        supabase.postgrest["books"].update(book) {
            filter { eq("id", id) }
        }
    }

    suspend fun deleteBook(id: String): Result<Unit> = runCatching {
        supabase.postgrest["books"].delete {
            filter { eq("id", id) }
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    suspend fun getProfile(userId: String): Profile? = runCatching {
        supabase.postgrest["profiles"]
            .select { filter { eq("user_id", userId) } }
            .decodeSingle<Profile>()
    }.getOrNull()

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    suspend fun borrowBook(book: Book, days: Int): Result<Unit> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("Kullanıcı girişi yapılmamış")
        val bookId = book.id ?: throw Exception("Kitap ID bulunamadı")
        
        if (days !in 1..5) throw Exception("Ödünç alma süresi 1 ile 5 gün arasında olmalıdır")
        
        // Veritabanından güncel kitap bilgisini çekelim
        val currentBook = getBookById(bookId).getOrThrow()
        if (currentBook.availableCopies <= 0) throw Exception("Bu kitap şu anda stokta yok.")

        // Aynı kullanıcı aynı kitabı aktif olarak zaten kiralamış mı kontrol et
        val activeRecords = supabase.postgrest["borrow_records"]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("book_id", bookId)
                    eq("status", "active")
                }
            }.decodeList<BorrowRecord>()

        if (activeRecords.isNotEmpty()) {
            throw Exception("Bu kitabı zaten ödünç aldınız.")
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val dueDate = dateFormat.format(calendar.time)

        val record = BorrowRecord(
            userId = userId,
            bookId = bookId,
            bookTitle = currentBook.title,
            bookAuthor = currentBook.author,
            dueDate = dueDate,
            status = "active"
        )

        // 1. Ödünç alma kaydını ekle
        supabase.postgrest["borrow_records"].insert(record)

        // 2. Kitap stok adedini azalt
        supabase.postgrest["books"].update(buildJsonObject {
            put("available_copies", currentBook.availableCopies - 1)
        }) {
            filter { eq("id", bookId) }
        }
    }

    suspend fun getBorrowRecordsForCurrentUser(): Result<List<BorrowRecord>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("Kullanıcı girişi yapılmamış")
        
        supabase.postgrest["borrow_records"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<BorrowRecord>()
            .sortedByDescending { it.borrowedAt }
    }

    suspend fun returnBook(record: BorrowRecord): Result<Unit> = runCatching {
        if (record.status != "active") return@runCatching

        // 1. Kaydı güncelle
        supabase.postgrest["borrow_records"].update(buildJsonObject {
            put("status", "returned")
            put("returned_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(Calendar.getInstance().time))
        }) {
            filter { eq("id", record.id ?: "") }
        }

        // 2. Kitap stok adedini artır
        val book = getBookById(record.bookId).getOrNull()
        if (book != null) {
            val newAvailableCopies = min(book.totalCopies, book.availableCopies + 1)
            supabase.postgrest["books"].update(buildJsonObject {
                put("available_copies", newAvailableCopies)
            }) {
                filter { eq("id", record.bookId) }
            }
        }
    }
}
