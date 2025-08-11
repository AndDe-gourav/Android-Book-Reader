package com.example.epubreader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TocSheet(
    currentPage: Int,
    onTocPageClicked: (Int) -> Unit,
    onChildPageClicked: (Int) -> Unit,
    bookDataViewModel: BookDataViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        val toc by bookDataViewModel.toc.collectAsState()
        val tocPage by bookDataViewModel.tocPage.collectAsState()
        val childPage by bookDataViewModel.childTocPage.collectAsState()
        val totalPages by bookDataViewModel.totalPages.collectAsState()
        var expanded by remember { mutableStateOf("") }

        val tocKeyList = toc.keys.toList()
        val nextHeadingMap = mutableMapOf<String,String?>()
        for (i in tocKeyList.indices){
            val current = tocKeyList[i]
            val next = tocKeyList.getOrNull(i+1)
            nextHeadingMap[current] = next
        }

        Surface(
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(1f).fillMaxHeight(0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Table of Contents",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
                if(toc.isEmpty()){
                    Text(
                            text = "Table of content not available",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(75.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp , bottom = 40.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    toc.forEach { (heading, subheadings) ->
                        var highlited = false
                        item{
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .background(
                                        color = if (currentPage >= tocPage[heading]!! && currentPage < (tocPage[nextHeadingMap[heading]]
                                                ?: totalPages)
                                        ) {
                                            highlited = true
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        } else {
                                            MaterialTheme.colorScheme.onBackground
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            onClick = {
                                                onTocPageClicked(tocPage[heading]!!)
                                            }
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    ) {
                                        Text(
                                            text = heading,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .padding(12.dp)
                                        )
                                    }
                                    Row {
                                        if (subheadings.isNotEmpty())
                                            IconButton(
                                                onClick = {
                                                    if (expanded == heading) expanded =
                                                        "" else {
                                                        expanded = heading
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = if (expanded != heading) R.drawable.expand_down else R.drawable.expand_up),
                                                    contentDescription = "ExpandMore",
                                                    tint = Color.Black,
                                                )
                                            }
                                        Text(
                                            text = "${tocPage[heading]}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        val nextSubHeadingMap = mutableMapOf<String, String?>()
                        for (i in subheadings.indices) {
                            val current = subheadings[i]
                            val next = subheadings.getOrNull(i + 1)
                            nextSubHeadingMap[current] = next
                        }
                        items(
                            items = subheadings,
                        ) { subheadings ->
                            AnimatedVisibility(heading == expanded) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .background(
                                                color = if (highlited == true) {
                                                    if (currentPage >= childPage[subheadings]!! && currentPage < (childPage[nextSubHeadingMap[subheadings]]
                                                            ?: totalPages)
                                                    ) {
                                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                                    } else MaterialTheme.colorScheme.onBackground
                                                } else MaterialTheme.colorScheme.onBackground,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .fillMaxWidth(0.9f)
                                            .clickable(
                                                onClick = {
                                                    onChildPageClicked(childPage[subheadings]!!)
                                                }
                                            ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(0.7f)
                                        ) {
                                            Text(
                                                text = subheadings,
                                                fontSize = 12.sp,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .padding(6.dp)
                                            )
                                        }
                                        Text(
                                            text = "${childPage[subheadings]}",
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
