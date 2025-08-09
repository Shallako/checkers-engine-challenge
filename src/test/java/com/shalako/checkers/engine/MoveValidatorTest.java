package com.shalako.checkers.engine;

import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class MoveValidatorTest {

    @Test
    public void testFlyingKings() {
        // Create a custom board with a king piece
        Map<Position, Piece> pieces = new HashMap<>();
        
        // Place a RED king in the middle of the board
        Position kingPosition = new Position(4, 3);
        pieces.put(kingPosition, Piece.PieceFactory.createKing(PlayerColor.RED));
        
        // Place a BLACK piece that can be captured
        Position capturePosition = new Position(3, 2);
        pieces.put(capturePosition, Piece.PieceFactory.createMan(PlayerColor.BLACK));
        
        Board board = Board.BoardFactory.createCustomBoard(BoardSize.STANDARD, pieces);
        
        // Print the board layout
        System.out.println("[DEBUG_LOG] Board layout for Flying Kings test:");
        System.out.println("[DEBUG_LOG] " + board.getDisplayString());
        
        // Create a move validator
        MoveValidator moveValidator = new MoveValidator();
        
        // Get valid moves for the king
        List<Move> validMoves = moveValidator.getValidMoves(board, kingPosition);
        
        // Print the valid moves
        System.out.println("[DEBUG_LOG] Valid moves for RED king at " + kingPosition + ":");
        for (Move move : validMoves) {
            System.out.println("[DEBUG_LOG]   " + move);
        }
        
        // Verify that the king can move multiple squares diagonally
        boolean canMoveMultipleSquares = false;
        for (Move move : validMoves) {
            Position to = move.getTo();
            int rowDiff = Math.abs(to.row() - kingPosition.row());
            if (rowDiff > 1 && !move.isJump()) {
                canMoveMultipleSquares = true;
                break;
            }
        }
        
        assertTrue(canMoveMultipleSquares, "King should be able to move multiple squares diagonally");
        
        // Verify that the king can capture and land on any empty square along the diagonal
        boolean canCaptureAndLandFar = false;
        for (Move move : validMoves) {
            if (move.isJump() && move.getCapturedPieces().contains(capturePosition)) {
                Position to = move.getTo();
                int rowDiff = Math.abs(to.row() - kingPosition.row());
                if (rowDiff > 2) {
                    canCaptureAndLandFar = true;
                    break;
                }
            }
        }
        
        assertTrue(canCaptureAndLandFar, "King should be able to capture and land on any empty square along the diagonal");
    }

    @Test
    public void testHasValidMovesOnNewBoard() throws Exception {
        // Create a custom board with pieces positioned to ensure both players have valid moves
        Map<Position, Piece> pieces = new HashMap<>();
        
        // Place a few BLACK pieces
        pieces.put(new Position(2, 1), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        pieces.put(new Position(2, 3), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        pieces.put(new Position(2, 5), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        
        // Place a BLACK piece that can move
        pieces.put(new Position(3, 2), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        
        // Place a few RED pieces
        pieces.put(new Position(5, 2), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(5, 4), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(5, 6), Piece.PieceFactory.createMan(PlayerColor.RED));
        
        // Place a RED piece that can move
        pieces.put(new Position(4, 3), Piece.PieceFactory.createMan(PlayerColor.RED));
        
        Board board = Board.BoardFactory.createCustomBoard(BoardSize.STANDARD, pieces);
        
        // Print the board layout
        System.out.println("[DEBUG_LOG] Board layout:");
        System.out.println("[DEBUG_LOG] " + board.getDisplayString());
        
        // Create a move validator
        MoveValidator moveValidator = new MoveValidator();
        
        // Use reflection to access private methods for debugging
        Method getMovementDirectionsMethod = MoveValidator.class.getDeclaredMethod("getMovementDirections", Piece.class);
        getMovementDirectionsMethod.setAccessible(true);
        
        // Debug RED pieces and their moves
        System.out.println("[DEBUG_LOG] RED pieces and their moves:");
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == PlayerColor.RED) {
                Position pos = entry.getKey();
                Piece piece = entry.getValue();
                System.out.println("[DEBUG_LOG] RED piece at " + pos + ", type: " + piece.getType() + ", direction: " + piece.getColor().getDirection());
                
                // Get movement directions for this piece
                @SuppressWarnings("unchecked")
                List<int[]> directions = (List<int[]>) getMovementDirectionsMethod.invoke(moveValidator, piece);
                System.out.println("[DEBUG_LOG]   Movement directions: " + directions.size());
                for (int[] dir : directions) {
                    Position newPos = pos.offset(dir[0], dir[1]);
                    boolean isValidPosition = newPos.isValidForBoard(board.getSize());
                    boolean isEmpty = isValidPosition && board.isEmpty(newPos);
                    System.out.println("[DEBUG_LOG]     Direction: [" + dir[0] + ", " + dir[1] + "] -> " + newPos + 
                                      ", valid: " + isValidPosition + ", empty: " + isEmpty);
                }
                
                // Get valid moves for this piece
                List<Move> moves = moveValidator.getValidMoves(board, pos);
                System.out.println("[DEBUG_LOG]   Valid moves: " + moves.size());
                for (Move move : moves) {
                    System.out.println("[DEBUG_LOG]     " + move);
                }
            }
        }
        
        // Debug BLACK pieces and their moves
        System.out.println("[DEBUG_LOG] BLACK pieces and their moves:");
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == PlayerColor.BLACK) {
                Position pos = entry.getKey();
                Piece piece = entry.getValue();
                System.out.println("[DEBUG_LOG] BLACK piece at " + pos + ", type: " + piece.getType() + ", direction: " + piece.getColor().getDirection());
                
                // Get movement directions for this piece
                @SuppressWarnings("unchecked")
                List<int[]> directions = (List<int[]>) getMovementDirectionsMethod.invoke(moveValidator, piece);
                System.out.println("[DEBUG_LOG]   Movement directions: " + directions.size());
                for (int[] dir : directions) {
                    Position newPos = pos.offset(dir[0], dir[1]);
                    boolean isValidPosition = newPos.isValidForBoard(board.getSize());
                    boolean isEmpty = isValidPosition && board.isEmpty(newPos);
                    System.out.println("[DEBUG_LOG]     Direction: [" + dir[0] + ", " + dir[1] + "] -> " + newPos + 
                                      ", valid: " + isValidPosition + ", empty: " + isEmpty);
                }
                
                // Get valid moves for this piece
                List<Move> moves = moveValidator.getValidMoves(board, pos);
                System.out.println("[DEBUG_LOG]   Valid moves: " + moves.size());
                for (Move move : moves) {
                    System.out.println("[DEBUG_LOG]     " + move);
                }
            }
        }
        
        // Check if both players have valid moves
        boolean redHasValidMoves = moveValidator.hasValidMoves(board, PlayerColor.RED);
        boolean blackHasValidMoves = moveValidator.hasValidMoves(board, PlayerColor.BLACK);
        
        System.out.println("[DEBUG_LOG] Red has valid moves: " + redHasValidMoves);
        System.out.println("[DEBUG_LOG] Black has valid moves: " + blackHasValidMoves);
        
        // Both players should have valid moves on a new board
        assertTrue(redHasValidMoves, "Red player should have valid moves on a new board");
        assertTrue(blackHasValidMoves, "Black player should have valid moves on a new board");
    }
}
