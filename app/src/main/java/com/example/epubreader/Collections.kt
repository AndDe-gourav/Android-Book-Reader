package com.example.epubreader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun OnNotInCollectionsIconClicked(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    onCreateClicked: () -> Unit,
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
                        text = "New collection",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                    )
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.onBackground , unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,),
                    maxLines = 1,
                    placeholder = { Text(text = "Enter collection name") },
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
                        onClick = { onCreateClicked() },
                    ) {
                        Text(
                            text = "Create",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(
                        modifier = Modifier.padding(2.dp)
                    )
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
fun OnInCollectionIconClicked(
    modifier: Modifier = Modifier,
    bookDataViewModel: BookDataViewModel,
    onDismiss: () -> Unit = {},
    onCreateNewClicked: () -> Unit,
    snackBarContent: (String) -> Unit = {}
) {
    val allCollectionsList by bookDataViewModel.listOfCollections.collectAsState()

    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
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
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Collections",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 15.dp, bottom = 10.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier.padding(12.dp).heightIn(max = 400.dp)
                ) {
                    item {
                        Spacer(
                            modifier = Modifier.padding(2.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow( elevation = 4.dp , shape = RoundedCornerShape(8.dp))
                                .clickable { onCreateNewClicked() }
                                .background(color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.folder_dublicate_fill),
                                contentDescription = "Create new collection"
                            )
                            Spacer(
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(
                                text = "Create new collection",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        Spacer(
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    items(allCollectionsList) { collection ->
                        var checked by remember { mutableStateOf(selectedBook?.collection?.contains(collection)) }
                        if (collection != "") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (checked == true){
                                            selectedBook?.let {
                                                bookDataViewModel.updateCollection(it.uri, remove = collection,   "")
                                            }
                                        }else{
                                            selectedBook?.let {
                                                bookDataViewModel.updateCollection(it.uri, remove = "", collection)
                                            }
                                        }
                                        snackBarContent(if (checked == true){ "Removed from Collection \n\"$collection\" " } else { "Added to Collection \n\"$collection\" " })
                                    checked = !checked!!
                                }
                            ) {
                                checked?.let {
                                    RadioButton(
                                        selected = it,
                                        onClick = {
                                            if (checked == true){
                                                selectedBook?.let {
                                                    bookDataViewModel.updateCollection(it.uri, remove = collection,   "")
                                                }
                                            }else{
                                                selectedBook?.let {
                                                    bookDataViewModel.updateCollection(it.uri, remove = "", collection)
                                                }
                                            }
                                            snackBarContent(if (checked == true){ "Removed from Collection \n\"$collection\" " } else { "Added to Collection \n\"$collection\" " })
                                            checked = !checked!!
                                        },
                                        colors = RadioButtonColors(
                                            selectedColor = colorResource(id = R.color.progress_bar_front_color),
                                            unselectedColor = colorResource(id = R.color.progress_bar_front_color),
                                            disabledSelectedColor = colorResource(id = R.color.progress_bar_front_color),
                                            disabledUnselectedColor = colorResource(id = R.color.progress_bar_front_color)
                                        )
                                    )
                                }
                                Spacer(
                                    modifier = Modifier.padding(2.dp)
                                )
                                Text(
                                    text = collection,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                            }
                            HorizontalDivider(
                                thickness = (0.5).dp,
                                color = colorResource(id = R.color.progress_bar_front_color)
                            )
                        }
                    }

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            onDismiss()
                                  },
                    ) {
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}
