package com.shalako.checkers.api.dto;

import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.BoardSize;
import lombok.Data;

/**
 * DTO for displaying a checkers board in a human-readable format.
 */
@Data
public class BoardDisplayDto {
    private String id;
    private BoardSize size;
    private String displayString;

    /**
     * Creates a BoardDisplayDto from a game ID and board.
     */
    public static BoardDisplayDto fromBoard(String gameId, Board board) {
        BoardDisplayDto dto = new BoardDisplayDto();
        dto.id = gameId;
        dto.size = board.getSize();
        dto.displayString = board.getDisplayString();
        return dto;
    }
}
