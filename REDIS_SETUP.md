# Redis Setup for Checkers Game

The Checkers game requires a Redis server for storing game state. The application now includes an embedded Redis server that starts automatically, so no external Redis installation is required for development or testing.

## Option 1: Use the Embedded Redis Server (Default)

The application now includes an embedded Redis server that starts automatically when the application runs. This is the simplest option and requires no additional setup.

Benefits:
- No installation required
- Starts and stops automatically with the application
- Works on all platforms

Limitations:
- Intended for development and testing only
- Limited performance compared to a standalone Redis server
- Data is not persisted between application restarts

## Option 2: Install Redis Server (For Production)

### macOS
1. Using Homebrew:
   ```
   brew install redis
   ```
2. Start Redis server:
   ```
   brew services start redis
   ```
   or for one-time use:
   ```
   redis-server
   ```
3. Configure the application to use your local Redis server by adding these properties to `application.properties`:
   ```properties
   redis.external.enabled=true
   redis.external.host=127.0.0.1
   redis.external.port=6379
   ```

### Windows
1. Download Redis for Windows from [https://github.com/microsoftarchive/redis/releases](https://github.com/microsoftarchive/redis/releases)
2. Run the installer
3. Start Redis server by running `redis-server.exe`
4. Configure the application to use your local Redis server by adding these properties to `application.properties`:
   ```properties
   redis.external.enabled=true
   redis.external.host=127.0.0.1
   redis.external.port=6379
   ```

### Linux (Ubuntu/Debian)
1. Install Redis:
   ```
   sudo apt update
   sudo apt install redis-server
   ```
2. Start Redis service:
   ```
   sudo systemctl start redis-server
   ```
3. Configure the application to use your local Redis server by adding these properties to `application.properties`:
   ```properties
   redis.external.enabled=true
   redis.external.host=127.0.0.1
   redis.external.port=6379
   ```

## Option 3: Use Docker

If you have Docker installed, you can run Redis in a container:

```
docker run --name redis -p 6379:6379 -d redis
```

To use a Docker Redis instance:

1. Make sure your Docker Redis container is running before starting the application
2. Configure the application to use your Docker Redis instance by adding these properties to `application.properties`:
   ```properties
   redis.external.enabled=true
   redis.external.host=127.0.0.1
   redis.external.port=6379
   ```

## Option 4: Use Redis Cloud

For production purposes, you can use a Redis Cloud instance:

1. Sign up at [https://redis.com/try-free/](https://redis.com/try-free/)
2. Create a database
3. Configure the application to use your Redis Cloud instance by adding these properties to `application.properties`:
   ```properties
   redis.external.enabled=true
   redis.external.host=your-redis-host
   redis.external.port=your-redis-port
   ```

4. For authenticated Redis connections, you'll need to modify the `jedisPool()` method in `CheckersRestApplication.java`:
   ```java
   @Bean
   public JedisPool jedisPool() {
       JedisPoolConfig poolConfig = new JedisPoolConfig();
       poolConfig.setMaxTotal(10);
       poolConfig.setMaxIdle(5);
       poolConfig.setMinIdle(1);
       
       return new JedisPool(poolConfig, "your-redis-host", port, timeout, "your-password");
   }
   ```

## How the Embedded Redis Server Works

The application now includes an enhanced embedded Redis server implementation:

1. The embedded Redis dependency is included in `build.gradle`:
   ```
   implementation('com.github.kstyrc:embedded-redis:0.6') {
       exclude group: 'commons-logging', module: 'commons-logging'
   }
   ```

2. The `EmbeddedRedisServer` utility class manages the server with improved reliability:
   ```java
package com.shalako.checkers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

public class EmbeddedRedisServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisServer.class);
    private static RedisServer redisServer;
    private static final int DEFAULT_PORT = 6379;
    private static final List<Integer> FALLBACK_PORTS = Arrays.asList(6380, 6381, 6382);
    private static final int currentPort = DEFAULT_PORT;

    public static void start() {
        // Tries multiple ports if the default port is unavailable
        // Provides detailed system information and troubleshooting tips
    }

    public static int getCurrentPort() {
        // Returns the port that Redis is actually using
        return currentPort;
    }

    public static void stop() {
        // Stop the embedded Redis server
    }
}
```

3. The `CheckersRestApplication` now intelligently manages the Redis server:
   ```java
   @PostConstruct
   public void initializeRedis() {
       if (externalRedisEnabled) {
           logger.info("External Redis server enabled, checking if available at {}:{}", redisHost, redisPort);
           
           // Check if external Redis server is available
           boolean externalRedisAvailable = false;
           if (redisHost.equals("127.0.0.1") || redisHost.equals("localhost")) {
               // For localhost, we can use the checkExternalRedisServer method
               externalRedisAvailable = EmbeddedRedisServer.checkExternalRedisServer(redisPort);
           } else {
               // For remote hosts, try to connect using Jedis
               try (redis.clients.jedis.Jedis jedis = new redis.clients.jedis.Jedis(redisHost, redisPort, 2000)) {
                   String pong = jedis.ping();
                   externalRedisAvailable = "PONG".equalsIgnoreCase(pong);
               } catch (Exception e) {
                   logger.warn("Failed to connect to external Redis server: {}", e.getMessage());
                   externalRedisAvailable = false;
               }
           }
           
           if (externalRedisAvailable) {
               logger.info("Successfully connected to external Redis server at {}:{}", redisHost, redisPort);
               return;
           } else {
               logger.warn("External Redis server not available at {}:{}, falling back to embedded Redis", 
                          redisHost, redisPort);
           }
       }
       
       // Start embedded Redis server if external server is not enabled or not available
       logger.info("Starting embedded Redis server");
       EmbeddedRedisServer.start();
   }
   ```

The embedded Redis server is intended for development and testing only. For production use, a standalone Redis server is recommended.

## Verifying Redis Connection

To verify Redis is running correctly:

1. For local Redis, run:
   ```
   redis-cli ping
   ```
   It should respond with "PONG"

2. For Docker:
   ```
   docker exec -it redis redis-cli ping
   ```

## Configuring the Application

By default, the application uses the embedded Redis server running on 127.0.0.1 with a dynamic port (starting with 6379 and falling back to 6380, 6381, or 6382 if needed).

### Using an External Redis Server

The application now supports connecting to an external Redis server through configuration properties. You can enable this feature by setting the following properties in `application.properties`:

```properties
# Redis configuration
# Set redis.external.enabled=true to use an external Redis server instead of the embedded one
redis.external.enabled=true
redis.external.host=127.0.0.1
redis.external.port=6379
```

When `redis.external.enabled` is set to `true`, the application will:
1. Check if an external Redis server is available at the specified host and port
2. If available, use that external Redis server
3. If not available, fall back to the embedded Redis server

This approach is recommended for:
- Development environments where you want to use a local Redis server
- Production environments where you want to use a standalone Redis server
- Cloud environments where you want to use a managed Redis service

### Advanced Configuration

For authenticated Redis connections or other advanced configurations, you may still need to modify the `jedisPool()` method in `CheckersRestApplication.java`:

```java
@Bean
public JedisPool jedisPool() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(10);
    poolConfig.setMaxIdle(5);
    poolConfig.setMinIdle(1);
    
    return new JedisPool(poolConfig, "your-redis-host", 6379, 2000, "your-password");
}
```

## Troubleshooting

If you encounter connection issues:

1. Check the application logs to see which port the embedded Redis server is using (it may be using a fallback port like 6380, 6381, or 6382)
2. Ensure no other applications are using ports 6379-6382
3. Check your system's permissions - the embedded Redis server may need elevated privileges
4. For external Redis servers, run `redis-cli -h <host> -p <port> ping` to check if Redis is responding
5. Check Redis logs for any errors:
   - macOS/Linux: `/var/log/redis/redis-server.log` or use `journalctl -u redis-server`
   - Windows: Check the Redis installation directory for log files
6. Review the application logs for detailed error messages and troubleshooting tips
7. On Windows, you may need to install Redis manually as the embedded server has limited compatibility
8. On macOS, you can install Redis using Homebrew: `brew install redis`
9. On Linux, you can install Redis using your package manager: `sudo apt install redis-server` (Ubuntu/Debian)
