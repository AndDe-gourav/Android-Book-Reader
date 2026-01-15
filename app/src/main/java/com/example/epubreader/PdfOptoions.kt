package com.example.epubreader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController

@Composable
fun PdfOptions(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var openRemoveDialog by remember { mutableStateOf(false) }

    if (openRemoveDialog) {
        RemovePopUp(
            onDismiss = { openRemoveDialog = false },
            onRemoveClicked = {
                selectedBook?.let { bookDataViewModel.deleteBook(it) }
                openRemoveDialog = false
                navController.navigate("homeScreen")
            },
        )
    }


    Box(
        modifier = modifier
    ) {
        FilledIconButton(
                 shape = RoundedCornerShape(8.dp),
                 colors = IconButtonDefaults.iconButtonColors(
                     if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh else colorResource(id = R.color.Book)
                 ),
                 onClick = { expanded = !expanded}
                ) {
            Icon(
                painter = painterResource(id = R.drawable.meatballs_menu),
                contentDescription = "Options",
                modifier = Modifier
                    .padding(3.dp)
                    .size(25.dp),
                tint = Color.Black
            )
        }
    }
        DropdownMenu(
            containerColor = MaterialTheme.colorScheme.onBackground,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
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