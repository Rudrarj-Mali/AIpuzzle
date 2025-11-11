package com.ai.playground.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ai.playground.nav.Route
import kotlinx.coroutines.delay

// *** FIX 1: Moved this data class to the top level ***
data class PlaygroundItem(
    val route: Route,
    val name: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(nav: NavController) {

    // List of all our app screens
    val items = listOf(
        PlaygroundItem(Route.Maze, "Maze Runner", Icons.Default.Route),
        PlaygroundItem(Route.Eight, "8-Puzzle", Icons.Default.Calculate),
        PlaygroundItem(Route.TicTacToe, "Tic-Tac-Toe", Icons.Default.GridOn),
        PlaygroundItem(Route.Rps, "Rock-Paper-Scissors", Icons.Default.SportsKabaddi)
    )

    // State for the entry animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100) // Small delay to make the animation noticeable
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI Playground") })
        }
    ) { padding ->
        // Animated container slides and fades in
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            modifier = Modifier.padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 columns
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    GameCard(item = item) {
                        nav.navigate(item.route.path)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// *** FIX 2: Changed parameter type to the top-level 'PlaygroundItem' ***
fun GameCard(item: PlaygroundItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the card a perfect square
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// *** FIX 3: Removed the entire conflicting 'HomeScreenKt' data class ***