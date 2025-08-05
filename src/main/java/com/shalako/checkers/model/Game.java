package com.shalako.checkers.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a checkers game.
 */
public class Game {
    private final String id;
    private final Board board;
    private final Player redPlayer;
    private final Player blackPlayer;
    private final PlayerColor currentTurn;
    private final GameState state;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Game(String id, Board board, Player redPlayer, Player blackPlayer, 
                PlayerColor currentTurn, GameState state, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.board = board;
        this.redPlayer = redPlayer;
        this.blackPlayer = blackPlayer;
        this.currentTurn = currentTurn;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public Player getRedPlayer() {
        return redPlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public PlayerColor getCurrentTurn() {
        return currentTurn;
    }

    public GameState getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the player whose turn it currently is.
     */
    public Player getCurrentPlayer() {
        return currentTurn == PlayerColor.RED ? redPlayer : blackPlayer;
    }

    /**
     * Gets the player with the specified color.
     */
    public Player getPlayerByColor(PlayerColor color) {
        return color == PlayerColor.RED ? redPlayer : blackPlayer;
    }

    /**
     * Checks if the game is over.
     */
    public boolean isGameOver() {
        return state.isGameOver();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", currentTurn=" + currentTurn +
                '}';
    }

    /**
     * Factory for creating games.
     */
    public static class GameFactory {
        /**
         * Creates a new game with the specified parameters.
         */
        public static Game createGame(String id, Board board, Player redPlayer, Player blackPlayer,
                                     PlayerColor currentTurn, GameState state, 
                                     Instant createdAt, Instant updatedAt) {
            return new Game(id, board, redPlayer, blackPlayer, currentTurn, state, createdAt, updatedAt);
        }

        /**
         * Creates a new game with default parameters.
         */
        public static Game createNewGame(BoardSize boardSize, Player redPlayer, Player blackPlayer) {
            String id = UUID.randomUUID().toString();
            Board board = Board.BoardFactory.createStandardBoard(boardSize);
            Instant now = Instant.now();
            
            return new Game(id, board, redPlayer, blackPlayer, PlayerColor.RED, 
                           GameState.IN_PROGRESS, now, now);
        }

        /**
         * Creates a new game with a human player against the computer.
         */
        public static Game createHumanVsComputerGame(BoardSize boardSize, String playerName, PlayerColor playerColor) {
            Player humanPlayer = Player.PlayerFactory.createHumanPlayer(playerName, playerColor);
            Player computerPlayer = Player.PlayerFactory.createComputerPlayer(playerColor.getOpposite());
            
            return createNewGame(boardSize, 
                                playerColor == PlayerColor.RED ? humanPlayer : computerPlayer,
                                playerColor == PlayerColor.BLACK ? humanPlayer : computerPlayer);
        }
    }
}
