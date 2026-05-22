package com.timepass.bookreader.ui.home

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.timepass.bookreader.R
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.BookStateEntity
import com.timepass.bookreader.data.entity.ReadingStatus
import com.timepass.bookreader.ui.pdfviewer.DialogBox

@Composable
fun BookStatusIconRow(
    openEdit: () -> Unit,
    selectedBook: BookEntity?,
    bookStateViewModel: BookStateViewModel,
    collectionViewModel: CollectionViewModel,
    modifier: Modifier = Modifier,
    onBookDeleted: () -> Unit = {},
) {
    var openCollectionDialog by remember { mutableStateOf(false) }

    val bookState by produceState<BookStateEntity?>(
        initialValue = null,
        key1 = selectedBook?.bookId
    ) {
        selectedBook?.let { book ->
            bookStateViewModel.observeBookState(book.bookId).collect { state ->
                value = state
            }
        }
    }

    val collectionsWithBooks by collectionViewModel.allCollectionsWithBooks.collectAsState()
    val isInAnyCollection = remember(collectionsWithBooks, selectedBook) {
        selectedBook != null && collectionsWithBooks.any { cwb ->
            cwb.books.any { it.bookId == selectedBook.bookId }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            isActive = bookState?.isFavorite == true,
            icon = R.drawable.star_24dp_000000_fill0_wght300_grad0_opsz24,
            contentDescription = "Favorites",
            onClick = {
                selectedBook?.let {
                    val currentFavorite = bookState?.isFavorite ?: false
                    bookStateViewModel.updateBookState(
                        bookId = it.bookId,
                        isFavorite = !currentFavorite
                    )
                }
            }
        )

        Button(
            isActive = bookState?.status == ReadingStatus.TO_READ,
            icon = R.drawable.alarm_24dp_000000_fill0_wght300_grad0_opsz24,
            contentDescription = "To Read",
            onClick = {
                selectedBook?.let {
                    val newStatus = if (bookState?.status == ReadingStatus.TO_READ) {
                        ReadingStatus.READING
                    } else {
                        ReadingStatus.TO_READ
                    }
                    bookStateViewModel.updateBookState(bookId = it.bookId, status = newStatus)
                }
            }
        )

        Button(
            isActive = isInAnyCollection,
            icon = R.drawable.ad_group_24dp_000000_fill0_wght300_grad0_opsz24,
            contentDescription = "Collection",
            onClick = { openCollectionDialog = true }
        )

        Button(
            isActive = bookState?.status == ReadingStatus.COMPLETED,
            icon = R.drawable.check_circle_24dp_000000_fill0_wght300_grad0_opsz24,
            contentDescription = "Done Reading",
            onClick = {
                selectedBook?.let {
                    val newStatus = if (bookState?.status == ReadingStatus.COMPLETED) {
                        ReadingStatus.READING
                    } else {
                        ReadingStatus.COMPLETED
                    }
                    bookStateViewModel.updateBookState(bookId = it.bookId, status = newStatus)
                }
            }
        )

        OptionsDropDownMenu(
            openEdit = openEdit,
            selectedBook = selectedBook,
            onBookDeleted = onBookDeleted
        )
    }

    if (openCollectionDialog) {
        OnCollectionDialog(
            selectedBook = selectedBook,
            collectionViewModel = collectionViewModel,
            onDismiss = { openCollectionDialog = false }
        )
    }
}


@Composable
fun OnCollectionDialog(
    selectedBook: BookEntity?,
    collectionViewModel: CollectionViewModel,
    onDismiss: () -> Unit
) {

    val collectionsWithBooks by collectionViewModel
        .allCollectionsWithBooks
        .collectAsState()

    val collectionNameState = rememberTextFieldState()

    DialogBox(
        onDismiss = onDismiss,
        dismissText = "Done",
        confirmText = "Create",
        heading = "Collections",
        onConfirm = {

            val trimmed = collectionNameState.text
                .toString()
                .trim()

            if (trimmed.isNotBlank()) {
                collectionViewModel.createCollection(trimmed)

                collectionNameState.setTextAndPlaceCursorAtEnd("")
            }
        },
        textFieldState = collectionNameState,
        textFieldLabel = "New collection name",
        content = {

            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {

                if (collectionsWithBooks.isEmpty()) {

                    item {

                        Text(
                            text = "No collections yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                else {

                    items(collectionsWithBooks) { cwb ->

                        val isInCollection =
                            selectedBook != null &&
                                    cwb.books.any {
                                        it.bookId == selectedBook.bookId
                                    }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    selectedBook?.let {

                                        collectionViewModel
                                            .toggleBookInCollection(
                                                bookId = it.bookId,
                                                collectionId = cwb.collection.collectionId,
                                                currentlyIn = isInCollection
                                            )
                                    }
                                }
                                .padding(vertical = 8.dp),

                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = cwb.collection.name,
                                modifier = Modifier.weight(1f)
                            )

                            Checkbox(
                                checked = isInCollection,
                                onCheckedChange = {

                                    selectedBook?.let { book ->

                                        collectionViewModel
                                            .toggleBookInCollection(
                                                bookId = book.bookId,
                                                collectionId = cwb.collection.collectionId,
                                                currentlyIn = isInCollection
                                            )
                                    }
                                }
                            )
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    )
}


@Composable
fun Button(
    isActive: Boolean,
    @DrawableRes icon: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface (
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(3.dp)
        )
    }
}

@Composable
fun OptionsDropDownMenu(
    openEdit: () -> Unit,
    selectedBook: BookEntity?,
    onBookDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var openRemoveDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            icon = R.drawable.more_horiz_24dp_000000_fill1_wght300_grad0_opsz24,
            contentDescription = "options",
            onClick = { expanded = true},
            isActive = expanded
        )

        DropdownMenu(
            shape = RoundedCornerShape(0.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    openEdit()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Share", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    expanded = false
                    val uri = selectedBook?.uri?.toUri()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Book"))
                },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        "Remove",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    openRemoveDialog = true
                },
            )
        }
    }

    if (openRemoveDialog && selectedBook != null) {
        RemoveBookDialog(
            bookTitle = selectedBook.title,
            onDismiss = { openRemoveDialog = false },
            onConfirm = {
                openRemoveDialog = false
                onBookDeleted()
            }
        )
    }
}

@Composable
fun RemoveBookDialog(
    bookTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {

    DialogBox(
        onDismiss = onDismiss,
        dismissText = "Cancel",
        confirmText = "Remove",
        heading = "Remove Book",
        onConfirm = onConfirm,
        content = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    painter = painterResource(
                        R.drawable.remove_selection_24dp_000000_fill0_wght300_grad0_opsz24
                    ),
                    contentDescription = "remove",
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Are you sure you want to remove \"$bookTitle\" from your library?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
