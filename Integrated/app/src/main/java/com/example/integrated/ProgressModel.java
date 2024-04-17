package com.example.integrated;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProgressModel {
    private String activityName;
    private Map<Integer, ArrayList<Entry>> levelEntriesMap;

    public ProgressModel(String activityName) {
        this.activityName = activityName;
        this.levelEntriesMap = new HashMap<>();
    }

    public void addEntriesForLevel(int levelNumber, ArrayList<Entry> entries) {
        this.levelEntriesMap.put(levelNumber, entries);
    }

    public String getActivityName() {
        return activityName;
    }

    public Map<Integer, ArrayList<Entry>> getLevelEntriesMap() {
        return levelEntriesMap;
    }
}

