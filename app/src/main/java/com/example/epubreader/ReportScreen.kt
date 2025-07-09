package com.example.epubreader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ReportScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        GerenalTopBar(
            titleText = "Report",
            onBackClicked = {
                navController.navigate("homeScreen")
            },
            modifier = Modifier.zIndex(1f)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.zIndex(0f)
        ) {
            item { Spacer(modifier = Modifier.padding(80.dp))  }
            items(
                items = listOf(1, 2,3,4,5,6),
            ){
                Surface(
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(0.2f)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = MaterialTheme.shapes.small,
                                    spotColor = colorResource(R.color.shadow)
                                )

                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ""
                                ),
                                contentDescription = "Book_cover_1",
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text =  "The Book title",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.inverseSurface,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Text(
                                text = "L__The Book author",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Spacer(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row {
                                        Text(
                                            text = "20m ",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                        )
                                        Text(
                                            text = "Done",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                        )
                                    }
                                    Row {
                                        Text(
                                            text = "2h 20m ",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            textAlign = TextAlign.End,
                                        )
                                        Text(
                                            text = "Time Goal",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            textAlign = TextAlign.End,
                                        )
                                    }
                                }
                                CustumSlideBar(
                                    value = 0.4f
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}

