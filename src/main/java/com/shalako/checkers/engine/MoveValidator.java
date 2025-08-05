package com.shalako.checkers.engine;

import com.shalako.checkers.model.*;

import java.util.*;

/**
 * Validates moves in a checkers game.
 */
public class MoveValidator {

    /**
     * Validates a move request and returns a valid Move object.
     */
    public Move validateMove(Game game, MoveRequest moveRequest) {
        Board board = game.getBoard();
        Position from = moveRequest.getFrom();
        Position to = moveRequest.getTo();
        
        // Check if positions are valid
        if (!from.isValidForBoard(board.getSize()) || !to.isValidForBoard(board.getSize())) {
            throw new IllegalArgumentException("Invalid position");
        }
        
        // Check if there is a piece at the from position
        Piece piece = board.getPieceAt(from);
        if (piece == null) {
            throw new IllegalArgumentException("No piece at position: " + from);
        }
        
        // Check if the piece belongs to the current player
        if (piece.getColor() != game.getCurrentTurn()) {
            throw new IllegalArgumentException("Cannot move opponent's piece");
        }
        
        // Check if the destination is empty
        if (!board.isEmpty(to)) {
            throw new IllegalArgumentException("Destination is not empty: " + to);
        }
        
        // Check if the move is diagonal
        if (!isDiagonalMove(from, to)) {
            throw new IllegalArgumentException("Move must be diagonal");
        }
        
        // Check if the move is a valid simple move or jump
        List<Move> validMoves = getValidMoves(board, from);
        for (Move move : validMoves) {
            if (move.getTo().equals(to)) {
                return move;
            }
        }
        
        throw new IllegalArgumentException("Invalid move");
    }

