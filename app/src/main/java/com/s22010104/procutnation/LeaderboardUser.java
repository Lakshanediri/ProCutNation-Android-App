package com.s22010104.procutnation;

public class LeaderboardUser {
    private String name;
    private long points;
    private long xpLevel; // Added xpLevel field

    public LeaderboardUser() {
        // Required for Firestore
    }

    // Getters and Setters for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public long getXpLevel() {
        return xpLevel;
    }

    public void setXpLevel(long xpLevel) {
        this.xpLevel = xpLevel;
    }
}
