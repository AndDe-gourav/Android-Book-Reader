package com.example.epubreader

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutDoucument(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    showAboutDocument: Boolean,
    onAboutDocumentClicked: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val listOfCollections by bookDataViewModel.listOfCollections.collectAsState()
    val time = selectedBook?.timestamp?.let { bookDataViewModel.convertMillisToDateTime(it)}
    val fileSize = selectedBook?.uri?.let { bookDataViewModel.getFileSize(context= context, it.toUri())}

    val encodedUri = Uri.encode(selectedBook?.uri)

    var showTimeGoal by remember{ mutableStateOf(false) }


    BackHandler {
        onAboutDocumentClicked()
    }

    Box(
        modifier = modifier
    ) {
        GerenalTopBar(
            titleText = "About Book",
            onBackClicked = {
                onAboutDocumentClicked()
            },
            modifier = Modifier.zIndex(1f)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier= Modifier.zIndex(0f)
        ) {
            item {
                Spacer(modifier = Modifier.padding(top = 80.dp))
                with(sharedTransitionScope) {
                    Box(
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(key = "bookCover"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { initial, target ->
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 380f
                                    )
                                }
                            )
                    ) {
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
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ){
                    Text(
                        text = selectedBook?.title ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.inverseSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 2.dp)
                    )
                    Text(
                        text = "L__ ${selectedBook?.author ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, end = 8.dp)
                    )
                }
                AnimatedIconRow(
                    bookDataViewModel = bookDataViewModel,
                    showAboutDocument = showAboutDocument,
                    navController = navController,
                    selectedBook = selectedBook,
                    listOfCollections = listOfCollections,
                    snackBarContent = {},
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$time",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 1.dp)
                        )
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
                        Surface(
                            onClick = { showTimeGoal = true},
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = modifier.padding(top = 5.dp),
                            shadowElevation = 6.dp,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = "Set Time Goal",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.inverseSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp),
                                )
                        }
                    }
                }
            }
        }
        if (showTimeGoal)
            TimePicker(
                onDismissRequest = { showTimeGoal = false },
                bookDataViewModel = bookDataViewModel
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
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable {
                    onBackClicked()
                }
            ,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shadowElevation = 4.dp,
        ) {
            IconButton(
                onClick = { onBackClicked() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

