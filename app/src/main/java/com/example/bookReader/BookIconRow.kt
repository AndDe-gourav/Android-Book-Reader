package com.example.bookReader

import android.content.Intent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.ReadingStatus
import com.example.bookReader.ui.theme.BookStateViewModel
import com.example.bookReader.ui.theme.CollectionViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedIconRow(
    selectedBook: BookEntity?,
    bookStateViewModel: BookStateViewModel,
    collectionViewModel: CollectionViewModel,
    navController: NavController,
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorite icon
        AnimatedIconButton(
            isActive = bookState?.isFavorite == true,
            activeIcon = R.drawable.star_fill,
            inactiveIcon = R.drawable.star,
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

        AnimatedIconButton(
            isActive = bookState?.status == ReadingStatus.TO_READ,
            activeIcon = R.drawable.clock_fill,
            inactiveIcon = R.drawable.clock,
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

        AnimatedIconButton(
            isActive = isInAnyCollection,
            activeIcon = R.drawable.folder_dublicate_fill,
            inactiveIcon = R.drawable.folder_dublicate,
            contentDescription = "Collection",
            onClick = { openCollectionDialog = true }
        )

        AnimatedIconButton(
            isActive = bookState?.status == ReadingStatus.COMPLETED,
            activeIcon = R.drawable.check_round_fill,
            inactiveIcon = R.drawable.check_ring_round,
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
            selectedBook = selectedBook,
            navController = navController,
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
    val collectionsWithBooks by collectionViewModel.allCollectionsWithBooks.collectAsState()
    var newCollectionName by remember { mutableStateOf("") }
    var showNewCollectionField by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Collections",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground.let { Color.Black }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (collectionsWithBooks.isEmpty()) {
                        item {
                            Text(
                                text = "No collections yet. Create one below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(collectionsWithBooks) { cwb ->
                            val isInCollection = selectedBook != null &&
                                    cwb.books.any { it.bookId == selectedBook.bookId }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedBook?.let {
                                            collectionViewModel.toggleBookInCollection(
                                                bookId = it.bookId,
                                                collectionId = cwb.collection.collectionId,
                                                currentlyIn = isInCollection
                                            )
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cwb.collection.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                                Checkbox(
                                    checked = isInCollection,
                                    onCheckedChange = {
                                        selectedBook?.let { book ->
                                            collectionViewModel.toggleBookInCollection(
                                                bookId = book.bookId,
                                                collectionId = cwb.collection.collectionId,
                                                currentlyIn = isInCollection
                                            )
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colorResource(id = R.color.progress_bar_front_color)
                                    )
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (showNewCollectionField) {
                    OutlinedTextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        label = { Text("New collection name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showNewCollectionField = false
                            newCollectionName = ""
                        }) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                val trimmed = newCollectionName.trim()
                                if (trimmed.isNotBlank()) {
                                    collectionViewModel.createCollection(trimmed)
                                    newCollectionName = ""
                                    showNewCollectionField = false
                                }
                            }
                        ) {
                            Text("Create", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                            .clickable { showNewCollectionField = true }
                            .background(
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.folder_dublicate_fill),
                            contentDescription = "New collection",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create new collection",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AnimatedIconButton(
    isActive: Boolean,
    activeIcon: Int,
    inactiveIcon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                isPressed = true
                onClick()
                isPressed = false
            }
        ) {
            Icon(
                painter = painterResource(id = if (isActive) activeIcon else inactiveIcon),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.scrim,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun OptionsDropDownMenu(
    selectedBook: BookEntity?,
    navController: NavController,
    onBookDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var openRemoveDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.meatballs_menu),
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.scrim,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            containerColor = MaterialTheme.colorScheme.onBackground,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    navController.navigate("EditScreen")
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
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.close_ring),
                    contentDescription = "remove",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Remove Book",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        },
        text = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.onBackground),
            ) {
                Text(
                    "Are you sure you want to remove \"$bookTitle\" from your library?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(20.dp)
                )
            }
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Remove") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        modifier = modifier
    )
}
