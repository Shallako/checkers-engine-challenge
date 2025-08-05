package com.shalako.checkers.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a checkers board.
 */
public class Board {
    private final BoardSize size;
    private final Map<Position, Piece> pieces;

    private Board(BoardSize size, Map<Position, Piece> pieces) {
        this.size = size;
        this.pieces = new HashMap<>(pieces);
    }

    public BoardSize getSize() {
        return size;
    }

    /**
     * Gets the piece at the specified position, or null if no piece exists.
     */
    public Piece getPieceAt(Position position) {
        return pieces.get(position);
    }

    /**
     * Checks if the specified position is empty.
     */
    public boolean isEmpty(Position position) {
        return !pieces.containsKey(position);
    }

    /**
     * Returns a copy of the pieces on the board.
     */
    public Map<Position, Piece> getPieces() {
        return new HashMap<>(pieces);
    }

    /**
     * Returns a string representation of the board for display.
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        
        // Column headers
        sb.append("  ");
        for (int col = 0; col < size.getColumns(); col++) {
            sb.append(" ").append(col).append(" ");
        }
        sb.append("\n");
        
        // Top border
        sb.append("  ");
        for (int col = 0; col < size.getColumns(); col++) {
            sb.append("---");
        }
        sb.append("\n");
        
        // Board rows
        for (int row = 0; row < size.getRows(); row++) {
            sb.append(row).append(" |");
            
            for (int col = 0; col < size.getColumns(); col++) {
                Position pos = new Position(row, col);
                Piece piece = pieces.get(pos);
                
                if (piece != null) {
                    sb.append(piece.getDisplaySymbol());
                } else {
                    // Checkerboard pattern for empty squares
                    boolean isDarkSquare = (row + col) % 2 == 1;
                    sb.append(isDarkSquare ? " . " : "   ");
                }
            }
            
            sb.append("|\n");
        }
        
        // Bottom border
        sb.append("  ");
        for (int col = 0; col < size.getColumns(); col++) {
            sb.append("---");
        }
        sb.append("\n");
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return size == board.size && Objects.equals(pieces, board.pieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, pieces);
    }

    /**
     * Factory for creating boards.
     */
    public static class BoardFactory {
        /**
         * Creates a new empty board of the specified size.
         */
        public static Board createEmptyBoard(BoardSize size) {
            return new Board(size, new HashMap<>());
        }

        /**
         * Creates a new board with the standard initial setup.
         */
        public static Board createStandardBoard(BoardSize size) {
            Map<Position, Piece> pieces = new HashMap<>();
            
            // Place black pieces at the top
            placePieces(pieces, 0, size.getInitialRows(), PlayerColor.BLACK, size);
            
            // Place red pieces at the bottom
            placePieces(pieces, size.getRows() - size.getInitialRows(), size.getRows(), PlayerColor.RED, size);
            
            return new Board(size, pieces);
        }

        /**
         * Creates a new board with the specified pieces.
         */
        public static Board createCustomBoard(BoardSize size, Map<Position, Piece> pieces) {
            return new Board(size, pieces);
        }

        /**
         * Helper method to place pieces in the initial setup.
         */
        private static void placePieces(Map<Position, Piece> pieces, int startRow, int endRow, 
                                       PlayerColor color, BoardSize size) {
            for (int row = startRow; row < endRow; row++) {
                for (int col = 0; col < size.getColumns(); col++) {
                    // In checkers, pieces are only placed on dark squares
                    // Dark squares are where row + column is odd
                    if ((row + col) % 2 == 1) {
                        Position pos = new Position(row, col);
                        pieces.put(pos, Piece.PieceFactory.createMan(color));
                    }
                }
            }
        }
    }
}
