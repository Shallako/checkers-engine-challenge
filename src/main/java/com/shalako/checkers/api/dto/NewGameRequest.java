package com.shalako.checkers.api.dto;

import com.shalako.checkers.model.BoardSize;
import com.shalako.checkers.model.PlayerColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewGameRequest {
    @NotBlank(message = "Player name is required")
    private String playerName;
    
    @NotNull(message = "Board size is required")
    private BoardSize boardSize;
    
    @NotNull(message = "Player color is required")
    private PlayerColor playerColor;
}
