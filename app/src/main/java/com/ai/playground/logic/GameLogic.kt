package com.ai.playground.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.PriorityQueue
import java.util.ArrayDeque
import kotlin.math.abs

// ========================
// Rock-Paper-Scissors
// ========================
data class RpsResult(val user: String, val ai: String, val winner: String)

fun getRpsResult(userMove: String): RpsResult {
    val moves = listOf("rock", "paper", "scissors")
    val aiMove = moves.random()
    val user = userMove.lowercase()

    val winner = when {
        user == aiMove -> "draw"
        (user == "rock" && aiMove == "scissors") ||
                (user == "paper" && aiMove == "rock") ||
                (user == "scissors" && aiMove == "paper") -> "user"
        else -> "ai"
    }
    return RpsResult(user = user, ai = aiMove, winner = winner)
}

// ========================
// Tic Tac Toe
// ========================

private fun checkTttWinner(board: List<List<String>>): String? {
    // Check rows
    for (row in board) {
        if (row[0].isNotEmpty() && row[0] == row[1] && row[1] == row[2]) {
            return row[0]
        }
    }
    // Check columns
    for (col in 0..2) {
        if (board[0][col].isNotEmpty() && board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
            return board[0][col]
        }
    }
    // Check diagonals
    if (board[0][0].isNotEmpty() && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
        return board[0][0]
    }
    if (board[0][2].isNotEmpty() && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
        return board[0][2]
    }
    // Check for draw
    if (board.all { row -> row.all { it.isNotEmpty() } }) {
        return "draw"
    }
    return null
}

private fun minimax(
    board: MutableList<MutableList<String>>,
    depth: Int,
    isMaximizing: Boolean,
    player: String,
    opponent: String
): Int {
    when (checkTttWinner(board)) {
        player -> return 10 - depth
        opponent -> return depth - 10
        "draw" -> return 0
    }

    if (isMaximizing) {
        var bestScore = Int.MIN_VALUE
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    board[i][j] = player
                    val score = minimax(board, depth + 1, false, player, opponent)
                    board[i][j] = ""
                    bestScore = maxOf(bestScore, score)
                }
            }
        }
        return bestScore
    } else {
        var bestScore = Int.MAX_VALUE
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    board[i][j] = opponent
                    val score = minimax(board, depth + 1, true, player, opponent)
                    board[i][j] = ""
                    bestScore = minOf(bestScore, score)
                }
            }
        }
        return bestScore
    }
}

fun findBestTttMove(flatBoard: List<String>, player: String): Int {
    val opponent = if (player == "X") "O" else "X"
    var bestScore = Int.MIN_VALUE
    var bestMove: Pair<Int, Int>? = null

    // Convert flat board to 3x3
    val board_3x3 = mutableListOf(
        mutableListOf(flatBoard[0], flatBoard[1], flatBoard[2]),
        mutableListOf(flatBoard[3], flatBoard[4], flatBoard[5]),
        mutableListOf(flatBoard[6], flatBoard[7], flatBoard[8])
    )

    for (i in 0..2) {
        for (j in 0..2) {
            if (board_3x3[i][j] == "") {
                board_3x3[i][j] = player
                val score = minimax(board_3x3, 0, false, player, opponent)
                board_3x3[i][j] = ""
                if (score > bestScore) {
                    bestScore = score
                    bestMove = Pair(i, j)
                }
            }
        }
    }

    // Convert 3x3 index back to flat index
    return bestMove?.let { (i, j) -> i * 3 + j } ?: -1 // return -1 if no move found (shouldn't happen)
}


// ========================
// Maze Solver
// ========================
private typealias Maze = List<List<Int>>
private typealias Point = Pair<Int, Int>
private typealias Path = List<List<Int>>

// --- BFS ---
private fun bfs(maze: Maze, start: List<Int>, end: List<Int>): Path? {
    val startPt = Point(start[0], start[1])
    val endPt = Point(end[0], end[1])
    val queue: ArrayDeque<Pair<Point, Path>> = ArrayDeque()
    queue.add(Pair(startPt, listOf(start)))
    val visited = mutableSetOf(startPt)

    while (queue.isNotEmpty()) {
        val (current, path) = queue.removeFirst()
        if (current == endPt) {
            return path
        }

        for ((dx, dy) in listOf(Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0))) {
            val nextPt = Point(current.first + dx, current.second + dy)
            val (nx, ny) = nextPt
            if (nx in maze.indices && ny in maze[0].indices && maze[nx][ny] == 0 && nextPt !in visited) {
                visited.add(nextPt)
                queue.add(Pair(nextPt, path + listOf(listOf(nx, ny))))
            }
        }
    }
    return null
}

// --- DFS ---
private fun dfs(maze: Maze, start: List<Int>, end: List<Int>): Path? {
    val startPt = Point(start[0], start[1])
    val endPt = Point(end[0], end[1])
    val visited = mutableSetOf<Point>()

    fun solve(current: Point, path: Path): Path? {
        if (current == endPt) {
            return path
        }
        visited.add(current)

        for ((dx, dy) in listOf(Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0))) {
            val nextPt = Point(current.first + dx, current.second + dy)
            val (nx, ny) = nextPt
            if (nx in maze.indices && ny in maze[0].indices && maze[nx][ny] == 0 && nextPt !in visited) {
                val result = solve(nextPt, path + listOf(listOf(nx, ny)))
                if (result != null) return result
            }
        }
        return null
    }

    return solve(startPt, listOf(start))
}

