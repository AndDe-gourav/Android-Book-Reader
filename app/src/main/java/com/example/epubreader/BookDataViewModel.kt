package com.example.epubreader

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.epubreader.model.bookStorage.Book
import com.example.epubreader.model.bookStorage.BookRepository
import kotlinx.coroutines.Job
import com.shockwave.pdfium.PdfDocument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BookDataViewModel(
    application: Application,
    private val repository: BookRepository
    ) : AndroidViewModel(application) {

    private val metadataExtractor = MetadataExtractor(application.applicationContext)

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks.asStateFlow()

    private val _favoriteBooks = MutableStateFlow<List<Book>>(emptyList())
    val favoriteBooks: StateFlow<List<Book>> = _favoriteBooks.asStateFlow()

    private val _toReadBooks = MutableStateFlow<List<Book>>(emptyList())
    val toReadBooks: StateFlow<List<Book>> = _toReadBooks.asStateFlow()

    private val _allCollections = MutableStateFlow<List<Book>>(emptyList())

    private val _listOfCollections = MutableStateFlow<List<String>>(emptyList())
    val listOfCollections: StateFlow<List<String>> = _listOfCollections.asStateFlow()

    private val _particularCollection = MutableStateFlow<List<List<Book>>>(emptyList())
    val particularCollection: StateFlow<List<List<Book>>> = _particularCollection.asStateFlow()

    private val _completedBooks = MutableStateFlow<List<Book>>(emptyList())
    val completedBooks: StateFlow<List<Book>> = _completedBooks.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _lastOpenedBook = MutableStateFlow<Book?>(null)
    val lastOpenedBook: StateFlow<Book?> = _lastOpenedBook.asStateFlow()

    private val _currentBookShelf =  MutableStateFlow("Recent")
    val currentBookShelf: StateFlow<String> = _currentBookShelf.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _lastPage = MutableStateFlow(0)
    val lastPage: StateFlow<Int> = _lastPage.asStateFlow()

    private val _toc = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val toc: StateFlow<Map<String, List<String>>> = _toc.asStateFlow()

    private val _tocPage = MutableStateFlow<Map<String, Int>>(emptyMap())
    val tocPage: StateFlow<Map<String, Int>> = _tocPage.asStateFlow()

    private val _childTocPage = MutableStateFlow<Map<String, Int>>(emptyMap())
    val childTocPage: StateFlow<Map<String, Int>> = _childTocPage.asStateFlow()

    init {
        viewModelScope.launch {
            loadAllBooksData()
            delay(300)
            firstSelectedBook()
        }
    }

    private fun loadAllBooksData() {
        viewModelScope.launch {
            val booksFlow = combine(
                repository.allBooks,
                repository.favoriteBooks,
                repository.toReadBooks,
                repository.completedBooks
            ) { all, fav, toRead, completed ->
                _allBooks.value = all
                _favoriteBooks.value = fav
                _toReadBooks.value = toRead
                _completedBooks.value = completed
            }

            val otherDataFlow = combine(
                repository.lastOpenedBook,
                repository.allCollections
            ) { lastBook, allCollections ->
                _lastOpenedBook.value = lastBook
                _allCollections.value = allCollections
                Log.d("lastOpenedBook", "${lastOpenedBook.value}")
            }

            launch { booksFlow.collectLatest {} }
            launch { otherDataFlow.collectLatest {} }
        }
    }



    private var selectBookJob: Job? = null

    fun selectBook(bookUri: String) {
        selectBookJob?.cancel()

        selectBookJob = viewModelScope.launch {
            repository.getBookByUri(bookUri).collectLatest { book ->
                if (book != null) {
                    _selectedBook.value = book
                }
            }
        }
        Log.d("selectedBook", "${selectedBook.value}")
    }

    fun addBook(uri: Uri) {
        viewModelScope.launch {
            try {
                val metadata = metadataExtractor.extractPdfMetadata(uri)

                val newBook = Book(
                    title = "Untitled",
                    uri = uri.toString(),
                    author = "Unknown Author",
                    bookCover = metadata.coverImage,
                    favourite = 0,
                    toRead = 0,
                    collection = "",
                    doneReading = 0,
                    lastPage = 0,
                    totalPages = metadata.pageCount,
                    timestamp = System.currentTimeMillis(),
                )
                repository.insertBook(newBook)
                selectBook(newBook.uri)



            } catch (_: Exception) {

            }
        }
    }

    fun changeCurrentBookShelf(shelf: String) {
        _currentBookShelf.value = shelf
    }

    fun updateBookTime(book: Book) {
        viewModelScope.launch {
            repository.updateBookTime(book)
        }
    }

    fun updateBookTitle(book: Book, title: String) {
        viewModelScope.launch {
            repository.updateBookTitle(book, title)
        }
    }

    fun updateBookAuthor(book: Book, author: String) {
        viewModelScope.launch {
            repository.updateBookAuthor(book, author)
        }
    }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            val newFavoriteStatus = book.favourite != 1
            repository.toggleFavorite(book.uri, newFavoriteStatus)
        }
    }

    fun toggleToRead(book: Book) {
        viewModelScope.launch {
            val newToReadStatus = book.toRead != 1
            repository.toggleToRead(book.uri, newToReadStatus)
        }
    }

    fun toggleDoneReading(book: Book) {
        viewModelScope.launch {
            val newDoneReadingStatus = book.doneReading != 1
            repository.toggleDoneReading(book.uri, newDoneReadingStatus)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repository.deleteBook(book)
            if(_lastOpenedBook.value?.uri == book.uri){
                _lastOpenedBook.value = repository.lastOpenedBook.firstOrNull()
                _selectedBook.value = _lastOpenedBook.value
            }
            else{
                _selectedBook.value = _lastOpenedBook.value
            }
        }
    }

    fun updateCollection(bookUri: String, remove: String, collection: String) {
        viewModelScope.launch {
            val book = repository.getBookByUri(bookUri).firstOrNull()
            val newCollection: String = if (!book?.collection?.contains(collection)!!) {
                if (collection != "") book.collection.plus(",").plus(collection) else {
                    book.collection.plus("")
                }
            }else{
                book.collection.replace(",$remove", "")
            }
            repository.updateCollection(bookUri, newCollection)
        }
    }

    fun collectionToList(){
        viewModelScope.launch {
            val listOfCollections: MutableList<String> = mutableListOf()
            for (book in _allCollections.value) {
                if (book.collection.contains(',')) {
                    book.collection.split(",").forEach { collection ->
                        if (!listOfCollections.contains(collection)) {
                            listOfCollections.add(collection)
                        }
                    }
                } else {
                    if (!listOfCollections.contains(book.collection)) {
                        listOfCollections.add(book.collection)
                    }
                }
            }
            val filteredList = listOfCollections.filter { it.isNotEmpty() }
            val orderedList = filteredList.sorted()
            _listOfCollections.value = orderedList
            bookOfCollection()
        }
    }

    suspend fun getBookFromUri(bookUri: String): Book? {
        return repository.getBookByUri(bookUri).firstOrNull()
    }


    private fun bookOfCollection() {
        viewModelScope.launch {
            val listOfBooksWithCollection: MutableList<List<Book>> = mutableListOf()
            for (collection in _listOfCollections.value) {
                val savedCollection = mutableListOf<Book>()
                for (contains in _allBooks.value) {
                    if (contains.collection.contains(collection)) {
                        savedCollection.add(contains)
                    }
                }
                listOfBooksWithCollection.add(savedCollection.toList())
            }
            _particularCollection.value = listOfBooksWithCollection.toList()
        }
    }

    private fun firstSelectedBook(){
        viewModelScope.launch {
            selectBook(_lastOpenedBook.value?.uri ?: "")
        }
        Log.d("firstSelectedBook", "${selectedBook.value}")
    }


    private var fetchLastPageJob: Job? = null

    fun fetchLastPage(bookUri: String) {
        fetchLastPageJob?.cancel()

        fetchLastPageJob = viewModelScope.launch {
            repository.getLastPage(bookUri).collect { page ->
                _lastPage.value = page
                Log.d("lastPage", "${lastPage.value}")
            }
        }
    }

    fun updateLastPage(bookUri: String, page: Int) {
        viewModelScope.launch {
            repository.updateLastPage(bookUri, page)
        }
    }

    private var fetchTotalPageJob: Job? = null

    fun fetchTotalPages(bookUri: String) {
        fetchTotalPageJob?.cancel()

        fetchTotalPageJob = viewModelScope.launch {
            repository.getTotalPages(bookUri).collectLatest { pages ->
                _totalPages.value = pages
            }
        }
    }

    fun updateToc(toc: List<PdfDocument.Bookmark>) {
        viewModelScope.launch {
            val tocMap: MutableMap<String, List<String>> = mutableMapOf()
            val pageMap: MutableMap<String, Int> = mutableMapOf()
            val childPageMap: MutableMap<String, Int> = mutableMapOf()
            toc.forEach { heading ->
                val key = heading.title
                if (heading.children.isNotEmpty() ){
                    val listOfChild: MutableList<String> = mutableListOf()
                   for( child in heading.children){
                       listOfChild.add(child.title)
                       childPageMap[child.title] = child.pageIdx.toInt()
                   }
                    tocMap[key] = listOfChild
                    pageMap[key] = heading.pageIdx.toInt()
                }else{
                    tocMap[key] = emptyList()
                    pageMap[key] = heading.pageIdx.toInt()
                }
            }
            _toc.value = tocMap
            _tocPage.value = pageMap
            _childTocPage.value = childPageMap
        }
    }




    fun sharePdf(
        context: Context,
        pdfUri: Uri
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share PDF via"))
    }

    fun convertMillisToDateTime(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val formatter = DateTimeFormatter.ofPattern( "dd MMMM yyyy HH:mm a")
            .withZone(ZoneId.systemDefault())

        return formatter.format(instant)
    }

    fun getFileSize(context: Context, uri: Uri): String {
        var sizeInBytes: Long = 0

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                cursor.moveToFirst()
                sizeInBytes = cursor.getLong(sizeIndex)
            }
        }

        val sizeInMB = sizeInBytes / (1024.0 * 1024.0) // Convert bytes to MB
        return DecimalFormat("#.##").format(sizeInMB) + " MB"
    }


}

class BookViewModelFactory(
    private val application: Application,
    private val repository: BookRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookDataViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}