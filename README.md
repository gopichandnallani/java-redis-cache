# Movie Function App

This project demonstrates an Azure Function application that retrieves movie details based on a serial number, using MySQL as the database and Redis for caching to improve performance.

## Prerequisites

- Java 17
- Maven
- MySQL
- Azure Functions Core Tools
- Azure Cache for Redis

## Setup

### 1. Clone the Repository

```sh
git clone https://github.com/your-repo/movie-function-app.git
cd java-redis-cache
```

Configure MySQL
Create a MySQL database and table using the following SQL script:
```sh
CREATE DATABASE moviedatabase;

USE moviedatabase;

CREATE TABLE movies (
    serial_number VARCHAR(3) PRIMARY KEY,
    actor_name VARCHAR(50),
    movie_name VARCHAR(50),
    release_year INT
);

INSERT INTO movies (serial_number, actor_name, movie_name, release_year) VALUES
('001', 'Robert Downey Jr.', 'Iron Man', 2008),
('002', 'Chris Evans', 'Captain America: The First Avenger', 2011),
('003', 'Mark Ruffalo', 'The Incredible Hulk', 2008);
```

Update local.settings.json
Create a local.settings.json file in the root directory of your project with the following content. Replace the placeholder values with your actual configuration:
```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "MYSQL_URL": "jdbc:mysql://<your-mysql-host>:3306/moviedatabase",
    "MYSQL_USER": "<your-mysql-username>",
    "MYSQL_PASSWORD": "<your-mysql-password>",
    "REDIS_HOST": "<your-redis-host>.redis.cache.windows.net",
    "REDIS_PORT": "6380",
    "REDIS_PASSWORD": "<your-redis-access-key>"
  }
}
```
Build and Run Locally:
```sh
mvn clean package
mvn azure-functions:run
```
To deploy on to azure Function apps;
```sh
az login
mvn azure-functions:deploy
```
