package com.ai.playground.nav

sealed class Route(val path: String) {
    object Home: Route("home")
    object Maze: Route("maze")
    object TicTacToe: Route("tictactoe")
    object Eight: Route("eight")
    object Rps: Route("rps")
}
