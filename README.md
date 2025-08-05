# Checkers Game Engine

A Java-based Checkers game engine that allows a human player to play against a computer opponent on either an 8x8 or 10x10 board. The game state is persisted using Redis.

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
- Redis server running on localhost:6379 (see [Redis Setup](#redis-setup))
- Gradle for building the project

## Building the Project

```bash
./gradlew build
```

## Running the Game

```bash
./gradlew run
```

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

The game follows standard Checkers rules:

- Red moves first
- Pieces move diagonally forward (unless they are kings)
- Kings can move diagonally in any direction
- Captures are made by jumping over an opponent's piece
- Multiple captures in a single turn are allowed and mandatory
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
- `CheckersApp`: Main application class with the text-based interface

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
