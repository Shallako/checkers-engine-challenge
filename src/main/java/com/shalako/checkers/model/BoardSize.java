package com.shalako.checkers.model;

/**
 * Represents the size of a checkers board.
 */
public enum BoardSize {
    EIGHT_BY_EIGHT(8, 8, 3),
    TEN_BY_TEN(10, 10, 4);

    private final int rows;
    private final int columns;
    private final int initialRows;

    BoardSize(int rows, int columns, int initialRows) {
        this.rows = rows;
        this.columns = columns;
        this.initialRows = initialRows;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    /**
     * Returns the number of rows initially filled with pieces for each player.
     */
    public int getInitialRows() {
        return initialRows;
    }
}
