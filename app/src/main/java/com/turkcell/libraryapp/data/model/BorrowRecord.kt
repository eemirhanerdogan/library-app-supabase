package com.turkcell.libraryapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BorrowRecord(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("borrow_date") val borrowDate: String,
    @SerialName("return_date") val returnDate: String? = null,
    val status: String = "borrowed"
)
