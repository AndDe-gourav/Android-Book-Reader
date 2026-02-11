package com.example.bookReader

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.ReadingStatus
import com.example.bookReader.ui.theme.BookStateViewModel
import com.example.bookReader.ui.theme.CollectionViewModel
import com.example.bookReader.ui.theme.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedIconRow(
    selectedBook: BookEntity?,
    libraryViewModel: LibraryViewModel,
    bookStateViewModel: BookStateViewModel,
    collectionViewModel: CollectionViewModel,
    navController: NavController,
    onBookDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allCollections by collectionViewModel.allCollections.collectAsState()

    var openNewCollectionDialog by remember { mutableStateOf(false) }
    var openCollectionDialog by remember { mutableStateOf(false) }
    var collectionValue by remember { mutableStateOf("") }

    // FIXED: Observe book state reactively instead of manual fetching
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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorite icon - automatically updates when favorite status changes
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
                    // NO NEED TO MANUALLY REFRESH - Flow handles it automatically
                }
            }
        )

        // To Read icon - automatically updates when status changes
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
                    bookStateViewModel.updateBookState(
                        bookId = it.bookId,
                        status = newStatus
                    )
                    // NO NEED TO MANUALLY REFRESH - Flow handles it automatically
                }
            }
        )

        // Collection icon
        AnimatedIconButton(
            isActive = false, // TODO: Check if book is in any collection
            activeIcon = R.drawable.folder_dublicate_fill,
            inactiveIcon = R.drawable.folder_dublicate,
            contentDescription = "Collection",
            onClick = {
                openCollectionDialog = true
            }
        )

        // Done Reading icon - automatically updates when status changes
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
                    bookStateViewModel.updateBookState(
                        bookId = it.bookId,
                        status = newStatus
                    )
                    // NO NEED TO MANUALLY REFRESH - Flow handles it automatically
                }
            }
        )

        // Options menu
        OptionsDropDownMenu(
            selectedBook = selectedBook,
            libraryViewModel = libraryViewModel,
            navController = navController,
            onBookDeleted = onBookDeleted
        )
    }

    // New Collection Dialog
    if (openNewCollectionDialog) {
        CreateCollectionDialog(
            value = collectionValue,
            onValueChange = { collectionValue = it },
            existingCollections = allCollections.map { it.name },
            onDismiss = {
                collectionValue = ""
                openNewCollectionDialog = false
            },
            onCreate = { name ->
                coroutineScope.launch {
                    val collectionId = collectionViewModel.createCollection(name)
                    if (collectionId != -1L && selectedBook != null) {
                        collectionViewModel.addBookToCollection(selectedBook.bookId, collectionId)
                    }
                    collectionValue = ""
                    openNewCollectionDialog = false
                    openCollectionDialog = false
                }
            }
        )
    }

    // Collection Selection Dialog
    if (openCollectionDialog) {
        CollectionSelectionDialog(
            collections = allCollections,
            selectedBook = selectedBook,
            collectionViewModel = collectionViewModel,
            onDismiss = { openCollectionDialog = false },
            onCreateNew = { openNewCollectionDialog = true }
        )
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
                // Reset press state after animation
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
    libraryViewModel: LibraryViewModel,
    navController: NavController,
    onBookDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = {
                    Text(
                        "Edit",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    navController.navigate("EditScreen")
                    expanded = false
                },
            )

            DropdownMenuItem(
                text = {
                    Text(
                        "Share",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    // TODO: Implement share functionality
                    expanded = false
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
                libraryViewModel.deleteBook(selectedBook.bookId)
                openRemoveDialog = false
                onBookDeleted()
            }
        )
    }
}

@Composable
fun CreateCollectionDialog(
    value: String,
    onValueChange: (String) -> Unit,
    existingCollections: List<String>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Collection",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        onValueChange(it)
                        errorMessage = null
                    },
                    label = { Text("Collection Name") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        value.isBlank() -> {
                            errorMessage = "Collection name cannot be empty"
                        }
                        existingCollections.any { it.equals(value, ignoreCase = true) } -> {
                            errorMessage = "Collection already exists"
                        }
                        else -> {
                            onCreate(value)
                        }
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
fun CollectionSelectionDialog(
    collections: List<CollectionEntity>,
    selectedBook: BookEntity?,
    collectionViewModel: CollectionViewModel,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add to Collection",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (collections.isEmpty()) {
                    Text(
                        text = "No collections yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    collections.forEach { collection ->
                        TextButton(
                            onClick = {
                                selectedBook?.let {
                                    collectionViewModel.addBookToCollection(
                                        bookId = it.bookId,
                                        collectionId = collection.collectionId
                                    )
                                }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = collection.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                TextButton(
                    onClick = onCreateNew,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_round),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Collection")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
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
            Text(
                "Remove Book",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                "Are you sure you want to remove \"$bookTitle\" from your library?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}