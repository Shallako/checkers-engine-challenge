package com.shalako.checkers.enums;

/**
 * Represents the type of a checkers piece.
 */
public enum PieceType {
    MAN("M"),
    KING("K");

    private final String symbol;

    PieceType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
