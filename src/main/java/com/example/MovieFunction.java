package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MovieFunction {

    @FunctionName("getMovie")
    public HttpResponseMessage getMovie(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Retrieve query parameters
        String serialNumber = request.getQueryParameters().get("serialNumber");

        if (serialNumber == null) {
            context.getLogger().info("Serial number is null");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please pass a serial number on the query string")
                    .build();
        }

        context.getLogger().info("Serial number received: " + serialNumber);

        // Measure time taken to retrieve from cache
        long startCacheTime = System.currentTimeMillis();
        String cacheKey = "movie:" + serialNumber;
        String cachedMovie = RedisCache.get(cacheKey);
        long endCacheTime = System.currentTimeMillis();
        long cacheDuration = endCacheTime - startCacheTime;

        if (cachedMovie != null) {
            context.getLogger().info("Movie found in cache: " + cachedMovie);
            context.getLogger().info("Time taken to retrieve from cache: " + cacheDuration + " ms");
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(cachedMovie)
                    .header("Content-Type", "application/json")
                    .header("X-Cache-Retrieval-Time", cacheDuration + " ms")
                    .build();
        }

        // Measure time taken to retrieve from database
        long startDbTime = System.currentTimeMillis();

        // MySQL configuration
        String jdbcUrl = System.getenv("MYSQL_URL");
        String jdbcUser = System.getenv("MYSQL_USER");
        String jdbcPassword = System.getenv("MYSQL_PASSWORD");

        // MySQL query to retrieve movie details
        String query = "SELECT * FROM movies WHERE serial_number = ?";

        try (Connection conn = getConnection(jdbcUrl, jdbcUser, jdbcPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, serialNumber);
            ResultSet rs = stmt.executeQuery();

            long endDbTime = System.currentTimeMillis();
            long dbDuration = endDbTime - startDbTime;

            if (rs.next()) {
                // Retrieve data from result set
                String actorName = rs.getString("actor_name");
                String movieName = rs.getString("movie_name");
                int releaseYear = rs.getInt("release_year");

                // Create a Movie object to hold the data
                Movie movie = new Movie(serialNumber, actorName, movieName, releaseYear);

                // Convert Movie object to JSON
                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(movie);

                context.getLogger().info("Movie found: " + jsonResponse);
                context.getLogger().info("Time taken to retrieve from database: " + dbDuration + " ms");

                // Store data in the Redis cache
                RedisCache.setex(cacheKey, 3600, jsonResponse); // Cache for 1 hour

                // Return JSON response
                return request.createResponseBuilder(HttpStatus.OK)
                        .body(jsonResponse)
                        .header("Content-Type", "application/json")
                        .header("X-DB-Retrieval-Time", dbDuration + " ms")
                        .build();
            } else {
                context.getLogger().info("No movie found with the provided serial number");
                // If no data is found, return 404
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("No movie found with the provided serial number")
                        .build();
            }
        } catch (SQLException e) {
            context.getLogger().severe("Database connection error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database connection error: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }

    @FunctionName("hello")
    public HttpResponseMessage hello(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger 'hello' processed a request.");

        // Return a simple message
        return request.createResponseBuilder(HttpStatus.OK)
                .body("Hello from Azure Functions!")
                .build();
    }

    // Helper method to get a connection to the MySQL database
    private Connection getConnection(String url, String user, String password) throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource.getConnection();
    }
}
