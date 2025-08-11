# Checkers Game Engine

NOT FULLY FUNCTIONAL

A Java-based Checkers game engine that allows a human player to play against a computer opponent on either an 8x8 or 10x10 board. The game state is persisted 
using Redis.

## Features

- Play Checkers on an 8x8 or 10x10 board
- Human vs Computer gameplay
- Text-based interface
- Game state persistence using Redis
- Support for standard Checkers rules including:
  - Regular moves
  - Jumps (captures)
  - Multi-jumps
  - Promotion to king
  - Mandatory captures

## Requirements

- Java 17 or higher
- Gradle for building the project
- Redis server (optional - embedded Redis server included, see [Redis Setup](#redis-setup))

## Building the Project

```bash
./gradlew build
```

## Running the Server (REST API)

```bash
./gradlew bootRun
```

- Server will start at http://localhost:8080
- CORS is enabled for http://localhost:3000 (the React client default)
- Note: If you prefer the API under a context path like /api, set `server.servlet.context-path=/api` in `src/main/resources/application.properties` and the endpoints below will be prefixed accordingly.

## RESTful API

The application provides a RESTful API for playing the game.

### Endpoints

- `POST /games` - Create a new game
  - Request body: `{ "playerName": "string", "boardSize": "EIGHT_BY_EIGHT|TEN_BY_TEN", "playerColor": "RED|BLACK" }`
  - Response: Game state including board, players, and current turn

- `GET /games/{gameId}` - Get game state
  - Response: Complete game state

- `GET /games/{gameId}/board` - Get a human-readable display of the game board
  - Response: Board display in ASCII format

- `POST /games/{gameId}/moves` - Make a move
  - For human move: `{ "gameId": "string", "playerId": "string", "from": {"row": number, "column": number}, "to": {"row": number, "column": number} }`
  - For computer move (triggered by client after >=3s): `{ "gameId": "string", "playerId": "string", "from": null, "to": null }`
  - Response: Updated game state

### Example Usage

1. Create a new game:
   ```
   POST /games
   {
     "playerName": "Player1",
     "boardSize": "EIGHT_BY_EIGHT",
     "playerColor": "RED"
   }
   ```

2. View the board:
   ```
   GET /games/{gameId}/board
   ```

3. Make a move:
   ```
   POST /games/{gameId}/moves
   {
     "gameId": "{gameId}",
     "playerId": "{playerId}",
     "from": {"row": 5, "column": 0},
     "to": {"row": 4, "column": 1}
   }
   ```

## CORS

See CORS_CONFIGURATION.md for details. By default, the API allows requests from http://localhost:3000.

## Client (React)

A simple React UI is available in the `checkers-client` folder.
- Setup and usage: see `checkers-client/README.md`
- Default dev URL: http://localhost:3000 (expects server at http://localhost:8080)

## How to Play

1. Start the application
2. Select "New Game" from the main menu
3. Enter your name
4. Choose a board size (8x8 or 10x10)
5. Select your color (Red or Black)
6. Make moves by entering coordinates in the format `row1,col1 row2,col2`
   - For example, to move from position (2,1) to (3,2), enter: `2,1 3,2`
7. The game will automatically handle turn switching, move validation, and game state management

## Game Rules

- On 8x8 boards: English/American checkers rules
  - Red moves first
  - Men move one step diagonally forward only; men capture forward only
  - Kings move one step diagonally in any direction; kings capture with an immediate landing square
  - Captures are mandatory; multiple captures are allowed when available
  - A piece is promoted to a king when it reaches the opposite end of the board
  - A player wins when the opponent has no pieces left or cannot make a legal move

- On 10x10 boards: International draughts rules
  - Red moves first
  - Men move one step diagonally forward; men may capture both forward and backward
  - Kings are flying kings: they move any number of squares diagonally across empty squares and may land on any empty square beyond a captured piece
  - Captures are mandatory; multiple captures are allowed and chaining is supported
  - A piece is promoted to a king when it reaches the opposite end of the board
  - A player wins when the opponent has no pieces left or cannot make a legal move

## Implementation Details

- Uses enums instead of booleans for better type safety and readability
- Employs factory patterns instead of if-then-else statements
- Methods have at most 3 parameters, using method input objects for more complex operations
- Flattened methods by returning as soon as possible
- Uses Redis hash data structures for game state persistence

## Project Structure

- `model`: Contains the domain model classes (Board, Piece, Player, etc.)
- `engine`: Contains the game logic (GameEngine, MoveValidator, ComputerPlayer)
- `persistence`: Contains the Redis-based persistence layer
- `api`: Contains the RESTful API components
  - `controller`: REST controllers that handle HTTP requests
  - `dto`: Data Transfer Objects for API requests and responses
- `CheckersApp`: Main application class with the text-based interface
- `CheckersRestApplication`: Main application class for the RESTful API

## Redis Data Model

- Game metadata stored in Redis hashes
- Board state stored in Redis hashes
- Game IDs stored in Redis sets

## Redis Setup

The application requires a Redis server running on localhost:6379. Here are the options for setting up Redis:

### Option 1: Install Redis Server (Recommended)

#### macOS
```bash
# Install Redis
brew install redis

# Start Redis server
brew services start redis
```

#### Windows
1. Download Redis for Windows from [https://github.com/microsoftarchive/redis/releases](https://github.com/microsoftarchive/redis/releases)
2. Run the installer
3. Start Redis server by running `redis-server.exe`

#### Linux (Ubuntu/Debian)
```bash
# Install Redis
sudo apt update
sudo apt install redis-server

# Start Redis service
sudo systemctl start redis-server
```

### Option 2: Use Docker
If you have Docker installed, you can run Redis in a container:
```bash
docker run --name redis -p 6379:6379 -d redis
```

### Option 3: Use Embedded Redis (Development Only)
For development purposes, you can use an embedded Redis server that runs within your application:
1. Add the embedded Redis dependency to your project
2. Create a utility class to manage the embedded server
3. Start the server when your application launches

For detailed implementation instructions and other options, see the [REDIS_SETUP.md](REDIS_SETUP.md) file.
