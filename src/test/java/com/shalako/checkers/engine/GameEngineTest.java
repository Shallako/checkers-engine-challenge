package com.shalako.checkers.engine;

import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.GameState;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.model.*;
import com.shalako.checkers.persistence.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GameEngineTest {

    @Mock
    private GameRepository gameRepository;

    private GameEngine gameEngine;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configure the mock repository to return the game that is saved
        when(gameRepository.saveGame(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        gameEngine = new GameEngine(gameRepository);
    }

    @Test
    public void testCreateGameAlwaysInProgress() {
        // Create a game with the standard board size
        Game game = gameEngine.createGame(BoardSize.STANDARD, "Player1", PlayerColor.RED);
        
        // Print the game state
        System.out.println("[DEBUG_LOG] Game state: " + game.getState());
        System.out.println("[DEBUG_LOG] Is game over: " + game.isGameOver());
        
        // Verify that the game is in the IN_PROGRESS state
        assertEquals(GameState.IN_PROGRESS, game.getState(), "Game should be in the IN_PROGRESS state");
        assertFalse(game.isGameOver(), "Game should not be over");
    }
}
