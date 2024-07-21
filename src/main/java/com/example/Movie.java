package com.example;

public class Movie {
    private String serialNumber;
    private String actor;
    private String movie;
    private int releaseYear;

    public Movie(String serialNumber, String actor, String movie, int releaseYear) {
        this.serialNumber = serialNumber;
        this.actor = actor;
        this.movie = movie;
        this.releaseYear = releaseYear;
    }

    // Getters and Setters
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }
}
