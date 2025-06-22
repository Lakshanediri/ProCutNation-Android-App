package com.s22010104.procutnation;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Project {
    private String projectId;
    private String projectName;
    private String color; // To store hex color string
    private long position; // For ordering
    @ServerTimestamp
    private Date createdAt; //  initial ordering

    public Project() {}

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getPosition() { return position; }
    public void setPosition(long position) { this.position = position; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}