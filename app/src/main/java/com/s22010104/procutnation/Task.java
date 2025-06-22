package com.s22010104.procutnation;

import com.google.firebase.Timestamp;

public class Task {
    private String taskId;
    private String taskName;
    private int taskHours;
    private int milestoneHours;
    private Timestamp startDate;
    private Timestamp endDate;
    private String priority;
    private int pomodoroSettings;
    private boolean isCompleted;
    private int progress;
    private String color;
    private long position;
    private String projectId;
    private String projectName; // Add this field

    public Task() {}

    // Getters and Setters for all fields
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public int getTaskHours() { return taskHours; }
    public void setTaskHours(int taskHours) { this.taskHours = taskHours; }

    public int getMilestoneHours() { return milestoneHours; }
    public void setMilestoneHours(int milestoneHours) { this.milestoneHours = milestoneHours; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getPomodoroSettings() { return pomodoroSettings; }
    public void setPomodoroSettings(int pomodoroSettings) { this.pomodoroSettings = pomodoroSettings; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getPosition() { return position; }
    public void setPosition(long position) { this.position = position; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}