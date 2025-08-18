package com.shalako.checkers.engine.rules;

import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.GameType;
import com.shalako.checkers.enums.PieceType;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.Move;
import com.shalako.checkers.model.Piece;
import com.shalako.checkers.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AmericanCheckersRulesTest {

    private AmericanCheckersRules rules;
    private Board board;

    @BeforeEach
    void setUp() {
        rules = new AmericanCheckersRules();
        Map<Position, Piece> pieces = new HashMap<>();
        // Add a red piece for testing
        pieces.put(new Position(5, 1), Piece.PieceFactory.createMan(PlayerColor.RED));
        board = Board.BoardFactory.createCustomBoard(BoardSize.EIGHT_BY_EIGHT, pieces);
    }

    @Test
    void testValidSimpleMove() {
        List<Move> moves = rules.getValidMoves(board, new Position(5, 1));
        assertFalse(moves.isEmpty());
        assertEquals(2, moves.size());
    }

    @Test
    void testValidJumpMove() {
        Map<Position, Piece> pieces = new HashMap<>();
        pieces.put(new Position(3, 1), Piece.PieceFactory.createMan(PlayerColor.RED));
        pieces.put(new Position(2, 2), Piece.PieceFactory.createMan(PlayerColor.BLACK));
        board = Board.BoardFactory.createCustomBoard(BoardSize.EIGHT_BY_EIGHT, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 1));
        assertFalse(moves.isEmpty());
        assertEquals(1, moves.size());
        assertTrue(moves.get(0).isJump());
        assertEquals(new Position(1, 3), moves.get(0).getTo());
    }

    @Test
    void testKingMovement() {
        Map<Position, Piece> pieces = new HashMap<>();
        pieces.put(new Position(3, 3), Piece.PieceFactory.createKing(PlayerColor.RED));
        board = Board.BoardFactory.createCustomBoard(BoardSize.EIGHT_BY_EIGHT, pieces);

        List<Move> moves = rules.getValidMoves(board, new Position(3, 3));
        assertEquals(4, moves.size());
    }
}
