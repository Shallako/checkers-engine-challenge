package com.shalako.checkers.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player in a checkers game.
 */
public class Player {
    private final String id;
    private final String name;
    private final PlayerType type;
    private final PlayerColor color;

    private Player(String id, String name, PlayerType type, PlayerColor color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerType getType() {
        return type;
    }

    public PlayerColor getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + color + ", " + type + ")";
    }

    /**
     * Factory for creating players.
     */
    public static class PlayerFactory {
        /**
         * Creates a human player with the specified name and color.
         */
        public static Player createHumanPlayer(String name, PlayerColor color) {
            return new Player(UUID.randomUUID().toString(), name, PlayerType.HUMAN, color);
        }

        /**
         * Creates a computer player with the specified color.
         */
        public static Player createComputerPlayer(PlayerColor color) {
            return new Player(UUID.randomUUID().toString(), "Computer", PlayerType.COMPUTER, color);
        }

        /**
         * Creates a player with the specified parameters.
         */
        public static Player createPlayer(String id, String name, PlayerType type, PlayerColor color) {
            return new Player(id, name, type, color);
        }
    }
}
