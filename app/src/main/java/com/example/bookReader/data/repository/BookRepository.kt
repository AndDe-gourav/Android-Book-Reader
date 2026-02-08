package com.example.bookReader.data.repository

@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val bookStateDao: BookStateDao,
    private val collectionDao: CollectionDao,
    private val bookCollectionDao: BookCollectionDao,
    private val sessionDao: ReadingSessionDao,
    @ApplicationContext private val context: Context
) {


    fun getLibrary(): Flow<List<BookEntity>> =
        bookDao.getAllBooks()

    suspend fun addBook(
        title: String,
        author: String?,
        uri: Uri,
        totalPages: Int
    ): Long {
        val bookId = bookDao.insertBook(
            BookEntity(
                title = title,
                author = author,
                uri = uri.toString(),
                totalPages = totalPages
            )
        )

        bookStateDao.insertState(
            BookStateEntity(
                bookId = bookId,
                status = ReadingStatus.TO_READ
            )
        )

        return bookId
    }


    fun openPdf(bookId: Long): InputStream {
        val book = runBlocking {
            bookDao.getBookById(bookId)
        } ?: throw IllegalStateException("Book not found")

        return context.contentResolver
            .openInputStream(Uri.parse(book.uri))
            ?: throw FileNotFoundException()
    }


    suspend fun updateProgress(bookId: Long, page: Int, totalPages: Int) {
        val status =
            if (page >= totalPages - 1) ReadingStatus.COMPLETED
            else ReadingStatus.READING

        bookStateDao.updateProgress(bookId, page, status)
    }

    suspend fun saveSession(
        bookId: Long,
        startTime: Long,
        endTime: Long,
        startPage: Int,
        endPage: Int
    ) {
        sessionDao.insertSession(
            ReadingSessionEntity(
                bookId = bookId,
                startTime = startTime,
                endTime = endTime,
                startPage = startPage,
                endPage = endPage
            )
        )
    }


    suspend fun createCollection(name: String): Long =
        collectionDao.insertCollection(CollectionEntity(name))

    suspend fun addBookToCollection(bookId: Long, collectionId: Long) {
        bookCollectionDao.addBookToCollection(
            BookCollectionCrossRef(bookId, collectionId)
        )
    }


    suspend fun setFavorite(bookId: Long, favorite: Boolean) {
        bookStateDao.setFavorite(bookId, favorite)
    }
}
