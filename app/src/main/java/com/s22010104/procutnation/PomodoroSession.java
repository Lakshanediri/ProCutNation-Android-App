package com.s22010104.procutnation;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class PomodoroSession {
    private String userId;
    private String taskId;
    private String projectId;
    private String projectName;
    private int minutesCompleted;
    @ServerTimestamp
    private Date completionTimestamp;

    public PomodoroSession() {} // Required for Firestore

    public PomodoroSession(String userId, String taskId, String projectId, String projectName, int minutesCompleted) {
        this.userId = userId;
        this.taskId = taskId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.minutesCompleted = minutesCompleted;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getTaskId() { return taskId; }
    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public int getMinutesCompleted() { return minutesCompleted; }
    public Date getCompletionTimestamp() { return completionTimestamp; }
}
