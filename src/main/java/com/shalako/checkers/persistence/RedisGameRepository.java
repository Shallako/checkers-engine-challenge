package com.shalako.checkers.persistence;

import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.GameState;
import com.shalako.checkers.enums.GameType;
import com.shalako.checkers.enums.PieceType;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.enums.PlayerType;
import com.shalako.checkers.model.Board;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.Piece;
import com.shalako.checkers.model.Player;
import com.shalako.checkers.model.Position;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis implementation of the GameRepository interface.
 */
public class RedisGameRepository implements GameRepository {
    private static final String GAME_KEY_PREFIX = "game:";
    private static final String GAMES_SET_KEY = "games";
    
    private final JedisPool jedisPool;
    
    public RedisGameRepository(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
    
    @Override
    public Game saveGame(Game game) {
        try (Jedis jedis = jedisPool.getResource()) {
            String gameKey = GAME_KEY_PREFIX + game.getId();
            
            // Store game metadata
            Map<String, String> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("state", game.getState().name());
            gameData.put("currentTurn", game.getCurrentTurn().name());
            gameData.put("boardSize", game.getBoard().getSize().name());
            gameData.put("createdAt", game.getCreatedAt().toString());
            gameData.put("updatedAt", Instant.now().toString());
            gameData.put("gameType", game.getGameType().name());
            
            // Store player data
            gameData.put("redPlayer:id", game.getRedPlayer().getId());
            gameData.put("redPlayer:name", game.getRedPlayer().getName());
            gameData.put("redPlayer:type", game.getRedPlayer().getType().name());
            
            gameData.put("blackPlayer:id", game.getBlackPlayer().getId());
            gameData.put("blackPlayer:name", game.getBlackPlayer().getName());
            gameData.put("blackPlayer:type", game.getBlackPlayer().getType().name());
            
            // Store the game data in a Redis hash
            jedis.hset(gameKey, gameData);
            
            // Store the board state
            saveBoardState(jedis, gameKey, game.getBoard());
            
            // Add the game ID to the set of all games
            jedis.sadd(GAMES_SET_KEY, game.getId());
            
            return game;
        }
    }
    
    @Override
    public Game getGame(String gameId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String gameKey = GAME_KEY_PREFIX + gameId;
            
            // Check if the game exists
            if (!jedis.exists(gameKey)) {
                return null;
            }
            
            // Get game metadata
            Map<String, String> gameData = jedis.hgetAll(gameKey);
            
            // Parse board size
            BoardSize boardSize = BoardSize.valueOf(gameData.get("boardSize"));
            
            // Create players
            Player redPlayer = Player.PlayerFactory.createPlayer(
                gameData.get("redPlayer:id"),
                gameData.get("redPlayer:name"),
                PlayerType.valueOf(gameData.get("redPlayer:type")),
                PlayerColor.RED
            );
            
            Player blackPlayer = Player.PlayerFactory.createPlayer(
                gameData.get("blackPlayer:id"),
                gameData.get("blackPlayer:name"),
                PlayerType.valueOf(gameData.get("blackPlayer:type")),
                PlayerColor.BLACK
            );
            
            // Load the board state
            Board board = loadBoardState(jedis, gameKey, boardSize);
            
            // Create and return the game
            return Game.GameFactory.createGame(
                gameId,
                board,
                redPlayer,
                blackPlayer,
                PlayerColor.valueOf(gameData.get("currentTurn")),
                GameState.valueOf(gameData.get("state")),
                Instant.parse(gameData.get("createdAt")),
                Instant.parse(gameData.get("updatedAt")),
                GameType.valueOf(gameData.getOrDefault("gameType", GameType.STANDARD_AMERICAN.name()))
            );
        }
    }
    
    @Override
    public List<Game> getAllGames() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> gameIds = jedis.smembers(GAMES_SET_KEY);
            List<Game> games = new ArrayList<>();
            
            for (String gameId : gameIds) {
                Game game = getGame(gameId);
                if (game != null) {
                    games.add(game);
                }
            }
            
            return games;
        }
    }
    
    @Override
    public boolean deleteGame(String gameId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String gameKey = GAME_KEY_PREFIX + gameId;
            String boardKey = gameKey + ":board";
            
            // Check if the game exists
            if (!jedis.exists(gameKey)) {
                return false;
            }
            
            // Delete the game data and board state
            jedis.del(gameKey, boardKey);
            
            // Remove the game ID from the set of all games
            jedis.srem(GAMES_SET_KEY, gameId);
            
            return true;
        }
    }
    
    /**
     * Saves the board state to Redis.
     */
    private void saveBoardState(Jedis jedis, String gameKey, Board board) {
        String boardKey = gameKey + ":board";
        
        // Clear any existing board data
        jedis.del(boardKey);
        
        // Store each piece on the board
        for (Map.Entry<Position, Piece> entry : board.getPieces().entrySet()) {
            Position pos = entry.getKey();
            Piece piece = entry.getValue();
            
            String posKey = pos.row() + ":" + pos.column();
            String pieceValue = piece.getColor().name() + ":" + piece.getType().name();
            
            jedis.hset(boardKey, posKey, pieceValue);
        }
    }
    
    /**
     * Loads the board state from Redis.
     */
    private Board loadBoardState(Jedis jedis, String gameKey, BoardSize boardSize) {
        String boardKey = gameKey + ":board";
        Map<String, String> boardData = jedis.hgetAll(boardKey);
        Map<Position, Piece> pieces = new HashMap<>();
        
        for (Map.Entry<String, String> entry : boardData.entrySet()) {
            // Parse position
            String[] positionParts = entry.getKey().split(":");
            int row = Integer.parseInt(positionParts[0]);
            int col = Integer.parseInt(positionParts[1]);
            Position position = new Position(row, col);
            
            // Parse piece
            String[] pieceParts = entry.getValue().split(":");
            PlayerColor color = PlayerColor.valueOf(pieceParts[0]);
            PieceType type = PieceType.valueOf(pieceParts[1]);
            
            // Create piece based on type
            Piece piece = (type == PieceType.MAN) 
                ? Piece.PieceFactory.createMan(color)
                : Piece.PieceFactory.createKing(color);
            
            pieces.put(position, piece);
        }
        
        return Board.BoardFactory.createCustomBoard(boardSize, pieces);
    }
}
