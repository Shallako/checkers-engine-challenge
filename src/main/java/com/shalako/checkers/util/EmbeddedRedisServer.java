package com.shalako.checkers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for managing an embedded Redis server.
 * This is intended for development and testing purposes only.
 */
public class EmbeddedRedisServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisServer.class);
    private static RedisServer redisServer;
    private static final int DEFAULT_PORT = 6379;
    private static final List<Integer> FALLBACK_PORTS = Arrays.asList(6380, 6381, 6382, 6383);
    private static int currentPort = DEFAULT_PORT;
    private static final String DEFAULT_MAXMEMORY = "128M";

    /**
     * Checks if a port is available.
     *
     * @param port The port to check
     * @return true if the port is available, false otherwise
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Starts the embedded Redis server on the default port (6379).
     * If the default port is unavailable, it will try fallback ports.
     */
    public static void start() {
        logger.info("Starting embedded Redis server");

        // If an external Redis already runs on the default port, don't start embedded
        try {
            if (!checkExternalRedisServer(DEFAULT_PORT)) {
                currentPort = DEFAULT_PORT;
                logger.info("Detected external Redis on port {}. Skipping embedded startup.", DEFAULT_PORT);
                return;
            }
        } catch (Exception ignore) {
            // proceed with attempting to start embedded
        }

        // Try only the default port to avoid repeated failures when the binary is not available
        if (startOnPort(DEFAULT_PORT)) {
            return;
        }

        logger.error("Failed to start embedded Redis server on port {}. You may set 'redis.embedded.enabled=false' and 'redis.external.enabled=true' to use an external Redis.", DEFAULT_PORT);
    }

    /**
     * Attempts to start the embedded Redis server on the specified port.
     *
     * @param port The port to run Redis on
     * @return true if the server started successfully, false otherwise
     */
    private static boolean startOnPort(int port) {
        try {
            if (redisServer != null && redisServer.isActive()) {
                logger.info("Embedded Redis server is already running");
                return true;
            }
            
            // Check if port is available
            if (!isPortAvailable(port)) {
                logger.warn("Port {} is already in use, trying next port", port);
                return false;
            }
            redisServer = new RedisServer(port);
            redisServer.start();
            currentPort = port;
            logger.info("Embedded Redis server started on port {}", port);
            return true;
        } catch (Exception e) {
            logger.error("Failed to start embedded Redis server on port {}: {}", port, e.getMessage());
            return false;
        }
    }

    /**
     * Stops the embedded Redis server if it's running.
     */
    public static void stop() {
        if (redisServer != null && redisServer.isActive()) {
          try {
            redisServer.stop();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          logger.info("Embedded Redis server stopped");
        }
    }

    /**
     * Checks if the embedded Redis server is active.
     *
     * @return true if the server is active, false otherwise
     */
    public static boolean isActive() {
        return redisServer != null && redisServer.isActive();
    }
    
    /**
     * Gets the current port that the Redis server is using.
     *
     * @return the current Redis port
     */
    public static int getCurrentPort() {
        return currentPort;
    }
    
    /**
     * Checks if an external Redis server is running on the specified port.
     *
     * @param port The port to check
     * @return true if an external Redis server is running, false otherwise
     */
    public static boolean checkExternalRedisServer(int port) {
        // First check if the port is in use
        if (isPortAvailable(port)) {
            // Port is available, so no Redis server is running on it
            return false;
        }
        
        // Port is in use, try to connect to Redis
        try (redis.clients.jedis.Jedis jedis = new redis.clients.jedis.Jedis("127.0.0.1", port, 2000)) {
            String pong = jedis.ping();
            // If we get PONG back, Redis is running
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            logger.debug("No Redis server found on port {}: {}", port, e.getMessage());
            return false;
        }
    }
}
