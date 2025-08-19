package com.shalako.checkers.engine.rules;

import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.Move;
import com.shalako.checkers.model.MoveRequest;
import com.shalako.checkers.model.Position;

import java.util.List;

public interface GameRules {
    Move validateMove(Game game, MoveRequest moveRequest);

    List<Move> getValidMoves(Board board, Position position);

    boolean hasValidMoves(Board board, PlayerColor color);
}
