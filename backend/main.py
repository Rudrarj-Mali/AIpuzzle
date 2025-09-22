from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import random
import heapq

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

@app.post("/rps/")
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

    return {"ai_move": ai_move, "result": result}


# ========================
# Tic Tac Toe AI
# ========================
class TicTacToeRequest(BaseModel):
    board: list
    player: str

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

@app.post("/tictactoe/")
def tictactoe_ai(request: TicTacToeRequest):
    board = request.board
    player = request.player
    opponent = "O" if player == "X" else "X"
    best_score = -float("inf")
    best_move = None

    for i in range(3):
        for j in range(3):
            if board[i][j] == "":
                board[i][j] = player
                score = minimax(board, 0, False, player, opponent)
                board[i][j] = ""
                if score > best_score:
                    best_score = score
                    best_move = (i, j)

    return {"best_move": best_move}


# ========================
# Maze Solver AI (BFS, DFS, A*)
# ========================
class MazeRequest(BaseModel):
    maze: list
    start: tuple
    end: tuple
    algorithm: str

def bfs(maze, start, end):
    queue = [(start, [start])]
    visited = set([start])
    while queue:
        (x, y), path = queue.pop(0)
        if (x, y) == end:
            return path
        for dx, dy in [(0,1), (1,0), (0,-1), (-1,0)]:
            nx, ny = x + dx, y + dy
            if 0 <= nx < len(maze) and 0 <= ny < len(maze[0]) and maze[nx][ny] == 0 and (nx, ny) not in visited:
                queue.append(((nx, ny), path + [(nx, ny)]))
                visited.add((nx, ny))
    return None

def dfs(maze, start, end, visited=None, path=None):
    if visited is None: visited = set()
    if path is None: path = []
    x, y = start
    if start == end:
        return path + [start]
    visited.add(start)
    for dx, dy in [(0,1), (1,0), (0,-1), (-1,0)]:
        nx, ny = x + dx, y + dy
        if 0 <= nx < len(maze) and 0 <= ny < len(maze[0]) and maze[nx][ny] == 0 and (nx, ny) not in visited:
            res = dfs(maze, (nx, ny), end, visited, path + [start])
            if res:
                return res
    return None

def heuristic(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])

def astar(maze, start, end):
    heap = [(0, start, [start])]
    visited = set()
    while heap:
        cost, (x, y), path = heapq.heappop(heap)
        if (x, y) == end:
            return path
        if (x, y) in visited:
            continue
        visited.add((x, y))
        for dx, dy in [(0,1), (1,0), (0,-1), (-1,0)]:
            nx, ny = x + dx, y + dy
            if 0 <= nx < len(maze) and 0 <= ny < len(maze[0]) and maze[nx][ny] == 0:
                new_cost = cost + 1 + heuristic((nx, ny), end)
                heapq.heappush(heap, (new_cost, (nx, ny), path + [(nx, ny)]))
    return None

@app.post("/maze/")
def maze_ai(request: MazeRequest):
    if request.algorithm == "bfs":
        path = bfs(request.maze, tuple(request.start), tuple(request.end))
    elif request.algorithm == "dfs":
        path = dfs(request.maze, tuple(request.start), tuple(request.end))
    elif request.algorithm == "astar":
        path = astar(request.maze, tuple(request.start), tuple(request.end))
    else:
        return {"error": "Invalid algorithm"}
    return {"path": path}


# ========================
# 8 Puzzle Solver (A*)
# ========================
class EightPuzzleRequest(BaseModel):
    start: list
    goal: list

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

@app.post("/eight/")
def eight_puzzle_ai(request: EightPuzzleRequest):
    path = eight_puzzle_solver(request.start, request.goal)
    return {"solution": path}


# ========================
# Run Server
# ========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
