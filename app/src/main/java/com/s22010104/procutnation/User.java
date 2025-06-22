package com.s22010104.procutnation;

public class User {
    private String uid;
    private String name;
    private String email;
    private long xpLevel;
    private long points;
    private String profileImageUrl;
    private String activePetDrawableName;

    public User() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getXpLevel() { return xpLevel; }
    public void setXpLevel(long xpLevel) { this.xpLevel = xpLevel; }
    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getActivePetDrawableName() { return activePetDrawableName; }
    public void setActivePetDrawableName(String activePetDrawableName) { this.activePetDrawableName = activePetDrawableName; }
}