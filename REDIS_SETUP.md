# Redis Setup for Checkers Game

The Checkers game requires a Redis server for storing game state. Here are instructions for setting up Redis:

## Option 1: Install Redis Server (Recommended)

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

### Windows
1. Download Redis for Windows from [https://github.com/microsoftarchive/redis/releases](https://github.com/microsoftarchive/redis/releases)
2. Run the installer
3. Start Redis server by running `redis-server.exe`

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

## Option 2: Use Docker

If you have Docker installed, you can run Redis in a container:

```
docker run --name redis -p 6379:6379 -d redis
```

## Option 3: Use Redis Cloud

For development purposes, you can also use a free Redis Cloud instance:

1. Sign up at [https://redis.com/try-free/](https://redis.com/try-free/)
2. Create a free database
3. Update the connection settings in `CheckersApp.java`:
   ```
   try (JedisPool jedisPool = new JedisPool(poolConfig, "your-redis-host", port, timeout, "your-password")) {
       // ...
   }
   ```

## Option 4: Embedded Redis (for Development)

If you prefer not to install a standalone Redis server, you can use an embedded Redis server for development:

1. Add the embedded Redis dependency to your `build.gradle`:
   ```
   implementation 'it.ozimov:embedded-redis:0.7.3'
   ```

2. Create a new class to start the embedded Redis server:
   ```java
   package com.shalako.checkers.util;

   import redis.embedded.RedisServer;

   public class EmbeddedRedisServer {
       private static RedisServer redisServer;

       public static void start() {
           try {
               redisServer = RedisServer.builder()
                   .port(6379)
                   .setting("maxmemory 128M")
                   .build();
               redisServer.start();
               System.out.println("Embedded Redis server started on port 6379");
           } catch (Exception e) {
               System.err.println("Failed to start embedded Redis server: " + e.getMessage());
               System.err.println("Please ensure a Redis server is running or install Redis.");
           }
       }

       public static void stop() {
           if (redisServer != null && redisServer.isActive()) {
               redisServer.stop();
               System.out.println("Embedded Redis server stopped");
           }
       }
   }
   ```

3. Modify `CheckersApp.java` to start the embedded server:
   ```java
   public static void main(String[] args) {
       // Start embedded Redis server
       EmbeddedRedisServer.start();
       
       // Set up Redis connection
       JedisPoolConfig poolConfig = new JedisPoolConfig();
       // ... rest of the code
       
       // Add shutdown hook to stop Redis server
       Runtime.getRuntime().addShutdownHook(new Thread(EmbeddedRedisServer::stop));
   }
   ```

Note: The embedded Redis server is intended for development and testing only. For production use, a standalone Redis server is recommended.

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

By default, the application connects to Redis at localhost:6379. If you need to connect to Redis at a different location:

1. Open `src/main/java/com/shalako/checkers/CheckersApp.java`
2. Locate the following code in the `main` method (around line 228):
   ```
   try (JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379)) {
   ```
3. Modify the host and port parameters as needed:
   ```
   try (JedisPool jedisPool = new JedisPool(poolConfig, "your-redis-host", port)) {
   ```
4. For authenticated Redis connections, use:
   ```
   try (JedisPool jedisPool = new JedisPool(poolConfig, "your-redis-host", port, timeout, "your-password")) {
   ```

## Troubleshooting

If you encounter connection issues:

1. Ensure Redis is running on port 6379
2. Check firewall settings if connecting to a remote Redis server
3. Verify credentials if using Redis Cloud
4. Run `redis-cli ping` to check if Redis is responding
5. Check Redis logs for any errors:
   - macOS/Linux: `/var/log/redis/redis-server.log` or use `journalctl -u redis-server`
   - Windows: Check the Redis installation directory for log files
