package com.shalako.checkers.model;

/**
 * Represents the color of a player in a checkers game.
 */
public enum PlayerColor {
    RED("R", 1),
    BLACK("B", -1);

    private final String symbol;
    private final int direction;

    PlayerColor(String symbol, int direction) {
        this.symbol = symbol;
        this.direction = direction;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the direction of movement for this color.
     * Positive for moving down the board, negative for moving up.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Returns the opposite color.
     */
    public PlayerColor getOpposite() {
        return this == RED ? BLACK : RED;
    }
}
