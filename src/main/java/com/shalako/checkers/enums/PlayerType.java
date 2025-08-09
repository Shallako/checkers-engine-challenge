package com.shalako.checkers.enums;

/**
 * Represents the type of player in a checkers game.
 */
public enum PlayerType {
    HUMAN("Human"),
    COMPUTER("Computer");

    private final String displayName;

    PlayerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
