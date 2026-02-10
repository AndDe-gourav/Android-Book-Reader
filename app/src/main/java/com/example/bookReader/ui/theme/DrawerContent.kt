package com.example.bookReader.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bookReader.R

@Composable
fun DrawerContent(
    libraryViewModel: LibraryViewModel,
    bookStateViewModel: BookStateViewModel,
    collectionViewModel: CollectionViewModel,
    navController: NavController,
    onBackPressed: () -> Unit,
    toCloseDrawer: () -> Unit,
    currentScreen: String,
    modifier: Modifier = Modifier
) {
    val allBooks by libraryViewModel.allBooks.collectAsState()
    val allCollections by collectionViewModel.allCollections.collectAsState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Header
        DrawerHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Items
        DrawerNavigationItem(
            icon = R.drawable.ic_launcher_foreground,
            label = "Home",
            isSelected = currentScreen == "homeScreen",
            onClick = {
                navController.navigate("homeScreen") {
                    popUpTo("homeScreen") { inclusive = true }
                }
                toCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = R.drawable.chart,
            label = "Statistics",
            isSelected = currentScreen == "StatsScreen",
            onClick = {
                navController.navigate("StatsScreen")
                toCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = R.drawable.ic_launcher_foreground,
            label = "Edit Library",
            isSelected = currentScreen == "EditScreen",
            onClick = {
                navController.navigate("EditScreen")
                toCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Library Stats
        Text(
            text = "Library",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LibraryStats(
            totalBooks = allBooks.size,
            totalCollections = allCollections.size
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // PDF Selection
        PDFSelection(
            navController = navController,
            libraryViewModel = libraryViewModel,
            toCloseDrawer = toCloseDrawer
        )

        Spacer(modifier = Modifier.weight(1f))

        // Settings/About
        DrawerNavigationItem(
            icon = R.drawable.ic_launcher_foreground,
            label = "Settings",
            isSelected = false,
            onClick = {
                // Navigate to settings
                toCloseDrawer()
            }
        )
    }
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Book Reader",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun LibraryStats(
    totalBooks: Int,
    totalCollections: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Books",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$totalBooks",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Collections",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$totalCollections",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PDFSelection(
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    toCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    // You can implement PDF selection here or reference the QuickPdfSelection from HomeScreen
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                // Handle PDF selection
                toCloseDrawer()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add_round),
                contentDescription = "Add Book",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Add New Book",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}