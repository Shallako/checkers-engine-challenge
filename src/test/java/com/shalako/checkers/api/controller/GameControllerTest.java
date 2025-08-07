package com.shalako.checkers.api.controller;

import com.shalako.checkers.api.dto.BoardDisplayDto;
import com.shalako.checkers.api.dto.GameResponseDto;
import com.shalako.checkers.api.dto.MoveRequestDto;
import com.shalako.checkers.api.dto.NewGameRequest;
import com.shalako.checkers.engine.GameEngine;
import com.shalako.checkers.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameEngine gameEngine;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/games";
    }

    @Test
    public void testCreateGame() {
        // Create a new game request
        NewGameRequest request = new NewGameRequest(
                "TestPlayer",
                BoardSize.EIGHT_BY_EIGHT,
                PlayerColor.RED
        );

        // Send the request
        ResponseEntity<GameResponseDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                GameResponseDto.class
        );

        // Verify the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TestPlayer", response.getBody().getRedPlayer().getName());
        assertEquals(BoardSize.EIGHT_BY_EIGHT, response.getBody().getBoard().getSize());
        assertFalse(response.getBody().isGameOver());
    }

    @Test
    public void testGetGame() {
        // Create a game first
        Game game = gameEngine.createGame(
                BoardSize.EIGHT_BY_EIGHT,
                "TestPlayer",
                PlayerColor.RED
        );

        // Get the game
        ResponseEntity<GameResponseDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + game.getId(),
                GameResponseDto.class
        );

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(game.getId(), response.getBody().getId());
    }

    @Test
    public void testGetBoard() {
        // Create a game first
        Game game = gameEngine.createGame(
                BoardSize.EIGHT_BY_EIGHT,
                "TestPlayer",
                PlayerColor.RED
        );

        // Get the board display
        ResponseEntity<BoardDisplayDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + game.getId() + "/board",
                BoardDisplayDto.class
        );

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(game.getId(), response.getBody().getId());
        assertNotNull(response.getBody().getDisplayString());
        assertEquals(BoardSize.EIGHT_BY_EIGHT, response.getBody().getSize());
        
        // Print the board display for debugging
        System.out.println("Board display:");
        System.out.println(response.getBody().getDisplayString());
    }

    @Test
    public void testMakeMove() {
        // Create a game with the human player as BLACK so computer (RED) goes first
        Game game = gameEngine.createGame(
                BoardSize.EIGHT_BY_EIGHT,
                "TestPlayer",
                PlayerColor.BLACK
        );

        // The computer (RED) should make the first move automatically during game creation
        // Now we'll test the human (BLACK) making a move
        
        // First, get the current game state to see the board after computer's move
        ResponseEntity<GameResponseDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + game.getId(),
                GameResponseDto.class
        );
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        
        // Print game state for debugging
        System.out.println("Game ID: " + getResponse.getBody().getId());
        System.out.println("Current turn: " + getResponse.getBody().getCurrentTurn());
        System.out.println("Current player: " + getResponse.getBody().getCurrentPlayerName());
        System.out.println("Red player ID: " + getResponse.getBody().getRedPlayer().getId());
        System.out.println("Black player ID: " + getResponse.getBody().getBlackPlayer().getId());
        
        // Make sure we're using the correct player ID from the response
        String currentPlayerId = getResponse.getBody().getCurrentTurn() == PlayerColor.BLACK ? 
                getResponse.getBody().getBlackPlayer().getId() : 
                getResponse.getBody().getRedPlayer().getId();
        
        // Create a dummy move request for the current player
        // We'll use null positions which will be handled by the controller
        // This simulates the approach used in the original CheckersApp
        MoveRequestDto moveRequest = new MoveRequestDto(
                game.getId(),
                currentPlayerId,
                null,
                null
        );

        // Make the move
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/" + game.getId() + "/moves",
                moveRequest,
                String.class
        );

        // Print the response for debugging
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
