package com.shalako.checkers.model;

import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.PlayerColor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class BoardTest {

    @Test
    public void testCreateStandardBoard() {
        // Create a standard board
        Board board = Board.BoardFactory.createStandardBoard(BoardSize.STANDARD);
        
        // Print the board layout
        System.out.println("[DEBUG_LOG] Board layout:");
        System.out.println("[DEBUG_LOG] " + board.getDisplayString());
        
        // Check that there are empty rows in the middle
        // For an 8x8 board, rows 3 and 4 should be empty
        boolean row3Empty = true;
        boolean row4Empty = true;
        
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            Position pos = entry.getKey();
            if (pos.row() == 3) {
                row3Empty = false;
                System.out.println("[DEBUG_LOG] Found piece at row 3: " + pos);
            }
            if (pos.row() == 4) {
                row4Empty = false;
                System.out.println("[DEBUG_LOG] Found piece at row 4: " + pos);
            }
        }
        
        assertTrue(row3Empty, "Row 3 should be empty");
        assertTrue(row4Empty, "Row 4 should be empty");
        
        // Check that there are pieces in rows 0, 1, 2 (BLACK) and 5, 6, 7 (RED)
        boolean hasBlackPieces = false;
        boolean hasRedPieces = false;
        
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            Position pos = entry.getKey();
            Piece piece = entry.getValue();
            
            if (pos.row() < 3 && piece.getColor() == PlayerColor.BLACK) {
                hasBlackPieces = true;
            }
            
            if (pos.row() > 4 && piece.getColor() == PlayerColor.RED) {
                hasRedPieces = true;
            }
        }
        
        assertTrue(hasBlackPieces, "There should be BLACK pieces in rows 0, 1, 2");
        assertTrue(hasRedPieces, "There should be RED pieces in rows 5, 6, 7");
    }
}
