package com.shalako.checkers.model;

import java.util.Objects;

/**
 * Represents a piece in a checkers game.
 */
public class Piece {
    private final PlayerColor color;
    private final PieceType type;

    private Piece(PlayerColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PlayerColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    /**
     * Promotes this piece to a king if it's a man.
     * Returns a new piece if promotion occurs, otherwise returns this piece.
     */
    public Piece promote() {
        if (type == PieceType.MAN) {
            return new Piece(color, PieceType.KING);
        }
        return this;
    }

    /**
     * Returns a string representation of this piece for display on the board.
     */
    public String getDisplaySymbol() {
        return color.getSymbol() + type.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return color == piece.color && type == piece.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return "Piece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }

    /**
     * Factory for creating pieces.
     */
    public static class PieceFactory {
        /**
         * Creates a new man piece of the specified color.
         */
        public static Piece createMan(PlayerColor color) {
            return new Piece(color, PieceType.MAN);
        }

        /**
         * Creates a new king piece of the specified color.
         */
        public static Piece createKing(PlayerColor color) {
            return new Piece(color, PieceType.KING);
        }
    }
}
