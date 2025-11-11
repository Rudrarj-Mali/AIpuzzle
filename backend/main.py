from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import random
import heapq
from typing import List, Tuple, Any

app = FastAPI()

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ========================
# Rock-Paper-Scissors AI
# ========================
class RPSRequest(BaseModel):
    user_move: str
    mode: str  # We accept the mode, even if we don't use it

class RPSResponse(BaseModel):
    user: str
    ai: str
    winner: str

@app.post("/rps/", response_model=RPSResponse)
def rps_ai(request: RPSRequest):
    moves = ["rock", "paper", "scissors"]
    ai_move = random.choice(moves)
    user = request.user_move.lower()

    if user == ai_move:
        result = "draw"
    elif (user == "rock" and ai_move == "scissors") or \
         (user == "paper" and ai_move == "rock") or \
         (user == "scissors" and ai_move == "paper"):
        result = "win"
    else:
        result = "lose"

    # Map our internal result to the response model your app expects
    winner_map = {
        "win": "user",
        "lose": "ai",
        "draw": "draw"
    }

    return RPSResponse(user=user, ai=ai_move, winner=winner_map[result])


# ========================
# Tic Tac Toe AI
# ========================
class TicTacToeRequest(BaseModel):
    board: List[str]  # App sends a flat list of 9
    player: str

class TicTacToeResponse(BaseModel):
    best_move: int  # App expects a single index (0-8)

# --- (Original Minimax Logic) ---
def minimax(board, depth, is_maximizing, player, opponent):
    winner = check_winner(board)
    if winner == player:
        return 10 - depth
    elif winner == opponent:
        return depth - 10
    elif not any(cell == "" for row in board for cell in row):
        return 0

    if is_maximizing:
        best_score = -float("inf")
        for i in range(3):
            for j in range(3):
                if board[i][j] == "":
                    board[i][j] = player
                    score = minimax(board, depth + 1, False, player, opponent)
                    board[i][j] = ""
                    best_score = max(best_score, score)
        return best_score
    else:
        best_score = float("inf")
        for i in range(3):
            for j in range(3):
                if board[i][j] == "":
                    board[i][j] = opponent
                    score = minimax(board, depth + 1, True, player, opponent)
                    board[i][j] = ""
                    best_score = min(best_score, score)
        return best_score

def check_winner(board):
    for row in board:
        if row[0] != "" and row[0] == row[1] == row[2]:
            return row[0]
    for col in range(3):
        if board[0][col] != "" and board[0][col] == board[1][col] == board[2][col]:
            return board[0][col]
    if board[0][0] != "" and board[0][0] == board[1][1] == board[2][2]:
        return board[0][0]
    if board[0][2] != "" and board[0][2] == board[1][1] == board[2][0]:
        return board[0][2]
    return None
# --- (End Original Logic) ---

@app.post("/tictactoe/", response_model=TicTacToeResponse)
def tictactoe_ai(request: TicTacToeRequest):
    # Convert flat board (from app) to 3x3 (for minimax)
    flat_board = request.board
    board_3x3 = [
        [flat_board[0], flat_board[1], flat_board[2]],
        [flat_board[3], flat_board[4], flat_board[5]],
        [flat_board[6], flat_board[7], flat_board[8]]
    ]

    player = request.player
    opponent = "O" if player == "X" else "X"
    best_score = -float("inf")
    best_move_tuple = None

    for i in range(3):
        for j in range(3):
            if board_3x3[i][j] == "":
                board_3x3[i][j] = player
                score = minimax(board_3x3, 0, False, player, opponent)
                board_3x3[i][j] = ""
                if score > best_score:
                    best_score = score
                    best_move_tuple = (i, j)

    # Convert (i, j) tuple (from minimax) back to flat index (for app)
    best_move_index = best_move_tuple[0] * 3 + best_move_tuple[1]

    return TicTacToeResponse(best_move=best_move_index)


# ========================
# Maze Solver AI (BFS, DFS, A*)
# ========================
class MazeRequest(BaseModel):
    maze: List[List[int]]
    start: List[int]  # App sends [row, col]
    end: List[int]    # App sends [row, col]
    algorithm: str

class MazeResponse(BaseModel):
    path: List[List[int]] # App expects list of [row, col]

# --- (Modified Search Algos) ---
# Modified to return list[list[int]] instead of list[tuple[int, int]]
def bfs(maze, start, end):
    queue = [(tuple(start), [start])]
    visited = set([tuple(start)])
    while queue:
        (x, y), path = queue.pop(0)
        if (x, y) == tuple(end):
            return path
        for dx, dy in [(0,1), (1,0), (0,-1), (-1,0)]:
            nx, ny = x + dx, y + dy
            if 0 <= nx < len(maze) and 0 <= ny < len(maze[0]) and maze[nx][ny] == 0 and (nx, ny) not in visited:
                queue.append(((nx, ny), path + [[nx, ny]])) # Return list
                visited.add((nx, ny))
    return None

