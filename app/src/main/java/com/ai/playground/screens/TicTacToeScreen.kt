package com.ai.playground.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ai.playground.network.ApiClient
import kotlinx.coroutines.launch

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
            if (b[line[0]] != "" && b[line[0]] == b[line[1]] && b[line[1]] == b[line[2]]) {
                return b[line[0]]
            }
        }
        return null
    }

    suspend fun makeAIMove() {
        try {
            println("Making AI move with board: $board")
            val res = ApiClient.api.ttt(com.ai.playground.network.TttReq(board, aiMark))
            println("Received response: $res")
            if (res.best_move in 0..8 && board[res.best_move] == "") {
                val newBoard = board.toMutableList()
                newBoard[res.best_move] = aiMark
                board = newBoard
                println("Updated board: $board")
            } else {
                println("Invalid move from AI: ${res.best_move}")
            }
        } catch (e: Exception) {
            println("Error making AI move: ${e.message}")
            e.printStackTrace()
            message = "Error: ${e.message}"
        }
        
        checkWinner(board)?.let { winner ->
            message = if (winner == "X") "You win! 🎉" else "AI wins! 🤖"
        } ?: run {
            if ("" !in board) {
                message = "It's a draw! 🤝"
            } else {
                message = "Your turn: X"
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
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game board
                repeat(3) { row ->
                    Row {
                        repeat(3) { col ->
                            val index = row * 3 + col
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                                    .background(Color.LightGray)
                                    .clickable(
                                        enabled = board[index] == "" && checkWinner(board) == null && message != "AI thinking..."
                                    ) {
                                        if (board[index] == "") {
                                            val newBoard = board.toMutableList()
                                            newBoard[index] = "X"
                                            board = newBoard
                                            
                                            checkWinner(newBoard)?.let { winner ->
                                                message = if (winner == "X") "You win! 🎉" else "AI wins! 🤖"
                                            } ?: run {
                                                if ("" !in newBoard) {
                                                    message = "It's a draw! 🤝"
                                                } else {
                                                    message = "AI thinking..."
                                                    scope.launch {
                                                        makeAIMove()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = board[index],
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = if (board[index] == "X") Color.Blue else Color.Red
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Game status and controls
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
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
}