    /**
     * Checks if a player has any valid moves.
     */
    public boolean hasValidMoves(Board board, PlayerColor color) {
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == color) {
                List<Move> moves = getValidMoves(board, entry.getKey());
                if (!moves.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets all valid moves for a piece at the specified position.
     */
    public List<Move> getValidMoves(Board board, Position position) {
        Piece piece = board.getPieceAt(position);
        if (piece == null) {
            return Collections.emptyList();
        }
        
        List<Move> validMoves = new ArrayList<>();
        
        // Check for jumps first (mandatory in checkers)
        List<Move> jumps = getValidJumps(board, position, piece, new ArrayList<>());
        if (!jumps.isEmpty()) {
            return jumps;
        }
        
        // If no jumps, check for simple moves
        validMoves.addAll(getValidSimpleMoves(board, position, piece));
        
        return validMoves;
    }

    /**
     * Gets all valid simple moves (non-jumps) for a piece.
     */
    private List<Move> getValidSimpleMoves(Board board, Position position, Piece piece) {
        List<Move> moves = new ArrayList<>();
        
        // Determine the directions the piece can move
        List<int[]> directions = getMovementDirections(piece);
        
        for (int[] dir : directions) {
            Position newPos = position.offset(dir[0], dir[1]);
            
            // Check if the new position is valid and empty
            if (newPos.isValidForBoard(board.getSize()) && board.isEmpty(newPos)) {
                // Check if the move results in a promotion
                boolean promotion = isPromotionMove(newPos, piece);
                
                // Create the appropriate move
                Move move = promotion
                    ? Move.MoveFactory.createPromotionMove(position, newPos)
                    : Move.MoveFactory.createSimpleMove(position, newPos);
                
                moves.add(move);
            }
        }
        
        return moves;
    }

    /**
     * Gets all valid jumps for a piece.
     */
    private List<Move> getValidJumps(Board board, Position position, Piece piece, List<Position> capturedSoFar) {
        List<Move> jumps = new ArrayList<>();
        
        // Determine the directions the piece can move
        List<int[]> directions = getMovementDirections(piece);
        
        for (int[] dir : directions) {
            // Calculate the position of the potential captured piece and the landing position
            Position capturePos = position.offset(dir[0], dir[1]);
            Position landingPos = position.offset(dir[0] * 2, dir[1] * 2);
            
            // Check if the jump is valid
            if (isValidJump(board, position, capturePos, landingPos, piece, capturedSoFar)) {
                // Create a new list of captured pieces
                List<Position> newCaptured = new ArrayList<>(capturedSoFar);
                newCaptured.add(capturePos);
                
                // Check if the move results in a promotion
                boolean promotion = isPromotionMove(landingPos, piece);
                
                // If promotion, we can't continue jumping
                if (promotion) {
                    jumps.add(Move.MoveFactory.createJumpPromotionMove(position, landingPos, capturePos));
                    continue;
                }
                
                // Check for additional jumps from the landing position
                List<Move> continuedJumps = getValidJumps(
                    createBoardAfterJump(board, position, landingPos, capturePos),
                    landingPos,
                    piece,
                    newCaptured
                );
                
                if (continuedJumps.isEmpty()) {
                    // No more jumps, create a single jump move
                    jumps.add(Move.MoveFactory.createJumpMove(position, landingPos, capturePos));
                } else {
                    // Add multi-jump moves
                    for (Move continuedJump : continuedJumps) {
                        List<Position> allCaptured = new ArrayList<>(newCaptured);
                        allCaptured.addAll(continuedJump.getCapturedPieces());
                        
                        jumps.add(Move.MoveFactory.createMultiJumpMove(
                            position,
                            continuedJump.getTo(),
                            allCaptured
                        ));
                    }
                }
            }
        }
        
        return jumps;
    }

    /**
     * Checks if a jump is valid.
     */
    private boolean isValidJump(Board board, Position from, Position capturePos, Position landingPos,
                               Piece piece, List<Position> capturedSoFar) {
        // Check if the landing position is valid and empty
        if (!landingPos.isValidForBoard(board.getSize()) || !board.isEmpty(landingPos)) {
            return false;
        }
        
        // Check if there is an opponent's piece to capture
        Piece capturePiece = board.getPieceAt(capturePos);
        if (capturePiece == null || capturePiece.getColor() == piece.getColor()) {
            return false;
        }
        
        // Check if the piece has already been captured in this sequence
        return !capturedSoFar.contains(capturePos);
    }

    /**
     * Creates a new board after a jump move.
     */
    private Board createBoardAfterJump(Board board, Position from, Position to, Position capturePos) {
        Map<Position, Piece> pieces = new HashMap<>(board.getPieces());
        Piece piece = pieces.get(from);
        
        pieces.remove(from);
        pieces.remove(capturePos);
        pieces.put(to, piece);
        
        return Board.BoardFactory.createCustomBoard(board.getSize(), pieces);
    }

    /**
     * Gets the possible movement directions for a piece.
     */
    private List<int[]> getMovementDirections(Piece piece) {
        List<int[]> directions = new ArrayList<>();
        
        // Kings can move in all diagonal directions
        if (piece.getType() == PieceType.KING) {
            directions.add(new int[]{-1, -1}); // up-left
            directions.add(new int[]{-1, 1});  // up-right
            directions.add(new int[]{1, -1});  // down-left
            directions.add(new int[]{1, 1});   // down-right
        } else {
            // Men can only move in the direction of their color
            int direction = piece.getColor().getDirection();
            directions.add(new int[]{direction, -1}); // left
            directions.add(new int[]{direction, 1});  // right
        }
        
        return directions;
    }

    /**
     * Checks if a move is diagonal.
     */
    private boolean isDiagonalMove(Position from, Position to) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());
        
        return rowDiff > 0 && rowDiff == colDiff;
    }

    /**
     * Checks if a move results in a promotion.
     */
    private boolean isPromotionMove(Position to, Piece piece) {
        if (piece.getType() == PieceType.KING) {
            return false;
        }
        
        // A piece is promoted when it reaches the opposite end of the board
        if (piece.getColor() == PlayerColor.RED) {
            return to.getRow() == 0;
        } else {
            // For BLACK pieces, we need to check if they reached the bottom row
            // This depends on the board size
            return to.getRow() >= 7; // Works for both 8x8 and 10x10 boards
        }
    }
}
