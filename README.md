# AI Puzzle Solver

An interactive Android application that showcases various AI algorithms through engaging puzzles and games. The app includes:

- **Maze Solver**: Visualizes BFS, DFS, and A* pathfinding algorithms
- **8-Puzzle Solver**: Implements A* algorithm with Manhattan distance heuristic
- **Tic-Tac-Toe**: Features an unbeatable AI using the Minimax algorithm
- **Rock-Paper-Scissors**: Adaptive AI that learns from your moves

## Project Structure

```
AIpuzzle/
├── backend/               # FastAPI server with AI algorithms
│   ├── main.py           # Main FastAPI application
│   └── requirements.txt  # Python dependencies
├── app/                  # Android application
│   ├── src/main/
│   │   ├── java/com/ai/playground/
│   │   │   ├── MainActivity.kt    # App entry point
│   │   │   ├── Theme.kt          # App theming
│   │   │   ├── nav/              # Navigation components
│   │   │   ├── network/          # API client and models
│   │   │   └── screens/          # UI screens
│   │   └── res/                  # Resources (not shown in detail)
│   └── build.gradle.kts          # App-level build config
└── README.md                     # This file
```

## Setup Instructions

### Backend Setup

1. **Install Python 3.8+** if not already installed.
2. **Install dependencies**:
   ```bash
   cd backend
   pip install -r requirements.txt
   ```
3. **Run the server**:
   ```bash
   python main.py
   ```
   The server will start at `http://localhost:8000`

### Android App Setup

1. **Open the project** in Android Studio
2. **Update API URL** (if needed):
   - For emulator: `http://10.0.2.2:8000` (default)
   - For physical device: Replace with your computer's local IP address
   - The setting is in `app/src/main/java/com/ai/playground/network/ApiService.kt`
3. **Build and run** the app on an emulator or physical device

## Features

### Maze Solver
- Generate random mazes
- Toggle walls by tapping
- Choose between BFS, DFS, and A* algorithms
- Visualize the solving process

### 8-Puzzle
- Interactive sliding puzzle
- Automatic solving using A* algorithm
- Step-by-step solution visualization

### Tic-Tac-Toe
- Play against an unbeatable AI
- Implements Minimax algorithm with alpha-beta pruning

### Rock-Paper-Scissors
- Three AI difficulty levels:
  - Random: Makes random moves
  - Probability: Learns your most common moves
  - Adaptive: Predicts patterns in your gameplay

## Dependencies

### Backend
- FastAPI
- Uvicorn
- Pydantic

### Android App
- Jetpack Compose
- Retrofit for API calls
- Kotlin Coroutines
- Material3 Design

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
