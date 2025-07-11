package com.example.epubreader

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.epubreader.model.bookStorage.Book

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookInReading(
    bookDataViewModel: BookDataViewModel,
    timeGoalViewModel: TimeGoalViewModel,
    showAboutDocument: Boolean,
    onAboutDocumentClicked: () -> Unit,
    snackBarContent: (String) -> Unit,
    navController: NavController,
    selectedBook: Book?,
    listOfCollections: List<String>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(selectedBook) {
        selectedBook?.let { bookDataViewModel.fetchTotalPages(it.uri) }
        selectedBook?.let { bookDataViewModel.fetchLastPage(it.uri) }
    }

    val lastPage by bookDataViewModel.lastPage.collectAsState()
    val totalPages by bookDataViewModel.totalPages.collectAsState()

    val sliderProgress by animateFloatAsState(
        targetValue = if (totalPages != 0) lastPage.toFloat() / totalPages else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "Slider Animation"
    )

    Column {
        Row(
            modifier = modifier.padding(
                top = dimensionResource(id = R.dimen.padding_medium),
                start = dimensionResource(id = R.dimen.padding_medium),
                bottom = dimensionResource(id = R.dimen.padding_small)
            )
        ) {
            with(sharedTransitionScope) {
                Surface(
                    color = Color.White,
                    shape = MaterialTheme.shapes.small,
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
                        .size(
                            dimensionResource(id = R.dimen.book_cover_width),
                            dimensionResource(id = R.dimen.book_cover_height)
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = MaterialTheme.shapes.small,
                            spotColor = colorResource(R.color.shadow)
                        )

                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = selectedBook?.bookCover
                        ),
                        contentDescription = "Book_cover_1",
                        contentScale = ContentScale.FillBounds,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(
                    top = dimensionResource(id = R.dimen.padding_very_small),
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LazyColumn{
                        item{
                            selectedBook?.title?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.inverseSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.book),
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    onClick = {
                                        onAboutDocumentClicked()
                                    }
                                ),
                        )
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseSurface
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.size(20.dp)
                )
                CustumSlideBar(
                    value = sliderProgress,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (totalPages != 0) {
                        val percentage = (lastPage.toFloat() / totalPages * 100).toInt()
                        "$lastPage/$totalPages Completed ($percentage%)"
                    } else {
                        " Completed (0%)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.inverseSurface
                )
            }
        }
        AnimatedIconRow(
            bookDataViewModel = bookDataViewModel,
            timeGoalViewModel = timeGoalViewModel,
            showAboutDocument = showAboutDocument,
            navController = navController,
            snackBarContent = snackBarContent,
            selectedBook = selectedBook,
            listOfCollections = listOfCollections
        )
    }
}


@Composable
fun CustumSlideBar(
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(value)
                .padding(3.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                )

        )
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
                .size(3.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}
