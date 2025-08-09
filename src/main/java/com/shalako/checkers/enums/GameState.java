package com.shalako.checkers.enums;

/**
 * Represents the current state of a checkers game.
 */
public enum GameState {
    WAITING_FOR_PLAYERS("Waiting for players"),
    IN_PROGRESS("Game in progress"),
    RED_WON("Red player won"),
    BLACK_WON("Black player won"),
    DRAW("Game ended in a draw");

    private final String displayText;

    GameState(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    /**
     * Checks if the game has ended.
     */
    public boolean isGameOver() {
        return this == RED_WON || this == BLACK_WON || this == DRAW;
    }
}
