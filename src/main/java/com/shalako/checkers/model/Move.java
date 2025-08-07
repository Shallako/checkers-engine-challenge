package com.shalako.checkers.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a move in a checkers game.
 */
@Getter
public class Move {
    private final Position from;
    private final Position to;
    private final List<Position> capturedPieces;
    private final boolean promotion;

    private Move(Position from, Position to, List<Position> capturedPieces, boolean promotion) {
        this.from = from;
        this.to = to;
        this.capturedPieces = Collections.unmodifiableList(
            capturedPieces != null ? new ArrayList<>(capturedPieces) : Collections.emptyList()
        );
        this.promotion = promotion;
    }

    public boolean isJump() {
        return !capturedPieces.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return promotion == move.promotion &&
               Objects.equals(from, move.from) &&
               Objects.equals(to, move.to) &&
               Objects.equals(capturedPieces, move.capturedPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, capturedPieces, promotion);
    }

    @Override
    public String toString() {
        return from + " -> " + to + 
               (isJump() ? " (captures: " + capturedPieces + ")" : "") +
               (promotion ? " (promotion)" : "");
    }

    /**
     * Factory for creating moves.
     */
    public static class MoveFactory {
        /**
         * Creates a simple move without captures or promotion.
         */
        public static Move createSimpleMove(Position from, Position to) {
            return new Move(from, to, Collections.emptyList(), false);
        }

        /**
         * Creates a move with promotion but without captures.
         */
        public static Move createPromotionMove(Position from, Position to) {
            return new Move(from, to, Collections.emptyList(), true);
        }

        /**
         * Creates a jump move with a single capture.
         */
        public static Move createJumpMove(Position from, Position to, Position captured) {
            return new Move(from, to, Collections.singletonList(captured), false);
        }

        /**
         * Creates a jump move with a single capture and promotion.
         */
        public static Move createJumpPromotionMove(Position from, Position to, Position captured) {
            return new Move(from, to, Collections.singletonList(captured), true);
        }

        /**
         * Creates a multi-jump move with multiple captures.
         */
        public static Move createMultiJumpMove(Position from, Position to, List<Position> captured) {
            return new Move(from, to, captured, false);
        }

        /**
         * Creates a multi-jump move with multiple captures and promotion.
         */
        public static Move createMultiJumpPromotionMove(Position from, Position to, List<Position> captured) {
            return new Move(from, to, captured, true);
        }
    }
}
