package com.shalako.checkers.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Represents a move in a checkers game. A move consists of a path of two or more positions. For
 * simple moves, the path will have two positions. For multi-jump moves, the path will have three or
 * more positions.
 */
@Getter
public class Move {

  private final List<Position> path;
  private final List<Position> capturedPieces;
  private final boolean promotion;

  private Move(List<Position> path, List<Position> capturedPieces, boolean promotion) {
    if (path == null || path.size() < 2) {
      throw new IllegalArgumentException("Move path must contain at least two positions.");
    }
    this.path = Collections.unmodifiableList(new ArrayList<>(path));
    this.capturedPieces =
        Collections.unmodifiableList(
            capturedPieces != null ? new ArrayList<>(capturedPieces) : Collections.emptyList());
    this.promotion = promotion;
  }

  public Position getFrom() {
    return path.get(0);
  }

  public Position getTo() {
    return path.get(path.size() - 1);
  }

  public boolean isJump() {
    return !capturedPieces.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Move move = (Move) o;
    return promotion == move.promotion
        && Objects.equals(path, move.path)
        && Objects.equals(capturedPieces, move.capturedPieces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, capturedPieces, promotion);
  }

  @Override
  public String toString() {
    return path.stream().map(Position::toString).collect(Collectors.joining(" -> "))
        + (isJump() ? " (captures: " + capturedPieces + ")" : "")
        + (promotion ? " (promotion)" : "");
  }

  /** Factory for creating moves. */
  public static class MoveFactory {
    /** Creates a simple move without captures or promotion. */
    public static Move createSimpleMove(Position from, Position to) {
      return new Move(Arrays.asList(from, to), Collections.emptyList(), false);
    }

    /** Creates a move with promotion but without captures. */
    public static Move createPromotionMove(Position from, Position to) {
      return new Move(Arrays.asList(from, to), Collections.emptyList(), true);
    }

    /** Creates a jump move with a single capture. */
    public static Move createJumpMove(Position from, Position to, Position captured) {
      return new Move(Arrays.asList(from, to), Collections.singletonList(captured), false);
    }

    /** Creates a jump move with a single capture and promotion. */
    public static Move createJumpPromotionMove(Position from, Position to, Position captured) {
      return new Move(Arrays.asList(from, to), Collections.singletonList(captured), true);
    }

    /** Creates a multi-jump move with multiple captures. */
    public static Move createMultiJumpMove(List<Position> path, List<Position> captured) {
      return new Move(path, captured, false);
    }

    /** Creates a multi-jump move with multiple captures and promotion. */
    public static Move createMultiJumpPromotionMove(
        List<Position> path, List<Position> captured) {
      return new Move(path, captured, true);
    }
  }
}
