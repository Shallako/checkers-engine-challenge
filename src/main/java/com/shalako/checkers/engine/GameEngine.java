package com.shalako.checkers.engine;

import com.shalako.checkers.model.*;
import com.shalako.checkers.persistence.GameRepository;

import java.time.Instant;
import java.util.*;

/**
 * The main engine for the checkers game.
 * Handles game logic, move validation, and game state management.
 */
public class GameEngine {
    private final GameRepository gameRepository;
    private final MoveValidator moveValidator;
    private final ComputerPlayer computerPlayer;

    public GameEngine(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.moveValidator = new MoveValidator();
        this.computerPlayer = new ComputerPlayer();
    }

    /**
     * Creates a new game with the specified parameters.
     */
    public Game createGame(BoardSize boardSize, String playerName, PlayerColor playerColor) {
        Game game = Game.GameFactory.createHumanVsComputerGame(boardSize, playerName, playerColor);
        return gameRepository.saveGame(game);
    }

    /**
     * Gets a game by its ID.
     */
    public Game getGame(String gameId) {
        return gameRepository.getGame(gameId);
    }

    /**
     * Makes a move in the game based on the move request.
     */
    public Game makeMove(MoveRequest moveRequest) {
        Game game = gameRepository.getGame(moveRequest.getGameId());
        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + moveRequest.getGameId());
        }

        if (game.isGameOver()) {
            throw new IllegalStateException("Game is already over");
        }

        Player currentPlayer = game.getCurrentPlayer();
        if (!currentPlayer.getId().equals(moveRequest.getPlayerId())) {
            throw new IllegalStateException("Not your turn");
        }

        // Validate the move
        Move move = moveValidator.validateMove(game, moveRequest);
        
        // For computer moves, the validator returns null and we let the ComputerPlayer select a move
        if (move == null) {
            return makeComputerMove(game);
        }
        
        // Execute the move
        Game updatedGame = executeMove(game, move);
        
        // If it's the computer's turn, make a computer move
        if (!updatedGame.isGameOver() && updatedGame.getCurrentPlayer().getType() == PlayerType.COMPUTER) {
            updatedGame = makeComputerMove(updatedGame);
        }
        
        return updatedGame;
    }

    /**
     * Executes a validated move and updates the game state.
     */
    private Game executeMove(Game game, Move move) {
        // Create a copy of the board pieces
        Map<Position, Piece> pieces = new HashMap<>(game.getBoard().getPieces());
        
        // Get the piece being moved
        Piece piece = pieces.get(move.getFrom());
        if (piece == null) {
            throw new IllegalStateException("No piece at position: " + move.getFrom());
        }
        
        // Remove the piece from its original position
        pieces.remove(move.getFrom());
        
        // Check for promotion
        if (move.isPromotion()) {
            piece = piece.promote();
        }
        
        // Place the piece at the new position
        pieces.put(move.getTo(), piece);
        
        // Remove any captured pieces
        for (Position capturedPos : move.getCapturedPieces()) {
            pieces.remove(capturedPos);
        }
        
        // Create a new board with the updated pieces
        Board newBoard = Board.BoardFactory.createCustomBoard(game.getBoard().getSize(), pieces);
        
        // Check for game over conditions
        GameState newState = determineGameState(newBoard, game.getCurrentTurn().getOpposite());
        
        // Create a new game with the updated state
        return Game.GameFactory.createGame(
            game.getId(),
            newBoard,
            game.getRedPlayer(),
            game.getBlackPlayer(),
            newState.isGameOver() ? game.getCurrentTurn() : game.getCurrentTurn().getOpposite(),
            newState,
            game.getCreatedAt(),
            Instant.now()
        );
    }

    /**
     * Makes a move for the computer player.
     */
    private Game makeComputerMove(Game game) {
        Move computerMove = computerPlayer.selectMove(game);
        if (computerMove == null) {
            // No valid moves, computer loses
            GameState newState = game.getCurrentTurn() == PlayerColor.RED ? 
                                GameState.BLACK_WON : GameState.RED_WON;
            
            return Game.GameFactory.createGame(
                game.getId(),
                game.getBoard(),
                game.getRedPlayer(),
                game.getBlackPlayer(),
                game.getCurrentTurn(),
                newState,
                game.getCreatedAt(),
                Instant.now()
            );
        }
        
        return executeMove(game, computerMove);
    }

    /**
     * Determines the game state after a move.
     */
    private GameState determineGameState(Board board, PlayerColor nextTurn) {
        // Check if any player has no pieces left
        boolean redHasPieces = false;
        boolean blackHasPieces = false;
        
        for (Piece piece : board.getPieces().values()) {
            if (piece.getColor() == PlayerColor.RED) {
                redHasPieces = true;
            } else {
                blackHasPieces = true;
            }
            
            if (redHasPieces && blackHasPieces) {
                break;
            }
        }
        
        if (!redHasPieces) {
            return GameState.BLACK_WON;
        }
        
        if (!blackHasPieces) {
            return GameState.RED_WON;
        }
        
        // Check if the next player has any valid moves
        boolean hasValidMoves = moveValidator.hasValidMoves(board, nextTurn);
        if (!hasValidMoves) {
            return nextTurn == PlayerColor.RED ? GameState.BLACK_WON : GameState.RED_WON;
        }
        
        return GameState.IN_PROGRESS;
    }
}
