package com.ai.playground.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ai.playground.nav.Route

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    Scaffold(
        topBar = { 
            TopAppBar(title = { Text("AI Playground") }) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { nav.navigate(Route.Maze.path) },
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("Maze Runner (BFS/DFS/A*)") 
            }
            
            Button(
                onClick = { nav.navigate(Route.Eight.path) },
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("8-Puzzle (A*)") 
            }
            
            Button(
                onClick = { nav.navigate(Route.TicTacToe.path) },
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("Tic-Tac-Toe (Minimax)") 
            }
            
            Button(
                onClick = { nav.navigate(Route.Rps.path) },
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("Rock–Paper–Scissors (Adaptive)") 
            }
        }
    }
}
