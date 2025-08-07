package com.shalako.checkers.api.dto;

import com.shalako.checkers.model.Position;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for making a move in a game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequestDto {
    @NotBlank(message = "Game ID is required")
    private String gameId;
    
    @NotBlank(message = "Player ID is required")
    private String playerId;
    
    // From and To can be null for computer moves
    private Position from;
    
    private Position to;
}
