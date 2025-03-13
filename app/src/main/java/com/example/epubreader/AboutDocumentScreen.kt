package com.example.epubreader

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun AboutDoucument(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    currentScreen: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val time = selectedBook?.timestamp?.let { bookDataViewModel.convertMillisToDateTime(it) }
    val fileSize = bookDataViewModel.getFileSize(context= context, Uri.parse(selectedBook?.uri))

    val encodedUri = Uri.encode(selectedBook?.uri)

    Box(
        modifier = modifier.statusBarsPadding()
    ) {
        GerenalTopBar(
            titleText = "About Book",
            onBackClicked = {
                navController.navigateUp()
            },
            modifier = Modifier.zIndex(1f)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier= Modifier.zIndex(0f)
        ) {
            item {
                Spacer(modifier = Modifier.padding(top = 70.dp))
                Box{
                    Surface(
                        color = Color.White,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier
                            .size(
                                200.dp,
                                300.dp
                            )
                            .shadow(
                                elevation = 8.dp,
                                shape = MaterialTheme.shapes.small,
                                spotColor = colorResource(R.color.shadow)
                            )
                            .clickable {
                                selectedBook?.let {
                                    bookDataViewModel.updateBookTime(it)
                                }
                                navController.navigate("BookScreen/${encodedUri}")
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = selectedBook?.bookCover
                            ),
                            contentDescription = "Book_cover_1",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    selectedBook?.let {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                }
                AnimatedIconRow(
                    bookDataViewModel = bookDataViewModel,
                    navController = navController,
                    currentScreen = currentScreen
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        selectedBook?.let {
                            Text(
                                text = "$time",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.inverseSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp, bottom = 1.dp)
                            )
                        }
                        Text(
                            text = "Last read time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseSurface,
                        )
                        Text(
                            text = "PDF, $fileSize",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 1.dp)
                        )
                        Text(
                            text = "File format and size",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedIconRow(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    currentScreen: String,
) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        AnimatedIconButton(
            isActive = selectedBook?.favourite == 1,
            activeIcon = R.drawable.baseline_star_rate_24,
            inactiveIcon = R.drawable.baseline_star_border_24,
            contentDescription = "Favourites",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleFavorite(it) }
            }
        )

        // To Read icon
        AnimatedIconButton(
            isActive = selectedBook?.toRead  == 1,
            activeIcon = R.drawable.baseline_access_time_filled_24,
            inactiveIcon = R.drawable.baseline_access_time_24,
            contentDescription = "To Read",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                if (selectedBook?.doneReading == 1){
                    selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                }
            }
        )

        // Collection icon
        AnimatedIconButton(
            isActive = selectedBook?.collection == "",
            activeIcon = R.drawable.baseline_folder_copy_24,
            inactiveIcon = R.drawable.baseline_folder_open_24,
            contentDescription = "Collection",
            onClick = {
            }
        )

        // Done Reading icon
        AnimatedIconButton(
            isActive = selectedBook?.doneReading == 1,
            activeIcon = R.drawable.baseline_done_24,
            inactiveIcon = R.drawable.baseline_done_outline_24,
            contentDescription = "Done Reading",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                if (selectedBook?.toRead == 1){
                    selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                }
            }
        )
        OptionsDropDownMenu(
            bookDataViewModel = bookDataViewModel,
            navController = navController,
            currentScreen = currentScreen
        )
    }
}

@Composable
fun GerenalTopBar(
    onBackClicked: () -> Unit,
    titleText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.TopStart),
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shadowElevation = 4.dp,
        ) {
            IconButton(
                onClick = {
                    onBackClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.TopEnd),
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            shadowElevation = 4.dp,
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 13.dp, horizontal = 34.dp)
            )
        }
    }
}
