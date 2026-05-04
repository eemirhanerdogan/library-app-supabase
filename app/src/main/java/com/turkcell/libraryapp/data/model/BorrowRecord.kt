package com.turkcell.libraryapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BorrowRecord(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("book_title") val bookTitle: String,
    @SerialName("book_author") val bookAuthor: String,
    @SerialName("borrowed_at") val borrowedAt: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("returned_at") val returnedAt: String? = null,
    val status: String = "active"
)