// --- A* ---
private fun heuristic(a: Point, b: Point): Int {
    return abs(a.first - b.first) + abs(a.second - b.second)
}

private fun astar(maze: Maze, start: List<Int>, end: List<Int>): Path? {
    val startPt = Point(start[0], start[1])
    val endPt = Point(end[0], end[1])

    // PriorityQueue stores: Triple(total_cost, point, path)
    val heap = PriorityQueue<Triple<Int, Point, Path>>(compareBy { it.first })
    heap.add(Triple(0, startPt, listOf(start)))
    val visited = mutableSetOf<Point>()

    while (heap.isNotEmpty()) {
        val (cost, current, path) = heap.poll()
        if (current == endPt) {
            return path
        }
        if (current in visited) {
            continue
        }
        visited.add(current)

        for ((dx, dy) in listOf(Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0))) {
            val nextPt = Point(current.first + dx, current.second + dy)
            val (nx, ny) = nextPt

            if (nx in maze.indices && ny in maze[0].indices && maze[nx][ny] == 0) {
                val newCost = path.size + heuristic(nextPt, endPt)
                heap.add(Triple(newCost, nextPt, path + listOf(listOf(nx, ny))))
            }
        }
    }
    return null
}

// --- Main Maze Function ---
fun solveMazeWith(maze: Maze, start: List<Int>, end: List<Int>, algorithm: String): Path? {
    return when (algorithm) {
        "bfs" -> bfs(maze, start, end)
        "dfs" -> dfs(maze, start, end)
        "astar" -> astar(maze, start, end)
        else -> null
    }
}

// ========================
// 8 Puzzle
// ========================
private typealias PuzzleState = List<List<Int>>
private typealias FlatState = List<Int>

private fun manhattanDistance(state: PuzzleState, goal: PuzzleState): Int {
    var dist = 0
    val goalPositions = mutableMapOf<Int, Point>()
    for (i in 0..2) {
        for (j in 0..2) {
            goalPositions[goal[i][j]] = Point(i, j)
        }
    }

    for (i in 0..2) {
        for (j in 0..2) {
            val value = state[i][j]
            if (value != 0) {
                val (gx, gy) = goalPositions[value]!!
                dist += abs(i - gx) + abs(j - gy)
            }
        }
    }
    return dist
}

private fun getNeighbors(state: PuzzleState): List<PuzzleState> {
    val neighbors = mutableListOf<PuzzleState>()
    var x = -1
    var y = -1
    // Find empty tile (0)
    for (i in 0..2) {
        for (j in 0..2) {
            if (state[i][j] == 0) {
                x = i
                y = j
                break
            }
        }
    }

    for ((dx, dy) in listOf(Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0))) {
        val (nx, ny) = Pair(x + dx, y + dy)
        if (nx in 0..2 && ny in 0..2) {
            val newState = state.map { it.toMutableList() }.toMutableList()
            // Swap
            val temp = newState[x][y]
            newState[x][y] = newState[nx][ny]
            newState[nx][ny] = temp
            neighbors.add(newState.map { it.toList() }) // Add immutable copy
        }
    }
    return neighbors
}

// Helper to make a state hashable for the visited set
private fun PuzzleState.toHashableString(): String = this.joinToString { it.joinToString(",") }

// *** FIX IS HERE ***
// This data class replaces the confusing Triple
private data class PuzzleNode(
    val est: Int,
    val cost: Int,
    val state: PuzzleState,
    val path: List<PuzzleState>
)

fun solveEightPuzzle(flatStart: FlatState): List<FlatState> {
    // Convert flat list to 3x3
    val startState: PuzzleState = listOf(
        flatStart.subList(0, 3),
        flatStart.subList(3, 6),
        flatStart.subList(6, 9)
    )
    val goalState: PuzzleState = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 0)
    )

    // *** FIX IS HERE ***
    // Use the new PuzzleNode data class
    val heap = PriorityQueue<PuzzleNode>(compareBy { it.est })
    heap.add(
        PuzzleNode(
            est = manhattanDistance(startState, goalState),
            cost = 0,
            state = startState,
            path = listOf(startState)
        )
    )
    val visited = mutableSetOf<String>()

    while (heap.isNotEmpty()) {
        // *** FIX IS HERE ***
        // Destructure the PuzzleNode
        val (est, cost, state, path) = heap.poll()!!

        if (state == goalState) {
            // Convert list of 3x3 states to list of flat states
            return path.map { it.flatten() }
        }

        val stateHash = state.toHashableString()
        if (stateHash in visited) {
            continue
        }
        visited.add(stateHash)

        for (neighbor in getNeighbors(state)) {
            if (neighbor.toHashableString() !in visited) {
                val newCost = cost + 1
                val newEst = newCost + manhattanDistance(neighbor, goalState)
                // *** FIX IS HERE ***
                // Add a new PuzzleNode
                heap.add(
                    PuzzleNode(
                        est = newEst,
                        cost = newCost,
                        state = neighbor,
                        path = path + listOf(neighbor)
                    )
                )
            }
        }
    }

    return emptyList() // No solution found
}