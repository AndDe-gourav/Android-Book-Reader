package com.example.epubreader

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Drawer(
    navController: NavController,
    bookDataViewModel: BookDataViewModel,
    toCloseDrawer: () -> Unit,
    currentScreen: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        DrawerRow(
            painter = painterResource(id = R.drawable.home),
            text = "Home",
            currentScreen = currentScreen,
            label = "homeScreen",
            toCloseDrawer = toCloseDrawer
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.notebook),
            text = "Books",
            currentScreen = currentScreen,
            toCloseDrawer = toCloseDrawer

        )
        DrawerRow(
            painter = painterResource(id = R.drawable.group),
            text = "Authors",
            currentScreen = currentScreen,
            toCloseDrawer = toCloseDrawer

        )
        PDFSelection(
            navController = navController,
            bookDataViewModel = bookDataViewModel,
            toCloseDrawer = toCloseDrawer
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.setting_line),
            text = "Settings",
            currentScreen = currentScreen,
            toCloseDrawer = toCloseDrawer
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.comment),
            text = "Feedback",
            currentScreen = currentScreen,
            toCloseDrawer = toCloseDrawer
        )


    }
}

@Composable
fun DrawerRow(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    label: String = "",
    currentScreen: String,
    toCloseDrawer: () -> Unit,
    onDrawerItemClick: () -> Unit = {},
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                width = 0.5.dp,
                color = if (currentScreen == label) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onDrawerItemClick()
                if (currentScreen == label) {
                    toCloseDrawer()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painter,
                contentDescription = "Navigators",
                tint = if (currentScreen == label) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.inverseSurface
            )
            Spacer(
                modifier.width(dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (currentScreen == label) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.inverseSurface
            )
        }
    }
}