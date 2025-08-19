package com.shalako.checkers.engine.rules;

import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.Move;
import com.shalako.checkers.model.Piece;
import com.shalako.checkers.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class InternationalDraughtsRulesTest {

    private InternationalDraughtsRules rules;
    private Board board;

    @BeforeEach
    void setUp() {
        rules = new InternationalDraughtsRules();
    }

    @Test
    void testManCapturesBackwards() {
        Map<Position, Piece> pieces = new HashMap<>();
        pieces.put(new Position(3, 3), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(4, 4), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        board = Board.BoardFactory.createCustomBoard(BoardSize.TEN_BY_TEN, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 3));
        assertFalse(moves.isEmpty());
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(new Position(5, 5))));
    }

    @Test
    void testFlyingKingSimpleMove() {
        Map<Position, Piece> pieces = new HashMap<>();
        pieces.put(new Position(3, 3), Piece.PieceFactory.createKing(PlayerColor.RED));
        board = Board.BoardFactory.createCustomBoard(BoardSize.TEN_BY_TEN, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 3));
        // Expects moves along all 4 diagonals
        assertTrue(moves.size() > 4);
    }

    @Test
    void testFlyingKingCapture() {
        Map<Position, Piece> pieces = new HashMap<>();
        pieces.put(new Position(3, 3), Piece.PieceFactory.createKing(PlayerColor.RED));
        pieces.put(new Position(5, 5), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        board = Board.BoardFactory.createCustomBoard(BoardSize.TEN_BY_TEN, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 3));
        assertFalse(moves.isEmpty());
        // King can land on any square behind the captured piece
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(new Position(6, 6))));
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(new Position(7, 7))));
    }

    @Test
    void testDoubleJumpIsMaxCapture() {
        Map<Position, Piece> pieces = new HashMap<>();
        // This piece can capture 2 pieces
        pieces.put(new Position(3, 1), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(4, 2), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        pieces.put(new Position(6, 2), Piece.PieceFactory.createMan(PlayerColor.BLACK));

        board = Board.BoardFactory.createCustomBoard(BoardSize.TEN_BY_TEN, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 1));
        log.info("Moves for (3,1): {}", moves);
        assertEquals(1, moves.size());
        assertEquals(2, moves.get(0).getCapturedPieces().size());
    }

    @Test
    void testSingleJumpIsNotMaxCapture() {
        Map<Position, Piece> pieces = new HashMap<>();
        // This piece can capture 2 pieces
        pieces.put(new Position(3, 1), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(4, 2), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        pieces.put(new Position(6, 2), Piece.PieceFactory.createMan(PlayerColor.BLACK));

        // This piece can capture 1 piece
        pieces.put(new Position(3, 5), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(4, 6), Piece.PieceFactory.createMan(PlayerColor.BLACK));

        board = Board.BoardFactory.createCustomBoard(BoardSize.TEN_BY_TEN, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 5));
        log.info("Moves for (3,5): {}", moves);
        assertTrue(moves.isEmpty());
    }

    @Test
    void testKingMultiJumpDoesNotCauseInfiniteLoop() {
        Map<Position, Piece> pieces = new HashMap<>();
        pieces.put(new Position(5, 5), Piece.PieceFactory.createKing(PlayerColor.RED));
        pieces.put(new Position(4, 4), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        pieces.put(new Position(2, 6), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        board = Board.BoardFactory.createCustomBoard(BoardSize.TEN_BY_TEN, pieces);

        // The king can jump from (5,5) to (3,3) capturing (4,4).
        // Then from (3,3) to (1,7) capturing (2,6).
        // This test ensures that the getValidMoves method does not hang.
        assertTimeout(java.time.Duration.ofSeconds(1), () -> {
            rules.getValidMoves(board, new Position(5, 5));
        });
    }
}