def dfs(maze, start, end, visited=None, path=None):
    if visited is None: visited = set()
    if path is None: path = []

    start_tuple = tuple(start)
    end_tuple = tuple(end)

    x, y = start_tuple
    if start_tuple == end_tuple:
        return path + [start]

    visited.add(start_tuple)

    for dx, dy in [(0,1), (1,0), (0,-1), (-1,0)]:
        nx, ny = x + dx, y + dy
        if 0 <= nx < len(maze) and 0 <= ny < len(maze[0]) and maze[nx][ny] == 0 and (nx, ny) not in visited:
            res = dfs(maze, [nx, ny], end, visited, path + [start]) # Return list
            if res:
                return res
    return None

def heuristic(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])

def astar(maze, start, end):
    start_tuple = tuple(start)
    end_tuple = tuple(end)
    heap = [(0, start_tuple, [start])] # Store path as list
    visited = set()
    while heap:
        cost, (x, y), path = heapq.heappop(heap)
        if (x, y) == end_tuple:
            return path
        if (x, y) in visited:
            continue
        visited.add((x, y))
        for dx, dy in [(0,1), (1,0), (0,-1), (-1,0)]:
            nx, ny = x + dx, y + dy
            if 0 <= nx < len(maze) and 0 <= ny < len(maze[0]) and maze[nx][ny] == 0:
                new_cost = cost + 1 + heuristic((nx, ny), end_tuple)
                heapq.heappush(heap, (new_cost, (nx, ny), path + [[nx, ny]])) # Return list
    return None
# --- (End Modified Algos) ---

@app.post("/maze/", response_model=MazeResponse)
def maze_ai(request: MazeRequest):
    path = None
    if request.algorithm == "bfs":
        path = bfs(request.maze, request.start, request.end)
    elif request.algorithm == "dfs":
        path = dfs(request.maze, request.start, request.end)
    elif request.algorithm == "astar":
        path = astar(request.maze, request.start, request.end)
    else:
        return {"error": "Invalid algorithm"}

    return MazeResponse(path=path or [])


# ========================
# 8 Puzzle Solver (A*)
# ========================
class EightPuzzleRequest(BaseModel):
    state: List[int] # App sends a flat list of 9

class EightPuzzleResponse(BaseModel):
    states: List[List[int]] # App expects a list of flat-list states

# --- (Original 8-Puzzle Logic) ---
def manhattan_distance(state, goal):
    dist = 0
    for i in range(3):
        for j in range(3):
            val = state[i][j]
            if val != 0:
                gx, gy = [(x,y) for x in range(3) for y in range(3) if goal[x][y] == val][0]
                dist += abs(i - gx) + abs(j - gy)
    return dist

def get_neighbors(state):
    neighbors = []
    x, y = [(i,j) for i in range(3) for j in range(3) if state[i][j] == 0][0]
    moves = [(0,1), (1,0), (0,-1), (-1,0)]
    for dx, dy in moves:
        nx, ny = x + dx, y + dy
        if 0 <= nx < 3 and 0 <= ny < 3:
            new_state = [row[:] for row in state]
            new_state[x][y], new_state[nx][ny] = new_state[nx][ny], new_state[x][y]
            neighbors.append(new_state)
    return neighbors

def eight_puzzle_solver(start, goal):
    heap = [(manhattan_distance(start, goal), 0, start, [start])]
    visited = set()
    while heap:
        est, cost, state, path = heapq.heappop(heap)
        if state == goal:
            return path
        state_tuple = tuple(tuple(row) for row in state)
        if state_tuple in visited:
            continue
        visited.add(state_tuple)
        for neighbor in get_neighbors(state):
            neighbor_tuple = tuple(tuple(row) for row in neighbor)
            if neighbor_tuple not in visited:
                heapq.heappush(
                    heap,
                    (cost + 1 + manhattan_distance(neighbor, goal), cost + 1, neighbor, path + [neighbor])
                )
    return None
# --- (End Original Logic) ---

@app.post("/eight/", response_model=EightPuzzleResponse)
def eight_puzzle_ai(request: EightPuzzleRequest):
    # Convert flat list from app to 3x3 grid
    flat_state = request.state
    start_3x3 = [
        [flat_state[0], flat_state[1], flat_state[2]],
        [flat_state[3], flat_state[4], flat_state[5]],
        [flat_state[6], flat_state[7], flat_state[8]]
    ]

    # Define the goal state
    goal_3x3 = [
        [1, 2, 3],
        [4, 5, 6],
        [7, 8, 0]
    ]

    path_3x3 = eight_puzzle_solver(start_3x3, goal_3x3)

    # Convert list of 3x3 grids (from solver) to list of flat lists (for app)
    if path_3x3:
        solution_flat_lists = [[cell for row in state for cell in row] for state in path_3x3]
    else:
        solution_flat_lists = []

    return EightPuzzleResponse(states=solution_flat_lists)


# ========================
# Run Server
# ========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)