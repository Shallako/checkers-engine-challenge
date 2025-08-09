package com.shalako.checkers;

import com.shalako.checkers.engine.GameEngine;
import com.shalako.checkers.persistence.GameRepository;
import com.shalako.checkers.persistence.RedisGameRepository;
import com.shalako.checkers.util.EmbeddedRedisServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Main Spring Boot application class for the Checkers REST API.
 */
@SpringBootApplication
public class CheckersRestApplication {

  private static final Logger logger = LoggerFactory.getLogger(CheckersRestApplication.class);

  // Constants to avoid magic numbers/strings
  private static final String LOCALHOST_IP = "127.0.0.1";
  private static final String LOCALHOST_NAME = "localhost";
  private static final String REDIS_PONG = "PONG";
  private static final int JEDIS_CONNECT_TIMEOUT_MS = 5000;

  private static final int POOL_MAX_TOTAL = 10;
  private static final int POOL_MAX_IDLE = 5;
  private static final int POOL_MIN_IDLE = 1;
  private static final int POOL_MAX_WAIT_MS = 10000;
  private static final boolean POOL_JMX_ENABLED = false;

  @Value("${redis.external.enabled:false}")
  private boolean externalRedisEnabled;

  @Value("${redis.external.host:127.0.0.1}")
  private String redisHost;

  @Value("${redis.external.port:6379}")
  private int redisPort;

  private static boolean embeddedRedisStarted = false;

  public static void main(String[] args) {
    // Start Spring application
    ConfigurableApplicationContext context = SpringApplication.run(CheckersRestApplication.class, args);

    // Add shutdown hook as a backup to ensure Redis server is stopped
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (embeddedRedisStarted) {
        logger.info("Application shutdown detected, stopping embedded Redis server");
        EmbeddedRedisServer.stop();
      }
    }));
  }

  /**
   * Initializes Redis after Spring context is loaded. Checks for external Redis server first, and starts embedded server only if needed.
   */
  @PostConstruct
  public void initializeRedis() {
    if (externalRedisEnabled) {
      logger.info("External Redis server enabled, checking if available at {}:{}", redisHost, redisPort);

      // Check if external Redis server is available
      boolean externalRedisAvailable = false;

      if (redisHost.equals(LOCALHOST_IP) || redisHost.equals(LOCALHOST_NAME)) {
        externalRedisAvailable = EmbeddedRedisServer.checkExternalRedisServer(redisPort);
      } else {
        try (redis.clients.jedis.Jedis jedis =
            new redis.clients.jedis.Jedis(redisHost, redisPort, JEDIS_CONNECT_TIMEOUT_MS)) {
          String pong = jedis.ping();
          externalRedisAvailable = REDIS_PONG.equalsIgnoreCase(pong);
        } catch (Exception e) {
          logger.warn("Failed to connect to external Redis server:[{}].", e.getMessage());
        }
      }

      if (externalRedisAvailable) {
        logger.info("Successfully connected to external Redis server at {}:{}", redisHost, redisPort);
        embeddedRedisStarted = false;
        return;
      } else {
        logger.warn("External Redis server not available at {}:{}, falling back to embedded Redis",
            redisHost, redisPort);
      }
    }

    // Start embedded Redis server if external server is not enabled or not available
    logger.info("Starting embedded Redis server");
    EmbeddedRedisServer.start();
    embeddedRedisStarted = true;
  }

  /**
   * Stops the embedded Redis server when the application is shutting down, but only if we started it.
   */
  @PreDestroy
  public void onShutdown() {
    if (embeddedRedisStarted) {
      logger.info("Application context is closing, stopping embedded Redis server");
      EmbeddedRedisServer.stop();
    } else {
      logger.info("Application context is closing, embedded Redis server was not started by this application");
    }
  }

  /**
   * Creates and configures a JedisPool for Redis connection. Uses either the external Redis server if available, or the embedded Redis server.
   */
  @Bean
  public JedisPool jedisPool() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(POOL_MAX_TOTAL);
    poolConfig.setMaxIdle(POOL_MAX_IDLE);
    poolConfig.setMinIdle(POOL_MIN_IDLE);
    poolConfig.setMaxWait(java.time.Duration.ofMillis(POOL_MAX_WAIT_MS)); // Set positive maxWait value
    poolConfig.setJmxEnabled(POOL_JMX_ENABLED); // Disable JMX to avoid MBean registration issues

    String host;
    int port;

    if (externalRedisEnabled && !embeddedRedisStarted) {
      // Use external Redis server
      host = redisHost;
      port = redisPort;
      logger.info("Configuring JedisPool to connect to external Redis at {}:{}", host, port);
    } else {
      // Use embedded Redis server
      host = LOCALHOST_IP;
      port = EmbeddedRedisServer.getCurrentPort();
      logger.info("Configuring JedisPool to connect to embedded Redis at {}:{}", host, port);
    }

    return new JedisPool(poolConfig, host, port);
  }

  /**
   * Creates a GameRepository bean.
   */
  @Bean
  public GameRepository gameRepository(JedisPool jedisPool) {
    return new RedisGameRepository(jedisPool);
  }

  /**
   * Creates a GameEngine bean.
   */
  @Bean
  public GameEngine gameEngine(GameRepository gameRepository) {
    return new GameEngine(gameRepository);
  }
}
