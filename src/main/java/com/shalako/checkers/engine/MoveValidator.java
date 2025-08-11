package com.shalako.checkers.engine;

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

/**
 * Validates moves in a checkers game.
 */
public class MoveValidator {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MoveValidator.class);

  /**
   * Validates a move request and returns a valid Move object.
   */
  public Move validateMove(Game game, MoveRequest moveRequest) {
      LOG.info("[VALIDATE MOVE REQUEST] gameId={}, playerId={}, from={}, to={}, currentTurn={}",
              moveRequest.getGameId(), moveRequest.getPlayerId(),
              moveRequest.getFrom(), moveRequest.getTo(), game.getCurrentTurn());
    Board board = game.getBoard();
    Position from = moveRequest.getFrom();
    Position to = moveRequest.getTo();

    // Require valid positions for validation (human move only)
    if (from == null || to == null) {
      LOG.warn("[VALIDATION FAILED] Missing positions: from={}, to={}", from, to);
      throw new IllegalArgumentException("From and To positions are required for move validation");
    }

    // Check if positions are valid
    if (!from.isValidForBoard(board.getSize()) || !to.isValidForBoard(board.getSize())) {
      LOG.warn("[VALIDATION FAILED] Invalid board positions: fromValid={}, toValid={}, boardSize={}", from.isValidForBoard(board.getSize()), to.isValidForBoard(board.getSize()), board.getSize());
      throw new IllegalArgumentException("Invalid position");
    }

    // Check if there is a piece at the from position
    Piece piece = board.getPieceAt(from);
    if (piece == null) {
      LOG.warn("[VALIDATION FAILED] No piece at from position: {}", from);
      throw new IllegalArgumentException("No piece at position: " + from);
    }

    // Check if the piece belongs to the current player
    if (piece.getColor() != game.getCurrentTurn()) {
      LOG.warn("[VALIDATION FAILED] Attempt to move opponent piece: pieceColor={}, currentTurn={}", piece.getColor(), game.getCurrentTurn());
      throw new IllegalArgumentException("Cannot move opponent's piece");
    }

    // Check if the destination is empty
    if (!board.isEmpty(to)) {
      LOG.warn("[VALIDATION FAILED] Destination not empty: {}", to);
      throw new IllegalArgumentException("Destination is not empty: " + to);
    }

    // Check if the move is diagonal
    if (!isDiagonalMove(from, to)) {
      LOG.warn("[VALIDATION FAILED] Move not diagonal: from={}, to={}", from, to);
      throw new IllegalArgumentException("Move must be diagonal");
    }

    // Check if any jump moves are available for the current player
    boolean jumpMovesAvailable = hasJumpMoves(board, game.getCurrentTurn());

    // Get valid moves for the selected piece
    List<Move> validMoves = getValidMoves(board, from);

    // If jump moves are available, only allow jump moves
    if (jumpMovesAvailable) {
      for (Move move : validMoves) {
        if (move.isJump() && move.getTo().equals(to)) {
          LOG.debug("[VALIDATION PASSED] Jump move selected: {} -> {}", from, to);
          return move;
        }
      }
      LOG.warn("[VALIDATION FAILED] Jump required but attempted non-jump: from={}, to={}", from, to);
      throw new IllegalArgumentException("Jump move is mandatory when available");
    } else {
      // If no jump moves are available, allow any valid move
      for (Move move : validMoves) {
        if (move.getTo().equals(to)) {
          LOG.debug("[VALIDATION PASSED] Simple move selected: {} -> {}", from, to);
          return move;
        }
      }
    }

    LOG.warn("[VALIDATION FAILED] Invalid move for piece at {} to {}", from, to);
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
   * Checks if a player has any jump moves available.
   */
  public boolean hasJumpMoves(Board board, PlayerColor color) {
    for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
      if (entry.getValue().getColor() == color) {
        List<Move> jumps = getValidJumps(board, entry.getKey(), entry.getValue(), new ArrayList<>());
        if (!jumps.isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets all valid moves for a piece at the specified position.
   * FIXED: Corrected the logic to properly handle both regular pieces and kings.
   */
  public List<Move> getValidMoves(Board board, Position position) {
    Piece piece = board.getPieceAt(position);
    if (piece == null) {
      return Collections.emptyList();
    }

    List<Move> validMoves = new ArrayList<>();

    // Check for jumps first
    List<Move> jumps = getValidJumps(board, position, piece, new ArrayList<>());

    // FIXED: Separate logic for kings vs regular pieces
    if (piece.getType() == PieceType.KING) {
      // For kings, add all available jumps
      validMoves.addAll(jumps);

      // Kings can also make simple moves (Flying Kings in international checkers)
      // But if there are jumps available for ANY piece of this color, jumps are mandatory
      if (!hasJumpMoves(board, piece.getColor())) {
        validMoves.addAll(getValidSimpleMoves(board, position, piece));
      }
    } else {
      // For regular pieces (men)
      if (!jumps.isEmpty()) {
        // If this piece has jumps available, only return jumps
        validMoves.addAll(jumps);
      } else {
        // If this piece has no jumps, check if ANY piece of this color has jumps
        if (!hasJumpMoves(board, piece.getColor())) {
          // No jumps available for any piece, so simple moves are allowed
          validMoves.addAll(getValidSimpleMoves(board, position, piece));
        }
        // If other pieces have jumps available, this piece cannot move
      }
    }

    return validMoves;
  }

  /**
   * Gets all valid simple moves (non-jumps) for a piece.
   * FIXED: Added debugging and improved logic.
   */
  private List<Move> getValidSimpleMoves(Board board, Position position, Piece piece) {
    List<Move> moves = new ArrayList<>();

    // Determine the directions the piece can move
    List<int[]> directions = getMovementDirections(piece);

    LOG.debug("Getting simple moves for {} {} at {}", piece.getColor(), piece.getType(), position);
    LOG.debug("Movement directions: {}", directions.size());

    for (int[] dir : directions) {
      LOG.debug("Checking direction [{}, {}]", dir[0], dir[1]);

      if (piece.getType() == PieceType.KING && isInternational(board)) {
        // International (10x10): Flying Kings can move any number of squares diagonally
        int distance = 1;
        while (true) {
          Position newPos = position.offset(dir[0] * distance, dir[1] * distance);

          LOG.debug("King checking position at distance {}: {}", distance, newPos);

          // Stop if we've reached the edge of the board
          if (!newPos.isValidForBoard(board.getSize())) {
            LOG.debug("Position invalid for board");
            break;
          }

          // Stop if the position is not empty
          if (!board.isEmpty(newPos)) {
            LOG.debug("Position not empty");
            break;
          }

          // This is a valid move
          Move move = Move.MoveFactory.createSimpleMove(position, newPos);
          moves.add(move);
          LOG.debug("Added valid king move: {} -> {}", position, newPos);

          // Check the next position in this direction
          distance++;
        }
      } else if (piece.getType() == PieceType.KING) {
        // Standard (8x8): kings move only one square diagonally
        Position newPos = position.offset(dir[0], dir[1]);
        LOG.debug("Standard king checking position: {}", newPos);
        if (newPos.isValidForBoard(board.getSize()) && board.isEmpty(newPos)) {
          Move move = Move.MoveFactory.createSimpleMove(position, newPos);
          moves.add(move);
          LOG.debug("Added valid standard king move: {} -> {}", position, newPos);
        }
      } else {
        // Regular pieces (men) can only move one square diagonally
        Position newPos = position.offset(dir[0], dir[1]);

        LOG.debug("Man checking position: {}", newPos);

        // Check if the new position is valid and empty
        if (newPos.isValidForBoard(board.getSize())) {
          LOG.debug("Position is valid for board");
          if (board.isEmpty(newPos)) {
            LOG.debug("Position is empty");

            // Check if the move results in a promotion
            boolean promotion = isPromotionMove(board, newPos, piece);
            LOG.debug("Is promotion move: {}", promotion);

            // Create the appropriate move
            Move move = promotion
                ? Move.MoveFactory.createPromotionMove(position, newPos)
                : Move.MoveFactory.createSimpleMove(position, newPos);

            moves.add(move);
            LOG.debug("Added valid man move: {} -> {}", position, newPos);
          } else {
            LOG.debug("Position is occupied");
          }
        } else {
          System.out.println("DEBUG: Position is invalid for board");
        }
      }
    }

    System.out.println("DEBUG: Total simple moves found: " + moves.size());
    return moves;
  }

  /**
   * Gets all valid jumps for a piece.
   */
  private List<Move> getValidJumps(Board board, Position position, Piece piece, List<Position> capturedSoFar) {
    List<Move> jumps = new ArrayList<>();

    // Determine the directions the piece can move/capture
    List<int[]> directions = getMovementDirections(piece);
    // In international rules (10x10), men can capture backwards as well
    if (piece.getType() == PieceType.MAN && isInternational(board)) {
      directions = new ArrayList<>();
      directions.add(new int[]{-1, -1});
      directions.add(new int[]{-1, 1});
      directions.add(new int[]{1, -1});
      directions.add(new int[]{1, 1});
    }

    for (int[] dir : directions) {
      // Calculate the position of the potential captured piece
      Position capturePos = position.offset(dir[0], dir[1]);

      if (piece.getType() == PieceType.KING) {
        if (isInternational(board)) {
          // International (10x10): Flying Kings capture with any landing distance beyond the captured piece
          if (capturePos.isValidForBoard(board.getSize())) {
            Piece capturePiece = board.getPieceAt(capturePos);
            if (capturePiece != null && capturePiece.getColor() != piece.getColor()
                && !capturedSoFar.contains(capturePos)) {
              int distance = 2;  // Start with the immediate landing position
              while (true) {
                Position landingPos = position.offset(dir[0] * distance, dir[1] * distance);
                if (!landingPos.isValidForBoard(board.getSize())) {
                  break;
                }
                if (!board.isEmpty(landingPos)) {
                  break;
                }
                List<Position> newCaptured = new ArrayList<>(capturedSoFar);
                newCaptured.add(capturePos);
                List<Move> continuedJumps = getValidJumps(
                    createBoardAfterJump(board, position, landingPos, capturePos),
                    landingPos,
                    piece,
                    newCaptured
                );
                if (continuedJumps.isEmpty()) {
                  jumps.add(Move.MoveFactory.createJumpMove(position, landingPos, capturePos));
                } else {
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
                distance++;
              }
            }
          }
        } else {
          // Standard (8x8): kings capture like men; landing immediately beyond captured piece
          Position landingPos = position.offset(dir[0] * 2, dir[1] * 2);
          if (isValidJump(board, position, capturePos, landingPos, piece, capturedSoFar)) {
            List<Position> newCaptured = new ArrayList<>(capturedSoFar);
            newCaptured.add(capturePos);
            // Continue jumping from landing position
            List<Move> continuedJumps = getValidJumps(
                createBoardAfterJump(board, position, landingPos, capturePos),
                landingPos,
                piece,
                newCaptured
            );
            if (continuedJumps.isEmpty()) {
              jumps.add(Move.MoveFactory.createJumpMove(position, landingPos, capturePos));
            } else {
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
      } else {
        // For regular pieces (men), we only check the immediate landing position after a jump
        Position landingPos = position.offset(dir[0] * 2, dir[1] * 2);

        // Check if the jump is valid
        if (isValidJump(board, position, capturePos, landingPos, piece, capturedSoFar)) {
          // Create a new list of captured pieces
          List<Position> newCaptured = new ArrayList<>(capturedSoFar);
          newCaptured.add(capturePos);

          // Check if the move results in a promotion
          boolean promotion = isPromotionMove(board, landingPos, piece);

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
   * FIXED: Added debugging to verify directions.
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
    int rowDiff = Math.abs(to.row() - from.row());
    int colDiff = Math.abs(to.column() - from.column());

    return rowDiff > 0 && rowDiff == colDiff;
  }

  /**
   * Checks if a move results in a promotion.
   * FIXED: Improved promotion logic to work with different board sizes.
   */
  private boolean isPromotionMove(Board board, Position to, Piece piece) {
    if (piece.getType() == PieceType.KING) {
      return false;
    }

    // A piece is promoted when it reaches the opposite end of the board
    if (piece.getColor() == PlayerColor.RED) {
      return to.row() == 0;
    } else {
      // For BLACK pieces, they need to reach the last row based on board size
      int lastRow = board.getSize().getRows() - 1;
      return to.row() == lastRow;
    }
  }

  // Determines whether the game uses international rules based on board size
  private boolean isInternational(Board board) {
    return board.getSize().getRows() >= 10;
  }
}
