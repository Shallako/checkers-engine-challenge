# Checkers React Client

NOT FULLY FUNCTIONAL

This is a standalone React client for the BCheckers engine. It is intentionally placed in a separate folder (checkers-client) and only communicates with the server in two cases:
- Create a new game
- Submit a human move (and after at least 3 seconds, trigger a computer move)

No polling of the server is performed.

## Prerequisites
- Node.js 18+
- The backend server running at http://localhost:8080
  - The server already enables CORS for http://localhost:3000

## Getting Started
1. Install dependencies
   npm install

2. Start the dev server (port 3000)
   npm run dev

3. Open the app
   http://localhost:3000

## Configuration
- API base URL is defined in `src/api.ts` as `BASE_URL`. If your server runs elsewhere, update it (e.g., to include a context path like `http://localhost:8080/api`).

## How it works
- New game: POST /games with body { playerName, boardSize, playerColor }
- Human move: POST /games/{gameId}/moves with body { gameId, playerId, from, to }
- Computer move (after >=3s): POST the same endpoint with from and to as null for the current computer player

## Board representation
- The server returns a map of pieces keyed by the string form of Position: "Position[row=R, column=C]".
- The client uses the same addressing to ensure the client board exactly mirrors the server board.
- Only dark squares are interactive.

## Notes
- If a human move is invalid, the server will reject it; the client shows an error and avoids polling.
- If the computer moves first, the server may already make a move during game creation; the server can also automatically make the first move during game creation when the computer goes first; the client renders the returned state.
