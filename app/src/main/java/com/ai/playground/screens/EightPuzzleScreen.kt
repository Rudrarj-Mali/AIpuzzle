package com.ai.playground.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ai.playground.logic.solveEightPuzzle // <-- Import local logic
import kotlinx.coroutines.Dispatchers // <-- Import Coroutine utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EightPuzzleScreen() {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf((0..8).shuffled()) }
    var isSolving by remember { mutableStateOf(false) }
    var moveCount by remember { mutableStateOf(0) }
    var showSolution by remember { mutableStateOf(false) }
    var solution by remember { mutableStateOf<List<List<Int>>>(emptyList()) }
    var currentStep by remember { mutableStateOf(0) }
    val buttonScale by animateFloatAsState(if (isSolving) 0.95f else 1f)

    fun isSolvable(puzzle: List<Int>): Boolean {
        var inversions = 0
        for (i in puzzle.indices) {
            for (j in i + 1 until puzzle.size) {
                if (puzzle[i] != 0 && puzzle[j] != 0 && puzzle[i] > puzzle[j]) {
                    inversions++
                }
            }
        }
        return inversions % 2 == 0
    }

    fun shufflePuzzle() {
        var newPuzzle: List<Int>
        do {
            newPuzzle = (0..8).shuffled()
        } while (!isSolvable(newPuzzle))
        state = newPuzzle
        moveCount = 0
        showSolution = false
        solution = emptyList()
        currentStep = 0
    }

    fun moveTile(index: Int) {
        if (isSolving) return

        val zeroIndex = state.indexOf(0)
        val zeroRow = zeroIndex / 3
        val zeroCol = zeroIndex % 3
        val tileRow = index / 3
        val tileCol = index % 3

        // Check if the tile is adjacent to the empty space
        if ((tileRow == zeroRow && (tileCol == zeroCol - 1 || tileCol == zeroCol + 1)) ||
            (tileCol == zeroCol && (tileRow == zeroRow - 1 || tileRow == zeroRow + 1))
        ) {
            val newState = state.toMutableList()
            newState[zeroIndex] = newState[index]
            newState[index] = 0
            state = newState
            moveCount++

            // Check if puzzle is solved
            if (state.take(8) == (1..8).toList() && state[8] == 0) {
                // Puzzle solved
            }
        }
    }

    suspend fun solvePuzzle() {
        if (isSolving) return

        isSolving = true
        showSolution = false

        // Run solving on a background thread
        val solutionPath = withContext(Dispatchers.Default) {
            solveEightPuzzle(state) // Pass the flat list directly
        }

        if (solutionPath.isNotEmpty()) {
            solution = solutionPath
            showSolution = true
            currentStep = 0

            // Animate the solution
            for (step in solution.indices) {
                state = solution[step]
                currentStep = step
                delay(300) // Animation speed
            }
        }

        isSolving = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("8-Puzzle Solver") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { shufflePuzzle() },
                modifier = Modifier.scale(buttonScale)
            ) {
                Icon(Icons.Default.Refresh, "Shuffle")
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
            // Puzzle board
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) { row ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(3) { col ->
                                val index = row * 3 + col
                                val number = state[index]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(4.dp)
                                        .background(
                                            if (number == 0) Color.Transparent
                                            else MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable(
                                            enabled = !isSolving && !showSolution && number != 0
                                        ) { moveTile(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (number != 0) {
                                        Text(
                                            text = number.toString(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { scope.launch { solvePuzzle() } },
                    enabled = !isSolving && !showSolution
                ) {
                    Text(if (isSolving) "Solving..." else "Solve with A*")
                }

                Text(
                    text = "Moves: $moveCount",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Solution progress
            if (showSolution) {
                LinearProgressIndicator(
                    progress = if (solution.size > 0) (currentStep + 1).toFloat() / solution.size else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Text(
                    text = "Solution step: $currentStep/${solution.size - 1}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Preview for the 8-Puzzle screen

@Composable
fun EightPuzzlePreview() {
    EightPuzzleScreen()
    EightPuzzleScreen()
}