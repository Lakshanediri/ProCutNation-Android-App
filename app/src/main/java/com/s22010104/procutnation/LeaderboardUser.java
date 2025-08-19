package com.s22010104.procutnation;

public class LeaderboardUser {
    private String name;
    private long xpLevel;
    private long points; // Added points field
    private String profileImageBase64;

    public LeaderboardUser() {}

    // Updated constructor
    public LeaderboardUser(String name, long xpLevel, long points, String profileImageBase64) {
        this.name = name;
        this.xpLevel = xpLevel;
        this.points = points;
        this.profileImageBase64 = profileImageBase64;
    }

    // Getters
    public String getName() {
        return name;
    }

    public long getXpLevel() {
        return xpLevel;
    }

    public long getPoints() {
        return points;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }
}