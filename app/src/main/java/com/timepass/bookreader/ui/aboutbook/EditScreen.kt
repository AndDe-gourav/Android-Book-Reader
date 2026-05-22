package com.timepass.bookreader.ui.aboutbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.TopBar
import com.timepass.bookreader.ui.home.LibraryViewModel
import com.timepass.bookreader.ui.pdfviewer.DialogBox
import kotlinx.coroutines.delay

enum class EditField {
    TITLE,
    AUTHOR
}

@Composable
fun EditScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
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
                onActionClicked = { onBack() },
                icon = R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
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
                onBack()
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
    value: String,
    onValueChange: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    onSaveClicked: () -> Unit = {},
) {

    val textFieldState = rememberTextFieldState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {

        textFieldState.setTextAndPlaceCursorAtEnd(value)

        focusRequester.requestFocus()

        delay(100)

        keyboardController?.show()
    }

    DialogBox(
        onDismiss = onDismiss,
        dismissText = "Cancel",
        confirmText = "Save",
        heading = cellName,
        onConfirm = {

            onValueChange(
                textFieldState.text.toString()
            )

            onSaveClicked()
        },
        textFieldState = textFieldState,
        textFieldLabel = if (cellName == "Author") {
            "Author's name"
        } else {
            "Title"
        },
        keyboardType = KeyboardType.Text,
        modifier = modifier,
        content = {

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Spacer(
                modifier = Modifier
                    .focusRequester(focusRequester)
            )
        }
    )
}