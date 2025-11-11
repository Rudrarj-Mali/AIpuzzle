package com.ai.playground.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.playground.logic.findBestTttMove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen() {
    val scope = rememberCoroutineScope()
    var board by remember { mutableStateOf(List(9) { "" }) }
    var message by remember { mutableStateOf("Your turn: X") }
    val aiMark = "O"

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
            listOf(0, 4, 8), listOf(2, 4, 6) // diagonals
        )

        for (line in lines) {
            if (b[line[0]].isNotEmpty() && b[line[0]] == b[line[1]] && b[line[1]] == b[line[2]]) {
                return b[line[0]]
            }
        }
        if ("" !in b) return "Draw" // Check for draw
        return null
    }

    suspend fun makeAIMove() {
        // Run logic on a background thread just in case
        val bestMove = withContext(Dispatchers.Default) {
            findBestTttMove(board, aiMark)
        }

        if (bestMove in 0..8 && board[bestMove] == "") {
            val newBoard = board.toMutableList()
            newBoard[bestMove] = aiMark
            board = newBoard
        }

        when (checkWinner(board)) {
            "X" -> message = "You win! 🎉"
            "O" -> message = "AI wins! 🤖"
            "Draw" -> message = "It's a draw! 🤝"
            else -> message = "Your turn: X"
        }
    }

    fun playerMove(index: Int) {
        if (board[index] != "" || checkWinner(board) != null || message == "AI thinking...") return

        val newBoard = board.toMutableList()
        newBoard[index] = "X"
        board = newBoard

        when (checkWinner(newBoard)) {
            "X" -> message = "You win! 🎉"
            "O" -> message = "AI wins! 🤖"
            "Draw" -> message = "It's a draw! 🤝"
            else -> {
                message = "AI thinking..."
                scope.launch {
                    makeAIMove()
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tic-Tac-Toe") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game status
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Game board
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    repeat(3) { row ->
                        Row {
                            repeat(3) { col ->
                                val index = row * 3 + col
                                GridCell(
                                    mark = board[index],
                                    onClick = { playerMove(index) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // New Game Button
            Button(
                onClick = {
                    board = List(9) { "" }
                    message = "Your turn: X"
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("New Game")
            }
        }
    }
}

@Composable
fun GridCell(mark: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // *** THE FIX: REMOVED AnimatedVisibility ***
            // We just show the Text directly.
            // If 'mark' is empty, Text() will just be empty.
            Text(
                text = mark,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (mark == "X") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}