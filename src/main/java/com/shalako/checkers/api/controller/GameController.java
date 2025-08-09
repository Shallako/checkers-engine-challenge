package com.shalako.checkers.api.controller;

import com.shalako.checkers.api.dto.BoardDisplayDto;
import com.shalako.checkers.api.dto.GameResponseDto;
import com.shalako.checkers.api.dto.MoveRequestDto;
import com.shalako.checkers.api.dto.NewGameRequest;
import com.shalako.checkers.engine.GameEngine;
import com.shalako.checkers.enums.PlayerType;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.MoveRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for Checkers game operations.
 */
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/games")
public class GameController {
    private final GameEngine gameEngine;

    public GameController(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Creates a new game.
     */
    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@Valid @RequestBody NewGameRequest request) {
        Game game = gameEngine.createGame(
                request.getBoardSize(),
                request.getPlayerName(),
                request.getPlayerColor()
        );
        
        // If computer goes first, make its move
        if (game.getCurrentPlayer().getType() == PlayerType.COMPUTER) {
            MoveRequest dummyRequest = MoveRequest.MoveRequestFactory.createMoveRequest(
                    game.getId(),
                    game.getCurrentPlayer().getId(),
                    null,
                    null
            );
            game = gameEngine.makeMove(dummyRequest);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GameResponseDto.fromGame(game));
    }

    /**
     * Gets a game by ID.
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponseDto> getGame(@PathVariable String gameId) {
        Game game = gameEngine.getGame(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return ResponseEntity.ok(GameResponseDto.fromGame(game));
    }

    /**
     * Gets a human-readable display of the game board.
     */
    @GetMapping("/{gameId}/board")
    public ResponseEntity<BoardDisplayDto> getBoard(@PathVariable String gameId) {
        Game game = gameEngine.getGame(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return ResponseEntity.ok(BoardDisplayDto.fromBoard(gameId, game.getBoard()));
    }

    /**
     * Makes a move in a game.
     */
    @PostMapping("/{gameId}/moves")
    public ResponseEntity<GameResponseDto> makeMove(
            @PathVariable String gameId,
            @Valid @RequestBody MoveRequestDto moveRequestDto) {
        
        // Validate that the game ID in the path matches the one in the request body
        if (!gameId.equals(moveRequestDto.getGameId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Game ID in the path must match the one in the request body"
            );
        }
        
        try {
            Game game = gameEngine.getGame(gameId);
            if (game == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
            }
            
            // Check if this is a computer move (indicated by null positions)
            if (moveRequestDto.getFrom() == null || moveRequestDto.getTo() == null) {
                // For computer moves, we just need the game ID and player ID
                if (game.getCurrentPlayer().getType() != PlayerType.COMPUTER) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Cannot make a computer move for a human player"
                    );
                }
                
                MoveRequest moveRequest = MoveRequest.MoveRequestFactory.createMoveRequest(
                        moveRequestDto.getGameId(),
                        moveRequestDto.getPlayerId(),
                        null,
                        null
                );
                
                Game updatedGame = gameEngine.makeMove(moveRequest);
                return ResponseEntity.ok(GameResponseDto.fromGame(updatedGame));
            } else {
                // For human moves, we need all the details
                MoveRequest moveRequest = MoveRequest.MoveRequestFactory.createMoveRequest(
                        moveRequestDto.getGameId(),
                        moveRequestDto.getPlayerId(),
                        moveRequestDto.getFrom(),
                        moveRequestDto.getTo()
                );
                
                Game updatedGame = gameEngine.makeMove(moveRequest);
                return ResponseEntity.ok(GameResponseDto.fromGame(updatedGame));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
