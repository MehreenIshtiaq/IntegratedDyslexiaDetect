package com.example.integrated;

public class LevelModel {
    private int level;
    private int maxScore;

    public LevelModel(int level, int maxScore) {
        this.level = level;
        this.maxScore = maxScore;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
