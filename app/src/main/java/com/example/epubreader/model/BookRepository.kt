package com.example.epubreader.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BookRepository(private val bookDao: BookDao) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val favoriteBooks: Flow<List<Book>> = bookDao.getFavoriteBooks()
    val toReadBooks: Flow<List<Book>> = bookDao.getToReadBooks()
    val completedBooks: Flow<List<Book>> = bookDao.getCompletedBooks()
    val lastOpenedBook: Flow<Book> = bookDao.lastOpenedBook()


    fun getBookById(id: Int): Flow<Book> {
        return bookDao.getBookById(id)
    }

    fun getBookByUri(uri: String): Flow<Book?> {
        return bookDao.getBookByUri(uri)
    }

    fun getLastPage(bookUri: String): Flow<Int> {
        return bookDao.getLastPage(bookUri)
    }

    fun getTotalPages(uri: String): Flow<Int> {
        return bookDao.getTotalPage(uri)
    }

    suspend fun insertBook(book: Book): Long? {
        val existingBook = bookDao.getBookByUri(book.uri).firstOrNull()
        return if (existingBook == null) {
            bookDao.insertBook(book)
        } else {
            bookDao.updateBook(existingBook.copy(timestamp = System.currentTimeMillis()))
            null
        }
    }

    suspend fun updateBookTime(book: Book) {
        bookDao.updateBook(book.copy(timestamp = System.currentTimeMillis()))
    }
    suspend fun updateBookTitle(book: Book , title: String) {
        bookDao.updateBook(book.copy(title = title))
    }

    suspend fun updateBookAuthor(book: Book , author: String) {
        bookDao.updateBook(book.copy(author = author))
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    suspend fun toggleFavorite(bookUri: String, isFavorite: Boolean) {
        bookDao.updateFavoriteStatus(bookUri, if (isFavorite) 1 else 0)
    }

    suspend fun toggleToRead(bookUri: String, isToRead: Boolean) {
        bookDao.updateToReadStatus(bookUri, if (isToRead) 1 else 0)
    }
    suspend fun toggleDoneReading(bookUri: String, isDoneReading: Boolean) {
        bookDao.updateDoneReadingStatus(bookUri, if (isDoneReading) 1 else 0)
    }
    suspend fun updateLastPage(bookUri: String, lastPage: Int) {
        bookDao.updateLastPage(bookUri, lastPage)
    }



}