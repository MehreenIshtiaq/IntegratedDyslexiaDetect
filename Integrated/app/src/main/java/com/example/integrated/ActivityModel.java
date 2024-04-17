package com.example.integrated;

import java.util.List;

public class ActivityModel {
    private String activityName;
    private List<LevelModel> levels;

    public ActivityModel(String activityName, List<LevelModel> levels) {
        this.activityName = activityName;
        this.levels = levels;
    }

    public String getActivityName() {
        return activityName;
    }

    public List<LevelModel> getLevels() {
        return levels;
    }
}

