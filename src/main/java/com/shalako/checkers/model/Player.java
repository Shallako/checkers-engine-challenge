package com.shalako.checkers.model;

import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.enums.PlayerType;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Represents a player in a checkers game.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"id", "name", "color", "type"}) // Custom toString implementation below
public class Player {
    @EqualsAndHashCode.Include
    String id;
    String name;
    PlayerType type;
    PlayerColor color;
    
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
