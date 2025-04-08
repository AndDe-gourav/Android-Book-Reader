package com.example.epubreader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun EditScreen(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    var titleTextFieldValue by remember { mutableStateOf(selectedBook?.title) }
    var openTitleDialog by remember { mutableStateOf(false) }
    var openAuthorDialog by remember { mutableStateOf(false) }
    var authorTextFieldValue by remember { mutableStateOf(selectedBook?.author) }

    LazyColumn(
        modifier = modifier
    ){
        item {
            GerenalTopBar(
                titleText = "Edit Book Details",
                onBackClicked = { navController.navigateUp() }
            )
            Spacer(
                modifier = Modifier.padding(6.dp)
            )
            (selectedBook?.title)?.let {
                EditCell(
                    titleText = it,
                    cellName = "Title",
                    onCellClicked = { openTitleDialog = true }
                )
            }
            if (openTitleDialog) {
                titleTextFieldValue?.let {
                    OnCellClicked(
                        cellName = "Title",
                        value = it,
                        onValueChange = { titleTextFieldValue = it },
                        onDismiss = { openTitleDialog = false },
                        onSaveClicked = {
                            selectedBook?.let { book ->
                                bookDataViewModel.updateBookTitle(
                                    book,
                                    title = titleTextFieldValue!!
                                )
                            }
                            openTitleDialog = false
                        }
                    )
                }
            }
            selectedBook?.author?.let {
                EditCell(
                    titleText = it,
                    cellName = "Author",
                    onCellClicked = { openAuthorDialog = true }
                )
            }
            if (openAuthorDialog) {
                authorTextFieldValue?.let {
                    OnCellClicked(
                        cellName = "Author",
                        value = it,
                        onValueChange = { authorTextFieldValue = it },
                        onDismiss = { openAuthorDialog = false },
                        onSaveClicked = {
                            selectedBook?.let { book ->
                                bookDataViewModel.updateBookAuthor(
                                    book,
                                    author = authorTextFieldValue!!
                                )
                            }
                            openAuthorDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditCell(
    modifier: Modifier = Modifier,
    titleText: String = "",
    cellName: String = "",
    onCellClicked: () -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 8.dp,
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clickable(
                onClick = { onCellClicked() }
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.inverseSurface,
            )
            Spacer(
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = cellName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun OnCellClicked(
    modifier: Modifier = Modifier,
    cellName: String = "",
    value: String ,
    onValueChange: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    onSaveClicked: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = cellName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                    )
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    maxLines = 3,
                    label = {Text(text = if(cellName == "Author")"Author's name" else "Title") },
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(
                    modifier = modifier.padding(8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onDismiss() },
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(
                        modifier = Modifier.padding(2.dp)
                    )
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onSaveClicked() }
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }

        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        delay(100)
        keyboardController?.show()
    }
}


@Composable
fun RemovePopUp(
    onDismiss: () -> Unit,
    onRemoveClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close_ring),
                    contentDescription = "Remove",
                    tint = Color.Red,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Are you sure?",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 14.dp)
                    )
                Text(
                    text = "Do you relly want to remove this book?",
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onDismiss() },
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(
                        modifier = Modifier.padding(2.dp)
                    )
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onRemoveClicked() }
                    ) {
                        Text(
                            text = "Remove",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}
