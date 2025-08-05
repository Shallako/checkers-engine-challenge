package com.shalako.checkers.model;

import java.util.Objects;

/**
 * Represents a position on the checkers board.
 */
public class Position {
    private final int row;
    private final int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Creates a new position by adding the specified offsets to this position.
     */
    public Position offset(int rowOffset, int columnOffset) {
        return new Position(row + rowOffset, column + columnOffset);
    }

    /**
     * Checks if this position is within the bounds of the specified board size.
     */
    public boolean isValidForBoard(BoardSize boardSize) {
        return row >= 0 && row < boardSize.getRows() && 
               column >= 0 && column < boardSize.getColumns();
    }

    /**
     * Returns the position halfway between this position and the target position.
     * Used for determining the position of a captured piece during a jump.
     */
    public Position midPointTo(Position target) {
        return new Position((row + target.row) / 2, (column + target.column) / 2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}
