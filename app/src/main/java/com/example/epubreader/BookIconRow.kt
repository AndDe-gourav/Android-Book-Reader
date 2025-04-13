package com.example.epubreader

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.epubreader.model.Book
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedIconRow(
    bookDataViewModel: BookDataViewModel,
    showAboutDocument: Boolean,
    navController: NavController,
    selectedBook: Book?,
    listOfCollections: List<String>,
    snackBarContent: (String) -> Unit ,
) {
    var collectionValue by remember { mutableStateOf("") }
    var openNewCollectionDialog by remember { mutableStateOf(false) }
    var openCollectionDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        AnimatedIconButton(
            isActive = selectedBook?.favourite == 1,
            activeIcon = R.drawable.star_fill,
            inactiveIcon = R.drawable.star,
            contentDescription = "Favourites",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleFavorite(it) }
                snackBarContent(if ((selectedBook?.favourite) == 0) "Added to \"Favourites\" " else "Removed from \"Favourites\" ")
            }
        )

        // To Read icon
        AnimatedIconButton(
            isActive = selectedBook?.toRead == 1,
            activeIcon = R.drawable.clock_fill,
            inactiveIcon = R.drawable.clock,
            contentDescription = "To Read",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                if (selectedBook?.doneReading == 1) {
                    selectedBook.let { bookDataViewModel.toggleDoneReading(it) }
                }
                snackBarContent(if (selectedBook?.toRead == 0) "Added to \"To Read\" " else "Removed from \"To Read\" ")
            }
        )

        // Collection icon
        AnimatedIconButton(
            isActive = (selectedBook?.collection) != "",
            activeIcon = R.drawable.folder_dublicate_fill,
            inactiveIcon = R.drawable.folder_dublicate,
            contentDescription = "Collection",
            onClick = {
                bookDataViewModel.collectionToList()
                openCollectionDialog = true
            }
        )

        // Done Reading icon
        AnimatedIconButton(
            isActive = (selectedBook?.doneReading) == 1,
            activeIcon = R.drawable.check_round_fill,
            inactiveIcon = R.drawable.check_ring_round,
            contentDescription = "Done Reading",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                if (selectedBook?.toRead == 1) {
                    selectedBook.let { bookDataViewModel.toggleToRead(it) }
                }
                snackBarContent(if (selectedBook?.doneReading == 0) "Added to \"Done Reading\" " else "Removed from \"Done Reading\" ")
            }
        )

        OptionsDropDownMenu(
            bookDataViewModel = bookDataViewModel,
            showAboutDocument = showAboutDocument,
            navController = navController,
            selectedBook = selectedBook
        )

        if (openNewCollectionDialog) {
            OnNotInCollectionsIconClicked(
                value = collectionValue,
                onValueChange = { collectionValue = it },
                onDismiss = {
                    collectionValue = ""
                    openNewCollectionDialog = false
                },
                onCreateClicked = {
                    if (!listOfCollections.map { it.lowercase() }
                            .contains(collectionValue.lowercase())) {
                        selectedBook?.let {
                            bookDataViewModel.updateCollection(
                                it.uri,
                                remove = "",
                                collectionValue
                            )
                        }
                        coroutineScope.launch {
                            delay(300)
                            bookDataViewModel.collectionToList()
                        }
                        snackBarContent("Added to Collection \n\"$collectionValue\" ")
                        collectionValue = ""
                        openNewCollectionDialog = false
                        openCollectionDialog = false

                    } else {
                        snackBarContent("Collection already exists")
                        collectionValue = ""
                        openNewCollectionDialog = false
                        openCollectionDialog = false
                    }
                },

                )
        }

        if (openCollectionDialog) {
            OnInCollectionIconClicked(
                bookDataViewModel = bookDataViewModel,
                onDismiss = {
                    bookDataViewModel.collectionToList()
                    openCollectionDialog = false
                },
                onCreateNewClicked = { openNewCollectionDialog = true },
                snackBarContent = snackBarContent
            )
        }

    }
}

@Composable
fun AnimatedIconButton(
    isActive: Boolean,
    activeIcon: Int,
    inactiveIcon: Int,
    contentDescription: String,
    onClick: () -> Unit
) {

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    IconButton(
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }

        Icon(
            painter = painterResource(if (isActive) activeIcon else inactiveIcon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.inverseSurface,
            modifier = Modifier.scale(scale)
        )
    }
}


@Composable
fun OptionsDropDownMenu(
    navController: NavController,
    showAboutDocument: Boolean,
    modifier: Modifier = Modifier,
    selectedBook: Book?,
    bookDataViewModel: BookDataViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current


    var openRemoveDialog by remember { mutableStateOf(false) }

    if (openRemoveDialog) {
        RemovePopUp(
            onDismiss = { openRemoveDialog = false },
            onRemoveClicked = {
                selectedBook?.let { bookDataViewModel.deleteBook(it) }
                openRemoveDialog = false
                if (showAboutDocument){
                    navController.navigate("homeScreen")
                }
            },
        )
    }

    Box(
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                expanded = true
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.meatballs_menu),
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.size(24.dp),
            )
        }
        DropdownMenu(
            containerColor = MaterialTheme.colorScheme.onBackground,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    navController.navigate("EditScreen")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Share",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    selectedBook?.uri?.let {
                        bookDataViewModel.sharePdf(
                            context = context,
                            it.toUri()
                        )
                    }
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    expanded = false
                    openRemoveDialog = true
                }
            )
        }
    }
}

