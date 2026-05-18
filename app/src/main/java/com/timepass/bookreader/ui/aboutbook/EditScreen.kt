package com.timepass.bookreader.ui.aboutbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.home.LibraryViewModel
import com.timepass.bookreader.ui.TopBar
import kotlinx.coroutines.delay

enum class EditField {
    TITLE,
    AUTHOR
}

@Composable
fun EditScreen(
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {

    val selectedBook by libraryViewModel.selectedBook.collectAsState()

    var editingField by remember { mutableStateOf<EditField?>(null) }
    var editValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopBar(
                titleText = "Edit Book",
                onActionClicked = { navController.popBackStack() },
                icon = R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24
            )
        },
        modifier = modifier
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                selectedBook?.title?.let { title ->
                    EditCell(
                        titleText = title,
                        cellName = "Title",
                        onCellClicked = {
                            editValue = title
                            editingField = EditField.TITLE
                        }
                    )
                }

                selectedBook?.author?.let { author ->
                    EditCell(
                        titleText = author,
                        cellName = "Author",
                        onCellClicked = {
                            editValue = author
                            editingField = EditField.AUTHOR
                        }
                    )
                }
            }
        }
    }

    editingField?.let { field ->

        OnCellClicked(
            cellName = if (field == EditField.AUTHOR) "Author" else "Title",
            value = editValue,
            onValueChange = { editValue = it },
            onDismiss = { editingField = null },
            onSaveClicked = {
                selectedBook?.let { book ->
                    when (field) {
                        EditField.TITLE ->
                            libraryViewModel.updateBookTitle(book.bookId, editValue)
                        EditField.AUTHOR ->
                            libraryViewModel.updateBookAuthor(book.bookId, editValue)
                    }
                }
                editingField = null
                navController.popBackStack()
            }
        )
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
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable(
                onClick = { onCellClicked() }
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = cellName,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(
                modifier = Modifier.padding(5.dp)
            )
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleSmall,
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        delay(100)
        keyboardController?.show()
    }

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
                Text(
                    text = cellName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    maxLines = 3,
                    label = { Text(text = if (cellName == "Author") "Author's name" else "Title") },
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
}