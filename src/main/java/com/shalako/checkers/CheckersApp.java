package com.shalako.checkers;

import com.shalako.checkers.engine.GameEngine;
import com.shalako.checkers.engine.GameRulesFactory;
import com.shalako.checkers.enums.BoardSize;
import com.shalako.checkers.enums.PlayerColor;
import com.shalako.checkers.enums.PlayerType;
import com.shalako.checkers.model.Game;
import com.shalako.checkers.model.MoveRequest;
import com.shalako.checkers.model.Player;
import com.shalako.checkers.model.Position;
import com.shalako.checkers.persistence.GameRepository;
import com.shalako.checkers.persistence.RedisGameRepository;
import java.util.Scanner;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Main application class for the Checkers game.
 */
public class CheckersApp {
    private final GameEngine gameEngine;
    private final Scanner scanner;
    private Game currentGame;
    private Player humanPlayer;

    public CheckersApp(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the application.
     */
    public void start() {
        System.out.println("Welcome to Checkers!");
        
        while (true) {
            if (currentGame == null) {
                showMainMenu();
            } else {
                playGame();
            }
        }
    }

    /**
     * Shows the main menu.
     */
    private void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. New Game");
        System.out.println("2. Exit");
        System.out.print("Enter your choice: ");
        
        int choice = readIntInput();
        
        switch (choice) {
            case 1:
                createNewGame();
                break;
            case 2:
                System.out.println("Thank you for playing!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Creates a new game.
     */
    private void createNewGame() {
        System.out.println("\n=== New Game ===");
        
        // Get player name
        System.out.print("Enter your name: ");
        String playerName = scanner.nextLine();
        
        // Get board size
        System.out.println("Select board size:");
        System.out.println("1. 8x8");
        System.out.println("2. 10x10");
        System.out.print("Enter your choice: ");
        
        int boardSizeChoice = readIntInput();
        BoardSize boardSize = (boardSizeChoice == 2) ? BoardSize.TEN_BY_TEN : BoardSize.EIGHT_BY_EIGHT;
        
        // Get player color
        System.out.println("Select your color:");
        System.out.println("1. Red (moves first)");
        System.out.println("2. Black");
        System.out.print("Enter your choice: ");
        
        int colorChoice = readIntInput();
        PlayerColor playerColor = (colorChoice == 2) ? PlayerColor.BLACK : PlayerColor.RED;
        
        // Create the game
        currentGame = gameEngine.createGame(boardSize, playerName, playerColor);
        humanPlayer = currentGame.getPlayerByColor(playerColor);
        
        System.out.println("\nGame created! You are playing as " + playerColor + ".");
        
        // If computer goes first, make its move
        if (currentGame.getCurrentPlayer().getType() == PlayerType.COMPUTER) {
            System.out.println("Computer is making its move...");
            MoveRequest dummyRequest = MoveRequest.MoveRequestFactory.createMoveRequest(
                currentGame.getId(), 
                currentGame.getCurrentPlayer().getId(),
                null,
                null,
                PlayerType.COMPUTER
            );
            currentGame = gameEngine.makeMove(dummyRequest);
        }
    }

    /**
     * Plays the current game.
     */
    private void playGame() {
        // Display the board
        System.out.println("\nCurrent board:");
        System.out.println(currentGame.getBoard().getDisplayString());
        
        // Check if the game is over
        if (currentGame.isGameOver()) {
            System.out.println("Game over! " + currentGame.getState().getDisplayText());
            currentGame = null;
            return;
        }
        
        // Display whose turn it is
        Player currentPlayer = currentGame.getCurrentPlayer();
        System.out.println("Current turn: " + currentPlayer.getName() + " (" + currentPlayer.getColor() + ")");
        
        // If it's the human player's turn, get their move
        if (currentPlayer.getType() == PlayerType.HUMAN) {
            getMoveFromHuman();
        } else {
            System.out.println("Computer is thinking...");
            // The computer's move is handled automatically in the game engine
            // We just need to pass a dummy request to trigger it
            MoveRequest dummyRequest = MoveRequest.MoveRequestFactory.createMoveRequest(
                currentGame.getId(), 
                currentPlayer.getId(),
                null,
                null,
                PlayerType.COMPUTER
            );
            
            try {
                currentGame = gameEngine.makeMove(dummyRequest);
                System.out.println("Computer made its move.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Gets a move from the human player.
     */
    private void getMoveFromHuman() {
        while (true) {
            System.out.println("Enter your move in format 'row1,col1 row2,col2' (e.g., '2,1 3,2')");
            System.out.println("Or type 'menu' to return to the main menu");
            System.out.print("> ");
            
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("menu")) {
                currentGame = null;
                return;
            }
            
            try {
                MoveRequest moveRequest = MoveRequest.MoveRequestFactory.createMoveRequestFromString(
                    currentGame.getId(),
                    humanPlayer.getId(),
                    input
                );
                
                currentGame = gameEngine.makeMove(moveRequest);
                break;
            } catch (Exception e) {
                System.out.println("Invalid move: " + e.getMessage());
            }
        }
    }

    /**
     * Reads an integer input from the user.
     * Waits for input to be available before attempting to read.
     * This prevents NoSuchElementException and ensures the application
     * doesn't exit when input isn't immediately available.
     * 
     * The method continuously checks if input is available using scanner.hasNextLine().
     * When no input is available, it waits briefly before checking again, rather than
     * exiting the application. This allows the application to wait for user input
     * indefinitely, which is particularly important when running in environments
     * where input might be delayed or when the user needs time to respond.
     */
    private int readIntInput() {
        while (true) {
            try {
                // Wait for input to be available
                while (!scanner.hasNextLine()) {
                    try {
                        // Sleep briefly to avoid high CPU usage while waiting
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Main method.
     */
    public static void main(String[] args) {
        // Set up Redis connection
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        
        try (JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379)) {
            // Create the repository and game engine
            GameRepository gameRepository = new RedisGameRepository(jedisPool);
            GameRulesFactory gameRulesFactory = new GameRulesFactory();
            GameEngine gameEngine = new GameEngine(gameRepository, gameRulesFactory);
            
            // Create and start the application
            CheckersApp app = new CheckersApp(gameEngine);
            app.start();
        }
    }
}
