package com.shalako.checkers.persistence;

import com.shalako.checkers.model.Game;

import java.util.List;

/**
 * Repository interface for game persistence.
 */
public interface GameRepository {
    
    /**
     * Saves a game to the repository.
     * 
     * @param game The game to save
     * @return The saved game
     */
    Game saveGame(Game game);
    
    /**
     * Gets a game by its ID.
     * 
     * @param gameId The ID of the game to retrieve
     * @return The game, or null if not found
     */
    Game getGame(String gameId);
    
    /**
     * Gets all games in the repository.
     * 
     * @return A list of all games
     */
    List<Game> getAllGames();
    
    /**
     * Deletes a game from the repository.
     * 
     * @param gameId The ID of the game to delete
     * @return true if the game was deleted, false otherwise
     */
    boolean deleteGame(String gameId);
}
