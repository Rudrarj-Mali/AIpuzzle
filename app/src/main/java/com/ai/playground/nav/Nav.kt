package com.ai.playground.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ai.playground.screens.*

sealed class Route(val path: String) {
    object Home : Route("home")
    object Maze : Route("maze")
    object Eight : Route("eight")
    object TicTacToe : Route("tictactoe")
    object Rps : Route("rps")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) {
            HomeScreen(nav = navController)
        }
        composable(Route.Maze.path) {
            MazeScreen()
        }
        composable(Route.Eight.path) {
            EightPuzzleScreen()
        }
        composable(Route.TicTacToe.path) {
            TicTacToeScreen()
        }
        composable(Route.Rps.path) {
            RpsScreen()
        }
    }
}