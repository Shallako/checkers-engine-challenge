package com.shalako.checkers.model;

import com.shalako.checkers.enums.PlayerType;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a request to make a move in a checkers game.
 * This is used as a method input object to avoid having too many parameters.
 */
@Getter
public class MoveRequest {
    private final String gameId;
    private final String playerId;
    private final Position from;
    private final Position to;
    private final PlayerType playerType;

    /**
     * Backward-compatible constructor that infers player type from positions.
     * If from/to are null -> COMPUTER, otherwise -> HUMAN.
     */
    private MoveRequest(String gameId, String playerId, Position from, Position to) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.from = from;
        this.to = to;
        this.playerType = (from == null || to == null) ? PlayerType.COMPUTER : PlayerType.HUMAN;
    }

    /**
     * Constructor allowing explicit player type specification.
     */
    private MoveRequest(String gameId, String playerId, Position from, Position to, PlayerType playerType) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.from = from;
        this.to = to;
        this.playerType = playerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveRequest that = (MoveRequest) o;
        return Objects.equals(gameId, that.gameId) &&
               Objects.equals(playerId, that.playerId) &&
               Objects.equals(from, that.from) &&
               Objects.equals(to, that.to) &&
               playerType == that.playerType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, playerId, from, to, playerType);
    }

    @Override
    public String toString() {
        return "MoveRequest{" +
                "gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", playerType=" + playerType +
                '}';
    }

    /**
     * Factory for creating move requests.
     */
    public static class MoveRequestFactory {
        /**
         * Creates a new move request with the specified parameters.
         */
        public static MoveRequest createMoveRequest(String gameId, String playerId, Position from, Position to) {
            return new MoveRequest(gameId, playerId, from, to);
        }

        /**
         * Creates a new move request with explicit player type.
         */
        public static MoveRequest createMoveRequest(String gameId, String playerId, Position from, Position to, PlayerType playerType) {
            return new MoveRequest(gameId, playerId, from, to, playerType);
        }

        /**
         * Creates a new move request from string coordinates.
         * Format: "row1,col1 row2,col2"
         */
        public static MoveRequest createMoveRequestFromString(String gameId, String playerId, String moveString) {
            String[] parts = moveString.trim().split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid move format. Expected 'row1,col1 row2,col2'");
            }

            String[] fromCoords = parts[0].split(",");
            String[] toCoords = parts[1].split(",");

            if (fromCoords.length != 2 || toCoords.length != 2) {
                throw new IllegalArgumentException("Invalid coordinate format. Expected 'row,col'");
            }

            try {
                int fromRow = Integer.parseInt(fromCoords[0]);
                int fromCol = Integer.parseInt(fromCoords[1]);
                int toRow = Integer.parseInt(toCoords[0]);
                int toCol = Integer.parseInt(toCoords[1]);

                return new MoveRequest(gameId, playerId, new Position(fromRow, fromCol), new Position(toRow, toCol));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid coordinate values. Expected integers.", e);
            }
        }
    }
}
