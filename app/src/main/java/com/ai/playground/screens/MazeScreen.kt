package com.ai.playground.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ai.playground.logic.solveMazeWith // <-- Import local logic
import kotlinx.coroutines.Dispatchers // <-- Import Coroutine utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MazeScreen() {
    val scope = rememberCoroutineScope()
    val rows = 10
    val cols = 10

    // Initialize maze with borders
    var maze by remember {
        mutableStateOf(
            List(rows) { row ->
                MutableList(cols) { col ->
                    if (row == 0 || row == rows - 1 || col == 0 || col == cols - 1) 1 else 0
                }
            }
        )
    }

    val start = 1 to 1
    val end = rows - 2 to cols - 2

    var path by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var isSolving by remember { mutableStateOf(false) }
    var selectedAlgo by remember { mutableStateOf("astar") }
    val buttonScale by animateFloatAsState(if (isSolving) 0.95f else 1f)

    fun toggleWall(row: Int, col: Int) {
        if ((row == start.first && col == start.second) ||
            (row == end.first && col == end.second)) return

        val newMaze = maze.map { it.toMutableList() }
        newMaze[row][col] = if (newMaze[row][col] == 0) 1 else 0
        maze = newMaze
        path = emptyList()
    }

    fun generateRandomMaze() {
        val newMaze = List(rows) { MutableList(cols) { 0 } }

        // Add borders
        for (i in 0 until rows) {
            newMaze[i][0] = 1
            newMaze[i][cols - 1] = 1
        }
        for (j in 0 until cols) {
            newMaze[0][j] = 1
            newMaze[rows - 1][j] = 1
        }

        // Add random walls (25% chance)
        for (i in 1 until rows - 1) {
            for (j in 1 until cols - 1) {
                if (i == start.first && j == start.second) continue
                if (i == end.first && j == end.second) continue
                if (Random.nextFloat() < 0.25f) {
                    newMaze[i][j] = 1
                }
            }
        }

        maze = newMaze
        path = emptyList()
    }

    suspend fun animateSolution(steps: List<Pair<Int, Int>>) {
        val animatedPath = mutableListOf<Pair<Int, Int>>()
        for (step in steps) {
            animatedPath.add(step)
            path = animatedPath.toList()
            delay(50) // Animation speed
        }
    }

    fun solveMaze() {
        if (isSolving) return

        scope.launch {
            isSolving = true
            path = emptyList() // Clear old path

            // Run solving on a background thread
            val resultPath = withContext(Dispatchers.Default) {
                solveMazeWith(
                    maze = maze,
                    start = listOf(start.first, start.second),
                    end = listOf(end.first, end.second),
                    algorithm = selectedAlgo
                )
            }

            if (resultPath != null && resultPath.isNotEmpty()) {
                val steps = resultPath.map { it[0] to it[1] }
                animateSolution(steps)
            }

            isSolving = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Maze Solver") }) },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { generateRandomMaze() },
                        modifier = Modifier.scale(buttonScale)
                    ) {
                        Icon(Icons.Default.Refresh, "Randomize")
                    }

                    Button(
                        onClick = { solveMaze() },
                        enabled = !isSolving,
                        modifier = Modifier.scale(buttonScale)
                    ) {
                        Text(if (isSolving) "Solving..." else "Solve")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Algorithm selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("BFS" to "bfs", "DFS" to "dfs", "A*" to "astar").forEach { (name, algo) ->
                    FilterChip(
                        selected = selectedAlgo == algo,
                        onClick = {
                            if (!isSolving) {
                                selectedAlgo = algo
                                path = emptyList()
                            }
                        },
                        label = { Text(name) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Maze grid
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(2.dp, Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    maze.forEachIndexed { row, rowList ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            rowList.forEachIndexed { col, cell ->
                                val isPath = path.contains(row to col)
                                val isStart = row == start.first && col == start.second
                                val isEnd = row == end.first && col == end.second

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            when {
                                                isStart -> Color.Green
                                                isEnd -> Color.Red
                                                isPath -> Color.Blue.copy(alpha = 0.3f)
                                                cell == 1 -> Color.Black
                                                else -> Color.White
                                            }
                                        )
                                        .border(0.5.dp, Color.Gray)
                                        .clickable(
                                            enabled = !isSolving && !isStart && !isEnd
                                        ) { toggleWall(row, col) }
                                )
                            }
                        }
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color.Green, text = "Start")
                LegendItem(color = Color.Red, text = "End")
                LegendItem(color = Color.Blue.copy(alpha = 0.3f), text = "Path")
                LegendItem(color = Color.Black, text = "Wall")
            }

            // Instructions
            Text(
                text = "Tap to toggle walls. Click Solve to find the path.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(2.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}