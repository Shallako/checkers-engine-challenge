package com.shalako.checkers.engine.rules;

import com.shalako.checkers.enums.PieceType;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.Move;
import com.shalako.checkers.model.MoveRequest;
import com.shalako.checkers.model.Piece;
import com.shalako.checkers.model.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmericanCheckersRules implements GameRules {

    @Override
    public Move validateMove(Game game, MoveRequest moveRequest) {
        log.info("[VALIDATE MOVE REQUEST] gameId={}, playerId={}, from={}, to={}, currentTurn={}",
            moveRequest.getGameId(), moveRequest.getPlayerId(),
            moveRequest.getFrom(), moveRequest.getTo(), game.getCurrentTurn());
        Board board = game.getBoard();
        Position from = moveRequest.getFrom();
        Position to = moveRequest.getTo();

        if (from == null || to == null) {
            log.warn("[VALIDATION FAILED] Missing positions: from={}, to={}", from, to);
            throw new IllegalArgumentException("From and To positions are required for move validation");
        }

        if (!from.isValidForBoard(board.getSize()) || !to.isValidForBoard(board.getSize())) {
            log.warn("[VALIDATION FAILED] Invalid board positions: fromValid={}, toValid={}, boardSize={}", from.isValidForBoard(board.getSize()), to.isValidForBoard(board.getSize()), board.getSize());
            throw new IllegalArgumentException("Invalid position");
        }

        Piece piece = board.getPieceAt(from);
        if (piece == null) {
            log.warn("[VALIDATION FAILED] No piece at from position: {}", from);
            throw new IllegalArgumentException("No piece at position: " + from);
        }

        if (piece.getColor() != game.getCurrentTurn()) {
            log.warn("[VALIDATION FAILED] Attempt to move opponent piece: pieceColor={}, currentTurn={}", piece.getColor(), game.getCurrentTurn());
            throw new IllegalArgumentException("Cannot move opponent's piece");
        }

        if (!board.isEmpty(to)) {
            log.warn("[VALIDATION FAILED] Destination not empty: {}", to);
            throw new IllegalArgumentException("Destination is not empty: " + to);
        }

        if (!isDiagonalMove(from, to)) {
            log.warn("[VALIDATION FAILED] Move not diagonal: from={}, to={}", from, to);
            throw new IllegalArgumentException("Move must be diagonal");
        }

        List<Move> allPossibleMovesForPlayer = getAllValidMoves(board, game.getCurrentTurn());
        boolean jumpMovesAvailable = allPossibleMovesForPlayer.stream().anyMatch(Move::isJump);
        List<Move> validMovesForPiece = getValidMoves(board, from);

        if (jumpMovesAvailable) {
            List<Move> jumpMovesForPiece = validMovesForPiece.stream().filter(Move::isJump).toList();
            if (jumpMovesForPiece.isEmpty()) {
                throw new IllegalArgumentException("Invalid move, jump is mandatory but not possible from selected piece");
            }

            for (Move move : jumpMovesForPiece) {
                // For a human player, we are just validating one leg of a multi-jump at a time.
                // The `to` position of the request must match the second position in the move's path.
                if (move.getPath().size() > 1 && move.getPath().get(1).equals(to)) {
                    log.debug("[VALIDATION PASSED] Jump move selected: {}", move);
                    return move;
                }
            }
            log.warn("[VALIDATION FAILED] Jump required but attempted invalid jump: from={}, to={}", from, to);
            throw new IllegalArgumentException("Invalid jump");
        } else {
            for (Move move : validMovesForPiece) {
                if (move.getTo().equals(to)) {
                    log.debug("[VALIDATION PASSED] Simple move selected: {} -> {}", from, to);
                    return move;
                }
            }
        }

        log.warn("[VALIDATION FAILED] Invalid move for piece at {} to {}", from, to);
        throw new IllegalArgumentException("Invalid move");
    }

    @Override
    public List<Move> getValidMoves(Board board, Position position) {
        Piece piece = board.getPieceAt(position);
        if (piece == null) {
            return Collections.emptyList();
        }

        List<Move> validMoves = new ArrayList<>();
        List<Move> jumps = findMaximumCaptureMoves(board, position);

        if (!jumps.isEmpty()) {
            validMoves.addAll(jumps);
        } else if (!hasJumpMoves(board, piece.getColor())) {
            validMoves.addAll(getValidSimpleMoves(board, position, piece));
        }

        return validMoves;
    }

    @Override
    public boolean hasValidMoves(Board board, PlayerColor color) {
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == color) {
                if (!getValidMoves(board, entry.getKey()).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Move> findMaximumCaptureMoves(Board board, Position position) {
        List<Move> allJumps = new ArrayList<>();
        Piece piece = board.getPieceAt(position);
        if (piece == null) {
            return Collections.emptyList();
        }

        List<Position> initialPath = new ArrayList<>();
        initialPath.add(position);
        getValidJumpsRecursive(board, piece, initialPath, new ArrayList<>(), allJumps);

        if (allJumps.isEmpty()) {
            return Collections.emptyList();
        }

        int maxCaptures = allJumps.stream()
            .mapToInt(move -> move.getCapturedPieces().size())
            .max()
            .orElse(0);

        return allJumps.stream()
            .filter(move -> move.getCapturedPieces().size() == maxCaptures)
            .toList();
    }

    private boolean hasJumpMoves(Board board, PlayerColor color) {
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == color) {
                if (!findMaximumCaptureMoves(board, entry.getKey()).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Move> getValidSimpleMoves(Board board, Position position, Piece piece) {
        List<Move> moves = new ArrayList<>();
        List<int[]> directions = getMovementDirections(piece);

        log.debug("Getting simple moves for {} at {}", piece, position);
        for (int[] dir : directions) {
            Position newPos = position.offset(dir[0], dir[1]);
            log.debug("Checking potential move to {}", newPos);
            if (newPos.isValidForBoard(board.getSize()) && board.isEmpty(newPos)) {
                log.debug("Move to {} is valid", newPos);
                boolean promotion = isPromotionMove(board, newPos, piece);
                Move move = promotion
                        ? Move.MoveFactory.createPromotionMove(position, newPos)
                        : Move.MoveFactory.createSimpleMove(position, newPos);
                moves.add(move);
            } else {
                log.debug("Move to {} is invalid", newPos);
            }
        }
        return moves;
    }

    private void getValidJumpsRecursive(Board board, Piece piece, List<Position> currentPath, List<Position> capturedSoFar, List<Move> allJumps) {
        Position currentPosition = currentPath.get(currentPath.size() - 1);
        List<int[]> directions = getMovementDirections(piece);
        boolean foundNextJump = false;

        for (int[] dir : directions) {
            Position capturePos = currentPosition.offset(dir[0], dir[1]);
            Position landingPos = currentPosition.offset(dir[0] * 2, dir[1] * 2);

            if (isValidJump(board, currentPosition, capturePos, landingPos, piece, capturedSoFar)) {
                foundNextJump = true;
                List<Position> newPath = new ArrayList<>(currentPath);
                newPath.add(landingPos);
                List<Position> newCaptured = new ArrayList<>(capturedSoFar);
                newCaptured.add(capturePos);

                boolean promotion = isPromotionMove(board, landingPos, piece);
                if (promotion) {
                    allJumps.add(Move.MoveFactory.createMultiJumpPromotionMove(newPath, newCaptured));
                    continue; // Stop searching further down this path upon promotion
                }

                getValidJumpsRecursive(
                    createBoardAfterJump(board, currentPosition, landingPos, capturePos),
                    piece,
                    newPath,
                    newCaptured,
                    allJumps);
            }
        }

        if (!foundNextJump && !capturedSoFar.isEmpty()) {
            allJumps.add(Move.MoveFactory.createMultiJumpMove(currentPath, capturedSoFar));
        }
    }

    private List<Move> getAllValidMoves(Board board, PlayerColor color) {
        List<Move> allMoves = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == color) {
                allMoves.addAll(getValidMoves(board, entry.getKey()));
            }
        }
        return allMoves;
    }

    private boolean isValidJump(Board board, Position from, Position capturePos, Position landingPos,
                                Piece piece, List<Position> capturedSoFar) {
        if (!landingPos.isValidForBoard(board.getSize()) || !board.isEmpty(landingPos)) {
            return false;
        }

        Piece capturePiece = board.getPieceAt(capturePos);
        if (capturePiece == null || capturePiece.getColor() == piece.getColor()) {
            return false;
        }

        return !capturedSoFar.contains(capturePos);
    }

    private Board createBoardAfterJump(Board board, Position from, Position to, Position capturePos) {
        Map<Position, Piece> pieces = new HashMap<>(board.getPieces());
        Piece piece = pieces.get(from);
        pieces.remove(from);
        pieces.remove(capturePos);
        pieces.put(to, piece);
        return Board.BoardFactory.createCustomBoard(board.getSize(), pieces);
    }

    private List<int[]> getMovementDirections(Piece piece) {
        List<int[]> directions = new ArrayList<>();
        if (piece.getType() == PieceType.KING) {
            directions.add(new int[]{-1, -1}); // up-left
            directions.add(new int[]{-1, 1});  // up-right
            directions.add(new int[]{1, -1});  // down-left
            directions.add(new int[]{1, 1});   // down-right
        } else {
            int direction = piece.getColor().getDirection();
            directions.add(new int[]{direction, -1}); // left
            directions.add(new int[]{direction, 1});  // right
        }
        return directions;
    }

    private boolean isDiagonalMove(Position from, Position to) {
        int rowDiff = Math.abs(to.row() - from.row());
        int colDiff = Math.abs(to.column() - from.column());
        return rowDiff > 0 && rowDiff == colDiff;
    }

    private boolean isPromotionMove(Board board, Position to, Piece piece) {
        if (piece.getType() == PieceType.KING) {
            return false;
        }
        if (piece.getColor() == PlayerColor.RED) {
            return to.row() == 0;
        } else {
            int lastRow = board.getSize().getRows() - 1;
            return to.row() == lastRow;
        }
    }
}
