package com.ai.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ai.playground.nav.Route
import com.ai.playground.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Home.path
            ) {
                composable(Route.Home.path) { 
                    HomeScreen(navController) 
                }
                composable(Route.Maze.path) { 
                    MazeScreen() 
                }
                composable(Route.TicTacToe.path) { 
                    TicTacToeScreen() 
                }
                composable(Route.Eight.path) { 
                    EightPuzzleScreen() 
                }
                composable(Route.Rps.path) { 
                    RpsScreen() 
                }
            }
        }
    }
}
