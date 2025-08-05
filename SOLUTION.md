# Redis Setup Solution

## What I've Done

1. Created a comprehensive `REDIS_SETUP.md` file with detailed instructions for:
   - Installing Redis on different platforms (macOS, Windows, Linux)
   - Using Docker to run Redis
   - Using Redis Cloud for hosted Redis
   - Setting up an embedded Redis server for development
   - Configuring the application to connect to Redis
   - Troubleshooting Redis connection issues

2. Updated the `README.md` file to:
   - Add a reference to the Redis setup section
   - Include basic Redis installation instructions
   - Mention all available options for running Redis
   - Link to the detailed REDIS_SETUP.md file

## How to Start Redis

The Checkers application requires Redis for persistence. You have multiple options:

1. **Standalone Redis Server** (recommended for production)
   - macOS: `brew install redis && brew services start redis`
   - Windows: Download from GitHub and run redis-server.exe
   - Linux: `sudo apt install redis-server && sudo systemctl start redis-server`

2. **Docker Container**
   - `docker run --name redis -p 6379:6379 -d redis`

3. **Embedded Redis** (for development)
   - Add the embedded Redis dependency to your project
   - Create a utility class to manage the server
   - Start the server when your application launches

4. **Redis Cloud** (hosted solution)
   - Sign up for a free account
   - Create a database
   - Update connection settings in the application

The application is configured to connect to Redis at localhost:6379 by default.
