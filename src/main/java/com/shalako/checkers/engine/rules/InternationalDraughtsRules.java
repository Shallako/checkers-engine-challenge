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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternationalDraughtsRules implements GameRules {

    @Override
    public Move validateMove(Game game, MoveRequest moveRequest) {
        log.info("[VALIDATE MOVE REQUEST] gameId={}, playerId={}, from={}, to={}, currentTurn={}",
                moveRequest.getGameId(), moveRequest.getPlayerId(),
                moveRequest.getFrom(), moveRequest.getTo(), game.getCurrentTurn());

        Position from = moveRequest.getFrom();
        Position to = moveRequest.getTo();
        Board board = game.getBoard();
        PlayerColor currentTurn = game.getCurrentTurn();
        Piece piece = board.getPieceAt(from);

        if (piece == null || piece.getColor() != currentTurn) {
            throw new IllegalArgumentException("Invalid piece to move.");
        }

        List<Move> allPossibleJumps = findAllJumps(board, currentTurn);

        if (!allPossibleJumps.isEmpty()) {
            int maxCaptures = allPossibleJumps.stream()
                    .max(Comparator.comparing(m -> m.getCapturedPieces().size()))
                    .get()
                    .getCapturedPieces().size();

            List<Move> maxJumps = allPossibleJumps.stream()
                    .filter(m -> m.getCapturedPieces().size() == maxCaptures)
                    .collect(Collectors.toList());

            for (Move jump : maxJumps) {
                if (jump.getFrom().equals(from) && jump.getTo().equals(to)) {
                    return jump;
                }
            }

            throw new IllegalArgumentException("A jump with maximum captures is mandatory.");
        }

        List<Move> simpleMoves = getValidSimpleMoves(board, from, piece);
        for (Move move : simpleMoves) {
            if (move.getTo().equals(to)) {
                return move;
            }
        }

        throw new IllegalArgumentException("Invalid move.");
    }

    @Override
    public List<Move> getValidMoves(Board board, Position position) {
        Piece piece = board.getPieceAt(position);
        if (piece == null) {
            return Collections.emptyList();
        }

        List<Move> allJumps = findAllJumps(board, piece.getColor());

        if (!allJumps.isEmpty()) {
            int maxCaptures = allJumps.stream()
                    .mapToInt(m -> m.getCapturedPieces().size())
                    .max()
                    .orElse(0);

            return allJumps.stream()
                    .filter(m -> m.getFrom().equals(position) && m.getCapturedPieces().size() == maxCaptures)
                    .collect(Collectors.toList());
        }

        return getValidSimpleMoves(board, position, piece);
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

    private List<Move> findAllJumps(Board board, PlayerColor color) {
        List<Move> allJumps = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == color) {
                allJumps.addAll(getValidJumps(board, entry.getKey(), entry.getValue(), new ArrayList<>()));
            }
        }
        return allJumps;
    }

    private List<Move> getValidSimpleMoves(Board board, Position position, Piece piece) {
        List<Move> moves = new ArrayList<>();
        List<int[]> directions = getMovementDirections(piece, false);

        for (int[] dir : directions) {
            if (piece.getType() == PieceType.KING) {
                int distance = 1;
                while (true) {
                    Position newPos = position.offset(dir[0] * distance, dir[1] * distance);
                    if (!newPos.isValidForBoard(board.getSize()) || !board.isEmpty(newPos)) {
                        break;
                    }
                    moves.add(Move.MoveFactory.createSimpleMove(position, newPos));
                    distance++;
                }
            } else {
                Position newPos = position.offset(dir[0], dir[1]);
                if (newPos.isValidForBoard(board.getSize()) && board.isEmpty(newPos)) {
                    boolean promotion = isPromotionMove(board, newPos, piece);
                    moves.add(promotion ? Move.MoveFactory.createPromotionMove(position, newPos) : Move.MoveFactory.createSimpleMove(position, newPos));
                }
            }
        }
        return moves;
    }

    private List<Move> getValidJumps(Board board, Position startPos, Piece piece, List<Position> capturedSoFar) {
        List<Move> jumps = new ArrayList<>();
        // Use a stack for iterative deepening search
        Stack<JumpState> stack = new Stack<>();
        stack.push(new JumpState(startPos, board, capturedSoFar, new HashSet<>()));

        while (!stack.isEmpty()) {
            JumpState currentState = stack.pop();
            boolean foundContinuation = false;

            currentState.visited.add(currentState.pos);

            for (int[] dir : getMovementDirections(piece, true)) {
                Position capturePos = findCapture(currentState.board, currentState.pos, dir, piece, currentState.captured);
                if (capturePos == null) continue;

                Position landingPos = capturePos.offset(dir[0], dir[1]);
                while (landingPos.isValidForBoard(board.getSize()) && currentState.board.isEmpty(landingPos)) {
                    if (currentState.visited.contains(landingPos)) {
                        landingPos = landingPos.offset(dir[0], dir[1]);
                        continue;
                    }

                    List<Position> nextCaptured = new ArrayList<>(currentState.captured);
                    nextCaptured.add(capturePos);

                    Board nextBoard = createBoardAfterJump(currentState.board, currentState.pos, landingPos, capturePos);

                    stack.push(new JumpState(landingPos, nextBoard, nextCaptured, new HashSet<>(currentState.visited)));
                    foundContinuation = true;

                    if (piece.getType() != PieceType.KING) break;
                    landingPos = landingPos.offset(dir[0], dir[1]);
                }
            }

            if (!foundContinuation && !currentState.captured.isEmpty()) {
                jumps.add(Move.MoveFactory.createMultiJumpMove(startPos, currentState.pos, currentState.captured));
            }
        }
        return jumps;
    }

    private static class JumpState {
        final Position pos;
        final Board board;
        final List<Position> captured;
        final Set<Position> visited;

        JumpState(Position pos, Board board, List<Position> captured, Set<Position> visited) {
            this.pos = pos;
            this.board = board;
            this.captured = captured;
            this.visited = visited;
        }
    }

    private Position findCapture(Board board, Position start, int[] dir, Piece piece, List<Position> capturedSoFar) {
        Position searchPos = start.offset(dir[0], dir[1]);
        while (searchPos.isValidForBoard(board.getSize())) {
            Piece foundPiece = board.getPieceAt(searchPos);
            if (foundPiece != null) {
                if (foundPiece.getColor() != piece.getColor() && !capturedSoFar.contains(searchPos)) {
                    return searchPos;
                }
                return null; // Blocked by another piece
            }
            if (piece.getType() != PieceType.KING) {
                break; // Men only scan one square
            }
            searchPos = searchPos.offset(dir[0], dir[1]);
        }
        return null;
    }

    private Board createBoardAfterJump(Board board, Position from, Position to, Position capturePos) {
        Map<Position, Piece> pieces = new HashMap<>(board.getPieces());
        Piece piece = pieces.remove(from);
        pieces.remove(capturePos);
        pieces.put(to, piece);
        return Board.BoardFactory.createCustomBoard(board.getSize(), pieces);
    }

    private List<int[]> getMovementDirections(Piece piece, boolean isCapturing) {
        List<int[]> directions = new ArrayList<>();
        if (piece.getType() == PieceType.KING || isCapturing) {
            directions.add(new int[]{-1, -1});
            directions.add(new int[]{-1, 1});
            directions.add(new int[]{1, -1});
            directions.add(new int[]{1, 1});
        } else { // Man simple move
            int direction = piece.getColor().getDirection();
            directions.add(new int[]{direction, -1});
            directions.add(new int[]{direction, 1});
        }
        return directions;
    }

    private boolean isPromotionMove(Board board, Position to, Piece piece) {
        if (piece.getType() == PieceType.KING) return false;
        return (piece.getColor() == PlayerColor.RED && to.row() == 0) ||
               (piece.getColor() == PlayerColor.BLACK && to.row() == board.getSize().getRows() - 1);
    }
}
