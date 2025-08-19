package com.shalako.checkers.engine;

import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.Move;
import com.shalako.checkers.model.Piece;
import com.shalako.checkers.model.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.shalako.checkers.engine.rules.GameRules;


/**
 * Implements the computer player logic for the checkers game.
 */
public class ComputerPlayer {
    private final GameRulesFactory gameRulesFactory;
    private final Random random;

    public ComputerPlayer(GameRulesFactory gameRulesFactory) {
        this.gameRulesFactory = gameRulesFactory;
        this.random = new Random();
    }

    /**
     * Selects a move for the computer player.
     * Returns null if no valid moves are available.
     */
    public Move selectMove(Game game) {
        PlayerColor computerColor = game.getCurrentTurn();
        Board board = game.getBoard();
        GameRules rules = gameRulesFactory.getRules(game.getGameType());

        // Get all pieces of the computer's color
        List<Position> computerPieces = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            if (entry.getValue().getColor() == computerColor) {
                computerPieces.add(entry.getKey());
            }
        }

        // Collect all valid moves
        List<Move> allValidMoves = new ArrayList<>();
        for (Position position : computerPieces) {
            allValidMoves.addAll(rules.getValidMoves(board, position));
        }

        if (allValidMoves.isEmpty()) {
            return null;
        }
        
        // Prioritize moves: jumps > promotions > regular moves
        List<Move> jumpMoves = new ArrayList<>();
        List<Move> promotionMoves = new ArrayList<>();
        List<Move> regularMoves = new ArrayList<>();
        
        for (Move move : allValidMoves) {
            if (move.isJump()) {
                jumpMoves.add(move);
            } else if (move.isPromotion()) {
                promotionMoves.add(move);
            } else {
                regularMoves.add(move);
            }
        }
        
        // Select the best move based on priority
        if (!jumpMoves.isEmpty()) {
            // Prioritize multi-jumps by the number of captures
            jumpMoves.sort(Comparator.comparing(move -> -move.getCapturedPieces().size()));
            return jumpMoves.get(0);
        } else if (!promotionMoves.isEmpty()) {
            return promotionMoves.get(random.nextInt(promotionMoves.size()));
        } else {
            return regularMoves.get(random.nextInt(regularMoves.size()));
        }
    }
}
